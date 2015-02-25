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
package org.openimaj.demos.image.text.extraction.swt;

import java.io.File;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.text.extraction.swt.LetterCandidate;
import org.openimaj.image.text.extraction.swt.LineCandidate;
import org.openimaj.image.text.extraction.swt.SWTTextDetector;
import org.openimaj.image.text.extraction.swt.WordCandidate;

public class SWTTest {
	public static void main(String[] args) throws Exception {
		final SWTTextDetector detector = new SWTTextDetector();
		detector.getOptions().direction = SWTTextDetector.Direction.LightOnDark;

		final MBFImage image = ImageUtilities.readMBF(new File(
				"/Users/jon/Pictures/Photo Booth Library/Pictures/Photo on 06-11-2014 at 09.50.jpg"));
		image.flipX();

		// final MBFImage image = new MBFImage(1000, 500, 3);
		// image.drawText("hello world hello world", 100, 100, new
		// GeneralFont("Time New Roman", 80), 80);

		detector.analyseImage(image.flatten());

		final MBFImage allLetters = image.clone();
		for (final LetterCandidate lc : detector.getLetters())
			allLetters.drawShape(lc.getRegularBoundingBox(), RGBColour.GREEN);
		DisplayUtilities.display(allLetters, "All candidate letters before line grouping.");

		for (final LineCandidate line : detector.getLines()) {
			image.drawShape(line.getRegularBoundingBox(), 3, RGBColour.RED);

			for (final LetterCandidate lc : line.getLetters())
				image.drawShape(lc.getRegularBoundingBox(), 3, RGBColour.GREEN);

			for (final WordCandidate wc : line.getWords())
				image.drawShape(wc.getRegularBoundingBox(), 3, RGBColour.BLUE);
		}

		DisplayUtilities.display(image, "Filtered candidate letters, lines and words.");
	}
}
