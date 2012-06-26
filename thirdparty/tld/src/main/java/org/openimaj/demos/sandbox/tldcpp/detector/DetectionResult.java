package org.openimaj.demos.sandbox.tldcpp.detector;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.shape.Rectangle;

/**
 * This state class holds the results of {@link DetectorCascade#detect(org.openimaj.image.FImage)} and
 * is used primarily to save having to do this work again, this can probably be protected or gone entirley 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DetectionResult {

	/**
	 * Whether the contained results are valid
	 */
	public boolean containsValidData;
//	public List<Rectangle> fgList;
	/**
	 * The probability of each window as set by the {@link EnsembleClassifier}
	 */
	public float [] posteriors; /* Contains the posteriors for each slding window. Is of size numWindows. Allocated by tldInitClassifier. */
	/**
	 * The windows which we are confident (by the 3 classifiers) might contain the object
	 */
	public List<Integer> confidentIndices;
	/**
	 * This is a numberOfTrees * numberOfWindows list containing the feature for each tree for each window
	 */
	public int [] featureVectors;
	/**
	 * This is a numberOfWindows list containing the variance of each window
	 */
	public float [] variances;
	/**
	 * The number of clusters which confident windows were grouped into based on their overlaps
	 */
	public int numClusters;
	/**
	 * The current window estimated by the detector (afte clustering)
	 */
	public Rectangle detectorBB;
	/**
	 * The number of windows skipped thanks to variance check
	 */
	public int varCount;
	/**
	 * The number of windows skipped thanks to ensemble classifier
	 */
	public int ensCount;
	/**
	 * The number of windows skipped thanks to normalised correlation.
	 */
	public int nnClassCount;

	DetectionResult() {
		containsValidData = false;
		confidentIndices = new ArrayList<Integer>();
		numClusters = 0;
		detectorBB = null;

		variances = null;
		posteriors = null;
		featureVectors = null;
	}


	void init(int numWindows, int numTrees) {
		variances = new float[numWindows];
		posteriors = new float[numWindows];
		featureVectors = new int[numWindows*numTrees];
		confidentIndices = new ArrayList<Integer>();

	}

	void reset() {
		containsValidData = false;
		if(confidentIndices != null) confidentIndices.clear();
		numClusters = 0;
		detectorBB = null;
		varCount = 0;
		ensCount = 0;
		nnClassCount = 0;
	}

	void release() {
		variances = null;
		posteriors = null;
		featureVectors = null;
		confidentIndices = null;
		detectorBB = null;
		containsValidData = false;
	}


}
