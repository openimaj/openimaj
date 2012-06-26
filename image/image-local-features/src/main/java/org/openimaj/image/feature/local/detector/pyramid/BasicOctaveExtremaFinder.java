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
 * A basic concrete implementation of an 
 * {@link AbstractOctaveExtremaFinder} that searches for 
 * local extrema in scale space. If detected extrema pass
 * a series of tests (namely those described in 
 * {@link AbstractOctaveExtremaFinder}) and a final test 
 * that tests the absolute value of the interest point pixel
 * against a threshold, then the listener object will be
 * informed that a point has been detected.
 * </p> 
 *  
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class BasicOctaveExtremaFinder extends AbstractOctaveExtremaFinder<GaussianOctave<FImage>> {
	/** 
	 * The default threshold for the magnitude of interest points 
	 */
	public static final float DEFAULT_MAGNITUDE_THRESHOLD = 0.04f; //Lowe's IJCV suggests 0.03, 
																   //but the open SIFT implementation uses 0.04 
	
	//Threshold on the magnitude of detected points (Lowe IJCV, p.11)
	protected float magnitudeThreshold = DEFAULT_MAGNITUDE_THRESHOLD;
	
	protected float scales;
	protected float normMagnitudeScales;
	
	public BasicOctaveExtremaFinder() {	}
	
	public BasicOctaveExtremaFinder(float magnitudeThreshold) {
		this(magnitudeThreshold, DEFAULT_EIGENVALUE_RATIO);
	}
	
	public BasicOctaveExtremaFinder(float magnitudeThreshold, float eigenvalueRatio) {	
		super(eigenvalueRatio);
		this.magnitudeThreshold = magnitudeThreshold;
	}
	
	@Override
	protected void beforeProcess(GaussianOctave<FImage> octave) {
		scales = octave.options.getScales();
		
		//the magnitude threshold must be adjusted based on the number of scales,
		//as more scales will result in smaller differences between scales
		normMagnitudeScales = magnitudeThreshold / octave.options.getScales();
	}
	
	@Override
	protected boolean firstCheck(float val, int x, int y, int s, FImage[] dogs) {
		//perform magnitude check
		if (Math.abs(dogs[s].pixels[y][x]) > normMagnitudeScales) {
			return true;
		}
		return false;
	}
	
	@Override
	protected void processExtrema(FImage[] dogs, int s, int x, int y, float octSize) {
		//calculate the actual scale within the octave
		float octaveScale = octave.options.getInitialSigma() * (float) Math.pow(2.0, s / scales);
			
		//fire the listener
		if (listener != null) listener.foundInterestPoint(this, x, y, octaveScale);
	}
}
