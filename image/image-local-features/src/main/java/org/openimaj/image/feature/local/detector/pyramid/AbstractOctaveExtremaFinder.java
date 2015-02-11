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
package org.openimaj.image.feature.local.detector.pyramid;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;

/**
 * <p>
 * Class for finding extrema within {@link Octave}s using the
 * approach in Section 4 of Lowe's IJCV paper (minus the bit on
 * using Brown's interpolation approach to improve localisation).
 * </p>
 * Interest points are detected if:
 * <ul>
 * 	<li>They are at a local extremum in scale-space</li>
 *  <li>The ratio of the Eigenvalues of the edge response at the point is above
 *  	a threshold (i.e. the point is not on a straight line, and has a certain
 *  	amount of curvature).
 *  </li>
 * </ul>
 * 
 *  <p>
 *  The AbstractOctaveExtremaFinder uses an event listener paradigm. Once
 *  interest points are found, the internal listener will be informed. 
 *  </p>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OCTAVE>
 */
public abstract class AbstractOctaveExtremaFinder< 
		OCTAVE extends GaussianOctave<FImage>> 
	extends 
		AbstractOctaveInterestPointFinder<OCTAVE, FImage> 
{
	/** The default threshold for the edge response Eigenvalue ratio */
	public static final float DEFAULT_EIGENVALUE_RATIO = 10.0f;
	
	//Threshold on the ratio of the Eigenvalues of the Hessian matrix (Lowe IJCV, p.12)
	protected float eigenvalueRatio = DEFAULT_EIGENVALUE_RATIO;
	
	/**
	 * Construct an AbstractOctaveExtremaFinder with the default
	 * Eigenvalue ratio threshold.
	 */
	public AbstractOctaveExtremaFinder() {
		this(DEFAULT_EIGENVALUE_RATIO);
	}
	
	/**
	 * Construct an AbstractOctaveExtremaFinder with the given
	 * Eigenvalue ratio threshold.
	 * @param eigenvalueRatio
	 */
	public AbstractOctaveExtremaFinder(float eigenvalueRatio) {
		this.eigenvalueRatio = eigenvalueRatio;
	}
	
	@Override
	public OCTAVE getOctave() {
		return octave;
	}
	
	@Override
	public int getCurrentScaleIndex() {
		return currentScaleIndex;
	}
	
	@Override
	public void process(OCTAVE octave) {
		beforeProcess(octave);
		
		this.octave = octave;
		
		FImage[] images = octave.images;
		int height = images[0].height;
		int width = images[0].width;
		int borderDist = octave.options.getBorderPixels();

		//search through the scale-space images, leaving a border 
		for (currentScaleIndex = 1; currentScaleIndex < images.length - 1; currentScaleIndex++) {
			for (int y = borderDist; y < height - borderDist; y++) {
				for (int x = borderDist; x < width - borderDist; x++) {
					float val = images[currentScaleIndex].pixels[y][x];

					if (firstCheck(val, x, y, currentScaleIndex, images) &&
						isLocalExtremum(val, images[currentScaleIndex-1], x, y) && 
						isLocalExtremum(val, images[currentScaleIndex], x, y) && 
						isLocalExtremum(val, images[currentScaleIndex+1], x, y) && 
						isNotEdge(images[currentScaleIndex], x, y)) {
						processExtrema(images, currentScaleIndex, x, y, octave.octaveSize);
					}
				}
			}
		}
	}

	/**
	 * Perform the first of the checks that determine whether
	 * a point is a valid interest point. This can be overridden 
	 * to allow for cheaper tests to occur before more expensive ones.
	 * 
	 * @param val the value at the point.
	 * @param x the x-coordinate of the point.
	 * @param y the y-coordinate of the point.
	 * @param scaleIndex the scale index at which the point was found
	 * @param images the scale images
	 * @return true if the point is potentially an interest point; false otherwise.
	 */
	protected boolean firstCheck(float val, int x, int y, int scaleIndex, FImage[] images) {
		return true;
	}

	/**
	 * Called at the start of {@link AbstractOctaveExtremaFinder#process(OCTAVE)}
	 * @param octave the octave being processed
	 */
	protected void beforeProcess(OCTAVE octave) {
		//do nothing
	}

	/**
	 * Test to see if a point is a local extremum by searching
	 * the +/- 1 pixel neighbourhood in x and y. 
	 * 
	 * @param val the value at x,y 
	 * @param image the image to test against
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @return true if extremum, false otherwise.
	 */
	protected boolean isLocalExtremum(float val, FImage image, int x, int y) {
		float pix[][] = image.pixels;

		if (val > 0.0) {
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++)
					if (pix[yy][xx] > val)
						return false;
		} else {
			for (int yy = y - 1; yy <= y + 1; yy++)
				for (int xx = x - 1; xx <= x + 1; xx++)
					if (pix[yy][xx] < val)
						return false;
		}
		return true;
	}

	
	/**
	 * Test if the pixel at x,y in the image is NOT on an edge.
	 * @param image the image
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @return true if the pixel is not an edge, false otherwise
	 */
	protected boolean isNotEdge(FImage image, int x, int y) {
		float pix[][] = image.pixels;

		//estimate Hessian from finite differences
		float H00 = pix[y - 1][x] - 2.0f * pix[y][x] + pix[y + 1][x];
		float H11 = pix[y][x - 1] - 2.0f * pix[y][x] + pix[y][x + 1];
		float H01 = ((pix[y + 1][x + 1] - pix[y + 1][x - 1]) - (pix[y - 1][x + 1] - pix[y - 1][x - 1])) / 4.0f;

		//determinant and trace of Hessian
		float det = H00 * H11 - H01 * H01;
		float trace = H00 + H11;

		float eigenvalueRatio1 = eigenvalueRatio + 1.0f;
		
		return (det * eigenvalueRatio1 * eigenvalueRatio1 > eigenvalueRatio * trace * trace);
	}

	/**
	 * Perform any additional checks on the point, and then inform
	 * the listener that a point has been found.
	 * 
	 * @param images the stack of images in this octave
	 * @param s the interest-point scale
	 * @param x the x-coordinate of the interest-point
	 * @param y the y-coordinate of the interest-point
	 * @param octSize
	 */
	protected abstract void processExtrema(FImage[] images, int s, int x, int y, float octSize);
}
