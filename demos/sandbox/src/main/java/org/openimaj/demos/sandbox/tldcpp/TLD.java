package org.openimaj.demos.sandbox.tldcpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.openimaj.demos.sandbox.tldcpp.tracker.MedianFlowTracker;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.IndependentPair;

public class TLD {
	public boolean trackerEnabled;
	public boolean detectorEnabled;
	public boolean learningEnabled;
	public boolean alternating;
	public boolean valid;
	public boolean wasValid;
	public boolean learning;
	public Rectangle currBB;
	public DetectorCascade detectorCascade;
	public NNClassifier nnClassifier;
	public MedianFlowTracker medianFlowTracker;
	public FImage prevImg;
	public FImage currImg;
	public Rectangle prevBB;
	public float currConf;

	public TLD() {
		trackerEnabled = true;
		detectorEnabled = true;
		learningEnabled = true;
		alternating = false;
		valid = false;
		wasValid = false;
		learning = false;
		currBB = null;

		detectorCascade = new DetectorCascade();
		nnClassifier = detectorCascade.nnClassifier;

		medianFlowTracker = new MedianFlowTracker();
	}

	void release() {
		detectorCascade.release();
		medianFlowTracker.cleanPreviousData();
		currBB = null;
	}

	//
	void storeCurrentData() {
		prevImg = null;
		prevImg = currImg; // Store old image (if any)
		prevBB = currBB; // Store old bounding box (if any)

		detectorCascade.cleanPreviousData(); // Reset detector results
		medianFlowTracker.cleanPreviousData();

		wasValid = valid;
	}

	//
	void selectObject(FImage img, Rectangle bb) throws Exception {
		// Delete old object
		detectorCascade.release();

		detectorCascade.objWidth = (int) bb.width;
		detectorCascade.objHeight = (int) bb.height;

		// Init detector cascade
		detectorCascade.init();

		currImg = img;
		currBB = bb;
		currConf = 1;
		valid = true;

		initialLearning();

	}

	//
	void processImage(FImage img) {
		storeCurrentData();
		FImage grey_frame = img.clone(); // Store new image , right after
											// storeCurrentData();
		currImg = grey_frame;
		//
		if (trackerEnabled) {
			medianFlowTracker.track(prevImg, currImg, prevBB);
		}
		//
		if (detectorEnabled && (!alternating || medianFlowTracker.trackerBB == null)) {
			detectorCascade.detect(grey_frame);
		}
		//
		fuseHypotheses();
		//
		learn();
		//
	}

	//
	void fuseHypotheses() {
		Rectangle trackerBB = medianFlowTracker.trackerBB;
		int numClusters = detectorCascade.detectionResult.numClusters;
		Rectangle detectorBB = detectorCascade.detectionResult.detectorBB;

		currBB = null;
		currConf = 0;
		valid = false;

		float confDetector = 0;
		
		if (numClusters == 1) {
			confDetector = nnClassifier.classifyBB(currImg, detectorBB);
		}

		if (trackerBB != null) {
			float confTracker = nnClassifier.classifyBB(currImg, trackerBB);

			if (numClusters == 1 && confDetector > confTracker
					&& TLDUtil.tldOverlapNorm(trackerBB, detectorBB) < 0.5) {

				currBB = detectorBB.clone();
				currConf = confDetector;
			} else {
				currBB = trackerBB.clone();
				currConf = confTracker;
				if (confTracker > nnClassifier.thetaTP) {
					valid = true;
				} else if (wasValid && confTracker > nnClassifier.thetaFP) {
					valid = true;
				}
			}
		} else if (numClusters == 1) {
			currBB = detectorBB.clone();
			currConf = confDetector;
		}

		/*
		 * float var = CalculateVariance(patch.values,
		 * nn.patch_size*nn.patch_size);
		 * 
		 * if(var < min_var) { //TODO: Think about incorporating this
		 * printf("%f, %f: Variance too low \n", var, classifier.min_var); valid
		 * = 0; }
		 */
	}

	//
	void initialLearning() {
		learning = true; // This is just for display purposes

		DetectionResult detectionResult = detectorCascade.detectionResult;

		detectorCascade.detect(currImg);

		// This is the positive patch
		NormalizedPatch patch = new NormalizedPatch();
		patch.source = currImg;
		patch.window = currBB;
		patch.positive = true;

		float initVar = patch.calculateVariance();
		detectorCascade.varianceFilter.minVar = initVar / 2;

		float[] overlap = new float[detectorCascade.numWindows];
		TLDUtil.tldOverlap(detectorCascade.windows, detectorCascade.numWindows,
				currBB, overlap);

		// Add all bounding boxes with high overlap

		List<IndependentPair<Integer, Float>> positiveIndices = new ArrayList<IndependentPair<Integer, Float>>();
		List<Integer> negativeIndices = new ArrayList<Integer>();

		// First: Find overlapping positive and negative patches

		for (int i = 0; i < detectorCascade.numWindows; i++) {

			if (overlap[i] > 0.6) {
				positiveIndices.add(IndependentPair.pair(i, overlap[i]));
			}

			if (overlap[i] < 0.2) {
				float variance = detectionResult.variances[i];

				if (!detectorCascade.varianceFilter.enabled
						|| variance > detectorCascade.varianceFilter.minVar) { // TODO:
																				// This
																				// check
																				// is
																				// unnecessary
																				// if
																				// minVar
																				// would
																				// be
																				// set
																				// before
																				// calling
																				// detect.
					negativeIndices.add(i);
				}
			}
		}

		// This might be absolutely and horribly SLOW. figure it out.
		Collections.sort(positiveIndices,
				new Comparator<IndependentPair<Integer, Float>>() {
					@Override
					public int compare(IndependentPair<Integer, Float> o1,
							IndependentPair<Integer, Float> o2) {
						return o1.secondObject().compareTo(o2.secondObject());
					}

				});

		List<NormalizedPatch> patches = new ArrayList<NormalizedPatch>();

		patches.add(patch); // Add first patch to patch list

		int numIterations = Math.min(positiveIndices.size(), 10); // Take at
																	// most 10
																	// bounding
																	// boxes
																	// (sorted
																	// by
																	// overlap)
		for (int i = 0; i < numIterations; i++) {
			int idx = positiveIndices.get(i).firstObject();
			// Learn this bounding box
			// TODO: Somewhere here image warping might be possible
			detectorCascade.ensembleClassifier.learn(currImg, true,
					detectionResult.featureVectors, detectorCascade.numTrees
							* idx);
		}

		// be WARY. the random indecies are not actually random. maybe this
		// doesn't matter.
		Random r = new Random(1); // TODO: This is not guaranteed to affect
									// random_shuffle

		// random_shuffle(negativeIndices.begin(), negativeIndices.end());
		Collections.shuffle(negativeIndices, r);

		// Choose 100 random patches for negative examples
		for (int i = 0; i < Math.min(100, negativeIndices.size()); i++) {
			int idx = negativeIndices.get(i);

			NormalizedPatch negPatch = new NormalizedPatch();
			negPatch.source = currImg;
			negPatch.window = detectorCascade.windows[idx];
			negPatch.prepareNormalisedPatch(); // This creates and sets the public valueImg which holds the normalised zoomed window
			negPatch.positive = false;
			patches.add(negPatch);
		}

		detectorCascade.nnClassifier.learn(patches);

	}

	//
	// //Do this when current trajectory is valid
	void learn() {
		if(!learningEnabled || !valid || !detectorEnabled) {
			learning = false;
			return;
		}
		learning = true;
//
		DetectionResult detectionResult = detectorCascade.detectionResult;
//
		if(!detectionResult.containsValidData) {
			detectorCascade.detect(currImg);
		}
//
		//This is the positive patch
		NormalizedPatch patch = new NormalizedPatch();
		patch.source = currImg;
		patch.window = currBB;
		patch.prepareNormalisedPatch();
//
		float [] overlap = new float[detectorCascade.numWindows];
		TLDUtil.tldOverlap(detectorCascade.windows, detectorCascade.numWindows, currBB,overlap);
//
//		//Add all bounding boxes with high overlap
//
		List<IndependentPair<Integer, Float>> positiveIndices = new ArrayList<IndependentPair<Integer, Float>>();
		List<Integer> negativeIndices = new ArrayList<Integer>();
		List<Integer> negativeIndicesForNN = new ArrayList<Integer>();
//		vector<pair<int,float> > positiveIndices;
//		vector<int> negativeIndices;
//		vector<int> negativeIndicesForNN;
//
//		//First: Find overlapping positive and negative patches
//
		for(int i = 0; i < detectorCascade.numWindows; i++) {
//
			if(overlap[i] > 0.6) {
				positiveIndices.add(IndependentPair.pair(i,overlap[i]));
			}
//
			if(overlap[i] < 0.2) {
				if(!detectorCascade.ensembleClassifier.enabled || detectionResult.posteriors[i] > 0.1) { //TODO: Shouldn't this read as 0.5?
					negativeIndices.add(i);
				}

				if(!detectorCascade.ensembleClassifier.enabled || detectionResult.posteriors[i] > 0.5) {
					negativeIndicesForNN.add(i);
				}

			}
		}
		
		Collections.sort(positiveIndices,
				new Comparator<IndependentPair<Integer, Float>>() {
					@Override
					public int compare(IndependentPair<Integer, Float> o1,
							IndependentPair<Integer, Float> o2) {
						return o1.secondObject().compareTo(o2.secondObject());
					}

				});
//
		List<NormalizedPatch> patches = new ArrayList<NormalizedPatch>();
//
		patch.positive = true;
		patches.add(patch);
//		//TODO: Flip
//
//
		int numIterations = Math.min(positiveIndices.size(), 10); //Take at most 10 bounding boxes (sorted by overlap)
//
		for(int i = 0; i < negativeIndices.size(); i++) {
			int idx = negativeIndices.get(i);
			//TODO: Somewhere here image warping might be possible
			detectorCascade.ensembleClassifier.learn(currImg, true, detectionResult.featureVectors, detectorCascade.numTrees * idx);
//			detectorCascade.ensembleClassifier.learn(currImg, detectorCascade.windows[idx], false, detectionResult.featureVectors[detectorCascade.numTrees*idx]);
		}
//
//		//TODO: Randomization might be a good idea
		for(int i = 0; i < numIterations; i++) {
			int idx = positiveIndices.get(i).firstObject();
//			//TODO: Somewhere here image warping might be possible
//			detectorCascade.ensembleClassifier.learn(currImg, &detectorCascade.windows[TLD_WINDOW_SIZE*idx], true, &detectionResult.featureVectors[detectorCascade.numTrees*idx]);
			detectorCascade.ensembleClassifier.learn(currImg, true, detectionResult.featureVectors, detectorCascade.numTrees * idx);
		}
//
		for(int i = 0; i < negativeIndicesForNN.size(); i++) {
			int idx = negativeIndicesForNN.get(i);
//
			patch = new NormalizedPatch();
			patch.source = currImg;
			patch.window = detectorCascade.windows[idx];
			patch.positive = false;
			patches.add(patch);
		}
//
		detectorCascade.nnClassifier.learn(patches);
//
//		//cout << "NN has now " << detectorCascade.nnClassifier.truePositives.size() << " positives and " << detectorCascade.nnClassifier.falsePositives.size() << " negatives.\n";
//
//		delete[] overlap;
	}
}
