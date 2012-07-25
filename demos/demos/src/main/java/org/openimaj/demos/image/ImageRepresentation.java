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
package org.openimaj.demos.image;

import java.io.IOException;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 * 	Demonstration of the MBFImage. Displays an image along with its red, 
 * 	green and blue channels. The code shows how these images can be written 
 * 	to files.
 *		
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 15 Feb 2012
 */
@Demo(
	author = "", 
	description = "Demonstration of the MBFImage. Displays an image along " +
			"with its red, green and blue channels. The code shows how these " +
			"images can be written to files.", 
	keywords = { "image", "colour", "bands" }, 
	title = "Colour Image Demo",
	icon = "/org/openimaj/demos/icons/image/bird-icon.png"
)
public class ImageRepresentation {
	/**
	 * 	Default main
	 *  @param args Command-line arguments
	 *  @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		MBFImage image = ImageUtilities.readMBF(
				ImageRepresentation.class.getResourceAsStream(
						"/org/openimaj/demos/image/bird.png"));
		DisplayUtilities.display(image);
		
		FImage blank = new FImage(image.getWidth(), image.getHeight());
		MBFImage red = new MBFImage(image.getBand(0), blank, blank);
		MBFImage green = new MBFImage(blank, image.getBand(1), blank);
		MBFImage blue = new MBFImage(blank, blank, image.getBand(2));
		
		DisplayUtilities.displayLinked( "Images", 2, image, red, green, blue );		
	}
}
