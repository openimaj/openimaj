package org.openimaj.rdf.storm.sparql.topology.bolt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.bolt.RETEStormNode;

import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETERuleContext;
import com.hp.hpl.jena.sparql.core.Var;

public class StormSPARQLUnionBolt extends StormSPARQLReteBolt {

	private Map<String,int[]> subQueries;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4763608421352799282L;

	public StormSPARQLUnionBolt(Query query, Map<String, Query> subQs){
		super(query);
		this.subQueries = new HashMap<String,int[]>();
		List<Var> superVars = query.getValuesVariables();
		for (String boltName : subQs.keySet()){
			int[] mapping = new int[superVars.size()];
			List<Var> subVars = subQs.get(boltName).getValuesVariables();
			for (int i = 0; i < superVars.size(); i++){
				mapping[i] = -1;
				for (int x = 0; x < subVars.size(); x++)
					if (superVars.get(i).getName().equals(subVars.get(x).getName())){
						mapping[i] = x;
						break;
					}
			}
			this.subQueries.put(boltName, mapping);
		}
	}
	
	@Override
	public void execute(Tuple input) {
		boolean isAdd = extractIsAdd(input);
		Values output = new Values();
		int[] mapping = this.subQueries.get(input.getSourceComponent());
		if (mapping != null){
			for (int i = 0; i < mapping.length; i++)
				if (mapping[i] < 0)
					output.add(null);
				else 
					output.add(input.getValue(mapping[i]));
			for (int i = input.getValues().size() - Component.values().length; i < input.getValues().size(); i++)
				output.add(input.getValue(i));
		}
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
