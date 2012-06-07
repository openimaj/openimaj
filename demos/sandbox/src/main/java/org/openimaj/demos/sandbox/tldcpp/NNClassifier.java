package org.openimaj.demos.sandbox.tldcpp;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher.TemplateMatcherMode;
import org.openimaj.math.geometry.shape.Rectangle;

public class NNClassifier {
	public boolean enabled;

	public ScaleIndexRectangle[] windows;
	public float thetaFP;
	public float thetaTP;
	public DetectionResult detectionResult;
	public List<NormalizedPatch> falsePositives;
	public List<NormalizedPatch> truePositives;
	
	public NNClassifier() {
		thetaFP = .5f;
		thetaTP = .65f;

		truePositives = new ArrayList<NormalizedPatch>();
		falsePositives = new ArrayList<NormalizedPatch>();

	}


	public void release() {
		falsePositives.clear();
		truePositives.clear();
	}

	public float ncc(FImage f1, FImage f2) {
		float normcorr = TemplateMatcherMode.NORM_CORRELATION.computeMatchScore(f1.pixels, 0, 0, f2.pixels, 0, 0, f1.width, f1.height);
		return normcorr;
	}

	public float classifyPatch(NormalizedPatch patch) {

		if(truePositives.isEmpty()) {
			return 0;
		}

		if(falsePositives.isEmpty()) {
			return 1;
		}

		float ccorr_max_p = 0;
		//Compare patch to positive patches
		for(int i = 0; i < truePositives.size(); i++) {
			float ccorr = ncc(truePositives.get(i).normalisedPatch, patch.normalisedPatch);
			if(ccorr > ccorr_max_p) {
				ccorr_max_p = ccorr;
			}
		}

		float ccorr_max_n = 0;
		//Compare patch to positive patches
		for(int i = 0; i < falsePositives.size(); i++) {
			float ccorr = ncc(falsePositives.get(i).normalisedPatch, patch.normalisedPatch);
			if(ccorr > ccorr_max_n) {
				ccorr_max_n = ccorr;
			}
		}

		float dN = 1-ccorr_max_n;
		float dP = 1-ccorr_max_p;

		float distance = dN/(dN+dP);
		return distance;
	}

	// FIXME: This is going to be FUCKING slow, extracts patch the classifys, stupid! 
	public float classifyBB(FImage img, Rectangle bb) {
		NormalizedPatch patch = new NormalizedPatch();
		patch.source = img;
		patch.window = bb;
		patch.prepareNormalisedPatch();
		return classifyPatch(patch);

	}

	public float classifyWindow(FImage img, int windowIdx) {

		ScaleIndexRectangle bbox = windows[windowIdx];
		NormalizedPatch patch = new NormalizedPatch();
		patch.window = bbox;
		patch.source = img;
		// here we reuse the scales images as the patch of the right width/height and just write into it.
		patch.normalisedPatch = patch.zoomAndNormaliseTo(NormalizedPatch.SLUT_WORKSPACE);

		return classifyPatch(patch);
	}

	public boolean filter(FImage img, int windowIdx) {
		if(!enabled) return true;

		float conf = classifyWindow(img, windowIdx);

		if(conf < thetaTP) {
			return false;
		}

		return true;
	}

	void learn(List<NormalizedPatch> patches) {
		//TODO: Randomization might be a good idea here
		for(int i = 0; i < patches.size(); i++) {

			NormalizedPatch patch = patches.get(i);
			// if the patch is a negative one, the image has not been normalised etc yet!
			// it uses the prepared windows, so a held scale patch can be used
			if(!patch.positive){
				patch.normalisedPatch = patch.zoomAndNormaliseTo(NormalizedPatch.SLUT_WORKSPACE);
			}

			float conf = classifyPatch(patch);

			if(patch.positive && conf <= thetaTP) {
				truePositives.add(patch);
			}

			if(!patch.positive && conf >= thetaFP) {
				// We must handle the SLUT_WORKSPACE!
				// If we're negative we are using the slut, if we're planning to keep this negative we must NOW clone the slut
				patch.normalisedPatch = patch.normalisedPatch.clone();
				falsePositives.add(patch);
			}
		}

	}


}
