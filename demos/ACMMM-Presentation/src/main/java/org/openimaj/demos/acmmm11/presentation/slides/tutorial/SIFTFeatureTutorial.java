package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

public class SIFTFeatureTutorial extends TutorialPanel {

	private DoGSIFTEngine dog;

	public SIFTFeatureTutorial(Video<MBFImage> capture, int width,int height) {
		super("SIFT features", capture, width, height);
		dog = new DoGSIFTEngine();
		dog.getOptions().setDoubleInitialImage(false);
	}

	@Override
	public void doTutorial(MBFImage toDraw) {
		LocalFeatureList<Keypoint> feats = dog.findFeatures(toDraw.flatten());
		KeypointVisualizer<Float[],MBFImage> vis = new KeypointVisualizer<Float[],MBFImage>(toDraw, feats);
		toDraw.internalAssign(vis.drawCenter(RGBColour.RED));
	}

	
}
