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
package org.openimaj.image.contour;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.contour.SuzukiContourProcessor.Border;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.threshold.OtsuThreshold;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ContourAestheticode {
	
	public static void main(String[] args) throws IOException {
		String code = "/org/openimaj/image/contour/aestheticode/aestheticode.jpg";
		MBFImage img = ImageUtilities.readMBF(ContourAestheticode.class.getResource(code));
		
		final OtsuThreshold thresh = new OtsuThreshold();
		
		ResizeProcessor resize = new ResizeProcessor(0.3f);
		FImage threshImg = img.flatten().process(resize ).process(thresh);
		DisplayUtilities.display(threshImg);
		Border root = SuzukiContourProcessor.findContours(threshImg);
		MBFImage contourImage = MBFImage.createRGB(threshImg);
		MBFImage detectedImage = MBFImage.createRGB(threshImg);
		ContourRenderer.drawContours(contourImage, root);
		DisplayUtilities.display(contourImage);
		DisplayUtilities.display(img.process(resize));
		
		List<Aestheticode> detected = new FindAestheticode().apply(root);
		
		for (Aestheticode aestheticode : detected) {
			ContourRenderer.drawContours(detectedImage, aestheticode.root);
		}
		DisplayUtilities.display(detectedImage);
	}

	
	
	
}
