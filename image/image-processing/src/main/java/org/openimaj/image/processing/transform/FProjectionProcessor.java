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
package org.openimaj.image.processing.transform;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Shape;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * Perform a set of matrix transforms on a set of images and construct a single image containing all the pixels (or a window of the pixels)
 * in the projected space. 
 */
public class FProjectionProcessor extends ProjectionProcessor<Float, FImage> {
	
	/**
	 * Perform projection but only request data for pixels within the windowed range provided. Specify the background colour, i.e. the value of pixels
	 * with no data post projection.
	 * @param windowMinC left X
	 * @param windowMaxC right X
	 * @param windowMinR top Y
	 * @param windowMaxR bottom Y
	 * @param backgroundColour background colour of pixels with no data
	 * @return projected image within the window
	 */
	@Override
	public FImage performProjection(int windowMinC , int windowMaxC , int windowMinR , int windowMaxR , Float backgroundColour) {
		FImage output = null;
		output = new FImage(windowMaxC-windowMinC,windowMaxR-windowMinR);
		if(backgroundColour!=null)
			output.fill(backgroundColour);
		Shape[][] shapeRects = this.getCurrentShapes();
		for(int y = 0; y < output.getHeight(); y++)
		{
			for(int x = 0; x < output.getWidth(); x++){
				Point2d realPoint = new Point2dImpl(windowMinC + x,windowMinR + y);
				int i = 0;
				for (int j = 0; j < shapeRects.length; j++) {
					if(backgroundColour == null || isInside(j,shapeRects,realPoint)){
						double[][] transform = this.transformsInverted.get(i).getArray();
						
						float xt = (float)transform[0][0] * realPoint.getX() + (float)transform[0][1] * realPoint.getY() + (float)transform[0][2];
						float yt = (float)transform[1][0] * realPoint.getX() + (float)transform[1][1] * realPoint.getY() + (float)transform[1][2];
						float zt = (float)transform[2][0] * realPoint.getX() + (float)transform[2][1] * realPoint.getY() + (float)transform[2][2];
						
						xt /= zt;
						yt /= zt;
						FImage im = this.images.get(i);
						if(backgroundColour!=null)
							output.pixels[y][x] = im.getPixelInterp(xt, yt,backgroundColour);
						else
							output.pixels[y][x] = im.getPixelInterp(xt, yt);
					}
					i++;
				}
			}
		}
		return output;
	}
	
	/**
	 * Perform blended projection but only request data for pixels within the windowed range provided. Specify the background colour, i.e. the value of pixels
	 * with no data post projection. This blends any existing pixels to newly added pixels
	 * @param windowMinC left X
	 * @param windowMaxC right X
	 * @param windowMinR top Y
	 * @param windowMaxR bottom Y
	 * @param backgroundColour background colour of pixels with no data
	 * @return projected image within the window
	 */
	@Override
	public FImage performBlendedProjection(int windowMinC , int windowMaxC , int windowMinR , int windowMaxR , Float backgroundColour) {
		FImage output = null;
		output = new FImage(windowMaxC-windowMinC,windowMaxR-windowMinR);
		Map<Integer,Boolean> setMap = new HashMap<Integer,Boolean>();
		FImage blendingPallet = output.newInstance(2, 1);
		for(int y = 0; y < output.getHeight(); y++)
		{
			for(int x = 0; x < output.getWidth(); x++){
				Point2d realPoint = new Point2dImpl(windowMinC + x,windowMinR + y);
				int i = 0;
				for(Shape s : this.projectedShapes){
					if(s.isInside(realPoint)){
						double[][] transform = this.transformsInverted.get(i).getArray();
						
						float xt = (float)transform[0][0] * realPoint.getX() + (float)transform[0][1] * realPoint.getY() + (float)transform[0][2];
						float yt = (float)transform[1][0] * realPoint.getX() + (float)transform[1][1] * realPoint.getY() + (float)transform[1][2];
						float zt = (float)transform[2][0] * realPoint.getX() + (float)transform[2][1] * realPoint.getY() + (float)transform[2][2];
						
						xt /= zt;
						yt /= zt;
						Float toSet = null;
						if(backgroundColour!=null)
							toSet = this.images.get(i).getPixelInterp(xt, yt,backgroundColour);
						else
							if(setMap.get(y * output.getWidth() + x)!=null)
								toSet = this.images.get(i).getPixelInterp(xt, yt,output.getPixelInterp(x, y));
							else
								toSet = this.images.get(i).getPixelInterp(xt, yt);
						// Blend the pixel with the existing pixel
						if(setMap.get(y * output.getWidth() + x)!=null){
							blendingPallet.pixels[0][1] = toSet;
							blendingPallet.pixels[0][0] = output.getPixel(x, y);
							
							toSet = blendingPallet.getPixelInterp(0.1, 0.5);
						}
						setMap.put(y * output.getWidth() + x,true);
						output.pixels[y][x] = toSet;
					}
					i++;
				}
			}
		}
		return output;
	}

}
