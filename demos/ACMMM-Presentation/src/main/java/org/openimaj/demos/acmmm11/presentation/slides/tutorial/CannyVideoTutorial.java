package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processing.edges.CannyEdgeDetector2;
import org.openimaj.ml.clustering.kmeans.fast.FastFloatKMeansCluster;
import org.openimaj.video.Video;

public class CannyVideoTutorial extends TutorialPanel {



	public CannyVideoTutorial( Video<MBFImage> capture,int width, int height) {
		super("Video Processing", capture, width, height);
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		toDraw.processInline(new CannyEdgeDetector2());
		
	}

}
