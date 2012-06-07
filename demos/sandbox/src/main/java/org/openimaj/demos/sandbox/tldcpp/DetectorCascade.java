package org.openimaj.demos.sandbox.tldcpp;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;

public class DetectorCascade {
	public static final int TLD_WINDOW_SIZE = 5;
	public static final int TLD_WINDOW_OFFSET_SIZE = 6;
	
	
	private int numScales;
	private Rectangle[] scales;
	//Configurable members
	public int minScale;
	public int maxScale;
	public boolean useShift;
	public float shift;
	public int minSize;
	public int numFeatures;
	public int numTrees;

	//Needed for init
	public int imgWidth;
	public int imgHeight;
	public int imgWidthStep;
	public int objWidth;
	public int objHeight;

	public int numWindows;
	public ScaleIndexRectangle[] windows;
	public int[][] windowOffsets; // CONCENTRATE. entries: [[x1-1,y1-1],[x1-1,y2],[x2,y1-1],[x2,y2], [featuresForScaleIndex], [areaOfBoundBox]
	
	ForegroundDetector foregroundDetector;
	VarianceFilter varianceFilter;
	EnsembleClassifier ensembleClassifier;
	Clustering clustering;
	NNClassifier nnClassifier;

	DetectionResult detectionResult;

	//State data
	public boolean initialised;

	public DetectorCascade() {
		objWidth = -1; //MUST be set before calling init
		objHeight = -1; //MUST be set before calling init
		useShift = true;
		imgHeight = -1;
		imgWidth = -1;

		shift=0.1f;
		minScale=-10;
		maxScale=10;
		minSize = 25;
		imgWidthStep = -1;

		numTrees = 13;
		numFeatures = 10;

		initialised = false;

		foregroundDetector = new ForegroundDetector();
		varianceFilter = new VarianceFilter();
		ensembleClassifier = new EnsembleClassifier();
		nnClassifier = new NNClassifier();
		clustering = new Clustering();

		detectionResult = new DetectionResult();
	}

	public void release() {
		if(!initialised) {
			return; //Do nothing
		}

		initialised = false;

		foregroundDetector.release();
		ensembleClassifier.release();
		nnClassifier.release();
		
		clustering.release();

		numWindows = 0;
		numScales = 0;

		scales = null;
		windows = null;
		windowOffsets = null;

		objWidth = -1;
		objHeight = -1;

		detectionResult.release();
	}

	public void cleanPreviousData() {
		// TODO Auto-generated method stub
		
	}

	public void init() throws Exception {
		if(imgWidth == -1 || imgHeight == -1 || objWidth == -1 || objHeight == -1) {
			throw new Exception("The image or object dimentions were not set");
		}

		initWindowsAndScales();
		initWindowOffsets();

		propagateMembers();

		ensembleClassifier.init();

		initialised = true;
	}

	private void propagateMembers() {
		detectionResult.init(numWindows, numTrees);

		varianceFilter.windowOffsets = windowOffsets;
		ensembleClassifier.windowOffsets = windowOffsets;
		ensembleClassifier.imgWidthStep = imgWidthStep;
		ensembleClassifier.numScales = numScales;
		ensembleClassifier.scales = scales;
		ensembleClassifier.numFeatures = numFeatures;
		ensembleClassifier.numTrees = numTrees;
		nnClassifier.windows = windows;
		clustering.windows = windows;
		clustering.numWindows = numWindows;

		foregroundDetector.minBlobSize = minSize*minSize;

		foregroundDetector.detectionResult = detectionResult;
		varianceFilter.detectionResult = detectionResult;
		ensembleClassifier.detectionResult = detectionResult;
		nnClassifier.detectionResult = detectionResult;
		clustering.detectionResult = detectionResult;
	}

	private void initWindowOffsets() {
		windowOffsets = new int[TLD_WINDOW_OFFSET_SIZE*numWindows][];
		int offIndex = 0;

//		int windowSize = TLD_WINDOW_SIZE;

		for (int i = 0; i < numWindows; i++) {
			ScaleIndexRectangle windowRect = windows[i];
			int x = (int)windowRect.x;
			int y = (int)windowRect.y;
			int width = (int)windowRect.width;
			int height = (int)windowRect.height;
			int scaleIndex = windowRect.scaleIndex;
			windowOffsets[offIndex++] = new int[]{x-1,y-1}; //sub2idx(window[0]-1,window[1]-1,imgWidthStep); // x1-1,y1-1
			windowOffsets[offIndex++] = new int[]{x-1,y+height-1}; // x1-1,y2
			windowOffsets[offIndex++] = new int[]{x+width-1,y-1}; // x2,y1-1
			windowOffsets[offIndex++] = new int[]{x+width-1,y+height-1}; // x2,y2
			windowOffsets[offIndex++] = new int[]{scaleIndex*2*numFeatures*numTrees}; // pointer to features for this scale
			windowOffsets[offIndex++] = new int[]{width*height};//Area of bounding box
		}
	}

	private void initWindowsAndScales() {
		int scanAreaX = 1; // It is important to start with 1/1, because the integral images aren't defined at pos(-1,-1) due to speed reasons
		int scanAreaY = 1;
		int scanAreaW = imgWidth-1;
		int scanAreaH = imgHeight-1;

		int windowIndex = 0;

	    scales = new Rectangle[maxScale-minScale+1];

		numWindows = 0;

		int scaleIndex = 0;
		for(int i = minScale; i <= maxScale; i++) {
			float scale = (float) Math.pow(1.2,i);
			int w = (int)(objWidth*scale);
			int h = (int)(objHeight*scale);
			int ssw,ssh;
			if(useShift) {
				ssw = (int) Math.max(1,w*shift);
				ssh = (int) Math.max(1,h*shift);
			} else {
				ssw = 1;
				ssh = 1;
			}

			if(w < minSize || h < minSize || w > scanAreaW || h > scanAreaH) continue;
			scales[scaleIndex] = new Rectangle(0,0,w,h);

			scaleIndex++;

			numWindows += Math.floor((float)(scanAreaW - w + ssw)/ssw)*Math.floor((float)(scanAreaH - h + ssh) / ssh);
		}

		numScales = scaleIndex;

		windows = new ScaleIndexRectangle[numWindows];

		for(scaleIndex = 0; scaleIndex < numScales; scaleIndex++) {
			int w = (int) scales[scaleIndex].width;
			int h = (int) scales[scaleIndex].height;

			int ssw,ssh;
			if(useShift) {
				ssw = (int) Math.max(1,w*shift);
				ssh = (int) Math.max(1,h*shift);
			} else {
				ssw = 1;
				ssh = 1;
			}

			for(int y = scanAreaY; y + h <= scanAreaY +scanAreaH; y+=ssh) {
				for(int x = scanAreaX; x + w <= scanAreaX + scanAreaW; x+=ssw) {
					int bb = windowIndex;
					windows[bb] = new ScaleIndexRectangle();
					windows[bb].x = x;
					windows[bb].y = y;
					windows[bb].width = w;
					windows[bb].height = h;
					windows[bb].scaleIndex = scaleIndex;

					windowIndex++;
				}
			}

		}

		assert(windowIndex == numWindows);
	}

	public void detect(FImage img) {
		//For every bounding box, the output is confidence, pattern, variance

		detectionResult.reset();

		if(!initialised) {
			return;
		}

		//Prepare components
		// Forget the foreground detector for now, this is an optimisation
//		foregroundDetector.nextIteration(img); //Calculates foreground
		varianceFilter.nextIteration(img); //Calculates integral images
		ensembleClassifier.nextIteration(img);
		
//		Rectangle windowRect = new Rectangle();
		int varCount = 0,ensCount = 0,nnClassCount = 0;
		for (int i = 0; i < numWindows; i++) {

//			int * window = &windows[TLD_WINDOW_SIZE*i];
//			int window = i;
//			windowRect = windows[window];
//			if(foregroundDetector.isActive()) {
//				boolean isInside = false;
//
//				for (Rectangle rect : this.detectionResult.fgList) {
//
////					int bgBox[4];
////					tldRectToArray(detectionResult->fgList->at(j), bgBox);
////					if(tldIsInside(window,bgBox)) { //TODO: This is inefficient and should be replaced by a quadtree
////						isInside = true;
////					}
//					if(windowRect.isInside(rect)){
//						isInside = true;
//						break;
//					}
//				}
//
//				if(!isInside) {
//					detectionResult.posteriors[i] = 0;
//					continue;
//				}
//			}
			
			if(!varianceFilter.filter(i)) {
				detectionResult.posteriors[i] = 0;
				varCount++;
				continue;
			}

			if(!ensembleClassifier.filter(i)) {
				ensCount++;
				continue;
			}

			if(!nnClassifier.filter(img, i)) {
				nnClassCount++;
				continue;
			}

			detectionResult.confidentIndices.add(i);


		}
		System.out.println("Counts: " + varCount + ", " + ensCount + ", " + nnClassCount);
		//Cluster
		clustering.clusterConfidentIndices();

		detectionResult.containsValidData = true;
	}
}
