package org.openimaj.demos.sandbox.tldcpp;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.shape.Rectangle;

public class DetectionResult {

	public boolean containsValidData;
	public List<Rectangle> fgList;
	public float [] posteriors; /* Contains the posteriors for each slding window. Is of size numWindows. Allocated by tldInitClassifier. */
	public List<Integer> confidentIndices;
	public int [] featureVectors;
	public float [] variances;
	public int numClusters;
	public Rectangle detectorBB;

	DetectionResult() {
		containsValidData = false;
		fgList = new ArrayList<Rectangle>();
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
		if(fgList != null) fgList.clear();
		if(confidentIndices != null) confidentIndices.clear();
		numClusters = 0;
		detectorBB = null;
	}

	void release() {
		fgList.clear();
		variances = null;
		posteriors = null;
		featureVectors = null;
		confidentIndices = null;
		detectorBB = null;
		containsValidData = false;
	}


}
