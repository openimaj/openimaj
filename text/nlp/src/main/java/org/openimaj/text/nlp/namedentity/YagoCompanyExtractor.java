package org.openimaj.text.nlp.namedentity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.namedentity.NGramGenerator.StringNGramGenerator;

/**
 * This class aims to use the Yago2 knowledge base to determine weather a
 * tokenised string has any references to companies. getEntities() is the
 * callable method for the results.
 * 
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 */
public class YagoCompanyExtractor implements NamedEntityExtractor {
	StopWordStripper ss;
	HashMap<String, ArrayList<String>> aliasMap;
	private static boolean verbose = false;
	
	public YagoCompanyExtractor(HashMap<String, ArrayList<String>> aliasMap) {
		this.aliasMap = aliasMap;
		ss = new StopWordStripper(StopWordStripper.ENGLISH);
	}

	private ArrayList<String> getYagoCandidates(String token) {
		if (aliasMap.containsKey(token))
			return aliasMap.get(token);
		else
			return null;
	}

	@Override
	public Map<Integer, ArrayList<String>> getEntities(List<String> tokens) {
		// get Ngram entities
		Map<Integer, ArrayList<String>> m1 = getNgramEntities(1, tokens);
		print("Unigrams");
		for (int ind : m1.keySet()) {
			print(ind + " : " + m1.get(ind));
		}
		Map<Integer, ArrayList<String>> m2 = getNgramEntities(2, tokens);
		print("Bigrams");
		for (int ind : m2.keySet()) {
			print(ind + " : " + m2.get(ind));
		}
		Map<Integer, ArrayList<String>> m3 = getNgramEntities(3, tokens);
		print("Trigrams");
		for (int ind : m3.keySet()) {
			print(ind + " : " + m3.get(ind));
		}
		// check for single token collisions
		for (int i : m1.keySet()) {
			boolean collision = false;
			for (int j : m2.keySet()) {
				if (j > i)
					break;
				if (i == j || i == j + 1) {
					collision = true;
					break;
				}
			}
			if (!collision)
				for (int j : m3.keySet()) {
					if (j > i)
						break;
					if (i >= j && i <= j + 2) {
						collision = true;
						break;
					}
				}
			if (!collision)
				m3.put(i, m1.get(i));
		}
		// check for bigram collisions
		for (int i : m2.keySet()) {
			boolean collision = false;
			for (int j : m3.keySet()) {
				if (j > i)
					break;
				if (i == j || i == j + 1) {
					collision = true;
					break;
				}
			}
			if (!collision)
				m3.put(i, m2.get(i));
		}
		return m3;
	}

	private Map<Integer, ArrayList<String>> getNgramEntities(int n,
			List<String> baseTokens) {
		List<String[]> ngrams = new StringNGramGenerator().getNGrams(
				baseTokens, n);
		List<String> tokens = new ArrayList<String>();
		for (int i = 0; i < ngrams.size(); i++) {
			tokens.add(StringUtils.join(ngrams.get(i), " "));
		}
		HashMap<Integer, ArrayList<String>> result = new HashMap<Integer, ArrayList<String>>();
		// Try and match ngrams
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (!ss.isStopWord(token)) {
				ArrayList<String> matches = getYagoCandidates(token);
				if (matches != null) {
					ArrayList<String> entityList = new ArrayList<String>();
					entityList.add(matches.get(0));
					entityList.add(Integer.toString(n));
					entityList.add(token);
					result.put(i, entityList);
				}
			}
		}
		return result;
	}

	private static void print(String message) {
		if (verbose)
			System.out.println(message);
	}

}