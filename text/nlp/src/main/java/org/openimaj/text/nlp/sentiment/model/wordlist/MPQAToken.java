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
package org.openimaj.text.nlp.sentiment.model.wordlist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.sentiment.model.TokenSentimentAnnotator;
import org.openimaj.text.nlp.sentiment.model.wordlist.util.TFF;
import org.openimaj.text.nlp.sentiment.model.wordlist.util.TFF.Clue;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;
import org.openimaj.text.nlp.sentiment.type.TFFCountSentiment;

/**
 * An implementation of the Prior-Polarity baseline sentiment classifier described by Wilson et. al.
 * For each word that may be associated to sentiment in english, use the prior (manually ascribed) sentiment
 * to ascribe some form of sentiment to a phrase.
 * 
 * This model is loaded from tff files using {@link TFF}.
 * 
 * The default model is found in:
 * 
 * /org/openimaj/text/sentiment/mpqa/subjclueslen1polar.tff
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@Reference(
	author = { "Janyce Wiebe","Theresa Wilson","Claire Cardie" }, 
	title = "Annotating expressions of opinions and emotions in language. ", 
	type = ReferenceType.Article, 
	year = "2005"
)
public class MPQAToken extends TokenSentimentAnnotator<IdentityFeatureExtractor<String>,MPQAToken>{
	private static final String DEFAULT_MODEL = "/org/openimaj/text/sentiment/mpqa/subjclueslen1polar.tff";
	private TFF model;
	
	/**
	 * Construct the sentiment model using the default word clue TFF
	 * @throws IOException
	 */
	public MPQAToken() throws IOException {
		super(new IdentityFeatureExtractor<String>());
		this.model = IOUtils.read(MPQAToken.class.getResourceAsStream(DEFAULT_MODEL), TFF.class);
	}
	
	/**
	 * Construct the sentiment model using the provided tff file
	 * @param f 
	 * @throws IOException
	 */
	public MPQAToken(File f) throws IOException {
		super(new IdentityFeatureExtractor<String>());
		this.model = IOUtils.read(f, TFF.class);
	}
	
	/**
	 * @param mpqa used by clone
	 */
	public MPQAToken(MPQAToken mpqa) {
		super(new IdentityFeatureExtractor<String>());
		this.model = mpqa.model.clone();
	}

	@Override
	public List<ScoredAnnotation<BipolarSentiment>> annotate(String word) {
		List<ScoredAnnotation<BipolarSentiment>> ret = new ArrayList<ScoredAnnotation<BipolarSentiment>>();
		List<Clue> clueList = this.model.entriesMap.get(word);
		if(clueList  == null) return ret; // This isn't a sentiment bearing word
		
		for (Clue clue : clueList) {
			ret.add(new ScoredAnnotation<BipolarSentiment>(TFFCountSentiment.bipolar(clue), 1.0f));
		}
		return ret;
	}
}
