package org.openimaj.tools.twitter.modes.preprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.EntityExtractionResourceBuilder;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateAnnotator;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory.YagoEntityCandidateFinder;
import org.openimaj.text.nlp.namedentity.YagoEntityCompleteAnnotator;
import org.openimaj.text.nlp.namedentity.YagoEntityContextAnnotator;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;
import org.openimaj.twitter.USMFStatus;

/**
 * -m NER
 * 
 * Named Entity Recognition Mode. This mode makes three types of annotation
 * under the heading of Named_Entities. These can be specified with the -sea
 * option. CANDIDATES - returns lists of possible Named Entities based on
 * character matches of aliases. CONTEXT - returns the Named Entities with the
 * highest contextual scores. DISAMBIG - Returns non overlapping unique Named
 * Entities that have been disambiguated based on context.
 * 
 * NB! - Requires the YagoEntityExtraction resource folder. See
 * {@link EntityExtractionResourceBuilder} for how to construct this folder.
 * 
 * @author laurence
 * 
 */
public class NERMode extends
		TwitterPreprocessingMode<Map<String, List<String>>> {
	private static final String NAMED_ENT_REC = "Named_Entities";
	private static final String ALIAS_LOOKUP = "Entity_Candidates";
	private static String CONTEXT_SCORES = "Entity_Context_Scores";
	private static String DISAMBIGUATED = "Entity_Disambiguated";
	private YagoEntityCandidateAnnotator ylca;
	private YagoEntityContextAnnotator ywca;
	private YagoEntityCompleteAnnotator ycca;

	enum NERModeMode {
		ALL, CANDIDATES, CONTEXT, DISAMBIG
	}

	@Option(name = "--set-entity-annotations", aliases = "-sea", required = false, usage = "The named entity annotations to be performed. Default is ALL", multiValued = true)
	private List<NERModeMode> twitterExtras = new ArrayList<NERModeMode>(
			Arrays.asList(new NERModeMode[] { NERModeMode.ALL }));

	@Option(name = "--set-resource-path", aliases = "-srp", required = false, usage = "The path to the resource folder. Default used if not specified.")
	private String resourcePath = null;

	/**
	 * Default Constructor
	 */
	public NERMode() {
		if (resourcePath == null) {
			YagoEntityCandidateFinder canF = YagoEntityCandidateFinderFactory
					.createFromAliasFile(EntityExtractionResourceBuilder
							.getDefaultAliasFilePath());
			ylca = new YagoEntityCandidateAnnotator(canF);
			YagoEntityContextScorer conS = YagoEntityContextScorerFactory
					.createFromIndexFile(EntityExtractionResourceBuilder
							.getDefaultIndexDirectoryPath());
			ywca = new YagoEntityContextAnnotator(conS);
			ycca = new YagoEntityCompleteAnnotator(conS, canF);
		}
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
		@SuppressWarnings("unchecked")
		List<String> allTokens = ((Map<String, List<String>>) twitterStatus
				.getAnalysis(TokeniseMode.TOKENS)).get(TokeniseMode.TOKENS_ALL);

		if (twitterExtras.contains(NERModeMode.ALL)
				|| twitterExtras.contains(NERModeMode.CANDIDATES)) {
			// Alias Lookup
			for (ScoredAnnotation<HashMap<String, Object>> anno : ylca
					.annotate(allTokens)) {
				result.get(ALIAS_LOOKUP).add(anno.annotation);
			}
		}
		if (twitterExtras.contains(NERModeMode.ALL)
				|| twitterExtras.contains(NERModeMode.CONTEXT)) {
			// Context
			for (ScoredAnnotation<HashMap<String, Object>> anno : ywca
					.annotate(allTokens)) {
				result.get(CONTEXT_SCORES).add(anno.annotation);
			}
		}
		if (twitterExtras.contains(NERModeMode.ALL)
				|| twitterExtras.contains(NERModeMode.DISAMBIG)) {
			// Disambiguated
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
	 * 
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
		String query = "British Airways and Lufthansa are airlines";
		System.out.println(query);
		u.fillFromString(query);
		m.process(u);
		HashMap<String, ArrayList<HashMap<String, Object>>> analysis = u
				.getAnalysis(NAMED_ENT_REC);
		System.out.println("ALIAS LOOKUP");
		for (HashMap<String, Object> anno : analysis.get(ALIAS_LOOKUP)) {
			System.out.println(anno.toString());
		}
		System.out.println("CONTEXT");
		for (HashMap<String, Object> anno : analysis.get(CONTEXT_SCORES)) {
			System.out.println(anno.toString());
		}
		System.out.println("DISAMBIGUATION");
		for (HashMap<String, Object> anno : analysis.get(DISAMBIGUATED)) {
			System.out.println(anno.toString());
		}
		System.out.println("Done");
	}

}
