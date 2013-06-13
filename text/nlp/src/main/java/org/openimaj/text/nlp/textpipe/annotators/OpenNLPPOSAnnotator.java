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
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import org.openimaj.text.nlp.textpipe.annotations.POSAnnotation.PartOfSpeech;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;

/**
 * Uses a {@link POSTaggerME} backed by a {@link POSModel}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class OpenNLPPOSAnnotator extends AbstractPOSAnnotator {

	/**
	 * Name of system property pointing to the POS model
	 */
	public static final String POS_MODEL_PROP = "org.openimaj.text.opennlp.models.pos";
	public static final String POS_MODEL_DEFAULT = "/org/openimaj/text/opennlp/models/en-pos-maxent.bin";
	POSTaggerME tagger;

	public OpenNLPPOSAnnotator() {
		super();
		InputStream modelIn = null;
		POSModel model = null;
		try {
			modelIn = OpenNLPPOSAnnotator.class
					.getResourceAsStream(System.getProperty(POS_MODEL_PROP, POS_MODEL_DEFAULT));
			model = new POSModel(modelIn);
		} catch (final IOException e) {
			// Model loading failed, handle the error
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (final IOException e) {
				}
			}
		}
		tagger = new POSTaggerME(model);
	}

	@Override
	public void annotate(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException
	{
		if (!annotation.getAnnotationKeyList().contains(
				SentenceAnnotation.class))
			throw new MissingRequiredAnnotationException(
					"No SentenceAnnotations found : OpenNLPPOSAnnotator requires sentance splitting");
		super.annotate(annotation);
	}

	@Override
	protected List<PartOfSpeech> pos(List<String> tokenList) {
		final List<PartOfSpeech> result = new ArrayList<PartOfSpeech>();
		String[] p = null;
		final String[] sentence = new String[tokenList.size()];
		for (int i = 0; i < sentence.length; i++) {
			sentence[i] = tokenList.get(i);
		}
		p = tagger.tag(sentence);
		for (final String pos : p) {
			if (PartOfSpeech.getPOSfromString(pos) == null)
				System.out.println("no matching pos " + pos);
			result.add(PartOfSpeech.getPOSfromString(pos));
		}
		return result;
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException
	{
		// TODO Auto-generated method stub

	}

}
