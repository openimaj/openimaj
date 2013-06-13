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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;

/**
 * {@link SentenceDetectorME} backed by a {@link SentenceModel} loaded from the
 * resource located at: {@link OpenNLPSentenceAnnotator#SENTENCE_MODEL_PROP}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class OpenNLPSentenceAnnotator extends AbstractSentenceAnnotator {

	/**
	 * Property name pointing to the sentence model
	 */
	public static final String SENTENCE_MODEL_PROP = "org.openimaj.text.opennlp.models.sent";
	public static final String SENTENCE_MODEL_DEFAULT = "/org/openimaj/text/opennlp/models/en-sent.bin";
	SentenceDetectorME sentenceDetector;

	public OpenNLPSentenceAnnotator() {
		super();
		InputStream modelIn = null;
		modelIn = OpenNLPSentenceAnnotator.class.getResourceAsStream(System.getProperty(SENTENCE_MODEL_PROP,
				SENTENCE_MODEL_DEFAULT));
		SentenceModel model = null;
		try {
			model = new SentenceModel(modelIn);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (final IOException e) {
				}
			}
		}
		sentenceDetector = new SentenceDetectorME(model);
	}

	@Override
	protected List<SentenceAnnotation> getSentenceAnnotations(String text) {
		final ArrayList<SentenceAnnotation> sents = new ArrayList<SentenceAnnotation>();
		final List<String> sentences = Arrays.asList(sentenceDetector.sentDetect(text));
		final int currentOff = 0;
		for (int i = 0; i < sentences.size(); i++) {
			final String sentence = sentences.get(i);
			final int start = currentOff + (text.substring(currentOff).indexOf(sentence));
			final int stop = start + sentence.length();
			sents.add(new SentenceAnnotation(sentence, start, stop));
		}
		return sents;
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException
	{
		// TODO Auto-generated method stub

	}

}
