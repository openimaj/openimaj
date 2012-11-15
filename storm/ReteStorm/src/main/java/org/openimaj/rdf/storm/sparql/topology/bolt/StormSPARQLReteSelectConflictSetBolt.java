package org.openimaj.rdf.storm.sparql.topology.bolt;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ModelUtils;

/**
 * Given a SELECT SPARQL query, output the bindings as name,value pairs.
 * Values may be blank if parts of the query are OPTIONAL.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StormSPARQLReteSelectConflictSetBolt extends StormSPARQLReteConflictSetBolt {

	/**
	 *
	 */
	private static final long serialVersionUID = -4996437363510534715L;

	private static Logger logger = Logger.getLogger(StormSPARQLReteSelectConflictSetBolt.class);

	/**
	 * @param query
	 */
	public StormSPARQLReteSelectConflictSetBolt(Query query) {
		super(query);
	}

	@Override
	public void handleBinding(QueryIterator bindingsIter) {
		Query query = this.getQuery();
		if (!query.isSelectType()) {
			logger.error("Query was not select, select terminal bolt failing!");
			return;
		}
		VarExprList project = query.getProject();
		if (bindingsIter != null) { // aggregation?
			Model model = ModelFactory.createDefaultModel();
			for (; bindingsIter.hasNext();) {
				// Create a solution for each binding
				QuerySolutionMap sol = new QuerySolutionMap();
				Binding bind = bindingsIter.next();
				for (String var : query.getResultVars()) {
					Var alloc = Var.alloc(var);
					Node n;
					if (project.getExprs().containsKey(alloc)) {
						Expr expr = project.getExprs().get(alloc);
						n = expr.eval(bind, null).asNode();
					} else {
						n = bind.get(alloc);
					}

					if (n == null) {
						// At this point we can only believe that this is an EMPTY binding for optional variables.
						n = Node.NULL;
					}
					RDFNode node = ModelUtils.convertGraphNodeToRDFNode(n, model);
					sol.add(var, node);
				}
				emitSolution(sol);
			}
		}

	}
}
