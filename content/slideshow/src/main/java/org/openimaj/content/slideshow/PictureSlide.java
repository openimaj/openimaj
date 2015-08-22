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
package org.openimaj.content.slideshow;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities.ScalingImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 * A slide that displays a picture, scaled to the size of the slide.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class PictureSlide implements Slide {
	protected URL url;
	protected ScalingImageComponent ic;
	protected MBFImage mbfImage;

	/**
	 * Create a picture slide
	 *
	 * @param picture
	 *            the url of the picture
	 * @throws IOException
	 */
	public PictureSlide(URL picture) throws IOException {
		this.url = picture;
		this.mbfImage = ImageUtilities.readMBF(this.url);
	}

	/**
	 * Create a picture slide
	 *
	 * @param mbfImage
	 *            the picture
	 */
	public PictureSlide(MBFImage mbfImage) {
		this.mbfImage = mbfImage;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final BufferedImage image = ImageUtilities.createBufferedImageForDisplay(mbfImage, null);

		ic = new ScalingImageComponent(true);
		ic.setImage(image);
		ic.setSize(width, height);
		ic.setPreferredSize(new Dimension(width, height));
		ic.setShowPixelColours(false);
		ic.setShowXYPosition(false);
		ic.removeMouseListener(ic);
		ic.removeMouseMotionListener(ic);

		return ic;
	}

	@Override
	public void close() {
		ic = null;
	}
}
