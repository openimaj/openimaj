package org.openimaj.demos.acmmm11.presentation.slides.tutorial;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;
import org.openimaj.video.Video;

/**
 * Slide showing DoG feature localisation
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */

public class SIFTFeatureTutorial extends TutorialPanel {
	private static final long serialVersionUID = 1L;
	
	private DoGSIFTEngine dog;

	/**
	 * Default constructor
	 * 
	 * @param capture
	 * @param width
	 * @param height
	 */
	public SIFTFeatureTutorial(Video<MBFImage> capture, int width,int height) {
		super("Difference-of-Gaussian Keypoints", capture, width, height);
		
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
