package org.openimaj.tools.twitter.modes.preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.openimaj.text.nlp.namedentity.YagoLookupMapFactory;
import org.openimaj.text.nlp.namedentity.YagoCompanyExtractor;
import org.openimaj.twitter.USMFStatus;

/**
 * -m NER -ete COMPANY
 * 
 * @author laurence
 * 
 */
public class NERMode extends
		TwitterPreprocessingMode<Map<String, List<String>>> {
	private static final String NAMED_ENT_REC = "Named_Entities";
	private static final String NAMED_ENT_ORGANISATIONS= "Organisations";
	
	private YagoCompanyExtractor yce;

	enum NERModeMode {
		ALL, COMPANY;
	}

	@Option(name = "--entity-to-extract", aliases = "-ete", required = false, usage = "The named entity extraction mode", multiValued = true)
	private NERModeMode twitterExtras = NERModeMode.COMPANY;

	public NERMode() {
		yce = new YagoCompanyExtractor(
				new YagoLookupMapFactory(false).createFromListFile(null)
						);
	}

	@Override
	public Map<String, List<String>> process(USMFStatus twitterStatus) {
		HashMap<String, ArrayList<HashMap<String, Object>>> result = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		result.put(NAMED_ENT_ORGANISATIONS, new ArrayList<HashMap<String, Object>>());
		//Check that the twitterStatus has been tokenised.
		if(twitterStatus.getAnalysis(TokeniseMode.TOKENS)==null){
			TokeniseMode tm = new TokeniseMode();
			tm.process(twitterStatus);
		}
		Map<String,List<String>> tokenAnalysis = twitterStatus.getAnalysis(TokeniseMode.TOKENS);
		Map<Integer, ArrayList<String>> companies = yce.getEntities(tokenAnalysis.get(TokeniseMode.TOKENS_ALL));
		for(int ind:companies.keySet()){
			HashMap<String,Object> ent = new HashMap<String,Object>();
			ent.put("Entity", companies.get(ind).get(0));
			ent.put("Start_Token",ind);
			ent.put("ngram", companies.get(ind).get(2));
			result.get(NAMED_ENT_ORGANISATIONS).add(ent);
			System.out.println(companies.get(ind).get(0));			
		}
		twitterStatus.addAnalysis(NAMED_ENT_REC, result);
		return null;
	}

	@Override
	public String getAnalysisKey() {
		return NAMED_ENT_REC;
	}
	
	public static void main(String[] args){
		NERMode m = new NERMode();
		USMFStatus u = new USMFStatus();
		u.fillFromString("Hello there Apple Store");
		m.process(u);
	}

}
