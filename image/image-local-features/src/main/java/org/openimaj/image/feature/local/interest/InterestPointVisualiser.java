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
package org.openimaj.image.feature.local.interest;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.shape.EllipseUtilities;

/**
 * Visualise the interest points extracted using an {@link InterestPointDetector}. Allows for points and areas of interest to be drawn
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <T>
 * @param <Q>
 */
public class InterestPointVisualiser <T, Q extends Image<T,Q> & SinglebandImageProcessor.Processable<Float,FImage,Q>> {
	Q image;
	List<? extends InterestPointData> interestPoints;
	
	/**
	 * Image from which interest points were extract and the extracted points.
	 * @param image source image
	 * @param keys extracted interest points
	 */
	public InterestPointVisualiser(Q image, List<? extends InterestPointData> keys) {
		this.image = image;
		this.interestPoints = keys;
	}
	
	public static <T, Q extends Image<T,Q> & SinglebandImageProcessor.Processable<Float,FImage,Q>>InterestPointVisualiser<T,Q> visualiseKeypoints(Q image, List<? extends InterestPointKeypoint> keys){
		List<InterestPointData> interestPoints = new ArrayList<InterestPointData>();
		for(InterestPointKeypoint k : keys){
			interestPoints.add(k.location);
		}
		return new InterestPointVisualiser<T,Q>(image,interestPoints);
	}
	
	/**
	 * Draw the interest points, a central dot for in the pointCol and a bordered area of interest by borderCol.
	 * If either is null it is not drawn.
	 * 
	 * @param pointCol
	 * @param borderCol
	 * @return image with patches drawn
	 */
	public Q drawPatches(T pointCol, T borderCol) {
		Q output = image.clone();
		
		for (InterestPointData k : interestPoints) {
			if(pointCol!=null){
				output.drawPoint(k, pointCol, 3);
			}
			if (borderCol != null) {
				output.drawShape(EllipseUtilities.ellipseFromSecondMoments(k.x,k.y,k.secondMoments,k.scale),borderCol);
			}
		}
		
		return output;
	}
	
	public Q drawCenter(T col) {
		Q output = image.clone();
		output.drawPoints(interestPoints, col,2);
		return output;
	}
}
