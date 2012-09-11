package org.openimaj.text.nlp.sentiment.lexicon;

import java.util.Iterator;

import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.SearchResult;

public class LexiconMatcher {
	
	public static void main(String[] args) {
		
		String[] words = new String[]{
				"hello",
				"help",
				"helicopter",
				"sub",
				"lime",
		};
		AhoCorasick<String> tri;
		tri = new AhoCorasick<String>();
		for (String string : words) {
			tri.add(string.getBytes(), string);
		}
		tri.prepare();
		Iterator<SearchResult<String>> result = tri.search("sublime".getBytes());
		while(result.hasNext()){
			SearchResult<String> sr = result.next();
			for(String s:sr.getOutputs()){
				System.out.println(s);
			}
		}
		tri.add("limed".getBytes(), "limed");
		tri.prepare();
		result = tri.search("sublimed".getBytes());
		while(result.hasNext()){
			SearchResult<String> sr = result.next();
			for(String s:sr.getOutputs()){
				System.out.println(s);
			}
		}
	}
	
	

}
