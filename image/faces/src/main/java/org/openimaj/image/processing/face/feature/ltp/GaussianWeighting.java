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
package org.openimaj.image.processing.face.feature.ltp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;

/**
 * A Gaussian {@link LTPWeighting} function. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Tan, Xiaoyang", "Triggs, Bill" },
		title = "Enhanced local texture feature sets for face recognition under difficult lighting conditions",
		year = "2010",
		journal = "Trans. Img. Proc.",
		pages = { "1635", "1650" },
		url = "http://dx.doi.org/10.1109/TIP.2010.2042645",
		month = "June",
		number = "6",
		publisher = "IEEE Press",
		volume = "19"
	)
public class GaussianWeighting implements LTPWeighting {
	private float sigma = 3;
	
	/**
	 * Construct with a default standard deviation of 3.0
	 */
	public GaussianWeighting() {}
	
	/**
	 * Construct with the given standard deviation
	 * @param sigma the standard deviation
	 */
	public GaussianWeighting(float sigma) {
		this.sigma= sigma;
	}
	
	@Override
	public float weightDistance(float distance) {
		return (float) Math.exp( -(distance * distance) / (sigma * sigma * 2));
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		sigma = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(sigma);
	}
	
	@Override
	public String toString() {
		return "GaussianWeighting[sigma="+sigma+"]";
	}
}
