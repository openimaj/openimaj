package org.openimaj.rdf.storm.sparql.topology.bolt;

import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.query.Query;

public class StormSPARQLReteConstructConflictSetBolt extends StormSPARQLReteConflictSetBolt {

	/**
	 *
	 */
	private static final long serialVersionUID = -1337401778771700226L;

	public StormSPARQLReteConstructConflictSetBolt(Query query) {
		super(query);
	}

	@Override
	public void execute(Tuple input) {
		System.out.println(input);
	}


}
