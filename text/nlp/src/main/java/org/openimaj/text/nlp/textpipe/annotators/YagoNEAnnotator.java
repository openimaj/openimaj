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
package org.openimaj.text.nlp.textpipe.annotators;

import java.util.List;

import org.openimaj.text.nlp.namedentity.NamedEntity;
import org.openimaj.text.nlp.namedentity.YagoEntityExactMatcherFactory;
import org.openimaj.text.nlp.namedentity.YagoEntityExactMatcherFactory.YagoEntityExactMatcher;
import org.openimaj.text.nlp.textpipe.annotations.NamedEntityAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public class YagoNEAnnotator extends AbstractNEAnnotator {

	private int cw = 1; // On either side of current sentence.

	public YagoEntityExactMatcher yagoMatcher;

	public YagoNEAnnotator() {
		yagoMatcher = YagoEntityExactMatcherFactory.getMatcher();
	}

	@Override
	void performAnnotation(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException
	{
		// Get the sentences
		final List<SentenceAnnotation> sentences = annotation.getAnnotationsFor(SentenceAnnotation.class);
		for (int i = 0; i < sentences.size(); i++) {
			// get context for sentence
			final String context = getContextFrom(sentences.subList(Math.max(0, i - cw),
					Math.min(i + cw + 1, sentences.size())));
			annotateSentence(sentences.get(i), context);
		}
	}

	private void annotateSentence(SentenceAnnotation sentence,
			String context)
	{

		final List<NamedEntity> ents = yagoMatcher.matchExact(sentence.getAnnotationsFor(TokenAnnotation.class), context);

		for (final NamedEntity ent : ents) {
			final NamedEntityAnnotation nea = new NamedEntityAnnotation();
			nea.namedEntity = ent;
			nea.tokensMatched.addAll(sentence.getAnnotationsFor(TokenAnnotation.class).subList(ent.startToken,
					ent.stopToken));
			sentence.addAnnotation(nea);
		}

		/*
		 * List<List<TokenAnnotation>> validEntityPhrases =
		 * getValidEntityPhrases(sentence); for(List<TokenAnnotation> entPhrase:
		 * validEntityPhrases){ List<NamedEntity> ents =
		 * yagoMatcher.matchExact(entPhrase, context); for(NamedEntity ent:
		 * ents){ NamedEntityAnnotation nea = new NamedEntityAnnotation();
		 * nea.namedEntity=ent;
		 * nea.tokensMatched.addAll(sentence.getAnnotationsFor
		 * (TokenAnnotation.class).subList(ent.startToken, ent.stopToken));
		 * sentence.addAnnotation(nea); } }
		 */
	}

	// private List<List<TokenAnnotation>>
	// getValidEntityPhrases(SentenceAnnotation sentence) {
	// List<List<TokenAnnotation>> results = new
	// ArrayList<List<TokenAnnotation>>();
	// List<TokenAnnotation> current = new ArrayList<TokenAnnotation>();
	// for(TokenAnnotation
	// tok:sentence.getAnnotationsFor(TokenAnnotation.class)){
	// if(start(tok)){
	// current.add(tok);
	// }
	// else if(cont(tok)&&current.size()>0){
	// current.add(tok);
	// }
	// else if(current.size()>0){
	// results.add(current);
	// current=new ArrayList<TokenAnnotation>();
	// }
	// }
	// if(current.size()>0)results.add(current);
	// return results;
	// }
	//
	// private boolean cont(TokenAnnotation tok) {
	// return (!tok.getAnnotationsFor(PhraseAnnotation.class).get(0).start);
	// }
	//
	// private boolean start(TokenAnnotation tok) {
	// final PhraseAnnotation phrase =
	// tok.getAnnotationsFor(PhraseAnnotation.class).get(0);
	// if (phrase.phrase.toString().equals("NP"))
	// return phrase.start;
	// else
	// return false;
	// }

	private String getContextFrom(List<SentenceAnnotation> sents) {
		final StringBuffer result = new StringBuffer();
		for (final SentenceAnnotation sent : sents) {
			result.append(sent.text + " ");
		}
		return result.toString();
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException
	{
		if (!annotation.getAnnotationKeyList().contains(SentenceAnnotation.class))
			throw new MissingRequiredAnnotationException("No SentenceAnnotations found");
	}

}
