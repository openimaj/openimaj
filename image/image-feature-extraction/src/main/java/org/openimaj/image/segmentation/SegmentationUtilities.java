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
package org.openimaj.image.segmentation;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.image.processor.connectedcomponent.render.BlobRenderer;

/**
 * Some utility functions for dealing with segmented output
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SegmentationUtilities {
	private SegmentationUtilities() {
	}

	/**
	 * Render the components to the image with randomly assigned colours.
	 * 
	 * @param image
	 *            Image to draw to
	 * @param components
	 *            the components
	 * @return the image
	 */
	public static MBFImage renderSegments(MBFImage image, List<? extends PixelSet> components) {
		for (final PixelSet cc : components) {
			final BlobRenderer<Float[]> br = new BlobRenderer<Float[]>(image, RGBColour.randomColour());
			br.process(new ConnectedComponent(cc.pixels));
		}

		return image;
	}

	/**
	 * Render the components to an image with randomly assigned colours.
	 * 
	 * @param width
	 *            Width of image.
	 * @param height
	 *            Height of image.
	 * @param components
	 *            the components.
	 * @return the rendered image.
	 */
	public static MBFImage renderSegments(int width, int height, List<? extends PixelSet> components) {
		return renderSegments(new MBFImage(width, height, ColourSpace.RGB), components);
	}
}
