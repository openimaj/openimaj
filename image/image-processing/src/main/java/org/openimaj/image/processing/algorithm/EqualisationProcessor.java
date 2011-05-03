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
package org.openimaj.image.processing.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;

/**
 *	An {@link ImageProcessor} that performs histogram equalisation
 *	(projecting the colours back into the image).
 * 
 * 	@see "http://www.generation5.org/content/2004/histogramEqualization.asp"
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 31 Mar 2011
 */
public class EqualisationProcessor implements ImageProcessor<FImage> 
{
	/**
	 * 	Equalise the colours in the image. Creates a histogram
	 * 	that contains as many bins as colours, equalises it,
	 * 	then back-projects it into an image. The resulting image
	 * 	has equalised values between 0 and 1. It assumes the image
	 * 	has already been normalised such that its values are also	
	 * 	between 0 and 1.
	 * 
	 * 	@see "http://www.generation5.org/content/2004/histogramEqualization.asp"
	 */
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		List<Float> x = getIntensities(image, true );

		// This will be a histogram of all intensities
		Map<Float, Integer> hg = new TreeMap<Float, Integer>();
		for( Float f : x )
			hg.put( f, 0 );

		// Create the histogram
		for( int r = 0; r < image.height; r++ )
			for( int c = 0; c < image.width; c++ )
				hg.put( image.pixels[r][c], hg.get( image.pixels[r][c] ) + 1 );

		// Create cumulative histogram
		int acc = 0;
		for( Float f : hg.keySet() )
		{
			int i = hg.get(f).intValue();
			acc += i;
			hg.put( f, acc );
		}

		// The assumption is that the max value will be 1
		float alpha = 1f / (image.getWidth()*image.getHeight());

		FImage ni = image.clone();

		// Back-project into the new image
		for( int r = 0; r < image.height; r++ )
			for( int c = 0; c < image.width; c++ )
				ni.setPixel( c, r, hg.get( image.pixels[r][c] ) * alpha );

		image.internalAssign(ni);
	}
	
	/**
	 *	Returns a list of all the discrete intensity values
	 *	in the image. The sort parameter can be used to determine
	 *	whether the returned list should be sorted by value.
	 * 
	 *  @param image the image
	 *	@param sort Whether to sort the return list by value
	 *	@return A List of intensity values
	 */
	public static List<Float> getIntensities( FImage image, boolean sort )
	{
		ArrayList<Float> x = new ArrayList<Float>();
		for( int r = 0; r < image.height; r++ )
			for( int c = 0; c < image.width; c++ )
				if( !x.contains( image.pixels[r][c] ) ) x.add( image.pixels[r][c] );
		if( sort ) Collections.sort( x );
		return x;
	}
	
	/**
	 * 	Returns the number of discrete intensity values
	 * 	in the provided image.
	 *  
	 *  @param image the image 
	 * 
	 *	@return The number of discrete intensity values in the image.
	 */
	public static int nIntensities(FImage image)
	{
		return getIntensities( image, false ).size();
	}
}
