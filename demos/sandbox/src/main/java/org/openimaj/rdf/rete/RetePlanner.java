package org.openimaj.rdf.rete;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileUtils;

public class RetePlanner {
	private static final String RDFS_RULES = "/org/openimaj/rdf/rules/rdfs-fb-tgc-noresource.rules";

	@SuppressWarnings("unchecked")
	public void setup(){
		InputStream instream = RetePlanner.class.getResourceAsStream(RDFS_RULES);
		List<Rule> rules = Rule.parseRules(Rule.rulesParserFromReader( new BufferedReader(new InputStreamReader(instream))));	
		System.out.println(rules);
	}
	
	public static void main(String[] args) {
		RetePlanner planner = new RetePlanner();
		planner.setup();
	}
}
