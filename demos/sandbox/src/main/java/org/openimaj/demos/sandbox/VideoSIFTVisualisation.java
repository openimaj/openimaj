package org.openimaj.demos.sandbox;

import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class VideoSIFTVisualisation implements VideoDisplayListener<MBFImage> {
	private DoGSIFTEngine engine;

	public VideoSIFTVisualisation() throws IOException {
		VideoDisplay.createVideoDisplay(new VideoCapture(320, 240)).addVideoListener(this);
		
		engine = new DoGSIFTEngine();
	}
	
	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		LocalFeatureList<Keypoint> kpts = engine.findFeatures(frame.flatten());
		engine.getOptions().setDoubleInitialImage(false);
		KeypointVisualizer.drawPatchesInline(frame, kpts, RGBColour.RED, RGBColour.GREEN);
	}
	
	public static void main(String[] args) throws IOException {
		new VideoSIFTVisualisation();
	}
}
