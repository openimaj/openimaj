package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mortbay.io.RuntimeIOException;
import org.openimaj.io.IOUtils;
import org.openimaj.rdf.storm.bolt.RETEStormNode;
import org.openjena.riot.RiotWriter;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
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
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;

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
		 * Usually as part of a SELECT query
		 * 
		 * @param binding
		 */
		void consumeSolution(QuerySolution binding);

		/**
		 * A Sink used mainly for debugging and tests
		 * 
		 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
		 * 
		 */
		public static class FileSink implements StormSPARQLReteConflictSetBoltSink {
			private FileOutputStream fos;
			private File file;
			private PrintWriter writer;

			/**
			 * @param file
			 * @throws FileNotFoundException
			 */
			public FileSink(File file) throws FileNotFoundException {
				this.file = file;
			}

			@Override
			public void consumeTriple(Triple triple) {
				Graph g = GraphFactory.createGraphMem();
				g.add(triple);
				RiotWriter.writeTriples(fos, g);
			}

			@Override
			public void consumeSolution(QuerySolution binding) {
			}

			@Override
			public void instantiate(StormSPARQLReteConflictSetBolt conflictSet) {
				try {
					if (file.exists()) {
						if (!file.isDirectory()) {
							throw new RuntimeIOException("File exists: " + file);
						}
					}
					else
						file.mkdirs();
					String name = String.format("%s_%d", conflictSet.context.getThisComponentId(), conflictSet.context.getThisTaskId());

					fos = new FileOutputStream(new File(file, name));
					this.writer = new PrintWriter(fos);
				} catch (FileNotFoundException e) {
					throw new RuntimeException("Couldn't open output file: " + file + " becuase " + e.getMessage());
				}
				Query query = conflictSet.getQuery();
				if (query.isSelectType()) {
					writeSelectHeader(query);
				}
			}

			private void writeSelectHeader(Query query) {
				String header = StringUtils.join(query.getResultVars(), ",");
				this.writer.println(header);
			}

		}
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
		this.bindingsQueue = new ArrayList<Binding>();
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

	private void setSink(StormSPARQLReteConflictSetBoltSink sink) {
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
	 * @param binding
	 */
	public void emitSolution(QuerySolution binding) {
		logger.debug("Emitting solution: " + binding);
		sink.consumeSolution(binding);
	}

}
