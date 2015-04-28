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
package org.openimaj.image.model.landmark;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.analysis.algorithm.TemplateMatcher.Mode;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.ObjectFloatPair;

/**
 * An {@link FPatchLandmarkModel} is a landmark represented by the 
 * local patch of pixels around of a point in an {@link FImage}. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FPatchLandmarkModel implements LandmarkModel<FImage> {
	/**
	 * A factory for producing {@link FPatchLandmarkModel}s
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class Factory implements LandmarkModelFactory<FImage> {
		@Override
		public LandmarkModel<FImage> createLandmarkModel() {
			return new FPatchLandmarkModel();
		}

		@Override
		public LandmarkModel<FImage> createLandmarkModel(float scaleFactor) {
			return new FPatchLandmarkModel();
		}
	}
	
	int blockSize = 11;
	int searchSize = 31;
	
	FImage average;
	int n = 0;
	TemplateMatcher matcher;
	
	protected Rectangle getROI(int x, int y, int w, int h) {
		if(w % 2 == 0 ) w+=1;
		if(h % 2 == 0 ) h+=1;
		
		int roiX = Math.max(0,x-(int)(w/2.0));
		int roiY = Math.max(0,y-(int)(h/2.0));
		
		int newWidth = (int)(w / 2.0) + 1 + (x - roiX);
		int newHeight = (int)(h / 2.0) + 1 + (y - roiY);
		
		return new Rectangle(roiX, roiY, newWidth, newHeight);
	}
	
	protected FImage extractBlock(FImage image, Point2d pt, int sz) {
		return image.extractROI(getROI((int)pt.getX(), (int)pt.getY(), sz, sz));
	}

	@Override
	public void updateModel(FImage image, Point2d point, PointList pointList) {
		FImage extracted = extractBlock(image, point, blockSize);
		
		n++;
		if (average == null) {
			average = extracted; 
		} else {
			average.addInplace( extracted.subtractInplace(average).divide((float)n) );
		}
		matcher = null;
	}

	@Override
	public float computeCost(FImage image, Point2d point, PointList pointList) {
		FImage extracted = extractBlock(image, point, blockSize);
		
		if (matcher == null)
			matcher = new TemplateMatcher(average, Mode.NORM_SUM_SQUARED_DIFFERENCE);
		
		matcher.setSearchBounds(null);
		extracted.analyseWith(matcher);
		
		return matcher.getResponseMap().pixels[0][0];
	}

	@Override
	public ObjectFloatPair<Point2d> updatePosition(FImage image, Point2d initial, PointList pointList) {
		Rectangle roi = getROI((int)initial.getX(), (int)initial.getY(), searchSize, searchSize);
		
		if (matcher == null)
			matcher = new TemplateMatcher(average, Mode.NORM_SUM_SQUARED_DIFFERENCE);
		
		matcher.setSearchBounds(roi);
		image.analyseWith(matcher);
		FValuePixel p = matcher.getBestResponses(1)[0];
		
		return new ObjectFloatPair<Point2d>(p, 0);
	}
}
