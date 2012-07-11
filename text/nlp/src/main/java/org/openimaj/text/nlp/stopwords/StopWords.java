package org.openimaj.text.nlp.stopwords;

//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Iterator;
//import java.util.List;
//
//import org.arabidopsis.ahocorasick.AhoCorasick;
//import org.arabidopsis.ahocorasick.SearchResult;
//import org.openimaj.io.FileUtils;
//import org.openimaj.text.nlp.sentiment.model.classifier.UniqueWordNaiveBayesSentimentModel;
//
///**
// * By loading a line seperated list of stop-words, check whether a word is junk and should be 
// * thrown away generally.
// * @author Sina Samangooei (ss@ecs.soton.ac.uk)
// *
// */
//public class StopWords {
//	private AhoCorasick<String> stopWordSearch;
//
//	/**
//	 * Load the default stop words list from /org/openimaj/text/stopwords/stopwords-list.txt
//	 */
//	public StopWords() {
//		try {
//			List<String> swords = Arrays.asList(
//				FileUtils.readlines(
//					UniqueWordNaiveBayesSentimentModel.class.getResourceAsStream(
//						"/org/openimaj/text/stopwords/stopwords-list.txt"
//					)
//				)
//			);
//			stopWordSearch = new AhoCorasick<String>();
//			for (String sword : swords) {
//				stopWordSearch.add(sword.getBytes(), sword);
//			}
//			stopWordSearch.prepare();
//		} catch (IOException e) {
//		}
//	}
//	
//	/**
//	 * @param word
//	 * @return whether the word is exactly a stop word 
//	 */
//	public boolean isStopWord(String word){
//		Iterator<SearchResult<String>> foundStopWords = this.stopWordSearch.search(word.getBytes());
//		boolean found = false;
//		for (; foundStopWords.hasNext();) {
//			SearchResult<String> results = foundStopWords.next();
//			found = results.getOutputs().contains(word);
//			if(found) break;
//		}
//		return found;
//	}
//}
