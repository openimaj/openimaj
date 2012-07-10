package org.openimaj.text.jorthospellcheck;

import java.util.ArrayList;
import java.util.List;


public class BasicSpellChecker {
	
	Dictionary myDictionary;
	
	public BasicSpellChecker (ArrayList<String> listOfWords){
		DictionaryFactory df = new DictionaryFactory();		
		df.loadWords(listOfWords.iterator());		
		myDictionary = df.create();
	}
	
	public boolean exists(String word){
		return myDictionary.exist(word);
	}
	
	public List<String> getSugggestions(String word){
		List<Suggestion> raw = myDictionary.searchSuggestions(word);
		ArrayList<String> result = new ArrayList<String>();
		for (Suggestion suggestion : raw) {
			result.add(suggestion.getWord());
		}
		return result;
	}

}
