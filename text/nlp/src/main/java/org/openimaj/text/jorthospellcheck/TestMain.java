package org.openimaj.text.jorthospellcheck;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestMain {	
	
	public static void main(String[] args){
		
		DictionaryFactory df = new DictionaryFactory();
		ArrayList<String> words = new ArrayList<String>();
		words.add("Hello");
		words.add("world");	
		
		df.loadWords(words.iterator());
		
		Dictionary myDictionary = df.create();
		List<Suggestion> possibles = myDictionary.searchSuggestions("helo");
		for (Suggestion suggestion : possibles) {
			System.out.println(suggestion.getWord());
		}
	}

}
