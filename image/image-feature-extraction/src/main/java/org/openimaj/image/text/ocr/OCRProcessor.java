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
package org.openimaj.image.text.ocr;

import java.util.Map;

import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Top-level class for classes that are able to process images to extract
 * textual content by performing OCR.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 4 Aug 2011
 *
 * @param <T>
 *            The type of {@link ImageProcessor}
 */
public abstract class OCRProcessor<T extends Image<?, T>>
		implements ImageAnalyser<T>
{
	/**
	 * After processing, this method should return a set of bounding boxes of
	 * regions within the image mapped to a String that contains the recognised
	 * text. If the OCR processor cannot extract regions (it just reads whatever
	 * image it's given), then the map can be singular with the rectangular
	 * bounding box the same as the image bounding box.
	 *
	 * @return A map of rectangle to string
	 */
	public abstract Map<Rectangle, String> getText();
}
