/**
 * 
 */
package org.openimaj.image.text.ocr;

import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 *	Top-level class for classes that are able to process images to
 *	extract textual content by performing OCR.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 4 Aug 2011
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class OCRProcessor implements ImageProcessor<FImage>
{
	/**
	 * 	After processing, this method should return a set of bounding
	 * 	boxes of regions within the image mapped to a String that contains
	 * 	the recognised text.
	 * 
	 *	@return A map of rectangle to string
	 */
	public abstract Map<Rectangle,String> getText(); 
}
