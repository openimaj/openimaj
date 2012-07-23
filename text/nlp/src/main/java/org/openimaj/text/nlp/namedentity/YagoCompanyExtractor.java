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

	public YagoCompanyExtractor(YagoCompanyIndexFactory.YagoCompanyIndex index) {
		super();
		ss = new StopWordStripper(StopWordStripper.ENGLISH);
		ci = index;
	}

	private ArrayList<String> getYagoCandidates(String token) {
		ArrayList<String> rootNames = ci.getCompanyListFromAliasToken(token);
		if (rootNames.size() > 0)
			return rootNames;
		else
			return null;
	}
	
	@Override
	public Map<Integer, String> getEntities(List<String> tokens) {
		// get Ngram entities
		Map<Integer, String> m1 = getNgramEntities(1,tokens);
		Map<Integer, String> m2 = getNgramEntities(2,tokens);
		Map<Integer, String> m3 = getNgramEntities(3,tokens);	
		// check for single token collisions
		for (int i = 0; i < m1.size(); i++) {
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
		for (int i = 0; i < m2.size(); i++) {
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
		List<String> context = ci.getCompanyListFromContext(StringUtils.join(tokens, " "));
		if(context.size()>0){
			m3.put(-1, StringUtils.join(context,", "));
		}
		return m3;
	}

	private Map<Integer, String> getNgramEntities(int n, List<String> baseTokens) {
		List<String[]> ngrams = new StringNGramGenerator().getNGrams(baseTokens, n);
		List<String> tokens = new ArrayList<String>();
		for(int i = 0; i<ngrams.size();i++){
			tokens.add(StringUtils.join(ngrams.get(i)," "));
		}
		HashMap<Integer, ArrayList<String>> candidates = new HashMap<Integer, ArrayList<String>>();		
		// See if any Company Aliases are used
		boolean aliasFound = false;
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (!ss.isStopWord(token)) {
				ArrayList<String> matches = getYagoCandidates(token);
				if (matches != null) {
					candidates.put(i*n, matches);
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
				result.put(ind, "("+n+")"+candidates.get(ind).get(0));
			}		
		}
		return result;				
	}

	public static void main(String[] args) {
		YagoCompanyExtractor ye = null;
		try {
			ye = new YagoCompanyExtractor(
					YagoCompanyIndexFactory
							.createFromExistingIndex("src/main/resources/org/openimaj/text/namedentity/yagolucene"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] t = "Apple must be an awesome company".split(" ");
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(t));
		Map<Integer, String> ents = ye.getEntities(tokens);
		for (int loc : ents.keySet()) {
			System.out.println(ents.get(loc));
		}

	}
}
