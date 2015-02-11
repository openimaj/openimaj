package org.openimaj.demos.sandbox.tldcpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.openimaj.demos.sandbox.tldcpp.detector.Clustering;
import org.openimaj.demos.sandbox.tldcpp.detector.DetectionResult;
import org.openimaj.demos.sandbox.tldcpp.detector.DetectorCascade;
import org.openimaj.demos.sandbox.tldcpp.detector.EnsembleClassifier;
import org.openimaj.demos.sandbox.tldcpp.detector.NNClassifier;
import org.openimaj.demos.sandbox.tldcpp.detector.NormalizedPatch;
import org.openimaj.demos.sandbox.tldcpp.detector.VarianceFilter;
import org.openimaj.demos.sandbox.tldcpp.tracker.MedianFlowTracker;
import org.openimaj.demos.sandbox.tldcpp.videotld.TLDUtil;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.IndependentPair;

/**
 * An implementation TLD tracker by Zdenek Kalal:
 * http://info.ee.surrey.ac.uk/Personal/Z.Kalal/tld.html based on the C++
 * implementation Georg Nebehay: http://gnebehay.github.com/OpenTLD/
 *
 * This class is the main controller class. TLD is instantiated on an image and
 * bounding box. Once the detector classifiers are initialised the
 * {@link TLD#processImage(FImage)} function must be called with suceutive
 * frames in which objects are: - Tracked using {@link MedianFlowTracker} -
 * ...and if not tracked correctly detected using the {@link DetectorCascade}. -
 * ... if tracked or detected correctly, but the object is different enough, it
 * is learnt using {@link DetectorCascade}!
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TLD {
	/**
	 * Whether the {@link MedianFlowTracker} is enabled
	 */
	public boolean trackerEnabled;
	/**
	 * Whether the {@link DetectorCascade} is enabled
	 */
	public boolean detectorEnabled;
	/**
	 * Whether previously unseen frames are learnt from
	 */
	public boolean learningEnabled;
	/**
	 * Whether some frames are skipped
	 */
	public boolean alternating;
	/**
	 * The current bounding box
	 */
	public Rectangle currBB;
	/**
	 * the previous bounding box
	 */
	public Rectangle prevBB;

	/**
	 * The detector
	 */
	public DetectorCascade detectorCascade;

	/**
	 * The tracker
	 */
	public MedianFlowTracker medianFlowTracker;
	/**
	 * The previous frame, where #prevBB was detected
	 */
	public FImage prevImg;
	/**
	 * The current frame, where #currBB will be detected
	 */
	public FImage currImg;
	/**
	 * The confidence of the current bounding box. Calculated from the detector
	 * or the tracker. The {@link MedianFlowTracker#trackerBB} is extracted from
	 * the {@link #currImg} and confidence is gauged using {@link NNClassifier}
	 */
	public float currConf;

	/**
	 * The nearest neighbour classifier
	 */
	private NNClassifier nnClassifier;
	private boolean learning;
	private boolean valid;
	private boolean wasValid;
	private int imgWidth;
	private int imgHeight;

	/**
	 * Initialises the TLD with a series of defaults. {@link #trackerEnabled} is
	 * true {@link #detectorEnabled} is true {@link #learningEnabled} is true
	 * {@link #alternating} is false {@link #alternating} is false valid
	 */
	private TLD() {
		trackerEnabled = true;
		detectorEnabled = true;
		learningEnabled = true;
		alternating = false;
		valid = false;
		wasValid = false;
		learning = false;
		currBB = null;

		detectorCascade = new DetectorCascade();
		nnClassifier = detectorCascade.getNNClassifier();

		medianFlowTracker = new MedianFlowTracker();
	}

	/**
	 * @param width
	 * @param height
	 */
	public TLD(int width, int height) {
		this();
		this.imgWidth = width;
		this.imgHeight = height;
	}

	/**
	 * Stop tracking whatever is currently being tracked
	 */
	public void release() {
		detectorCascade.release();
		medianFlowTracker.cleanPreviousData();
		currBB = null;
	}

	//
	private void storeCurrentData() {
		prevImg = null;
		prevImg = currImg; // Store old image (if any)
		prevBB = currBB; // Store old bounding box (if any)

		detectorCascade.cleanPreviousData(); // Reset detector results
		medianFlowTracker.cleanPreviousData();

		wasValid = valid;
	}

	/**
	 * Set the current object being tracked. Initialilise the detector casecade
	 * using {@link DetectorCascade#init()}. The {@link #initialLearning()} is
	 * called
	 *
	 * @param img
	 * @param bb
	 * @throws Exception
	 */
	public void selectObject(FImage img, Rectangle bb) throws Exception {
		// Delete old object
		detectorCascade.release();

		detectorCascade.setObjWidth((int) bb.width);
		detectorCascade.setObjHeight((int) bb.height);
		detectorCascade.setImgWidth(this.imgWidth);
		detectorCascade.setImgHeight(this.imgHeight);

		// Init detector cascade
		detectorCascade.init();

		currImg = img;
		currBB = bb;
		currConf = 1;
		valid = true;

		initialLearning();

	}

	/**
	 * An attempt is made to track the object from the previous frame. The
	 * {@link DetectorCascade} instance is used regardless to detect the object.
	 * The {@link #fuseHypotheses()} is then used to combine the estimate of the
	 * tracker and detector together. Finally, using the detectorCascade the
	 * learn function is called and the classifier is improved
	 * 
	 * @param img
	 */
	public void processImage(FImage img) {
		storeCurrentData();
		final FImage grey_frame = img.clone(); // Store new image , right after
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

		// if(!valid){
		// currBB = null;
		// currConf = 0;
		// }
		//
	}

	/**
	 * The bounding box is retrieved from the tracker and detector as well as
	 * the number of clusters detected by the detector {@link Clustering} step.
	 * If exactly one cluster exists in {@link Clustering} (i.e. the detector is
	 * very sure) the detector confidence is included. If the tracker was able
	 * to keep track of the bounding box (i.e. trackerBB is not null) then the
	 * tracker confidence is combined.
	 *
	 * if the detector is more confident than the tracker and their overlap is
	 * very small, the detectors BB is used. Otherwise the trackers BB and
	 * confidence is used. If the trackerBB is used the tracking is valid only
	 * if the tracking was invalid last time and the confidence is above
	 * {@link NNClassifier#thetaTP} or if the tracking was valid last time a
	 * smaller threshold of {@link NNClassifier#thetaFP} is used.
	 *
	 * TODO: Maybe a better combination of the two bounding boxes from the
	 * detector and tracker would be better?
	 */
	public void fuseHypotheses() {
		final Rectangle trackerBB = medianFlowTracker.trackerBB;
		final int numClusters = detectorCascade.getDetectionResult().numClusters;
		final Rectangle detectorBB = detectorCascade.getDetectionResult().detectorBB;

		currBB = null;
		currConf = 0;
		valid = false;

		float confDetector = 0;

		if (numClusters == 1) {
			confDetector = nnClassifier.classifyBB(currImg, detectorBB);
		}

		if (trackerBB != null) {
			final float confTracker = nnClassifier.classifyBB(currImg, trackerBB);

			if (numClusters == 1 && confDetector > confTracker
					&& TLDUtil.tldOverlapNorm(trackerBB, detectorBB) < 0.5)
			{

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

	/**
	 * The initial learning is done using the input bounding box.
	 *
	 * Firstly, the {@link VarianceFilter} is told its
	 * {@link VarianceFilter#minVar} by finding the variance of the selected
	 * patch.
	 *
	 * Next all patches in {@link DetectorCascade} with a large offset (over
	 * 0.6f) with the selected box are used as positive examples while all
	 * windows with an overlap of less tha 0.2f and a variance greater than the
	 * minimum variance (i.e. they pass the variance check but yet do not
	 * overlap) are used as negative examples. The {@link EnsembleClassifier} is
	 * trained on the positive examples for {@link EnsembleClassifier}.
	 *
	 * Finally, the negative and positive examples are all fed to the
	 * {@link NNClassifier} using the {@link NNClassifier}.
	 *
	 * The usage of these 3 classifiers is explained in more detail in
	 * {@link DetectorCascade#detect(FImage)}. The {@link NNClassifier} is also
	 * used to calculate confidences in {@link #fuseHypotheses()}
	 */
	public void initialLearning() {
		final int numWindows = detectorCascade.getNumWindows();
		learning = true; // This is just for display purposes

		final DetectionResult detectionResult = detectorCascade.getDetectionResult();

		detectorCascade.detect(currImg);

		// This is the positive patch
		final NormalizedPatch patch = new NormalizedPatch();
		patch.source = currImg;
		patch.window = currBB;
		patch.positive = true;

		final float initVar = patch.calculateVariance();
		detectorCascade.getVarianceFilter().minVar = initVar / 2;

		final float[] overlap = new float[numWindows];
		detectorCascade.windowOverlap(currBB, overlap);

		// Add all bounding boxes with high overlap

		final List<IndependentPair<Integer, Float>> positiveIndices = new ArrayList<IndependentPair<Integer, Float>>();
		final List<Integer> negativeIndices = new ArrayList<Integer>();

		// First: Find overlapping positive and negative patches
		for (int i = 0; i < numWindows; i++) {

			if (overlap[i] > 0.6) {
				positiveIndices.add(IndependentPair.pair(i, overlap[i]));
			}

			if (overlap[i] < 0.2) {
				final float variance = detectionResult.variances[i];

				if (!detectorCascade.getVarianceFilter().enabled
						|| variance > detectorCascade.getVarianceFilter().minVar)
				{ // TODO:
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
		System.out.println("Number of positive features on init: " + positiveIndices.size());

		// This might be absolutely and horribly SLOW. figure it out.
		Collections.sort(positiveIndices,
				new Comparator<IndependentPair<Integer, Float>>() {
			@Override
			public int compare(IndependentPair<Integer, Float> o1,
					IndependentPair<Integer, Float> o2)
					{
				return o1.secondObject().compareTo(o2.secondObject());
			}

		});

		final List<NormalizedPatch> patches = new ArrayList<NormalizedPatch>();

		patches.add(patch); // Add first patch to patch list

		final int numIterations = Math.min(positiveIndices.size(), 10); // Take
																		// at
		// most 10
		// bounding
		// boxes
		// (sorted
		// by
		// overlap)
		for (int i = 0; i < numIterations; i++) {
			final int idx = positiveIndices.get(i).firstObject();
			// Learn this bounding box
			// TODO: Somewhere here image warping might be possible
			detectorCascade.getEnsembleClassifier().learn(
					currImg, true,
					detectionResult.featureVectors, detectorCascade.numTrees * idx
					);
		}

		// be WARY. the random indecies are not actually random. maybe this
		// doesn't matter.
		final Random r = new Random(1); // TODO: This is not guaranteed to
										// affect
		// random_shuffle

		// random_shuffle(negativeIndices.begin(), negativeIndices.end());
		Collections.shuffle(negativeIndices, r);

		// Choose 100 random patches for negative examples
		for (int i = 0; i < Math.min(100, negativeIndices.size()); i++) {
			final int idx = negativeIndices.get(i);

			final NormalizedPatch negPatch = new NormalizedPatch();
			negPatch.source = currImg;
			negPatch.window = detectorCascade.getWindow(idx);
			negPatch.prepareNormalisedPatch(); // This creates and sets the
												// public valueImg which holds
												// the normalised zoomed window
			negPatch.positive = false;
			patches.add(negPatch);
		}

		detectorCascade.getNNClassifier().learn(patches);

	}

	/**
	 * If the detection results are good and {@link #fuseHypotheses()} believes
	 * that the area was tracked to, but was not detected well then there is
	 * potential that the classifiers should be updated with the bounding box.
	 *
	 * The bounding box is used to extract highly overlapping windows as
	 * positive examples, and two kinds of negative examples are collected if
	 * they overlap less than 0.2f. For the ensemble classifier, negative
	 * examples are collected if the results of the {@link DetectorCascade}
	 */
	public void learn() {
		if (!learningEnabled || !valid || !detectorEnabled) {
			learning = false;
			return;
		}
		final int numWindows = detectorCascade.getNumWindows();
		learning = true;
		//
		final DetectionResult detectionResult = detectorCascade.getDetectionResult();
		//
		if (!detectionResult.containsValidData) {
			detectorCascade.detect(currImg);
		}
		//
		// This is the positive patch
		NormalizedPatch patch = new NormalizedPatch();
		patch.source = currImg;
		patch.window = currBB;
		patch.prepareNormalisedPatch();
		//
		final float[] overlap = new float[numWindows];
		this.detectorCascade.windowOverlap(currBB, overlap);
		//
		// //Add all bounding boxes with high overlap
		//
		final List<IndependentPair<Integer, Float>> positiveIndices = new ArrayList<IndependentPair<Integer, Float>>();
		final List<Integer> negativeIndices = new ArrayList<Integer>();
		final List<Integer> negativeIndicesForNN = new ArrayList<Integer>();
		// vector<pair<int,float> > positiveIndices;
		// vector<int> negativeIndices;
		// vector<int> negativeIndicesForNN;
		//
		// //First: Find overlapping positive and negative patches
		//
		for (int i = 0; i < numWindows; i++) {
			//
			if (overlap[i] > 0.6) {
				positiveIndices.add(IndependentPair.pair(i, overlap[i]));
			}
			//
			if (overlap[i] < 0.2) {
				if (!detectorCascade.getEnsembleClassifier().enabled || detectionResult.posteriors[i] > 0.1) { // TODO:
																												// Shouldn't
																												// this
																												// read
																												// as
																												// 0.5?
					negativeIndices.add(i);
				}

				if (!detectorCascade.getEnsembleClassifier().enabled || detectionResult.posteriors[i] > 0.5) {
					negativeIndicesForNN.add(i);
				}

			}
		}

		Collections.sort(positiveIndices,
				new Comparator<IndependentPair<Integer, Float>>() {
			@Override
			public int compare(IndependentPair<Integer, Float> o1,
					IndependentPair<Integer, Float> o2)
					{
				return o1.secondObject().compareTo(o2.secondObject());
			}

		});
		//
		final List<NormalizedPatch> patches = new ArrayList<NormalizedPatch>();
		//
		patch.positive = true;
		patches.add(patch);
		// //TODO: Flip
		//
		//
		final int numIterations = Math.min(positiveIndices.size(), 10); // Take
																		// at
																		// most
																		// 10
																		// bounding
																		// boxes
																		// (sorted
																		// by
																		// overlap)
		//
		for (int i = 0; i < negativeIndices.size(); i++) {
			final int idx = negativeIndices.get(i);
			// TODO: Somewhere here image warping might be possible
			detectorCascade.getEnsembleClassifier().learn(currImg, false, detectionResult.featureVectors,
					detectorCascade.numTrees * idx);
			// detectorCascade.ensembleClassifier.learn(currImg,
			// detectorCascade.windows[idx], false,
			// detectionResult.featureVectors[detectorCascade.numTrees*idx]);
		}
		//
		// //TODO: Randomization might be a good idea
		for (int i = 0; i < numIterations; i++) {
			final int idx = positiveIndices.get(i).firstObject();
			// //TODO: Somewhere here image warping might be possible
			// detectorCascade.ensembleClassifier.learn(currImg,
			// &detectorCascade.windows[TLD_WINDOW_SIZE*idx], true,
			// &detectionResult.featureVectors[detectorCascade.numTrees*idx]);
			detectorCascade.getEnsembleClassifier().learn(currImg, true, detectionResult.featureVectors,
					detectorCascade.numTrees * idx);
		}
		//
		for (int i = 0; i < negativeIndicesForNN.size(); i++) {
			final int idx = negativeIndicesForNN.get(i);
			//
			patch = new NormalizedPatch();
			patch.source = currImg;
			patch.window = detectorCascade.getWindow(idx);
			patch.positive = false;
			patches.add(patch);
		}
		//
		detectorCascade.getNNClassifier().learn(patches);
		//
		// //cout << "NN has now " <<
		// detectorCascade.nnClassifier.truePositives.size() <<
		// " positives and " <<
		// detectorCascade.nnClassifier.falsePositives.size() <<
		// " negatives.\n";
		//
		// delete[] overlap;
	}

	/**
	 * @return whether the tracker is learning from the previous frame
	 */
	public boolean isLearning() {
		return this.learning;
	}
}
