package org.openimaj.rdf.rete;

import java.util.List;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class ReteInterpreter {

	public ReteInterpreter(List<Rule> rules) {
		ReteRuleCompiler compiler = new ReteRuleCompiler();
		compiler.compile(rules);
	}

}
