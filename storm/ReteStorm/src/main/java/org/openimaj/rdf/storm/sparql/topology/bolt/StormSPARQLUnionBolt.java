package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.rdf.storm.bolt.RETEStormNode;

import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;

public class StormSPARQLUnionBolt extends StormSPARQLReteBolt {

	private Map<String,int[]> subQueries;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4763608421352799282L;

	public StormSPARQLUnionBolt(Query query, Map<String, Query> subQs){
		super(query);
		
		this.subQueries = new HashMap<String,int[]>();
		for (String boltName : subQs.keySet()){
			
		}
	}
	
	@Override
	public void execute(Tuple input) {
		boolean isAdd = extractIsAdd(input);
		Values output = new Values();
		this.fire(output, isAdd);
	}

	@Override
	public void prepare() {
		//No preparation required
	}
	
	@Override
	public RETEStormNode clone(Map<RETEStormNode, RETEStormNode> netCopy,
			RETERuleContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
