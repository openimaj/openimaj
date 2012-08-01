package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.NGramGenerator.StringNGramGenerator;

public class YagoLookupCompanyAnnotator
		extends
		AbstractAnnotator<List<String>, HashMap<String, Object>, TfIdfFeatureExtractor> {
	
	private HashMap<String, ArrayList<String>> aliasMap;
	private StopWordStripper ss;
	private boolean verbose=false;	
	
	public YagoLookupCompanyAnnotator(HashMap<String, ArrayList<String>> aliasMap,Map<String, List<String>> tokenisedCorpus){
		super(new TfIdfFeatureExtractor(tokenisedCorpus)); 
		this.aliasMap=aliasMap;
		ss = new StopWordStripper(StopWordStripper.ENGLISH);
	}
	
	@Override
	public Set<HashMap<String, Object>> getAnnotations() {
		return null;
	}

	@Override
	public List<ScoredAnnotation<HashMap<String, Object>>> annotate(
			List<String> tokens) {
		// get Ngram entities
		Map<Integer, HashMap<String,Object>> m1 = getNgramEntities(1, tokens);
		print("Unigrams");
		for (int ind : m1.keySet()) {
			print(ind + " : " + m1.get(ind));
		}
		Map<Integer, HashMap<String,Object>> m2 = getNgramEntities(2, tokens);
		print("Bigrams");
		for (int ind : m2.keySet()) {
			print(ind + " : " + m2.get(ind));
		}
		Map<Integer, HashMap<String,Object>> m3 = getNgramEntities(3, tokens);
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
		ArrayList<ScoredAnnotation<HashMap<String, Object>>> rr = new ArrayList<ScoredAnnotation<HashMap<String,Object>>>();
		for (HashMap<String,Object> ent: m3.values()) {
			rr.add(new ScoredAnnotation<HashMap<String,Object>>(ent, 1));
		}		
		return rr;
	}

	private ArrayList<String> getYagoCandidates(String token) {		
			if (aliasMap.containsKey(token))
				return aliasMap.get(token);
			else
				return null;		
	}

	private HashMap<Integer,HashMap<String,Object>> getNgramEntities(int n, List<String> baseTokens) {
		List<String[]> ngrams = new StringNGramGenerator().getNGrams(
				baseTokens, n);
		List<String> tokens = new ArrayList<String>();
		for (int i = 0; i < ngrams.size(); i++) {
			tokens.add(StringUtils.join(ngrams.get(i), " "));
		}
		HashMap<Integer, HashMap<String, Object>> result = new HashMap<Integer,HashMap<String,Object>>();
		// Try and match ngrams		
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (!ss.isStopWord(token)) {
				ArrayList<String> matches = getYagoCandidates(token);				
				if (matches != null) {
					HashMap<String, Object> entity = new HashMap<String,Object>();
					entity.put("URI", matches.get(0));
					entity.put("START_TOKEN", i);
					entity.put("END_TOKEN", i-1+n);
					entity.put("ENTITY_TYPE", "Company");
					result.put(i,entity);	
				}
			}
		}
		return result;
	}
	
	private void print(String message) {
		if (verbose)
			System.out.println(message);
	}

}
