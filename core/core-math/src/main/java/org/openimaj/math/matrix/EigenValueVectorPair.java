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
package org.openimaj.math.matrix;

import java.util.Arrays;

import Jama.Matrix;


/**
 * A pair of Eigen values and corresponding vectors
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class EigenValueVectorPair {
	private Matrix val;
	private Matrix vec;
	
	/**
	 * Construct with Eigen values and vectors.
	 * @param val values
	 * @param vec vectors
	 */
	public EigenValueVectorPair(Matrix val, Matrix vec) {
		this.setValues(val);
		this.setVectors(vec);
	}
	
	@Override
	public String toString(){
		String out = "[ \n";
		for(int i = 0; i < 2; i ++ ){
			out+="\t" + (this.getVectors().get(i, 0)) + ";"  + "\n";
		}
		out += "] \n";
		out += "[ \n";
		for(int i = 0; i < 2; i ++ ){
			out+="\t" + Arrays.toString(this.getValues().getArray()[i]) + ";" + "\n" ;
		}
		out += "] \n";
		
		return out;
	}

	/**
	 * Set the Eigen values
	 * @param val the Eigen values
	 */
	public void setValues(Matrix val) {
		this.val = val;
	}

	/**
	 * @return The Eigen values
	 */
	public Matrix getValues() {
		return val;
	}

	/**
	 * Set the Eigen vectors
	 * @param vec the Eigen vectors
	 */
	public void setVectors(Matrix vec) {
		this.vec = vec;
	}

	/**
	 * @return The Eigen vectors
	 */
	public Matrix getVectors() {
		return vec;
	}
}
