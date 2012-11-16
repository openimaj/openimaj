package org.openimaj.rdf.storm.sparql.topology.builder.group;

import org.openimaj.rdf.storm.topology.bolt.CompilationStormRuleReteBoltHolder;
import org.openimaj.util.pair.IndependentPair;

/**
 * A holder class for an {@link IndependentPair} of {@link String} and a {@link CompilationStormRuleReteBoltHolder} extention
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NamedCompilation<T extends CompilationStormRuleReteBoltHolder> extends IndependentPair<String, T> {

	public NamedCompilation(String name, T compilation) {
		super(name, compilation);
	}

}