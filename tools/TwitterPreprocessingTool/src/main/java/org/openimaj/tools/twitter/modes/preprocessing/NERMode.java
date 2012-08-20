package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoEntityCompleteAnnotator;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateAnnotator;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateMapFileBuilder;
import org.openimaj.text.nlp.namedentity.YagoQueryUtils;
import org.openimaj.text.nlp.namedentity.YagoEntityContextIndexBuilder;
import org.openimaj.text.nlp.namedentity.YagoEntityContextAnnotator;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory;
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
	private static final String ALIAS_LOOKUP = "Company_Aliases";
	private static String CONTEXT_SCORES = "Company_Context";
	private static String DISAMBIGUATED = "Company_Disambiguated";
	private YagoEntityCandidateAnnotator ylca;
	private YagoEntityContextAnnotator ywca;
	private YagoEntityCompleteAnnotator ycca;
	private boolean verbose = true;	

	enum NERModeMode {
		ALL, ALIAS, CONTEXT, DISAMBIG
	}

	@Option(name = "--set-entity-annotations", aliases = "-sea", required = false, usage = "The named entity annotations to be performed", multiValued = true)
	private List<NERModeMode> twitterExtras = new ArrayList<NERModeMode>(Arrays.asList(new NERModeMode[]{NERModeMode.ALL}));

	/**
	 * Default Constructor
	 */
	public NERMode() {
		try {
			System.out.println("Building YagoLookupCompanyAnnotator...");
			ylca = new YagoEntityCandidateAnnotator(
					new YagoEntityCandidateFinderFactory(true)
							.createFromAliasFile(YagoEntityCandidateMapFileBuilder
									.getDefaultMapFilePath()));
		} catch (IOException e) {
			System.out
					.println("YagoLookup Map text file not found:\nBuilding in default location...");
			try {
				YagoEntityCandidateMapFileBuilder.buildDefault();
				ylca = new YagoEntityCandidateAnnotator(new YagoEntityCandidateFinderFactory(
						verbose).createFromAliasFile(YagoEntityCandidateMapFileBuilder
						.getDefaultMapFilePath()));
			} catch (IOException e1) {
				System.out
						.println("Unable to build in default location: "
								+ YagoEntityCandidateMapFileBuilder
										.getDefaultMapFilePath()
								+ "\nAttempting to build in memory from Yago endpoint...");
				ylca = new YagoEntityCandidateAnnotator(
						new YagoEntityCandidateFinderFactory(true)
								.createFromSparqlEndpoint(YagoQueryUtils.YAGO_SPARQL_ENDPOINT));
			}

		}
		if (ylca == null) {
			throwHammer("Unable to build LookupAnnotator");
		}
		// Build Context Annotator
		try {
			System.out.println("Building YagoWikiIndexCompanyAnnotators...");
			ywca = new YagoEntityContextAnnotator(new YagoEntityContextScorerFactory(
					verbose).createFromIndexFile(YagoEntityContextIndexBuilder
					.getDefaultMapFilePath()));
		} 
		catch (IOException e) {
			System.out
					.println("YagoWikiIndex not found:\nBuilding in default location...");
			try {
				YagoEntityContextIndexBuilder.buildDefault();
				ywca = new YagoEntityContextAnnotator(
						new YagoEntityContextScorerFactory(verbose)
								.createFromIndexFile(YagoEntityContextIndexBuilder
										.getDefaultMapFilePath()));
			} catch (IOException e1) {
				System.out
						.println("Unable to build in default location: "
								+ YagoEntityContextIndexBuilder.getDefaultMapFilePath()
								+ "\nAttempting to build in memory from Yago endpoint...");
				try {
					ywca = new YagoEntityContextAnnotator(
							new YagoEntityContextScorerFactory(verbose)
									.createFromSparqlEndPoint(
											YagoQueryUtils.YAGO_SPARQL_ENDPOINT,
											null));
				} catch (IOException e2) {
					throwHammer("Unable to build YagoWikiIndexCompanyAnnotator");
				}
			}
		}
		// Build Complete Annotator
		if (ywca == null)
			throwHammer("Unable to build YagoWikiIndexCompanyAnnotator");
		//ycca = new YagoEntityCompleteAnnotator(ylca, ywca);
	}

	private void throwHammer(String message) {
		System.out.println(message);
	}

	@Override
	public Map<String, List<String>> process(USMFStatus twitterStatus) {
		HashMap<String, ArrayList<HashMap<String, Object>>> result = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		// Add Alias Lookup annotations
		result.put(ALIAS_LOOKUP, new ArrayList<HashMap<String, Object>>());
		// Add context scoring annotations
		result.put(CONTEXT_SCORES, new ArrayList<HashMap<String, Object>>());
		// Add disambiguated annotations
		result.put(DISAMBIGUATED, new ArrayList<HashMap<String, Object>>());

		// Check that the twitterStatus has been tokenised.
		if (twitterStatus.getAnalysis(TokeniseMode.TOKENS) == null) {
			TokeniseMode tm = new TokeniseMode();
			tm.process(twitterStatus);
		}
		List<String> allTokens = ((Map<String, List<String>>) twitterStatus
				.getAnalysis(TokeniseMode.TOKENS)).get(TokeniseMode.TOKENS_ALL);
		
		if (twitterExtras.contains(NERModeMode.ALL) || twitterExtras.contains(NERModeMode.ALIAS)) {
			//Alias Lookup
			for (ScoredAnnotation<HashMap<String, Object>> anno : ylca
					.annotate(allTokens)) {
				result.get(ALIAS_LOOKUP).add(anno.annotation);
			}
		}
		if (twitterExtras.contains(NERModeMode.ALL) || twitterExtras.contains(NERModeMode.CONTEXT)) {
			//Context
			for (ScoredAnnotation<HashMap<String, Object>> anno : ywca
					.annotate(allTokens)) {
				result.get(CONTEXT_SCORES).add(anno.annotation);
			}
		}
		if (twitterExtras.contains(NERModeMode.ALL) || twitterExtras.contains(NERModeMode.DISAMBIG)) {
			//Disambiguated
			for (ScoredAnnotation<HashMap<String, Object>> anno : ycca
					.annotate(allTokens)) {
				result.get(DISAMBIGUATED).add(anno.annotation);
			}
		}
		twitterStatus.addAnalysis(NAMED_ENT_REC, result);
		return null;
	}

	@Override
	public String getAnalysisKey() {
		return NAMED_ENT_REC;
	}

	/**
	 * Tester for mode.
	 * @param args
	 */
	public static void main(String[] args) {
		NERMode m = null;
		try {
			m = new NERMode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		USMFStatus u = new USMFStatus();
		String query = "Nats";
		System.out.println(query);
		u.fillFromString(query);
		m.process(u);
		HashMap<String, ArrayList<HashMap<String, Object>>> analysis = u.getAnalysis(NAMED_ENT_REC);		
		System.out.println("ALIAS LOOKUP");
		for(HashMap<String,Object> anno : analysis.get(ALIAS_LOOKUP)){
			System.out.println(anno.toString());
		}
		System.out.println("CONTEXT");
		for(HashMap<String,Object> anno : analysis.get(CONTEXT_SCORES)){
			System.out.println(anno.toString());
		}
		System.out.println("DISAMBIGUATION");
		for(HashMap<String,Object> anno : analysis.get(DISAMBIGUATED)){
			System.out.println(anno.toString());
		}
		System.out.println("Done");
	}

}
