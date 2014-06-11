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
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.math.geometry.shape.Ellipse;

/**
 * Visualise the interest points extracted using an {@link InterestPointDetector}. Allows for points and areas of interest to be drawn
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 * @param <Q>
 */
public class InterestPointVisualiser <T, Q extends Image<T,Q> & SinglebandImageProcessor.Processable<Float,FImage,Q>> {
	Q image;
	List<Ellipse> interestPoints;
	
	/**
	 * Image from which interest points were extract and the extracted points.
	 * @param image source image
	 * @param keys extracted interest points
	 */
	public InterestPointVisualiser(Q image, List<Ellipse> keys) {
		this.image = image;
		this.interestPoints = keys;
	}
	
	/**
	 * Extract ellipses from second moment matricies of interest point keypoints
	 * @param <T> Image pixel type
	 * @param <Q> Image type
	 * @param image the image to visualise with
	 * @param keys the list of interest points
	 * @return a prepared visualiser
	 */
	public static <T, Q extends Image<T,Q> & SinglebandImageProcessor.Processable<Float,FImage,Q>>InterestPointVisualiser<T,Q> visualiseKeypoints(Q image, List<? extends InterestPointKeypoint<? extends InterestPointData>> keys){
		List<Ellipse> interestPoints = new ArrayList<Ellipse>();
		for(InterestPointKeypoint<? extends InterestPointData> k : keys){
			interestPoints.add(k.location.getEllipse());
		}
		return new InterestPointVisualiser<T,Q>(image,interestPoints);
	}
	
	/**
	 * Extract ellipses from second moment matricies of interest point keypoints
	 * @param <T> Image pixel type
	 * @param <Q> Image type
	 * @param image the image to visualise with
	 * @param keys the list of interest points
	 * @return a prepared visualiser
	 */
	public static <T, Q extends Image<T,Q> & SinglebandImageProcessor.Processable<Float,FImage,Q>>InterestPointVisualiser<T,Q> visualiseInterestPoints(Q image, List<? extends InterestPointData> keys){
		List<Ellipse> interestPoints = new ArrayList<Ellipse>();
		for(InterestPointData k : keys){
			interestPoints.add(k.getEllipse());
		}
		return new InterestPointVisualiser<T,Q>(image,interestPoints);
	}
	
	/**
	 * Extract ellipses from second moment matricies of interest point keypoints
	 * @param <T> Image pixel type
	 * @param <Q> Image type
	 * @param image the image to visualise with
	 * @param keys the list of interest points
	 * @param scale scale axis
	 * @return a prepared visualiser
	 */
	public static <T, Q extends Image<T,Q> & SinglebandImageProcessor.Processable<Float,FImage,Q>>InterestPointVisualiser<T,Q> visualiseInterestPoints(Q image, List<? extends InterestPointData> keys, double scale){
		List<Ellipse> interestPoints = new ArrayList<Ellipse>();
		for(InterestPointData k : keys){
			interestPoints.add(k.getEllipse());
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
		ImageRenderer<T, Q> renderer = output.createRenderer();
		
		for (Ellipse k : interestPoints) {
			if(pointCol!=null){
				renderer.drawPoint(k.calculateCentroid(), pointCol, 3);
			}
			if (borderCol != null) {
				renderer.drawShape(k,borderCol);
			}
		}
		
		return output;
	}
	
	public Q drawCenter(T col) {
		Q output = image.clone();
		ImageRenderer<T, Q> renderer = output.createRenderer();
		
		for(Ellipse e : interestPoints){
			renderer.drawPoint(e.calculateCentroid(), col,2);
		}
		return output;
	}
}
