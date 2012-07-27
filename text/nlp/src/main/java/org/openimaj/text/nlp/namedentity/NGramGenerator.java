package org.openimaj.text.nlp.namedentity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.util.StringUtils;



public abstract class NGramGenerator<T> {
	
	public static class StringNGramGenerator extends NGramGenerator<String>{

		public StringNGramGenerator() {
			super(String.class);
		}
		
	}

	private Class<?> clazz; 
	public NGramGenerator(Class type) {
		clazz = type;
	}

	@SuppressWarnings("unchecked")
	public List<T[]> getNGrams(List<T> tokens, int... ngrams) {
		if(tokens==null)return new ArrayList<T[]>();
		ArrayList<T[]> result = new ArrayList<T[]>();
		for (int i = 0; i < tokens.size(); i++) {
			for (int nsize : ngrams) {
				if (i + nsize <= tokens.size()) {					
					T[] ngram = (T[]) Array.newInstance(clazz, nsize);
					for (int j = 0; j < nsize; j++) {
						ngram[j] = tokens.get(i + j);
					}
					result.add(ngram);
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		String[] tokA = "#まひろ同盟".split(" ");
		ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(tokA));
		NGramGenerator<String> ngg = new StringNGramGenerator();
		
		List<String[]> ngrams = ngg.getNGrams(null, 3);
		for (String[] ngram : ngrams) {
			System.out.println(StringUtils.join(ngram," "));
		}
	}

}
