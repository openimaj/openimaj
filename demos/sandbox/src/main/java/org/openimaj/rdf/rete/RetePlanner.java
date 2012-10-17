/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.rete;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.util.filter.Filter;
import org.openimaj.util.filter.FilterUtils;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class RetePlanner {
	private static final String RDFS_RULES = "/org/openimaj/rdf/rules/rdfs-fb-tgc-noresource.rules";
	static{
		Logger.getRootLogger().setLevel(Level.ERROR);
	}

	@SuppressWarnings("unchecked")
	public void setup(){
		InputStream instream = RetePlanner.class.getResourceAsStream(RDFS_RULES);
		List<Rule> rules = Rule.parseRules(Rule.rulesParserFromReader( new BufferedReader(new InputStreamReader(instream))));
		ReteInterpreter interp = new ReteInterpreter(rules);
		System.out.println(interp);
	}

	public static void main(String[] args) {
		RetePlanner planner = new RetePlanner();
//		planner.setup();
		planner.setupJena();
	}

	private void setupJena() {
		InputStream instream = RetePlanner.class.getResourceAsStream(RDFS_RULES);
		List<Rule> rules = Rule.parseRules(Rule.rulesParserFromReader( new BufferedReader(new InputStreamReader(instream))));
		rules = FilterUtils.filter(rules, new Filter<Rule>(){
			boolean oneAxiom = true;
			@Override
			public boolean accept(Rule rule) {
				String name = rule.getName();
				if(name!=null && name.equals("rdfs6"))
					return true;
				if(rule.isAxiom()){
					if(oneAxiom){
						if(rule.getHead()[0] instanceof Functor)return false;
						oneAxiom = false;
						rule.isAxiom() ;
						return true;
					}
				}
				return false;
			}

		});
		Model emptyModel = ModelFactory.createDefaultModel();
		InfModel inf = ModelFactory.createRDFSModel(emptyModel);
		System.out.println(inf);

		RDFSRuleReasoner reasoner = (RDFSRuleReasoner) RDFSRuleReasonerFactory.theInstance().create(null);
		reasoner.setMode(GenericRuleReasoner.FORWARD_RETE);
		reasoner.bind(emptyModel.getGraph());
	}
}
