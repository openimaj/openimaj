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
package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.namedentity.NGramGenerator.StringNGramGenerator;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

/**
 * Factory object for : -creating {@link YagoEntityCandidateFinder} in various
 * ways. -creating Yago Entity Alias text files in various ways.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class YagoEntityCandidateFinderFactory {
	
	
	/**
	 * Returns a {@link YagoEntityCandidateFinder} given a path Yago Entity
	 * Alias textfile
	 * 
	 * @param pathToAliasFile
	 * @return {@link YagoEntityCandidateFinder}
	 */
	public static YagoEntityCandidateFinder createFromAliasFile(String pathToAliasFile){
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		
		InputStream fstream = null;
		try {
			fstream = new FileInputStream(pathToAliasFile);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = null;
		try {
			strLine = br.readLine();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		// Read File Line By Line
		String entity_Uri = null;
		while (strLine != null) {
			if (!strLine.startsWith("+") && !strLine.startsWith(".")) {
				try {
					strLine = br.readLine();
				} catch (IOException e) {					
					e.printStackTrace();
				}
				continue;
			}
			if (strLine.startsWith("+")) {
				entity_Uri = strLine.substring(1);
				try {
					strLine = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
			while (strLine != null && strLine.startsWith(".")) {
				String alias = strLine.substring(1).trim();
				if (result.containsKey(alias)) {
					if (!result.get(alias).contains(entity_Uri)) {
						result.get(alias).add(entity_Uri);						
					}
				} else {
					ArrayList<String> comps = new ArrayList<String>();
					comps.add(entity_Uri);
					result.put(alias, comps);					
				}
				try {
					strLine = br.readLine();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
		}
		// Close the input stream
		try {
			in.close();
		} catch (IOException e) { 			
			e.printStackTrace();
		}		
		return new YagoEntityCandidateFinder(result);
	}

	/**
	 * Class that uses an Alias HashMap to find candidate Entities for a list of
	 * tokens.
	 */
	public static class YagoEntityCandidateFinder {

		private HashMap<String, ArrayList<String>> aliasMap;
		private IgnoreTokenStripper ss;
		private ArrayList<Integer> ngrams;

		private YagoEntityCandidateFinder(
				HashMap<String, ArrayList<String>> aliasMap) {
			ss = new IgnoreTokenStripper(IgnoreTokenStripper.Language.English);
			this.aliasMap = aliasMap;
			this.setNgrams( 1, 2, 3, 4 , 5);
		};

		/**
		 * Set the ngram sizes that the CandidateFinder will search with.
		 * @param ngrams
		 */
		public void setNgrams(Integer... ngrams) {
			HashSet<Integer> ngUnique = new HashSet<Integer>(
					Arrays.asList(ngrams));
			ArrayList<Integer> n = new ArrayList<Integer>(ngUnique);
			Collections.sort(n);
			this.ngrams = n;
		}

		/**
		 * Gets candidate entities.
		 * 
		 * @param tokens
		 * @return A list of a list of {@link NamedEntity}s that are matched to
		 *         the same tokens. ( A token ngram can match to multiple
		 *         entities )
		 */
		public List<List<NamedEntity>> getCandidates(List<String> tokens) {
			// get Ngram entities
			HashMap<Integer, Map<Integer, List<NamedEntity>>> ngramEntities = new HashMap<Integer, Map<Integer, List<NamedEntity>>>();
			for (int i = 0; i < ngrams.size(); i++) {
				ngramEntities.put(ngrams.get(i),
						getNgramEntities(ngrams.get(i), tokens));
			}
			// Resolve Collisions
			Map<Integer, List<NamedEntity>> top = ngramEntities.get(ngrams
					.get(ngrams.size() - 1));
			// For each map of ngram Entities starting from smallest ngram...
			for (int i = 0; i < ngrams.size(); i++) {
				int lowSize = ngrams.get(i);
				Map<Integer, List<NamedEntity>> lowEnts = ngramEntities
						.get(lowSize);
				// ...for each ngram Entity in the map...
				for (int startTokenLow : lowEnts.keySet()) {
					int endTokenLow = startTokenLow + lowSize - 1;
					boolean collision = false;
					// ...check that it does not collide with a larger ngram
					// entity...
					breakLoop: for (int j = i + 1; j < ngrams.size(); j++) {

						int highSize = ngrams.get(j);
						for (int startTokenHigh : ngramEntities.get(highSize)
								.keySet()) {
							int endTokenHigh = startTokenHigh + highSize - 1;
							if ((startTokenLow <= endTokenHigh && startTokenLow >= startTokenHigh)
									|| (endTokenLow >= startTokenHigh && endTokenLow <= endTokenHigh)) {
								collision = true;
								break breakLoop;
							}
						}
					}
					if (!collision) {
						top.put(startTokenLow, lowEnts.get(startTokenLow));
					}
				}
			}
			ArrayList<List<NamedEntity>> rr = new ArrayList<List<NamedEntity>>();
			for (List<NamedEntity> entList : top.values()) {
				rr.add(entList);
			}
			return rr;
		}
		
		/**
		 * Gets candidate entities.
		 * 
		 * @param tokens
		 * @return A list of a list of {@link NamedEntity}s that are matched to
		 *         the same tokens. ( A token ngram can match to multiple
		 *         entities )
		 */
		public List<List<NamedEntity>> getCandidatesFromReversableTokenList(List<TokenAnnotation> tokens) {
			// get Ngram entities
			HashMap<Integer, Map<Integer, List<NamedEntity>>> ngramEntities = new HashMap<Integer, Map<Integer, List<NamedEntity>>>();
			for (int i = 0; i < ngrams.size(); i++) {
				ngramEntities.put(ngrams.get(i),
						getNgramEntitiesFromRTL(ngrams.get(i), tokens));
			}
			// Resolve Collisions
			Map<Integer, List<NamedEntity>> top = ngramEntities.get(ngrams
					.get(ngrams.size() - 1));
			// For each map of ngram Entities starting from smallest ngram...
			for (int i = 0; i < ngrams.size(); i++) {
				int lowSize = ngrams.get(i);
				Map<Integer, List<NamedEntity>> lowEnts = ngramEntities
						.get(lowSize);
				// ...for each ngram Entity in the map...
				for (int startTokenLow : lowEnts.keySet()) {
					int endTokenLow = startTokenLow + lowSize - 1;
					boolean collision = false;
					// ...check that it does not collide with a larger ngram
					// entity...
					breakLoop: for (int j = i + 1; j < ngrams.size(); j++) {

						int highSize = ngrams.get(j);
						for (int startTokenHigh : ngramEntities.get(highSize)
								.keySet()) {
							int endTokenHigh = startTokenHigh + highSize - 1;
							if ((startTokenLow <= endTokenHigh && startTokenLow >= startTokenHigh)
									|| (endTokenLow >= startTokenHigh && endTokenLow <= endTokenHigh)) {
								collision = true;
								break breakLoop;
							}
						}
					}
					if (!collision) {
						top.put(startTokenLow, lowEnts.get(startTokenLow));
					}
				}
			}
			ArrayList<List<NamedEntity>> rr = new ArrayList<List<NamedEntity>>();
			for (List<NamedEntity> entList : top.values()) {
				rr.add(entList);
			}
			return rr;
		}

		private Map<Integer, List<NamedEntity>> getNgramEntitiesFromRTL(
				Integer n, List<TokenAnnotation> baseTokens) {
			NGramGenerator<TokenAnnotation> ngen = new NGramGenerator<TokenAnnotation>(TokenAnnotation.class);
			List<TokenAnnotation[]> ngrams = ngen.getNGrams(
					baseTokens, n);
			List<String> tokens = new ArrayList<String>();
			for (int i = 0; i < ngrams.size(); i++) {
				tokens.add(ngrams.get(0)[0].reverse(Arrays.asList(ngrams.get(i))).trim());
			}
			HashMap<Integer, List<NamedEntity>> result = new HashMap<Integer, List<NamedEntity>>();
			// Try and match ngrams
			for (int i = 0; i < tokens.size(); i++) {
				String token = tokens.get(i);
				if (!ss.isIgnoreToken(token)) {
					ArrayList<String> matches = getYagoCandidates(token);
					if (matches != null) {
						ArrayList<NamedEntity> subRes = new ArrayList<NamedEntity>();
						for (String match : matches) {
							NamedEntity ne = new NamedEntity();
							TokenAnnotation[] ngram = ngrams.get(i);
							ne.rootName=match;
							ne.startChar=ngram[0].start;
							ne.stopChar=ngram[ngram.length-1].stop;
							ne.type = NamedEntity.Type.Organisation;
							subRes.add(ne);
						}
						result.put(i, subRes);
					}
				}
			}
			return result;
		}

		private HashMap<Integer, List<NamedEntity>> getNgramEntities(int n,
				List<String> baseTokens) {
			List<String[]> ngrams = new StringNGramGenerator().getNGrams(
					baseTokens, n);
			List<String> tokens = new ArrayList<String>();
			for (int i = 0; i < ngrams.size(); i++) {
				tokens.add(StringUtils.join(ngrams.get(i), " "));
			}
			HashMap<Integer, List<NamedEntity>> result = new HashMap<Integer, List<NamedEntity>>();
			// Try and match ngrams
			for (int i = 0; i < tokens.size(); i++) {
				String token = tokens.get(i);
				if (!ss.isIgnoreToken(token)) {
					ArrayList<String> matches = getYagoCandidates(token);
					if (matches != null) {
						ArrayList<NamedEntity> subRes = new ArrayList<NamedEntity>();
						for (String match : matches) {
							NamedEntity ne = new NamedEntity();
							ne.rootName = match;
							ne.startToken = i;
							ne.stopToken = i - 1 + n;
							ne.type = NamedEntity.Type.Organisation;
							subRes.add(ne);
						}
						result.put(i, subRes);
					}
				}
			}
			return result;
		}

		private ArrayList<String> getYagoCandidates(String token) {
			if (aliasMap.containsKey(token)) {
				//System.out.println(aliasMap.get(token));
				ArrayList<String> result = aliasMap.get(token);
				return result;
			} else
				return null;
		}

	}

}
