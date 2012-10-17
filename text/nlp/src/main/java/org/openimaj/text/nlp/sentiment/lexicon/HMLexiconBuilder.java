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
package org.openimaj.text.nlp.sentiment.lexicon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.SearchResult;
import org.openimaj.text.nlp.textpipe.annotations.AnnotationUtils;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;
import org.openimaj.text.nlp.textpipe.annotators.MissingRequiredAnnotationException;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPTokenAnnotator;

/**
 * An implementation of Hatzivassiloglou and McKeown's approach to a
 * semisupervised method of building a bipolar sentiment lexicon. This is a one
 * pass version, in that the corpus to build from is fixed.
 * 
 */
public class HMLexiconBuilder {

	Set<String> positiveLexicon;
	Set<String> negativeLexicon;
	List<String> newPos;
	List<String> newNeg;
	List<String> corpus;
	OpenNLPTokenAnnotator tokA;

	public HMLexiconBuilder(List<String> posBootStrap, List<String> negBootStrap) {
		this.positiveLexicon = new HashSet<String>();
		this.negativeLexicon = new HashSet<String>();
		this.newPos = new LinkedList<String>();
		this.newNeg = new LinkedList<String>();
		this.tokA = new OpenNLPTokenAnnotator();
		for (String s : posBootStrap) {
			addToLexicon(positiveLexicon, newPos, s);
		}
		for (String s : negBootStrap) {
			addToLexicon(negativeLexicon, newNeg, s);
		}
	}

	private void addToLexicon(Set<String> compSet, List<String> q, String token) {
		if (compSet.add(token))
			q.add(token);
	}

	public void buildFromCorpus(List<String> corpus) {
		this.corpus = corpus;
		process();
	}

	private void process() {
		while (!newPos.isEmpty())
			processNewLexTokens(positiveLexicon, newPos, negativeLexicon,
					newNeg);
		while (!newNeg.isEmpty())
			processNewLexTokens(negativeLexicon, newNeg, positiveLexicon,
					newPos);
		// Make sure that they have not added to each other after processing.
		if (!(newPos.isEmpty() || !newNeg.isEmpty()))
			process();
	}

	private void processNewLexTokens(Set<String> lexicon, List<String> q,
			Set<String> anti_lexicon, List<String> anti_q) {
		AhoCorasick<String> tri;
		tri = new AhoCorasick<String>();
		for (String string : q) {
			String syno = string + " and";
			String anti = string + " but";
			tri.add(syno.getBytes(), syno);
			tri.add(anti.getBytes(), anti);
		}
		tri.prepare();
		q.clear();
		for (String doc : corpus) {
			String lcdoc = doc.toLowerCase();
			Iterator<SearchResult<String>> result = tri.search(lcdoc.getBytes());
			List<String> hits = new ArrayList<String>();
			while (result.hasNext()) {
				SearchResult<String> sr = result.next();
				for (String s : sr.getOutputs()) {
					hits.add(s);
				}
			}
			for (String hit : hits) {
				int tokeniseFrom = lcdoc.indexOf(hit) + hit.length();
				List<String> tokens = tokenise(lcdoc.substring(tokeniseFrom));
				Iterator<String> it = tokens.iterator();
				String newLex=null;
				boolean anti =false;
				if(it.hasNext()){
					String first = it.next();
					if(first.equals("not")){
						anti=true;
						if(it.hasNext()){
							newLex=it.next();
						}
					}
					else{
						newLex = first;
					}
				}
				
				if(hit.endsWith("but"))anti=!anti;
				if (newLex!=null) {
					if (!anti)addToLexicon(lexicon, q, newLex);
					else addToLexicon(anti_lexicon, anti_q, newLex);					
				}
			}
		}
	}

	private List<String> tokenise(String text) {
		RawTextAnnotation rta = new RawTextAnnotation(text);
		try {
			tokA.annotate(rta);
			return AnnotationUtils.getStringTokensFromTokenAnnotationList(rta.getAnnotationsFor(TokenAnnotation.class));
		} catch (MissingRequiredAnnotationException e) {
			e.printStackTrace();
		}
		return null;
	}

}
