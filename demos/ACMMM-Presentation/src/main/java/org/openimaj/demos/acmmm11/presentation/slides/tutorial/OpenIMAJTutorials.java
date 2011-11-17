package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JPanel;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.capture.VideoCapture;

public class OpenIMAJTutorials extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private VideoCapture capture;
	
	public OpenIMAJTutorials() throws IOException{
		int width = 640;
		int height = 480;
		int widthT = width/2;
		int heightT = height/2;
		
		capture = new VideoCapture(widthT,heightT);
		
		this.setLayout(new GridLayout(2,3));
		TutorialPanel tut1 = new FaceTrackingTutorial(capture,widthT, heightT);
		this.add(tut1);
		TutorialPanel tut2 = new SIFTFeatureTutorial(capture,widthT, heightT);
		this.add(tut2);
		TutorialPanel tut3 = new SegmentationTutorial(capture,widthT, heightT);
		this.add(tut3);
		TutorialPanel tut4 = new CannyVideoTutorial(capture,widthT, heightT);
		this.add(tut4);
		TutorialPanel tut5 = new ShapeRenderingTutorial(capture,widthT, heightT);
		this.add(tut5);
		TutorialPanel tut6 = new ColourHistogramGrid(capture,widthT, heightT);
		this.add(tut6);
		
		VideoDisplay.createOffscreenVideoDisplay(capture).addVideoListener(tut1);
		VideoDisplay.createOffscreenVideoDisplay(capture).addVideoListener(tut2);
		VideoDisplay.createOffscreenVideoDisplay(capture).addVideoListener(tut3);
		VideoDisplay.createOffscreenVideoDisplay(capture).addVideoListener(tut4);
		VideoDisplay.createOffscreenVideoDisplay(capture).addVideoListener(tut5);
		VideoDisplay.createOffscreenVideoDisplay(capture).addVideoListener(tut6);
	}

}
