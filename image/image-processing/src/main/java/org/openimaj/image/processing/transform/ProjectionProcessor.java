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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

import Jama.Matrix;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * Perform a set of matrix transforms on a set of images and construct a single image containing all the pixels (or a window of the pixels)
 * in the projected space. 
 *
 * @param <Q> The image pixel type
 * @param <T> the image type
 */
public class ProjectionProcessor 
		<Q,T extends Image<Q,T>> 
	implements
		SinglebandImageProcessor<Q,T>{
	
	private int minc;
	private int minr;
	private int maxc;
	private int maxr;
	private boolean unset;
	private List<Matrix> transforms;
	private List<T> images;
	private List<Shape> projectedShapes;
	
	private Matrix currentMatrix = new Matrix(new double[][]{{1,0,0},{0,1,0},{0,0,1}});

	/**
	 * Construct a projection processor starting with an identity matrix for any images processed (i.e., don't do anything)
	 */
	public ProjectionProcessor() {
		unset = true;
		this.minc = 0;
		this.minr = 0;
		this.maxc = 0;
		this.maxr = 0;
		
		transforms = new ArrayList<Matrix>();
		images = new ArrayList<T>();
		this.projectedShapes = new ArrayList<Shape>();
	}
	
	/**
	 * Set the matrix, any images processed from this point forward will be projected using this matrix
	 * @param matrix a 3x3 matrix representing a 2d transform
	 */
	public void setMatrix(Matrix matrix) {
		this.currentMatrix = matrix;
	}
	
	@Override
	/**
	 * Prepare an image to be transformed using the current matrix. The bounds of the image post transform are calculated
	 * so the default {@link ProjectionProcessor#performProjection} knows what range of pixels to draw
	 * @param image to be transformed
	 */
	public void processImage(T image, Image<?, ?>... otherimages) {
		Rectangle actualBounds = image.getBounds();
		Shape transformedActualBounds = actualBounds.transform(this.currentMatrix);
		double tminX = transformedActualBounds.minX() ;
		double tmaxX = transformedActualBounds.maxX() ;
		double tminY = transformedActualBounds.minY() ;
		double tmaxY = transformedActualBounds.maxY() ;
		if(unset){
			this.minc = (int) Math.floor(tminX) ;
			this.minr = (int) Math.floor(tminY) ;
			this.maxc = (int) Math.floor(tmaxX) ;
			this.maxr = (int) Math.floor(tmaxY) ;
			unset=false;
		}
		else{
			if (tminX < minc) minc = (int) Math.floor(tminX) ;
			if (tmaxX > maxc) maxc = (int) Math.floor(tmaxX) ;
			if (tminY < minr) minr = (int) Math.floor(tminY) ;
			if (tmaxY > maxr) maxr = (int) Math.floor(tmaxY) ;
		}
		// Expand the borders by 1 pixel so we get a nicer effect around the edges
		float padding = 1f;
		Rectangle expandedBounds = new Rectangle(actualBounds.x-padding,actualBounds.y-padding,actualBounds.width+padding*2,actualBounds.height+padding*2);
		Shape transformedExpandedBounds = expandedBounds.transform(this.currentMatrix);
		
		this.images.add(image);
		this.transforms.add(this.currentMatrix.copy());
		this.projectedShapes.add(transformedExpandedBounds);
		
//		System.out.println("added image with transform: ");
//		this.currentMatrix.print(5,5);
//		System.out.println("and the inverse:");
//		this.currentMatrix.inverse().print(5,5);
//		System.out.println("New min/max become:" + minc + "x" + minr + "/" + maxc + "x" + maxr);
	}
	
	/**
	 * Using all the images currently processed, perform the projection on each image and draw every pixel with valid data. Pixels within
	 * the bounding box but with no data are set to black (more specifically 0, whatever that may mean for this kind of image)
	 * @return the image containing all the pixels drawn
	 */
	public T performProjection() {
		// The most long winded way to get a black pixel EVER
		return performProjection(false,this.images.get(0).newInstance(1, 1).getPixel(0,0));
	}
	/**
	 * Perform projection specifying the background colour (i.e. the colour of pixels with no data) and whether the original window size should be kept. 
	 * If set to true the window of pixels drawn post projection are within the window of the first image processed. 
	 * @param keepOriginalWindow whether to keep the original image's window
	 * @param backgroundColour the background colour
	 * @return projected images
	 */
	public T performProjection(boolean keepOriginalWindow, Q backgroundColour) {
		int projectionMinC = minc, projectionMaxC = maxc, projectionMinR = minr, projectionMaxR = maxr;
		if(keepOriginalWindow)
		{
			projectionMinC = 0;
			projectionMinR = 0;
			projectionMaxR = images.get(0).getRows();
			projectionMaxC = images.get(0).getCols();
		}
		return performBackProjection(projectionMinC , projectionMaxC , projectionMinR , projectionMaxR, backgroundColour);
	}
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
	public T performBackProjection(int windowMinC , int windowMaxC , int windowMinR , int windowMaxR , Q backgroundColour) {
		T output = null;
		output = images.get(0).newInstance(windowMaxC-windowMinC,windowMaxR-windowMinR);
		for(int y = 0; y < output.getHeight(); y++)
		{
			for(int x = 0; x < output.getWidth(); x++){
				Point2d realPoint = new Point2dImpl(windowMinC + x,windowMinR + y);
				int i = 0;
				for(Shape s : this.projectedShapes){
					if(s.isInside(realPoint)){
						Matrix transform = this.transforms.get(i).copy().inverse();
						
						float xt = (float)transform.get(0, 0) * realPoint.getX() + (float)transform.get(0, 1) * realPoint.getY() + (float)transform.get(0, 2);
						float yt = (float)transform.get(1, 0) * realPoint.getX() + (float)transform.get(1, 1) * realPoint.getY() + (float)transform.get(1, 2);
						float zt = (float)transform.get(2, 0) * realPoint.getX() + (float)transform.get(2, 1) * realPoint.getY() + (float)transform.get(2, 2);
						
						xt /= zt;
						yt /= zt;
						
						output.setPixel(x, y, this.images.get(i).getPixelInterp(xt, yt,backgroundColour));
					}
					i++;
				}
			}
		}
		return output;
	}

	/**
	 * @return Current matrix
	 */
	public Matrix getMatrix() {
		return this.currentMatrix;
	}

	/**
	 * Utility function, project one image with one matrix. Every valid pixel in the space the image is projected into is 
	 * displayed in the final image.
	 * @param <Q> the image pixel type
	 * @param <T> image type
	 * @param image the image to project
	 * @param matrix the matrix to project against
	 * @return projected image
	 */
	public static <Q,T extends Image<Q,T>> T project(T image,Matrix matrix) {
		ProjectionProcessor<Q, T> proc = new ProjectionProcessor<Q, T>();
		proc.setMatrix(matrix);
		image.process(proc);
		return proc.performProjection();
	}
}
