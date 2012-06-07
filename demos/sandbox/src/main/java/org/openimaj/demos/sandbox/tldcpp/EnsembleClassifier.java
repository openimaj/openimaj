package org.openimaj.demos.sandbox.tldcpp;

import java.util.Random;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;

public class EnsembleClassifier {
	private float[][] img;
	
	public boolean enabled;

	//Configurable members
	public int numTrees;
	public int numFeatures;

	public int imgWidthStep;
	public int numScales;
	public Rectangle[] scales;

	public int[][] windowOffsets;
	public int[][] featureOffsets;
	public float[] features;

	int numIndices;

	public float [] posteriors;
	public int [] positives;
	public int [] negatives;

	DetectionResult detectionResult;

	private Random rand;
	
	private int sub2idx(double row, double col, int height) {
		return ((int) (Math.floor((row)+0.5) + Math.floor((col)+0.5)*(height)));
	}

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

	//Creates offsets that can be added to bounding boxes
	//offsets are contained in the form delta11, delta12,... (combined index of dw and dh)
	//Order: scale.tree->feature
	void initFeatureOffsets() {

		featureOffsets= new int[numScales*numTrees*numFeatures*2][2];
//		int *off = featureOffsets;
		int offIndex = 0;

		for (int k = 0; k < numScales; k++){
			Rectangle scale = scales[k];
			for (int i = 0; i < numTrees; i++) {
				for (int j = 0; j < numFeatures; j++) {
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

	public void nextIteration(FImage img) {
		if(!enabled) return;

		this.img = img.pixels;
	}

	//Classical fern algorithm
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

	void calcFeatureVector(int windowIdx, int[] featureVector, int featureVectorIndex) {
	    for(int i = 0; i < numTrees; i++) {
	    	featureVector[featureVectorIndex + i] = calcFernFeature(windowIdx, i);
		}
	}

	float calcConfidence(int [] featureVector, int featureVectorIndex) {
		float conf = 0.0f;

		for(int i = 0; i < numTrees; i++) {
			conf += posteriors[i * numIndices + featureVector[featureVectorIndex + i]];
		}

		return conf;
	}

	void classifyWindow(int windowIdx) {
//		int* featureVector = detectionResult->featureVectors + numTrees * windowIdx;
		int featureVectorIndex = numTrees * windowIdx;
		calcFeatureVector(windowIdx, detectionResult.featureVectors, featureVectorIndex);

		detectionResult.posteriors[windowIdx] = calcConfidence(detectionResult.featureVectors, featureVectorIndex);
	}

	boolean filter(int i)  {
		if(!enabled) return true;

		classifyWindow(i);
		if(detectionResult.posteriors[i] < 0.5) return false;
		return true;
	}

	void updatePosterior(int treeIdx, int idx, boolean positive, int amount) {
		int arrayIndex = treeIdx * numIndices + idx;
		if(positive)
		{
			positives[arrayIndex] += amount;
		}
		else
		{
			negatives[arrayIndex] += amount;
		}
		posteriors[arrayIndex] = ((float) positives[arrayIndex]) / (positives[arrayIndex] + negatives[arrayIndex]) / 10.0f;
	}

	void updatePosteriors(int []featureVector,int featureIndex, boolean positive, int amount) {

		for (int i = 0; i < numTrees; i++) {

			int idx = featureVector[featureIndex+i];
			updatePosterior(i, idx, positive, amount);

		}
	}

	void learn(FImage img, boolean positive, int [] featureVector, int featureIndex) {
	    if(!enabled) return;

		float conf = calcConfidence(featureVector,featureIndex);

	    //Update if positive patch and confidence < 0.5 or negative and conf > 0.5
	    if((positive && conf < 0.5) || (!positive && conf > 0.5)) {
	    	updatePosteriors(featureVector, featureIndex, positive,1);
	    }

	}

}
