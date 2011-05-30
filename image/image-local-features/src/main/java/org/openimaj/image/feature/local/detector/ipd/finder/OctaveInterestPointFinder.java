package org.openimaj.image.feature.local.detector.ipd.finder;

import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.detector.ipd.collector.InterestPointFeatureCollector;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.processing.pyramid.OctaveProcessor;
import org.openimaj.image.processing.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.processing.pyramid.gaussian.GaussianPyramid;

/**
 * Finder with a specified detector which finds interest points at a given gaussian octave. This is often
 * used in conjunction with a {@link GaussianPyramid} which provides {@link GaussianOctave} instances. 
 * 
 * This finder calls a specified {@link InterestPointFeatureCollector} which does something with the features
 * located at a given octave.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class OctaveInterestPointFinder implements OctaveProcessor<GaussianOctave<FImage>, FImage> {
	/**
	 * How interest points are selected
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
	 *
	 */
	public enum FeatureMode {
		/**
		 * Threshold the interest points score
		 */
		THRESHOLD, 
		/**
		 * Accept only the top N  interest points
		 */
		NUMBER
		
	}
	private InterestPointDetector detector;
	private InterestPointFeatureCollector listener;
	private FeatureMode mode;
	private double modeNumber;

	/**
	 * @param detector the detector with which features are found
	 * @param mode the detector's feature selection mode
	 * @param modeNumber the selection mode's selection condition number
	 */
	public OctaveInterestPointFinder(InterestPointDetector detector, FeatureMode mode, double modeNumber) {
		this.detector = detector;
		this.mode = mode;
		this.modeNumber = modeNumber;
	}

	@Override
	public void process(GaussianOctave<FImage> octave) {
		// Get the first image of this octave, it contains the image at the correct scale
		if(octave.octaveSize!=1)return;
		detector.findInterestPoints(octave.images[0]);
		
		List<InterestPointData> points = null;
		switch(this.mode){
		case THRESHOLD:
			points = this.detector.getInterestPoints((float) modeNumber);
			break;
		case NUMBER:
			points = this.detector.getInterestPoints((int) modeNumber);
			break;
		}
		for(InterestPointData  point: points){
			this.listener.foundInterestPoint(octave.images[0], point,octave.octaveSize);
		}	
	}
	
	

	/**
	 * @param listener to be informed on detection of new interest points
	 */
	public void setOctaveInterestPointListener(InterestPointFeatureCollector listener) {
		this.listener = listener;
	}

}
