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
package org.openimaj.text.nlp.textpipe.annotations;

import org.openimaj.text.nlp.textpipe.annotations.POSAnnotation.PartOfSpeech;
import org.openimaj.text.nlp.textpipe.annotations.PhraseAnnotation.Phrase;
import org.openimaj.text.nlp.textpipe.annotators.MissingRequiredAnnotationException;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPPOSAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPPhraseChunkAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPSentenceAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.OpenNLPTokenAnnotator;
import org.openimaj.text.nlp.textpipe.annotators.YagoNEAnnotator;

/**
 * Simple demo and play area.
 *
 * @author laurence
 *
 */
public class PipePlayground {

	@SuppressWarnings("javadoc")
	public static void main(String[] args) {
		RawTextAnnotation rta = new RawTextAnnotation(
				"The tall curtains");

		// Set the properties
		System.setProperty(OpenNLPTokenAnnotator.TOKEN_MODEL_PROP, "/org/openimaj/text/opennlp/models/en-token.bin");
		System.setProperty(OpenNLPSentenceAnnotator.SENTENCE_MODEL_PROP, "/org/openimaj/text/opennlp/models/en-sent.bin");
		System.setProperty(OpenNLPPOSAnnotator.POS_MODEL_PROP, "/org/openimaj/text/opennlp/models/en-pos-maxent.bin");
		System.setProperty(OpenNLPPhraseChunkAnnotator.PHRASE_MODEL_PROP,"/org/openimaj/text/opennlp/models/en-chunker.bin");
		OpenNLPTokenAnnotator ta = new OpenNLPTokenAnnotator();
		OpenNLPSentenceAnnotator sa = new OpenNLPSentenceAnnotator();
		OpenNLPPOSAnnotator pa = new OpenNLPPOSAnnotator();
		OpenNLPPhraseChunkAnnotator pca = new OpenNLPPhraseChunkAnnotator();
		YagoNEAnnotator yna = new YagoNEAnnotator();
		try {
			sa.annotate(rta);
			ta.annotate(rta);
			pa.annotate(rta);
			pca.annotate(rta);
			yna.annotate(rta);
		} catch (MissingRequiredAnnotationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (SentenceAnnotation sentence : rta
				.getAnnotationsFor(SentenceAnnotation.class))
		{
			System.out.println(sentence.text);
			if (sentence.getAnnotationKeyList().contains(NamedEntityAnnotation.class))
				for (NamedEntityAnnotation ne : sentence.getAnnotationsFor(NamedEntityAnnotation.class)) {
					System.out.println(ne.namedEntity.rootName);
					System.out.println(ne.namedEntity.type);
				}
			for (TokenAnnotation token : sentence.getAnnotationsFor(TokenAnnotation.class)) {
				PartOfSpeech pos = token.getAnnotationsFor(POSAnnotation.class).get(0).pos;
				Phrase ph = token.getAnnotationsFor(PhraseAnnotation.class).get(0).phrase;
				String phraseOrder = token.getAnnotationsFor(PhraseAnnotation.class).get(0).getOrder();
				System.out.println(token.stringToken + "  " + pos.toString() + "  " + pos.DESCRIPTION + "    "
						+ ph.toString() + "-" + phraseOrder);
				System.out.println(sentence.text.substring(token.start, token.stop));
				System.out.println("|" + token.getRawString() + "|");
				String fromRaw = rta.text.substring(sentence.start + token.start, sentence.start + token.stop);
				System.out.print(fromRaw + "\n");
			}
		}
	}

}
