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
