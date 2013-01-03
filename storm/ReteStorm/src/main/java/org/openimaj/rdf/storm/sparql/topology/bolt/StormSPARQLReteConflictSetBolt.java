package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.mortbay.io.RuntimeIOException;
import org.openimaj.io.IOUtils;
import org.openimaj.rdf.storm.bolt.RETEStormNode;
import org.openimaj.rdf.storm.topology.logging.LoggerBolt.LoggedEvent;
import org.openimaj.rdf.storm.topology.logging.LoggerBolt.LoggedEvent.EventType;
import org.openimaj.rdf.storm.utils.CircularPriorityWindow;
import org.openimaj.rdf.storm.utils.CircularPriorityWindow.OverflowHandler;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphMap;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterGroup;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase;

/**
 * This bolt deals with the consequences of a valid binding for a SPARQL query.
 * The subclasses of this bolt deal with the specifics of SELECT, CONSTRUCT, ASK
 * and DESCRIBE
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class StormSPARQLReteConflictSetBolt extends StormSPARQLReteBolt {

	/**
	 * Deal with triples or bindings
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static interface StormSPARQLReteConflictSetBoltSink {
		/**
		 * Start things off
		 * 
		 * @param conflictSet
		 */
		void instantiate(StormSPARQLReteConflictSetBolt conflictSet);

		/**
		 * Usually as part of a CONSTRUCT query
		 * 
		 * @param triple
		 */
		void consumeTriple(Triple triple);

		/**
		 * Usually calls a formatter from {@link ResultSetFormatter}
		 * 
		 * @param bindingsIter
		 */
		void consumeBindings(QueryIterator bindingsIter);

		/**
		 * Close the sink
		 */
		void close();

	}

	private static Logger logger = Logger.getLogger(StormSPARQLReteConflictSetBolt.class);
	/**
	 *
	 */
	private static final long serialVersionUID = 5248125498316607622L;
	private List<ExprAggregator> aggregators;
	private List<Expr> having;
	private VarExprList groupBy;
	private Collection<Binding> bindingsQueue;
	private FunctionEnvBase execCxt;
	private StormSPARQLReteConflictSetBoltSink sink;
	private byte[] sinkBytes;

	/**
	 * @param query
	 */
	public StormSPARQLReteConflictSetBolt(Query query) {
		super(query);

	}

	@Override
	public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy, RETERuleContext context) {
		return null;
	}

	@Override
	public void execute(Tuple input) {
		logger.debug("Conflict set Tuple: " + input);
		Graph g = extractGraph(input);
		DatasetGraph dsg = new DatasetGraphMap(extractGraph(input));
		this.execCxt = new FunctionEnvBase(ARQ.getContext(), g, dsg);

		Binding binding = tupleToBinding(input);
		logger.debug("Conflict set extracted binding: " + binding);
		QueryIterator bindingsIter = null;
		// If there are no aggregators we keep no queue! simple!
		if (!this.aggregators.isEmpty()) {
			updateBindings(binding);
			bindingsIter = updateAggregators();
		}
		else {
			bindingsIter = new QueryIterPlainWrapper(Arrays.asList(binding).iterator());
		}
		if (!this.having.isEmpty())
			bindingsIter = checkHaving(bindingsIter);
		handleBinding(bindingsIter);
		acknowledge(input);
	}

	private QueryIterator checkHaving(QueryIterator iterator) {
		List<Binding> valid = new ArrayList<Binding>();
		for (; iterator.hasNext();) {
			Binding binding = iterator.next();
			for (Expr having : this.having) {
				if (having.isSatisfied(binding, execCxt)) {
					valid.add(binding);
				}
			}

		}

		return new QueryIterPlainWrapper(valid.iterator());
	}

	private void updateBindings(Binding binding) {
		bindingsQueue.add(binding);
	}

	/**
	 * @param aggregatorRet
	 *            the result of aggregation. This is a terribly named class that
	 *            should be called BindingIterator
	 */
	public abstract void handleBinding(QueryIterator aggregatorRet);

	@Override
	public void prepare() {
		this.bindingsQueue = new CircularPriorityWindow<Binding>(new OverflowHandler<Binding>() {
			@Override
			public void handleCapacityOverflow(Binding overflow) {
				logStream.emit("Binding overflowing! Binding removed",overflow);
				logger.debug("Binding overflowing! Binding removed");
			}
		}, 5000, 36000, TimeUnit.SECONDS);
		Query query = this.getQuery();
		this.aggregators = query.getAggregators();
		this.having = query.getHavingExprs();
		this.groupBy = query.getGroupBy();
		ByteArrayInputStream bais = new ByteArrayInputStream(this.sinkBytes);
		try {
			this.sink = IOUtils.read(new DataInputStream(bais));
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
		this.sink.instantiate(this);
	}

	protected QueryIterGroup updateAggregators() {
		QueryIterGroup qig = new QueryIterGroup(
				new QueryIterPlainWrapper(
						this.bindingsQueue.iterator()
				),
				groupBy,
				aggregators,
				null
				);
		return qig;
	}

	/**
	 * @param simpleQuery
	 * @param sink
	 * @return constructs the correct {@link StormSPARQLReteConflictSetBolt}
	 *         given the query's type
	 */
	public static StormSPARQLReteConflictSetBolt construct(Query simpleQuery, StormSPARQLReteConflictSetBoltSink sink) {

		StormSPARQLReteConflictSetBolt toRet = null;
		if (simpleQuery.isSelectType()) {
			toRet = new StormSPARQLReteSelectConflictSetBolt(simpleQuery);
		}
		else if (simpleQuery.isConstructType()) {
			toRet = new StormSPARQLReteConstructConflictSetBolt(simpleQuery);
		}
		toRet.setSink(sink);
		return toRet;
	}

	protected void setSink(StormSPARQLReteConflictSetBoltSink sink) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			IOUtils.write(sink, new DataOutputStream(baos));
			this.sinkBytes = baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}

	}

	/**
	 * Emit the triple (For CONSTRUCT)
	 * 
	 * @param triple
	 */
	public void emitTriple(Triple triple) {
		logger.debug("Emitting triple: " + triple);
		sink.consumeTriple(triple);
	}

	/**
	 * @param bindingsIter
	 */
	public void emitSolutions(QueryIterator bindingsIter) {
		logger.debug("Emitting iterator solutions!");
		sink.consumeBindings(bindingsIter);
	}

	public OutputCollector getCollector() {
		return this.collector;
	}

	public TopologyContext getContext() {
		// TODO Auto-generated method stub
		return this.context;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		this.sink.close();
	}

}
