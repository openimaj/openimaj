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
/**
 * 
 */
package org.openimaj.image.text.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.text.ocr.OCRProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.IndependentPair;

/**
 *	An interface for classes that are able to extract text from images.
 *	The single method allows the retrieval of the text mapped to the
 *	bounding boxes of the text within the image.
 *	<p>
 *	Note that this is an {@link ImageProcessor} extension so that the
 *	{@link TextExtractor} should process the image prior to the
 *	{@link #getTextRegions()} method being called.
 *	<p>
 *	This class will deal with the processing of extracted text regions
 *	with the OCR processor. Use {@link #setOCRProcessor(OCRProcessor)} to
 *	choose with OCR processor will be used on the extracted regions. 
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 11 Aug 2011
 *	
 *  @param <T> The type of {@link Image} 
 */
public abstract class TextExtractor<T extends Image<?,T>> 
	implements ImageProcessor<T>
{
	/** The OCR Processor to extract strings from text regions. */
	private OCRProcessor<T> ocr = null;
	
	/**
	 * 	Get the text regions that can be extracted from an image. The images
	 * 	in the values of the map need not be simply the extracted region that
	 * 	is bounded by the rectangular key (this can be done afterwards), but 
	 * 	may be a representation that is as near to canonical as possible -
	 * 	that is, it may be warped or thresholded such that an OCR processor
	 * 	may have less trouble reading the text 
	 * 
	 *	@return A map from bounding box in original image to a canonical
	 *		representation of the text (may be warped or thresholded)
	 */
	public abstract Map<Rectangle,T> getTextRegions();
	
	/**
	 * 	Get text that can be extracted from an image. The map should map a 
	 * 	bounding box within the processed image to a pair of extracted image vs.
	 * 	text string. The extracted image may not necessarily be the region
	 * 	of interest which the rectangle bounds; it can be as close to a 
	 * 	canonical representation of the text as possible such that an OCR
	 * 	would have less difficulty in classifying the text. For example,
	 *  the image may be thresholded or warped such that the text is straight.
	 * 
	 *	@return A map of bounding box to a pair of image and text string
	 */
	public Map<Rectangle, IndependentPair<T, String>> getText()
	{
		// The result map for the method
		Map<Rectangle, IndependentPair<T, String>> textMap = 
			new HashMap<Rectangle, IndependentPair<T,String>>();
				
		// Get the regions
		Map<Rectangle,T> textRegions = getTextRegions();
		
		// OCR the text from the text regions
		if( ocr != null )
		{
			for( Rectangle r : textRegions.keySet() )
			{
				// Process the image with the OCR Processor
				textRegions.get(r).analyseWith( ocr );
				
				// Get the text from the OCR Processor
				Map<Rectangle, String> m = ocr.getText();
				
				// For each of the rectangles returned from the OCR
				// we add them individually into the output set.
				for( Rectangle subR: m.keySet() )
				{
					String s = m.get( subR );
					
					// Translate into image coordinates (from sub-image coords)
					subR.translate( r.x, r.y );
					
					// Put into the output map
					textMap.put( subR, 
						new IndependentPair<T,String>( textRegions.get(r), s ) 
					);
				}
			}
		}
		else
		{
			// If no OCR is done, we simply add all the extracted text
			// regions with a null string.
			for( Rectangle r : textRegions.keySet() )
			{
				textMap.put( r, 
					new IndependentPair<T,String>( 
						textRegions.get(r), null ) );
			}
		}		
		
		return textMap;
	}
	
	/**
	 * 	If you're not interested in where the strings are located in the image
	 * 	you can use this method to simply get a list of extracted strings.
	 * 
	 *	@return A {@link List} of strings extracted from the image.
	 */
	public List<String> getTextStrings()
	{
		List<String> strings = new ArrayList<String>();
		
		if( ocr != null )
		{
			// Get the regions
			Map<Rectangle,T> textRegions = getTextRegions();

			for( Rectangle r : textRegions.keySet() )
			{
				// Process the image with the OCR Processor
				textRegions.get(r).analyseWith( ocr );
				
				// Get the text from the OCR Processor
				Map<Rectangle, String> m = ocr.getText();
				strings.addAll( m.values() );
			}
		}
		
		return strings;
	}
	
	/**
	 * 	For the text regions that are extracted to be associated with textual
	 * 	representations of the text regions, an OCR processor must be used.
	 * 	Use this function to choose which OCR processor is used to extract
	 *  read text regions.
	 * 
	 *	@param ocr The {@link OCRProcessor} to use
	 */
	public void setOCRProcessor( OCRProcessor<T> ocr )
	{
		this.ocr = ocr;
	}
	
	/**
	 * 	Return the OCR processor being used to extract text from the
	 * 	image.
	 * 
	 *	@return The {@link OCRProcessor}
	 */
	public OCRProcessor<T> getOCRProcessor()
	{
		return this.ocr;
	}
}
