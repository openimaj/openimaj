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
package org.openimaj.pgm.util;

import org.openimaj.feature.SparseIntFV;

/**
 * A document is a bag of words
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Document extends SparseIntFV{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2073772149798865435L;
	private int length;
	
	/**
	 * @param corpus documents from the same corpus share the same vocabulary size
	 */
	public Document(Corpus corpus) {
		super(corpus.vocabularySize());
	}
	
	/**
	 * @param vocabularySize the number of words in this vocabulary
	 */
	public Document(int vocabularySize) {
		super(vocabularySize);
	}
	/**
	 * @return the number of unique words in this document
	 */
	public int countUniqueWords(){
		return this.getVector().used();
	}
	
	
	@Override
	public int length(){
		return this.length;
	}
	
	/**
	 * sets a word in the document's count.
	 * @param word
	 * @param count
	 */
	public void setWordCount(int word, int count){
		if(this.getVector().isUsed(word)){
			this.length -= this.getVector().get(word);
		}
		this.length += this.getVector().set(word, count);
	}

	
}
