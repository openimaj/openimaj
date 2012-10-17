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
import java.util.List;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

import org.openimaj.text.nlp.textpipe.annotations.AnnotationUtils;
import org.openimaj.text.nlp.textpipe.annotations.PhraseAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.PhraseAnnotation.Phrase;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

/**
 * Phrase chunker instantiating a {@link ChunkerME} backed by a {@link ChunkerModel}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OpenNLPPhraseChunkAnnotator extends AbstractPhraseAnnotator {
	/**
	 * The system property
	 */
	public static final String PHRASE_MODEL_PROP = "org.openimaj.text.opennlp.models.chunker";
	ChunkerME chunker;

	/**
	 *
	 */
	public OpenNLPPhraseChunkAnnotator() {
		super();
		InputStream modelIn = null;
		ChunkerModel model = null;
		try {

			modelIn = OpenNLPPhraseChunkAnnotator.class.getResourceAsStream(System.getProperty(PHRASE_MODEL_PROP));
			model = new ChunkerModel(modelIn);
		} catch (IOException e) {
			// Model loading failed, handle the error
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		chunker = new ChunkerME(model);
	}

	@Override
	protected void phraseChunk(List<TokenAnnotation> tokens) {
		String[] tags = chunker.chunk(AnnotationUtils
				.ListToArray(AnnotationUtils
						.getStringTokensFromTokenAnnotationList(tokens)),
				AnnotationUtils.ListToArray(AnnotationUtils
						.getStringPOSsFromTokenAnnotationList(tokens)));
		for (int i = 0; i < tags.length; i++) {
			if (tags[i].contains("-")) {
				String[] comps = tags[i].split("-");
				boolean start = comps[0].equals("B");
				tokens.get(i).addAnnotation(
						new PhraseAnnotation(Phrase
								.getPhrasefromString(comps[1]), start));
			}
			else tokens.get(i).addAnnotation(
					new PhraseAnnotation(Phrase
							.getPhrasefromString(tags[i]),true));
		}
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		// TODO Auto-generated method stub

	}

}
