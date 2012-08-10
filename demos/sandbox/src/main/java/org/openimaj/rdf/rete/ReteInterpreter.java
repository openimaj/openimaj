package org.openimaj.rdf.rete;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class ReteInterpreter {

	private List<Rule> rules;

	public ReteInterpreter(List<Rule> rules) {
		this.rules = rules;
		ReteRuleCompiler compiler = new ReteRuleCompiler();
		compiler.compile(rules);
	}
	
	@Override
	public String toString() {
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		for (Rule rule: this.rules) {
			writer.println("RULENAME = " + rule.getName());
			writer.println("\tBODY (len = " + rule.getBody().length + ")");
			for (ClauseEntry bodyClause : rule.getBody()) {				
				writer.println("\t\tBODY CLAUSE = " + bodyClause);
			}
			writer.println("\tHEAD (len = " + rule.getHead().length + ")");
			for (ClauseEntry headClause : rule.getHead()) {				
				boolean isFunctor = headClause instanceof Functor;
				String[] clz = headClause.getClass().toString().split("[.]");
				String type = clz[clz.length-1];
				writer.println("\t\tHEAD CLAUSE = " + headClause + "(type = " + type + ")");
			}
//			writer.println(rule.toString());
		}
		writer.flush();
		return swriter.toString();
	}
}
