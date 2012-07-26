package org.openimaj.rdf.rete;

import java.util.List;

import org.openimaj.rdf.rete.nodes.AlphaNode;
import org.openimaj.rdf.rete.nodes.AlphaStore;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class ReteRuleCompiler {
	
	public ReteRuleCompiler() {
		Rete rete = new Rete();
	}
	
	public void compile(List<Rule> rules) {
		for (Rule rule : rules) {
			if(rule.isAxiom() || rule.bodyLength() == 0) continue;
			AlphaStore store = new AlphaStore();
			for (ClauseEntry entry : rule.getBody()) {
				if(entry instanceof TriplePattern){
					TriplePattern pattern = (TriplePattern) entry;
					AlphaNode node = makeAlphaNode(pattern);
//					store.addNode(node);
				}
			}
		}
	}

	private AlphaNode makeAlphaNode(TriplePattern pattern) {
		return new AlphaNode(pattern);		
	}

}
