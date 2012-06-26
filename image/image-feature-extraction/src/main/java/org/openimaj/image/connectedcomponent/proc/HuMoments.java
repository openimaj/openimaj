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
 * Implementation of the 7 Hu moments for describing
 * connected component shape.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HuMoments implements ConnectedComponentProcessor, FeatureVectorProvider<DoubleFV> {
	/**
	 * The first Hu moment
	 */
	public double h1;
	
	/**
	 * The second Hu moment
	 */
	public double h2;
	
	/**
	 * The third Hu moment
	 */
	public double h3;
	
	/**
	 * The forth Hu moment
	 */
	public double h4;
	
	/**
	 * The fifth Hu moment
	 */
	public double h5;
	
	/**
	 * The sixth Hu moment
	 */
	public double h6;
	
	/**
	 * The seventh Hu moment
	 */
	public double h7;
	
	@Override
	public void process(ConnectedComponent cc) {
		double v11 = cc.calculateMomentNormalised(1, 1);
		
		double v12 = cc.calculateMomentNormalised(1, 2);
		double v21 = cc.calculateMomentNormalised(2, 1);
		
		double v02 = cc.calculateMomentNormalised(0, 2);
		double v20 = cc.calculateMomentNormalised(2, 0);
		
		double v03 = cc.calculateMomentNormalised(0, 3);
		double v30 = cc.calculateMomentNormalised(3, 0);
		
		h1 = v20 + v02;
		h2 = ((v20 - v02) * (v20 - v02)) + (4 * v11 * v11); 
		h3 = ((v30 - 3*v12)*(v30 - 3*v12)) + ((3*v21 - v03)*(3*v21 - v03));
		h4 = ((v30+v12)*(v30+v12)) + ((v21+v03)*(v21+v03));
		h5 = (((v30 - 3*v12)*(v30+v12)) * (((v30+v12)*(v30+v12) - (3*(v21+v03)*(v21+v03))))) +
				(((3*v21-v03)*(v21+v03)) * ((3*(v30+v12)*(v30+v12)) - (v21+v03)*(v21+v03)));
		h6 = ((v20 - v02) * ((v30+v12)*(v30+v12) - (v21 + v03)*(v21 + v03))) + (4*v11*(v30+v12)*(v21+v03));
		h7 = ((3*v21 - v03)*(v30+v12)*((v30+v12)*(v30+v12) - 3*(v21+v03)*(v21+v03))) -
				((v30-3*v12)*(v21+v03)*(3*(v30+v12)*(v30+v12) - (v21+v03)*(v21+v03)));
	}
	
	@Override
	public String toString() {
		return String.format("%2.2f, %2.2f, %2.2f, %2.2f, %2.2f, %2.2f, %2.2f", h1, h2, h3, h4, h5, h6, h7);
	}

	/**
	 * Get all the values of the descriptor as an array.
	 * @return an array of descriptor values
	 */
	public double[] getFeatureVectorArray() {
		return new double[] {h1, h2, h3, h4, h5, h6, h7};
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(getFeatureVectorArray());
	}
}

