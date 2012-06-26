package org.openimaj.demos.sandbox.tldcpp.detector;

import java.util.Random;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * The ensemble classifier implements a forest of random binary pixel
 * comparisons. Each tree in the forest is made up of several features.
 * Each feature is a randomly selected pair of pixels (at the same location
 * across scales) within a window. The boolean comparison of the pixels 
 * sets the value of a bit of the feature int. This means technically each 
 * feature can only be 32 decisions wide, 13 seems to work well though
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class EnsembleClassifier {
	private float[][] img;
	
	/**
	 * Whether this classifier is enabled
	 */
	public boolean enabled;

	/**
	 * The number of trees, each with {@link #numFeatures} features (default = 10)
	 */
	public int numTrees;
	/**
	 * The number of bits per feature (each feature is in fact held as an int of bits, default = 13)
	 */
	public int numFeatures;

	/**
	 * The number of scales, not configurable, dependant on the scales searched in {@link DetectorCascade}
	 */
	private int numScales;
	private Rectangle[] scales;

	private int[][] windowOffsets;
	private int[][] featureOffsets;
	private float[] features;

	int numIndices;

	private float [] posteriors;
	private int [] positives;
	private int [] negatives;

	DetectionResult detectionResult;

	private Random rand;

	EnsembleClassifier()
	{
		numTrees=10;
		numFeatures = 13;
		enabled = true;
		rand = new Random();
	}


	void init() {
		numIndices = (int) Math.pow(2.0f, numFeatures);

		initFeatureLocations();
		initFeatureOffsets();
		initPosteriors();
	}

	void release() {
		features = null;
		featureOffsets = null;
		posteriors = null;
		positives = null;
		negatives = null;
	}

	/*
	 * Generates random measurements in the format <x1,y1,x2,y2>
	 */
	void initFeatureLocations() {
		int size = 2 * 2 * numFeatures * numTrees;

		features = new float[size];

		for(int i=0; i < size; i++) {
			features[i] = rand.nextFloat();
		}

	}

	/**
	 * Creates the offsets which represent the pixels which are compared within a bounding box
	 * for each feature within each tree at each scale.
	 * 
	 * This process just picks two random pixels within the bounding box based on the known size
	 * of the bounding box at a particular scale.
	 * 
	 * How the two random pixels are compared to form the feature itself is described in
	 * {@link #calcFernFeature(int, int)}
	 */
	void initFeatureOffsets() {

		featureOffsets= new int[numScales*numTrees*numFeatures*2][2];
//		int *off = featureOffsets;
		int offIndex = 0;

		for (int k = 0; k < numScales; k++){
			Rectangle scale = scales[k];
			for (int i = 0; i < numTrees; i++) {
				for (int j = 0; j < numFeatures; j++) {
//					float *currentFeature  = features + (4*numFeatures)*i +4*j;
//					*off++ = sub2idx((scale.width-1)*currentFeature[0]+1,(scale.height-1)*currentFeature[1]+1,imgWidthStep); //We add +1 because the index of the bounding box points to x-1, y-1
//					*off++ = sub2idx((scale.width-1)*currentFeature[2]+1,(scale.height-1)*currentFeature[3]+1,imgWidthStep);
					// float *currentFeature  = features + (4*numFeatures)*i +4*j;
					int currentFeatureIndex  = (4*numFeatures)*i +4*j;
					featureOffsets[offIndex++] = new int[]{
							(int) ((scale.width-1)*features[currentFeatureIndex + 0]+1),
							(int) ((scale.height-1)*features[currentFeatureIndex + 1]+1)
					}; //We add +1 because the index of the bounding box points to x-1, y-1
					featureOffsets[offIndex++] = new int[]{
							(int) ((scale.width-1)*features[currentFeatureIndex + 2]+1),
							(int) ((scale.height-1)*features[currentFeatureIndex + 3]+1)
					};
				}
			}
		}
	}

	void initPosteriors() {
		posteriors = new float[numTrees * numIndices];
		positives = new int[numTrees * numIndices];
		negatives = new int[numTrees * numIndices];

		for (int i = 0; i<numTrees; i++) {
			for(int j = 0; j < numIndices; j++) {
				posteriors[i*numIndices + j] = 0;
				positives[i*numIndices + j] = 0;
				negatives[i*numIndices + j] = 0;
			}
		}
	}

	/**
	 * @param img just sets the image internally read to be used to calculate the feature
	 */
	public void nextIteration(FImage img) {
		if(!enabled) return;

		this.img = img.pixels;
	}

	/**
	 * calculate the value for each feature of a tree at a scale for a given window.
	 * 
	 * The value of each feature is an int whose bits look like this:
	 * 01110010101.
	 * 
	 * 1 if the first random pixel for that feature is bigger than the second random pixel
	 * 0 otherwise!
	 * WHOA
	 * @param windowIdx
	 * @param treeIdx
	 * @return the int feature of the tree for the window
	 */
	int calcFernFeature(int windowIdx, int treeIdx) {

		int index = 0;
//		int *bbox = windowOffsets+ windowIdx* TLD_WINDOW_OFFSET_SIZE;
		int bboxIndex = windowIdx* DetectorCascade.TLD_WINDOW_OFFSET_SIZE;
//		int *off = featureOffsets + bbox[4] + treeIdx*2*numFeatures; //bbox[4] is pointer to features for the current scale
		int offIndex = windowOffsets[bboxIndex + 4][0] + treeIdx*2*numFeatures;
		for (int i=0; i<numFeatures; i++) {
//			int fp0 = img[bbox[0] + off[0]];
//			int fp1 = img[bbox[0] + off[1]];
//			if (fp0>fp1) { index |= 1;}
//			off += 2;
			index<<=1;
			int[] bbox0 = windowOffsets[bboxIndex];
			int[] off0 = featureOffsets[offIndex];
			int[] off1 = featureOffsets[offIndex+1];
			float fp0 = img[bbox0[1] + off0[1]][bbox0[0] + off0[0]];
			float fp1 = img[bbox0[1] + off1[1]][bbox0[0] + off1[0]];
			if (fp0>fp1) { index |= 1;}
			offIndex += 2;
		}
		return index;
	}
	
	/**
	 * For a window index, calculate the feature values for each tree. Store them in 
	 * the featureVector array from the featureVectorIndex for a length of numTrees. 
	 * 
	 * @param windowIdx
	 * @param featureVector
	 * @param featureVectorIndex
	 */
	public void calcFeatureVector(int windowIdx, int[] featureVector, int featureVectorIndex) {
	    for(int i = 0; i < numTrees; i++) {
	    	featureVector[featureVectorIndex + i] = calcFernFeature(windowIdx, i);
		}
	}

	/**
	 * sum the probability calculated for each tree with the calculated feature value (which 
	 * is in fact the index into the posteriors array)
	 * @param featureVector
	 * @param featureVectorIndex
	 * @return the sum confidence from each tree of this feature assignment
	 */
	public float calcConfidence(int [] featureVector, int featureVectorIndex) {
		float conf = 0.0f;

		for(int i = 0; i < numTrees; i++) {
			conf += posteriors[i * numIndices + featureVector[featureVectorIndex + i]];
		}

		return conf;
	}
	
	/**
	 * Fill the feature vector values for each tree for a given window. Use the features
	 * for each tree for this window is to calculate the confidence of this window.
	 * @param windowIdx
	 */
	void classifyWindow(int windowIdx) {
//		int* featureVector = detectionResult->featureVectors + numTrees * windowIdx;
		int featureVectorIndex = numTrees * windowIdx;
		calcFeatureVector(windowIdx, detectionResult.featureVectors, featureVectorIndex);

		detectionResult.posteriors[windowIdx] = calcConfidence(detectionResult.featureVectors, featureVectorIndex);
	}

	/**
	 * find the proability of this window and return true if this window is more
	 * than 50% likely.
	 * @param i
	 * @return whether this window is likely to match given the previously seen positive examples
	 */
	public boolean filter(int i)  {
		if(!enabled) return true;

		classifyWindow(i);
		if(detectionResult.posteriors[i] < 0.5) return false;
		return true;
	}

	/**
	 * An array index is calculated from treeIdx (the y) and the calculated
	 * feature index (the x) so: (y * width) + x where width is the maximum feature 
	 * index (2 ^ numverOfFeatures).
	 *  
	 * The posterior for each arrayIndex is calculated by doing:
	 * nPositive / (nPositive + nNegative). So it will be higher the more positive 
	 * examples are seen for this particular feature in this particular tree.
	 * 
	 * The logic is that things that are similar will be positive in the same tree/feature
	 * so they will have higher counts.
	 * 
	 * 
	 * @param treeIdx
	 * @param idx
	 * @param positive
	 * @param amount
	 */
	public void updatePosterior(int treeIdx, int idx, boolean positive, int amount) {
		int arrayIndex = treeIdx * numIndices + idx;
		if(positive)
		{
			positives[arrayIndex] += amount;
		}
		else
		{
			negatives[arrayIndex] += amount;
		}
		posteriors[arrayIndex] = ((float) positives[arrayIndex]) / (positives[arrayIndex] + negatives[arrayIndex]) / (float)numTrees;
	}

	/**
	 * Update the calculated feature value in every tree. Remembering that the feature value is
	 * calculated based on underlying values of the what the image is doing in the window 
	 * at a few random pixels. So if two images match a featureIndex in a given tree
	 * they match a numIndices pixels
	 * @param featureVector
	 * @param featureIndex
	 * @param positive
	 * @param amount
	 */
	public void updatePosteriors(int []featureVector,int featureIndex, boolean positive, int amount) {

		for (int i = 0; i < numTrees; i++) {

			int idx = featureVector[featureIndex+i];
			updatePosterior(i, idx, positive, amount);

		}
	}
	
	/**
	 * Calculate the confidence of the given feature vector at a given index (which is 
	 * the feature vector of a particular window). 
	 * 
	 * If the window is particularly low confidence but is meant to be positive, then update
	 * the posteriors with its values.
	 * 
	 * If the window is particularly high confidence but is meant to be negative, then update
	 * the posteriors also.
	 * 
	 * @param img
	 * @param positive
	 * @param featureVector
	 * @param featureIndex
	 */
	public void learn(FImage img, boolean positive, int [] featureVector, int featureIndex) {
	    if(!enabled) return;

		float conf = calcConfidence(featureVector,featureIndex);

	    //Update if positive patch and confidence < 0.5 or negative and conf > 0.5
	    if((positive && conf < 0.5) || (!positive && conf > 0.5)) {
	    	updatePosteriors(featureVector, featureIndex, positive,1);
	    }

	}

	/**
	 * @param numScales the number of scales must be set dynamically
	 */
	void setNumScales(int numScales) {
		this.numScales = numScales;
	}


	/**
	 * @param windowOffsets the location of each window
	 */
	public void setWindowOffsets(int[][] windowOffsets) {
		this.windowOffsets = windowOffsets;
	}


	/**
	 * @param scales the scale rectangles (i.e. the window sizes at each scale)
	 */
	public void setScales(Rectangle[] scales) {
		this.scales = scales;
	}

}
