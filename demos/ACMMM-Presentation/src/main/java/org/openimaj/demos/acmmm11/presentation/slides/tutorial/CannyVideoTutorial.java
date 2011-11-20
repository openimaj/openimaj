package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.edges.CannyEdgeDetector2;
import org.openimaj.video.Video;

public class CannyVideoTutorial extends TutorialPanel {
	private static final long serialVersionUID = 5612774671360730283L;

	public CannyVideoTutorial( Video<MBFImage> capture,int width, int height) {
		super("Canny Edges", capture, width, height);
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		toDraw.processInline(new CannyEdgeDetector2());		
	}
}
