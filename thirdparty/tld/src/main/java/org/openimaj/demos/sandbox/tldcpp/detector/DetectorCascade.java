package org.openimaj.demos.sandbox.tldcpp.detector;

import org.openimaj.demos.sandbox.tldcpp.videotld.TLDUtil;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * The detector cascade prepares, inititates and controls the 3 underlying
 * detection steps in the TLD algorithm. Each step is more accurate than the last,
 * and is also more costly. The name of the game of this algorithm is to stop as soon
 * as possible. If a permissive but fast classifier says you are not correct, then odds
 * are you are DEFINITELY not correct.
 * 
 * The first step is a Variance check. If the variance at least equal to the variance detected in the patch.
 * This is done using {@link VarianceFilter} and uses integral images. very fast. but an easy check to pass
 * 
 * The second step is a {@link EnsembleClassifier}. This is more complicated but boils down to checking 
 * very few pixels of a patch against those same few pixels in previously seen correct patches and 
 * previously seen incorrect patches. Better than dumb variance, but also permissive
 * 
 * The final step is a {@link NNClassifier} which quite literally does a normalised correlation between the
 * patch and variance positive and negative examples. An excellent way to see if a patch is more similar to
 * correct things than incorrect things, but obviously massively slow so this is only done when the other two classifiers
 * are sure.
 * 
 * Generally the first two drop 26,000 patches and 30 or so are checked with normalised correlation. 
 * This is where TLD gets its detection speed.
 * 
 * The detector works across an overlapping grid of windows at different scales. These scales are controlled
 * by the size of the original box selected. The idea is that instead of checking arbitrary windows the grid windows
 * are checked. This means that you get checks across scales and x,y locations. The whole point is that you 
 * make quick decisions about not checking completely incorrect windows quickly.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DetectorCascade {
	/**
	 * The size to which TLD windows are reduced in order to be checked by {@link NNClassifier}
	 */
	public static final int TLD_WINDOW_SIZE = 5;
	static final int TLD_WINDOW_OFFSET_SIZE = 6;
	
	
	private int numScales;
	private Rectangle[] scales;
	//Configurable members
	/**
	 * The minimum scale factor to check as compared to the selected object dims.
	 */
	public int minScale;
	/**
	 * The maximum scale factor to check as compared to the selected object dims.
	 */
	public int maxScale;
	/**
	 * Whether a shift value should be applied to all scales
	 */
	public boolean useShift;
	/**
	 * The shift applied, 0.1f by default
	 */
	public float shift;
	/**
	 * The minimum window size, defaults to 25, a 5x5 pixel area. fair.
	 */
	public int minSize;
	
	/**
	 * The number of features per tree in the {@link EnsembleClassifier}
	 */
	public int numFeatures;
	/**
	 * The number of trees in the {@link EnsembleClassifier}
	 */
	public int numTrees;

	//Needed for init
	private int imgWidth;
	private int imgHeight;
	private int objWidth;
	private int objHeight;

	private int numWindows;
	private ScaleIndexRectangle[] windows;
	private int[][] windowOffsets; // CONCENTRATE. entries: [[x1-1,y1-1],[x1-1,y2],[x2,y1-1],[x2,y2], [featuresForScaleIndex], [areaOfBoundBox]
	
	VarianceFilter varianceFilter;
	EnsembleClassifier ensembleClassifier;
	Clustering clustering;
	NNClassifier nnClassifier;

	DetectionResult detectionResult;

	//State data
	private boolean initialised;

	/**
	 * Initialise the cascade and the underlying classifiers using the default values
	 */
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

		numTrees = 13;
		numFeatures = 10;

		initialised = false;

		varianceFilter = new VarianceFilter();
		ensembleClassifier = new EnsembleClassifier();
		nnClassifier = new NNClassifier();
		clustering = new Clustering();

		detectionResult = new DetectionResult();
	}

	/**
	 * Release all underlying classifiers and rest windows etc.
	 */
	public void release() {
		if(!initialised) {
			return; //Do nothing
		}

		initialised = false;

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

	/**
	 * initialise the cascade, prepare the windows and the classifiers
	 * @throws Exception
	 */
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
		ensembleClassifier.setWindowOffsets(windowOffsets);
		ensembleClassifier.setNumScales(numScales);
		ensembleClassifier.setScales(scales);
		ensembleClassifier.numFeatures = numFeatures;
		ensembleClassifier.numTrees = numTrees;
		nnClassifier.windows = windows;
		clustering.windows = windows;

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

	/**
	 * In their current state, apply each classifier to each window in order of 
	 * computational simplicity. i.e. variance, then ensembleclassifier then nnclassifier.
	 * 
	 * If any windows remain, call {@link Clustering} instance and cluster the selected windows.
	 * @param img 
	 */
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
//		System.out.println("Number of windows is: " + numWindows);
		detectionResult.varCount = 0;
		detectionResult.ensCount = 0;
		detectionResult.nnClassCount = 0;
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
				detectionResult.varCount++;
				continue;
			}

			if(!ensembleClassifier.filter(i)) {
				detectionResult.ensCount++;
				continue;
			}

			if(!nnClassifier.filter(img, i)) {
				detectionResult.nnClassCount++;
				continue;
			}

			detectionResult.confidentIndices.add(i);


		}
//		System.out.println("Counts: " + varCount + ", " + ensCount + ", " + nnClassCount);
		//Cluster
		clustering.clusterConfidentIndices();

		detectionResult.containsValidData = true;
	}

	/**
	 * FIXME? arguably this should change as the BB changes? would that be too slow?
	 * @param width sets the underlying scale windows in which to search based on factors of the original object detected
	 */
	public void setObjWidth(int width) {
		this.objWidth = width;
	}
	
	/**
	 * FIXME? arguably this should change as the BB changes? would that be too slow?
	 * @param height sets the underlying scale windows in which to search based on factors of the original object detected
	 */
	public void setObjHeight(int height) {
		this.objHeight = height;
	}

	/**
	 * resets the underlying {@link DetectionResult} instance
	 */
	public void cleanPreviousData() {
		this.detectionResult.reset();
	}

	/**
	 * @return total number of windows searching within
	 */
	public int getNumWindows() {
		return this.numWindows;
	}

	/**
	 * The overlap of a bounding box with each underlying window. An assumption is
	 * made that overlap is the same size {@link #getNumWindows()}
	 * @param bb
	 * @param overlap the output
	 */
	public void windowOverlap(Rectangle bb, float[] overlap) {
		TLDUtil.tldOverlap(windows, numWindows,bb, overlap);
		
	}

	/**
	 * @param idx
	 * @return the underlying {@link ScaleIndexRectangle} instance which is the idxth window
	 */
	public ScaleIndexRectangle getWindow(int idx) {
		return this.windows[idx];
	}

	/**
	 * @return whether the cascade has been correctly initialised (i.e. whether {@link #init()} has been called)
	 */
	public boolean isInitialised() {
		return initialised;
	}

	/**
	 * @param imgWidth the width of images to expect
	 */
	public void setImgWidth(int imgWidth) {
		this.imgWidth = imgWidth; 
	}
	
	/**
	 * @param imgHeight the height of images to expect
	 */
	public void setImgHeight(int imgHeight) {
		this.imgHeight = imgHeight; 
	}

	/**
	 * @return the underlying {@link NNClassifier} instance
	 */
	public NNClassifier getNNClassifier() {
		return this.nnClassifier;
	}

	/**
	 * @return the underlying {@link DetectionResult} instance
	 */
	public DetectionResult getDetectionResult() {
		return this.detectionResult;
	}

	/**
	 * @return the underlying {@link VarianceFilter} instance
	 */
	public VarianceFilter getVarianceFilter() {
		return this.varianceFilter;
	}

	/**
	 * @return the underlying {@link EnsembleClassifier} instance
	 */
	public EnsembleClassifier getEnsembleClassifier() {
		return this.ensembleClassifier;
	}
}
