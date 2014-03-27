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
package org.openimaj.image.feature.astheticode;

import java.util.Arrays;

import org.openimaj.image.contour.SuzukiContourProcessor.Border;

/**
 * A detected Aestheticode
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class Aestheticode{
	private int[] code;
	public Border root;
	/**
	 * @param code the code
	 */
	public Aestheticode(int[] code) {
		this.code = code;
		Arrays.sort(code);
	}
	
	/**
	 * @param acode copy this {@link Aestheticode}
	 */
	public Aestheticode(Aestheticode acode) {
		this.code = Arrays.copyOf(acode.code, acode.code.length);
	}
	
	/**
	 * @param root the detected borders to construct a code from
	 */
	public Aestheticode(Border root) {
		this.root = root;
		
		this.code = new int[root.children.size()];
		int i = 0;
		for (Border child : root.children) {
			this.code[i++] = child.children.size();
		}
		Arrays.sort(code);
	}
	
	public int hashCode() {
		return Arrays.hashCode(this.code);
	};
	
	public boolean equals(Object obj) {
		if(!(obj instanceof Aestheticode)){
			return false;
		}
		Aestheticode that = (Aestheticode) obj;
		if(that.code.length!= this.code.length) return false;
		for (int i = 0; i < this.code.length; i++) {
			if(this.code[i] != that.code[i])return false;
		}
		return true;
	};
	
	public String toString() {
		String ret = "" + this.code[0];
		for (int i = 1; i < code.length; i++) {
			ret += ":" + code[i];
		}
		return ret;
	};
	
}