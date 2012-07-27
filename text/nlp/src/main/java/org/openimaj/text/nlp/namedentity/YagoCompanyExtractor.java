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
	YagoCompanyIndexFactory.YagoCompanyIndex ci;
	HashMap<String, ArrayList<String>> aliasMap;
	boolean useMap;
	private static boolean verbose=false;

	/*public YagoCompanyExtractor(YagoCompanyIndexFactory.YagoCompanyIndex index) {
		super();
		ss = new StopWordStripper(StopWordStripper.ENGLISH);
		ci = index;
		useMap = false;
	}*/

	public YagoCompanyExtractor(HashMap<String, ArrayList<String>> aliasMap) {
		this.aliasMap = aliasMap;
		ss = new StopWordStripper(StopWordStripper.ENGLISH);
		useMap = true;
	}

	private ArrayList<String> getYagoCandidates(String token) {
		if (!useMap) {
			ArrayList<String> rootNames = ci
					.getCompanyListFromAliasToken(token);
			if (rootNames.size() > 0)
				return rootNames;
			else
				return null;
		} else {
			if (aliasMap.containsKey(token))
				return aliasMap.get(token);
			else
				return null;
		}
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
		for (int i :m1.keySet()) {
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
		for (int i :m2.keySet()) {
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

	private Map<Integer, ArrayList<String>> getNgramEntities(int n, List<String> baseTokens) {
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

	/*private Map<Integer, ArrayList<String>> entsFromIndex(int n, List<String> baseTokens){
		List<String[]> ngrams = new StringNGramGenerator().getNGrams(
				baseTokens, n);
		List<String> tokens = new ArrayList<String>();
		for (int i = 0; i < ngrams.size(); i++) {
			tokens.add(StringUtils.join(ngrams.get(i), " "));
		}
		HashMap<Integer, ArrayList<String>> candidates = new HashMap<Integer, ArrayList<String>>();
		// See if any Company Aliases are used
		boolean aliasFound = false;
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (!ss.isStopWord(token)) {
				ArrayList<String> matches = getYagoCandidates(token);
				if (matches != null) {
					candidates.put(i * n, matches);
					aliasFound = true;
				}
			}
		}
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		// If aliases are found...
		if (aliasFound) {
			String context = StringUtils.join(ss.getNonStopWords(tokens), " ");
			// check if any aliases have more then 1 possible company and
			// disambiguate.
			for (int ind : candidates.keySet()) {
				ArrayList<String> ents = candidates.get(ind);
				// disambiguate
				if (ents.size() > 1) {
					ArrayList<String> conts = ci
							.getCompanyListFromContext(context);
					boolean disambiguated = false;
					for (int i = 0; i < conts.size(); i++) {
						if (ents.contains(conts.get(i))) {
							disambiguated = true;
							ents.clear();
							ents.add(conts.get(i));
							break;
						}
					}
					if (!disambiguated) {
						String top = ents.get(0);
						ents.clear();
						ents.add(top);
					}
				}
			}
			result = new HashMap<Integer, String>();
			for (int ind : candidates.keySet()) {
				result.put(ind, "(" + n + ")" + candidates.get(ind).get(0));
			}
		}
		return result;
	}*/
	
	private static void print(String message) {
		if (verbose)
			System.out.println(message);
	}

	public static void main(String[] args) {
		YagoCompanyExtractor ye = new YagoCompanyExtractor(
				YagoCompanyAliasHashMapFactory
						.createFromListFile());
		
		String in = "Apple store";
		System.out.println(in);
		String[] t = in.split(" ");
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(t));
		Map<Integer, ArrayList<String>> ents = ye.getEntities(tokens);
		System.out.println("Results from Main");
		for (int loc : ents.keySet()) {
			System.out.println(loc+" "+ents.get(loc).get(0)+" "+ents.get(loc).get(1)+" "+ents.get(loc).get(2));
		}

	}
}
