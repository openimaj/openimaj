package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.bolt.RETEStormNode;

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

/**
 * This bolt deals with the consequences of a valid binding for a SPARQL query.
 * The subclasses of this bolt deal with the specifics of SELECT, CONSTRUCT, ASK
 * and DESCRIBE
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class StormSPARQLReteConflictSetBolt extends StormSPARQLReteBolt {
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

	public void execute(Tuple input) {

		Graph g = extractGraph(input);
		DatasetGraph dsg = new DatasetGraphMap(extractGraph(input));
		this.execCxt = new FunctionEnvBase(ARQ.getContext(), g, dsg);

		Binding binding = tupleToBinding(input);
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
		this.aggregators = this.getQuery().getAggregators();
		this.having = this.getQuery().getHavingExprs();
		this.groupBy = this.getQuery().getGroupBy();
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
	 * @return constructs the correct {@link StormSPARQLReteConflictSetBolt}
	 *         given the query's type
	 */
	public static StormSPARQLReteConflictSetBolt construct(Query simpleQuery) {
		if (simpleQuery.isSelectType()) {
			return new StormSPARQLReteSelectConflictSetBolt(simpleQuery);
		}
		else if (simpleQuery.isConstructType()) {
			return new StormSPARQLReteConstructConflictSetBolt(simpleQuery);
		}
		return null;
	}

	/**
	 * Emit the triple (For CONSTRUCT)
	 * 
	 * @param triple
	 */
	public void emitTriple(Triple triple) {
		logger.debug("Emitting triple: " + triple);
	}

	/**
	 * @param binding
	 */
	public void emitSolution(QuerySolution binding) {
		logger.debug("Emitting solution: " + binding);
	}

}
