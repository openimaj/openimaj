package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.Map;

import org.openimaj.rdf.storm.bolt.RETEStormNode;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

/**
 * This bolt deals with the consequences of a valid binding for a SPARQL query.
 * The subclasses of this bolt deal with the specifics of SELECT, CONSTRUCT, ASK and DESCRIBE
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class StormSPARQLReteConflictSetBolt extends StormSPARQLReteBolt{

	/**
	 *
	 */
	private static final long serialVersionUID = 5248125498316607622L;

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
	public void prepare() {
	}

	/**
	 * @param simpleQuery
	 * @return constructs the correct {@link StormSPARQLReteConflictSetBolt} given the query's type
	 */
	public static StormSPARQLReteConflictSetBolt construct(Query simpleQuery) {
		if(simpleQuery.isSelectType()){
			return new StormSPARQLReteSelectConflictSetBolt(simpleQuery);
		}
		else if(simpleQuery.isConstructType()){
			return new StormSPARQLReteConstructConflictSetBolt(simpleQuery);
		}
		return null;
	}

}
