package org.openimaj.tools.twitter.modes.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoCompleteCompanyAnnotator;
import org.openimaj.text.nlp.namedentity.YagoLookupCompanyAnnotator;
import org.openimaj.text.nlp.namedentity.YagoLookupMapFactory;
import org.openimaj.text.nlp.namedentity.YagoCompanyExtractor;
import org.openimaj.text.nlp.namedentity.YagoLookupMapFileBuilder;
import org.openimaj.text.nlp.namedentity.YagoQueryUtils;
import org.openimaj.text.nlp.namedentity.YagoWikiIndexBuilder;
import org.openimaj.text.nlp.namedentity.YagoWikiIndexCompanyAnnotator;
import org.openimaj.text.nlp.namedentity.YagoWikiIndexFactory;
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
	private String CONTEXT_SCORES = "Company_Context";
	private String DISAMBIGUATED = "Company_Disambiguated";
	private YagoLookupCompanyAnnotator ylca;
	private YagoWikiIndexCompanyAnnotator ywca;
	private YagoCompleteCompanyAnnotator ycca;
	private boolean verbose = true;	

	enum NERModeMode {
		ALL, ALIAS, CONTEXT, DISAMBIG
	}

	@Option(name = "--set-entity-annotations", aliases = "-sea", required = false, usage = "The named entity annotations to be performed", multiValued = true)
	private List<NERModeMode> twitterExtras = new ArrayList<NERModeMode>(Arrays.asList(new NERModeMode[]{NERModeMode.ALL}));

	public NERMode() throws Exception {
		// Build the lookup Annotator
		try {
			ylca = new YagoLookupCompanyAnnotator(
					new YagoLookupMapFactory(true)
							.createFromListFile(YagoLookupMapFileBuilder
									.getDefaultMapFilePath()));
		} catch (IOException e) {
			System.out
					.println("YagoLookup Map text file not found:\nBuilding in default location...");
			try {
				YagoLookupMapFileBuilder.buildDefault();
				ylca = new YagoLookupCompanyAnnotator(new YagoLookupMapFactory(
						verbose).createFromListFile(YagoLookupMapFileBuilder
						.getDefaultMapFilePath()));
			} catch (IOException e1) {
				System.out
						.println("Unable to build in default location: "
								+ YagoLookupMapFileBuilder
										.getDefaultMapFilePath()
								+ "\nAttempting to build in memory from Yago endpoint...");
				ylca = new YagoLookupCompanyAnnotator(
						new YagoLookupMapFactory(true)
								.createFromSparqlEndpoint(YagoQueryUtils.YAGO_SPARQL_ENDPOINT));
			}

		}
		if (ylca == null) {
			throwHammer("Unable to build LookupAnnotator");
		}
		// Build Context Annotator
		try {
			ywca = new YagoWikiIndexCompanyAnnotator(new YagoWikiIndexFactory(
					verbose).createFromIndexFile(YagoWikiIndexBuilder
					.getDefaultMapFilePath()));
		} catch (IOException e) {
			System.out
					.println("YagoWikiIndex not found:\nBuilding in default location...");
			try {
				YagoWikiIndexBuilder.buildDefault();
				ywca = new YagoWikiIndexCompanyAnnotator(
						new YagoWikiIndexFactory(verbose)
								.createFromIndexFile(YagoWikiIndexBuilder
										.getDefaultMapFilePath()));
			} catch (IOException e1) {
				System.out
						.println("Unable to build in default location: "
								+ YagoWikiIndexBuilder.getDefaultMapFilePath()
								+ "\nAttempting to build in memory from Yago endpoint...");
				try {
					ywca = new YagoWikiIndexCompanyAnnotator(
							new YagoWikiIndexFactory(verbose)
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
		ycca = new YagoCompleteCompanyAnnotator(0.2, ylca, ywca);
	}

	private void throwHammer(String message) throws Exception {
		throw new Exception(message);
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

	public static void main(String[] args) {
		NERMode m = null;
		try {
			m = new NERMode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		USMFStatus u = new USMFStatus();
		u.fillFromString("Hello there Apple Store");
		m.process(u);
	}

}
