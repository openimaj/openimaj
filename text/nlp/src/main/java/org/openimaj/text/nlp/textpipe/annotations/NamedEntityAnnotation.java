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

import java.util.ArrayList;
import java.util.Collection;

import org.openimaj.text.nlp.namedentity.NamedEntity;

/**
 * A Nameed Entity Annotation.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * 
 */
public class NamedEntityAnnotation extends TextPipeAnnotation {
	/**
	 * Tokens matched by this Named Entity
	 */
	public ArrayList<TokenAnnotation> tokensMatched;
	/**
	 * The Named Entity.
	 */
	public NamedEntity namedEntity;

	public NamedEntityAnnotation() {
		super();
		tokensMatched = new ArrayList<TokenAnnotation>();
	}

	public NamedEntityAnnotation(ArrayList<TokenAnnotation> tokensMatched,
			NamedEntity namedEntity)
	{
		super();
		this.tokensMatched = tokensMatched;
		this.namedEntity = namedEntity;
	}

	/**
	 * Get the start char the matching substring for this Named Entity.
	 * 
	 * @return integer of start char
	 */
	public int getStart() {
		if (tokensMatched.size() > 0)
			return tokensMatched.get(0).start;
		else
			return -1;
	}

	/**
	 * Get the end char the matching substring for this Named Entity.
	 * 
	 * @return integer of start char
	 */
	public int getEnd() {
		if (tokensMatched.size() > 0)
			return tokensMatched.get(tokensMatched.size() - 1).stop;
		else
			return -1;
	}

	/**
	 * Set the {@link TokenAnnotation}s matched by this
	 * {@link NamedEntityAnnotation}
	 * 
	 * @param tokens
	 */
	public void addAllTokensMatched(Collection<TokenAnnotation> tokens) {
		tokensMatched.addAll(tokens);
	}

}
