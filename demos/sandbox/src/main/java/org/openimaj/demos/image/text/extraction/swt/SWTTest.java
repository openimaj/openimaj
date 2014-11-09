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
