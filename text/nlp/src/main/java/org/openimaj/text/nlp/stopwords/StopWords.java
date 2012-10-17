/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
