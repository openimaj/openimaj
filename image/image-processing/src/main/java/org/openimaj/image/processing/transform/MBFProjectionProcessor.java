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

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * Perform a set of matrix transforms on a set of images and construct a single image containing all the pixels (or a window of the pixels)
 * in the projected space. 
 */
public class MBFProjectionProcessor extends ProjectionProcessor<Float[], MBFImage> {
	@Override
	public MBFImage performProjection(int windowMinC , int windowMinR , MBFImage output) {
		final FImage[] bands = new FImage[output.numBands()];
		for(int i = 0; i < bands.length; i++){
			bands[i] = output.getBand(i);
		}
		final FImage[][] input = new FImage[this.projectedShapes.size()][];
		for(int i = 0; i < input.length; i++){
			MBFImage inputMBF = this.images.get(i);
			input[i] = new FImage[inputMBF.numBands()];
			for(int j = 0; j < input[i].length; j++){
				input[i][j] = inputMBF.getBand(j);
			}
		}
		for(int y = 0; y < output.getHeight(); y++)
		{
			for(int x = 0; x < output.getWidth(); x++){
				Point2dImpl realPoint = new Point2dImpl(windowMinC + x,windowMinR + y);
				int i = 0;
				for(int k = 0; k < this.projectedRectangles.size(); k++){
					Rectangle r = this.projectedRectangles.get(k);
					Shape s = this.projectedShapes.get(k);
					if( r.isInside(realPoint) && s.isInside(realPoint)){
						double[][] transform = this.transformsInverted.get(i).getArray();
						
						float xt = (float)transform[0][0] * realPoint.x + (float)transform[0][1] * realPoint.y + (float)transform[0][2];
						float yt = (float)transform[1][0] * realPoint.x + (float)transform[1][1] * realPoint.y + (float)transform[1][2];
						float zt = (float)transform[2][0] * realPoint.x + (float)transform[2][1] * realPoint.y + (float)transform[2][2];
						
						xt /= zt;
						yt /= zt;
						
						for(int j = 0; j < bands.length; j++){
							FImage in = input[i][j];
							FImage out = bands[j];
							out.pixels[y][x] = in.getPixelInterpNative(xt, yt,out.pixels[y][x]);
						}
						
					}
					i++;
				}
			}
		}
		return output;
	}
}
