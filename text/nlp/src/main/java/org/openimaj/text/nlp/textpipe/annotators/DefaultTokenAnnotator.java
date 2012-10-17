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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.text.nlp.EntityTweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public class DefaultTokenAnnotator extends AbstractTokenAnnotator{

	@Override
	public List<TokenAnnotation> tokenise(String text) {
		EntityTweetTokeniser t = null;
		try {
			t = new EntityTweetTokeniser(text);
		} catch (UnsupportedEncodingException e) {		
			e.printStackTrace();
		} catch (TweetTokeniserException e) {			
			e.printStackTrace();
		}
		List<TokenAnnotation> tla = new ArrayList<TokenAnnotation>();
		for(String token:t.getStringTokens()){
			tla.add(new TokenAnnotation(token,null, -1, -1));
		}
		return tla;
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		// TODO Auto-generated method stub
		
	}	

}
