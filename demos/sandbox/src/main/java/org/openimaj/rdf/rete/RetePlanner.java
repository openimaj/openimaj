package org.openimaj.rdf.rete;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.util.filter.Filter;
import org.openimaj.util.filter.FilterUtils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.FGraph;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.RDFSReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.RETERuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEEngine;
import com.hp.hpl.jena.util.FileUtils;

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
//		InputStream instream = RetePlanner.class.getResourceAsStream(RDFS_RULES);
//		List<Rule> rules = Rule.parseRules(Rule.rulesParserFromReader( new BufferedReader(new InputStreamReader(instream))));
//		rules = FilterUtils.filter(rules, new Filter<Rule>(){
//			boolean oneAxiom = true;
//			@Override
//			public boolean accept(Rule rule) {
//				String name = rule.getName();
//				if(name!=null && name.equals("rdfs6"))
//					return true;
//				if(rule.isAxiom()){
//					if(oneAxiom){
//						if(rule.getHead()[0] instanceof Functor)return false;
//						oneAxiom = false;
//						rule.isAxiom() ;
//						return true;
//					}
//				}
//				return false;
//			}
//			
//		});
////		Model emptyModel = ModelFactory.createDefaultModel();
////		Graph empty = emptyModel.getGraph();
////		GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
////		InfModel schema = ModelFactory.createInfModel(reasoner, emptyModel);
////		RETERuleInfGraph infGraph = new RETERuleInfGraph(reasoner,schema.getGraph());
////		RETEEngine engine = new RETEEngine(infGraph,rules);
////		engine.init(true, new FGraph(empty));
//		RDFSRuleReasonerFactory.theInstance().create(configuration);
	}
}
