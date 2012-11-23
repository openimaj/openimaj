package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.bolt.RETEStormQueue;
import org.openimaj.rdf.storm.bolt.RETEStormSinkNode;
import org.openimaj.rdf.storm.sparql.topology.builder.datasets.StaticRDFDataset;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.time.Timer;

import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.util.ModelUtils;

/**
 * An implementation of the {@link RETEStormQueue} which handles the querying of
 * static data stores using the query of the sibling bound with the variables
 * of this queue.
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StaticDataRETEStormQueue extends RETEStormQueue {
	private static Logger logger = Logger.getLogger(StaticDataRETEStormQueue.class);
	private Query query;
	/**
	 * A list of static datasets
	 */
	public List<StaticRDFDataset> staticDatasets = new ArrayList<StaticRDFDataset>();

	/**
	 * Constructor. The window is not usable until it has been bound
	 * to a sibling and a continuation node.
	 *
	 * @param matchFields
	 *            Maps each field of the input tuple to the index of the
	 *            equivalent field in tuples from the other side of the join.
	 * @param outputFields
	 *            Maps each field of the output tuple to the index of the
	 *            equivalent field of the input tuple.
	 * @param size
	 * @param delay
	 * @param unit
	 * @param q
	 */
	public StaticDataRETEStormQueue(String name,
			int[] matchFields,
			int[] outputFields,
			int size,
			long delay,
			TimeUnit unit, Query q) {
		super(name, matchFields, outputFields, size, delay, unit);
		this.query = q;
	}

	/**
	 * Constructor including sibling to bind to. The window is not usable until
	 * it has
	 * also been bound to a continuation node.
	 *
	 * @param matchFields
	 *            Maps each field of the input tuple to the index of the
	 *            equivalent field in tuples from the other side of the join.
	 * @param outputFields
	 *            Maps each field of the output tuple to the index of the
	 *            equivalent field of the input tuple.
	 * @param size
	 * @param delay
	 * @param unit
	 * @param sib
	 * @param q
	 */
	public StaticDataRETEStormQueue(String name,
			int[] matchFields,
			int[] outputFields,
			int size,
			long delay,
			TimeUnit unit,
			StaticDataRETEStormQueue sib, Query q) {
		super(name, matchFields, outputFields, size, delay, unit, sib);
		this.query = q;
	}

	/**
	 * Constructor including sibling to bind to. The window is not usable until
	 * it has
	 * also been bound to a continuation node.
	 *
	 * @param matchFields
	 *            Maps each field of the input tuple to the index of the
	 *            equivalent field in tuples from the other side of the join.
	 * @param outputFields
	 *            Maps each field of the output tuple to the index of the
	 *            equivalent field of the input tuple.
	 * @param size
	 * @param delay
	 * @param unit
	 * @param sib
	 * @param sink
	 * @param q
	 */
	public StaticDataRETEStormQueue(String name,
			int[] matchFields,
			int[] outputFields,
			int size,
			long delay,
			TimeUnit unit,
			StaticDataRETEStormQueue sib,
			RETEStormSinkNode sink, Query q) {
		super(name, matchFields, outputFields, size, delay, unit, sib, sink);
		this.query = q;
	}

	/**
	 * @param dataset a static data source
	 */
	public void addStaticRDFDataset(StaticRDFDataset dataset){
		this.staticDatasets.add(dataset);
	}

	@Override
	public void fire(Tuple env, boolean isAdd, long timestamp) {
		super.fire(env, isAdd, timestamp);
		// Now check the static data source
		StaticDataRETEStormQueue staticDataSiblling = (StaticDataRETEStormQueue) this.sibling;
		Query siblingQuery = staticDataSiblling.query;

		QuerySolutionMap solution = new QuerySolutionMap();
		List<Object> vals = env.getValues();
		Model model = ModelFactory.createDefaultModel();
		for (int i = 0; i < matchIndices.length; i++) {
			int  matchIndex = this.matchIndices[i];
			if(matchIndex!=-1){
				RDFNode node = ModelUtils.convertGraphNodeToRDFNode((Node) vals.get(i), model);
				solution.add("?" + matchIndex, node);
			}
		}
		logger.debug("\n" + "Testing query: \n" + siblingQuery);
		logger.debug("\n" + "With bindings: \n" + solution);
		for (StaticRDFDataset ds : this.staticDatasets) {
			Timer t = Timer.timer();
			logger.debug("Querying static dataset: " + ds);
			ResultSet rs = ds.performQuery(siblingQuery,solution);
			while (rs.hasNext()) {
				QuerySolution binding = rs.next();
				Values newVals = new Values();
				for (int i = 0; i < this.outputIndices.length; i++) {
					int ind = this.outputIndices[i];
					if(ind < 0){
						int sibInd = staticDataSiblling.outputIndices[i];
						newVals.add(binding.get("?" + sibInd).asNode());
					}else{
						newVals.add(env.getValue(ind));
					}
				}
				// We only take the graph from the matching component
				// The static graph might be huge after all right?
				Graph newG = (Graph) env.getValueByField(StormReteBolt.Component.graph.toString());
				addMetaValues(newVals, isAdd, newG, timestamp);
				continuation.fire(newVals, isAdd);
				continuation.emit(env);
				logger.debug("Found static binding: " + binding);
			}
			logger.debug("Dataset query took: " + t.duration()/1000f + "s");
		}

	}
}
