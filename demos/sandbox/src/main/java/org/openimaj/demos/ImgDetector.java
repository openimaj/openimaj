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
package org.openimaj.demos;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.processing.convolution.AverageBoxFilter;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.math.geometry.shape.Rectangle;

public class ImgDetector {

	public static void main(String[] args) throws IOException {
		final FImage img = ImageUtilities
				.readF(new File(
						"/Users/jon/Library/Containers/com.apple.mail/Data/Library/Mail Downloads/BD5C21CA-CC93-4C38-A96D-A44E961C5544/in-2.jpg"));

		// blur the image, with more blur in the horizontal direction than
		// vertical
		final FImage i1 = img.process(new AverageBoxFilter(40, 10));

		// numerical issues from the way the box filter is implemented might
		// mean that there are values slightly bigger than 1, so clip the values
		// to a maximum of 1.
		i1.clip(0f, 1f);

		// threshold the image to make it purely black and white
		i1.processInplace(new OtsuThreshold());

		// invert the image before input to the ConnectedComponentLabeler
		i1.inverse();

		// Apply connected component labelling to find all the "blobs"
		final ConnectedComponentLabeler ccl = new ConnectedComponentLabeler(ConnectMode.CONNECT_8);
		final List<ConnectedComponent> ccs = ccl.findComponents(i1);

		// values for the computed bounds of the "graphic"
		int left = i1.width;
		int right = 0;
		int top = i1.height;
		int bottom = 0;

		// loop through the "blobs" and filter them
		for (final ConnectedComponent cc : ccs) {
			final Rectangle bb = cc.calculateRegularBoundingBox();

			// ignore components that are near the top edge
			if (bb.y < 0.1 * i1.height)
				continue;

			// ignore components that are near the right edge
			if (bb.x + bb.width > 0.9 * i1.width)
				continue;

			// filter the other components based on their width and the relative
			// area of the blob to its bounding box (should be ~1 for a text
			// block)
			if (bb.width < 0.4 * i1.width || bb.calculateArea() / cc.calculateArea() > 2) {
				if (bb.x < left)
					left = (int) bb.x;
				if (bb.x + bb.width > right)
					right = (int) (bb.x + bb.width);
				if (bb.y < top)
					top = (int) bb.y;
				if (bb.y + bb.height > bottom)
					bottom = (int) (bb.y + bb.height);
				i1.drawShape(bb, 0.5f);
			}
		}

		// construct the final bounds rectangle (might want to extend/pad it)
		final Rectangle bounds = new Rectangle(left, top, right - left, bottom - top);
		DisplayUtilities.display(img.extractROI(bounds));
	}
}
