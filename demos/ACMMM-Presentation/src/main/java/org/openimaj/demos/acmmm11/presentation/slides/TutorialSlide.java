package org.openimaj.demos.acmmm11.presentation.slides;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.CameraSelector;
import org.openimaj.content.slideshow.Slide;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.CannyVideoTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.ColourHistogramGrid;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.FaceTrackingTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.SIFTFeatureTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.SegmentationTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.ShapeRenderingTutorial;
import org.openimaj.demos.acmmm11.presentation.slides.tutorial.TutorialPanel;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;

public class TutorialSlide implements Slide {
	private static final long serialVersionUID = 1L;
	
	private VideoCapture capture;
	private List<VideoDisplay<MBFImage>> displays;
	
	public TutorialSlide() throws IOException {
		
	}
	
	@Override
	public Component getComponent(int slideWidth, int slideHeight) throws IOException {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridLayout(2,3));
		
		int videoWidth = 320;
		int videoHeight = 240;
		
		capture = CameraSelector.getPreferredVideoCapture(videoWidth, videoHeight);
		displays = new ArrayList<VideoDisplay<MBFImage>>();
		
		TutorialPanel[] tutorials = new TutorialPanel[] {
			new FaceTrackingTutorial(capture, videoWidth, videoHeight),
			new SIFTFeatureTutorial(capture, videoWidth, videoHeight),
			new SegmentationTutorial(capture, videoWidth, videoHeight),
			new CannyVideoTutorial(capture, videoWidth, videoHeight),
			new ShapeRenderingTutorial(capture, videoWidth, videoHeight),
			new ColourHistogramGrid(capture, videoWidth, videoHeight)
		};
		
		for (TutorialPanel innerPanel : tutorials) {
			panel.add(innerPanel);
			
			VideoDisplay<MBFImage> vd = VideoDisplay.createOffscreenVideoDisplay( capture );
			vd.addVideoListener( innerPanel );
			displays.add( vd );
		}
		
		panel.validate();
		
		return panel;
	}

	@Override
	public void close() {
		for (VideoDisplay<MBFImage> vd : displays)
			vd.close();
		displays.clear();		
		capture.stopCapture();
	}
}
