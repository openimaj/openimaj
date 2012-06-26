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
package org.openimaj.image.connectedcomponent.proc;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;


/**
 * Affine-invariant moment descriptor for the
 * shape of a connected component.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class AffineInvariantMoments implements ConnectedComponentProcessor, FeatureVectorProvider<DoubleFV> {
	/**
	 * The first affine-invariant moment
	 */
	public double I1;
	
	/**
	 * The second affine-invariant moment
	 */
	public double I2;
	
	/**
	 * The third affine-invariant moment
	 */
	public double I3;
	
	/**
	 * The forth affine-invariant moment
	 */
	public double I4;

	@Override
	public void process(ConnectedComponent cc) {
		double u00 = cc.calculateMoment(0, 0);
		double u20 = cc.calculateMoment(2, 0);
		double u02 = cc.calculateMoment(0, 2);
		double u11 = cc.calculateMoment(1, 1);
		double u21 = cc.calculateMoment(2, 1);
		double u12 = cc.calculateMoment(1, 2);
		double u30 = cc.calculateMoment(3, 0);
		double u03 = cc.calculateMoment(0, 3);

		I1 = ((u20 * u02) - (u11*u11)) / Math.pow(u00, 4);
		
		I2 = ((u30*u30 * u03*u03) - (6 * u30 * u21 * u12 * u03) + 
				(4 * u30 * Math.pow(u12, 3)) + (4 * Math.pow(u21, 3) * u03) - (3 * u21 * u21 * u12 *u12)) / 
				Math.pow(u00, 10);
		
		I3 = ((u20 * (u21*u03 - u12*u12)) - (u11 * (u30*u03 - u21*u12)) + (u02 * (u30*u12 - u21*u21))) / Math.pow(u00, 7);
		
		I4 = (Math.pow(u20, 3)*u03*u03 - 6*u20*u20*u11*u12*u03 - 6*u20*u20*u02*u21*u03 + 9*u20*u20*u02*u12*u12 
				+ 12*u20*u11*u11*u21*u03 + 6*u21*u11*u02*u30*u03 - 18*u20*u11*u02*u21*u12
				- 8*Math.pow(u11, 3)*u30*u03 - 6*u20*u02*u02*u30*u12 + 9*u20*u02*u02*u21*u21
				+ 12*u11*u11*u02*u30*u12 - 6*u11*u02*u02*u30*u21 + Math.pow(u02, 3)*u30*u30) / Math.pow(u00, 11);
	}

	@Override
	public String toString() {
		return String.format("%2.2f, %2.2f, %2.2f, %2.2f", I1, I2, I3, I4);
	}

	/**
	 * Get all the values of the descriptor as an array.
	 * @return an array of descriptor values
	 */
	public double[] getFeatureVectorArray() {
		return new double[] {I1, I2, I3, I4};
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(getFeatureVectorArray());
	}
}