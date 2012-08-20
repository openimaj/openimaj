package org.openimaj.text.nlp.namedentity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory.YagoEntityCandidateFinder;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;

public class YagoWikiPlayground {
	double threshold = 0.0;
	static String input = "in-airlines-08-aug-2001.xml";

	public static void main(String[] args) throws IOException {
		YagoWikiPlayground checker = new YagoWikiPlayground();
		String raw = "Tesco";
		// String raw =
		// YagoCompanyAnnotatorEvaluator.getRawStringFromTest(args[0]+File.separator+input);
		System.out.println(raw);
		checker.process(raw);
	}

	public void process(String input) {
		YagoEntityCandidateFinder ycf = null;
		try {
			ycf = new YagoEntityCandidateFinderFactory(true)
					.createFromAliasFile(YagoEntityCandidateMapFileBuilder
							.getDefaultMapFilePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		YagoEntityContextScorer ycs = null;
		try {
			ycs = new YagoEntityContextScorerFactory(true)
					.createFromIndexFile(YagoEntityContextIndexBuilder
							.getDefaultMapFilePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		YagoEntityCandidateAnnotator ylca = new YagoEntityCandidateAnnotator(
				ycf);
		YagoEntityContextAnnotator ywca = new YagoEntityContextAnnotator(ycs);
		YagoEntityCompleteAnnotator ycca = new YagoEntityCompleteAnnotator(ycs,
				ycf);
		TweetTokeniser t = null;
		try {
			t = new TweetTokeniser(input);
		} catch (TweetTokeniserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String> tokens = (ArrayList<String>) t.getStringTokens();
		List<ScoredAnnotation<HashMap<String, Object>>> alias = ylca
				.annotate(tokens);
		List<ScoredAnnotation<HashMap<String, Object>>> cont = ywca
				.annotate(tokens);
		List<ScoredAnnotation<HashMap<String, Object>>> comp = ycca
				.annotate(tokens);
		System.out.println("--------ALIAS------");
		for (ScoredAnnotation<HashMap<String, Object>> scoredAnnotation : alias) {
			System.out.println(scoredAnnotation.annotation.toString());

		}
		System.out.println("--------CONT------");
		for (ScoredAnnotation<HashMap<String, Object>> scoredAnnotation : cont) {
			System.out.println(scoredAnnotation.annotation.toString());
		}
		System.out.println("--------TOTAL------");
		for (ScoredAnnotation<HashMap<String, Object>> scoredAnnotation : comp) {
			System.out.println(scoredAnnotation.annotation.toString());
		}
	}

}
