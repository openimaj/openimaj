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

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class OpenNLPTokenAnnotator extends AbstractTokenAnnotator {

	/**
	 *
	 */
	public static final String TOKEN_MODEL_PROP = "org.openimaj.text.opennlp.models.token";
	private static final String TOKEN_MODEL_DEFAULT = "/org/openimaj/text/opennlp/models/en-token.bin";
	TokenizerME tokenizer;

	/**
	 *
	 */
	public OpenNLPTokenAnnotator() {
		super();
		InputStream modelIn = null;
		modelIn = OpenNLPTokenAnnotator.class.getResourceAsStream(System.getProperty(TOKEN_MODEL_PROP,
				TOKEN_MODEL_DEFAULT));
		TokenizerModel model = null;
		try {
			model = new TokenizerModel(modelIn);
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
		tokenizer = new TokenizerME(model);
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException
	{

	}

	@Override
	public List<TokenAnnotation> tokenise(String text) {
		final List<TokenAnnotation> tla = new ArrayList<TokenAnnotation>();
		int currentOff = 0;
		for (final String token : tokenizer.tokenize(text)) {
			final int start = currentOff + (text.substring(currentOff).indexOf(token));
			final int stop = start + token.length();
			tla.add(new TokenAnnotation(token, text.substring(currentOff, stop), start, stop));
			currentOff = stop;
		}
		return tla;
	}

}
