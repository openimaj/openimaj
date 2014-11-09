package org.openimaj.demos.image.text.extraction.swt;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.text.extraction.swt.LetterCandidate;
import org.openimaj.image.text.extraction.swt.LineCandidate;
import org.openimaj.image.text.extraction.swt.SWTTextDetector;
import org.openimaj.image.text.extraction.swt.WordCandidate;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayAdapter;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class SWTVideoTest {
	public static void main(String[] args) throws VideoCaptureException {
		final SWTTextDetector detector = new SWTTextDetector();
		detector.getOptions().direction = SWTTextDetector.Direction.LightOnDark;

		VideoDisplay.createVideoDisplay(new VideoCapture(640, 480)).addVideoListener(new VideoDisplayAdapter<MBFImage>()
		{
			@Override
			public void beforeUpdate(MBFImage frame) {
				if (frame == null)
					return;

				detector.analyseImage(frame.flatten());

				for (final LineCandidate line : detector.getLines()) {
					frame.drawShape(line.getRegularBoundingBox(), RGBColour.RED);

					for (final WordCandidate wc : line.getWords()) {
						frame.drawShape(wc.getRegularBoundingBox(), RGBColour.BLUE);

						for (final LetterCandidate lc : wc.getLetters())
							frame.drawShape(lc.getRegularBoundingBox(), RGBColour.GREEN);
					}
				}
			}
		});
	}
}
