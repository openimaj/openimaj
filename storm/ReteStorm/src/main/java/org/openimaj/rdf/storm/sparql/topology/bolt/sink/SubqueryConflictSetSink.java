package org.openimaj.rdf.storm.sparql.topology.bolt.sink;

import java.util.Iterator;

import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt.StormSPARQLReteConflictSetBoltSink;

import backtype.storm.task.OutputCollector;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SubqueryConflictSetSink implements StormSPARQLReteConflictSetBoltSink {

	private OutputCollector context;

	@Override
	public void instantiate(StormSPARQLReteConflictSetBolt conflictSet) {
		this.context = conflictSet.getCollector();
	}

	@Override
	public void consumeTriple(Triple triple) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void consumeBindings(QueryIterator bindingsIter) {
		while (bindingsIter.hasNext()) {
			Values vals = new Values();
			Binding binding = bindingsIter.next();
			Iterator<Var> vars = binding.vars();
			while (vars.hasNext()) {
				Var var = vars.next();
				vals.add(binding.get(var));
			}
			context.emit(vals);
		}
	}

}
