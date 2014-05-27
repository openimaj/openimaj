package org.openimaj.demos.sandbox.tldcpp.detector;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * The third and most powerful, but equally most slow parts of the
 * {@link DetectorCascade}. Holding a list of falsePositives and truePositives,
 * a classification score can be ascribed to a new patch which can be used as a
 * confidence that a given patch is positive. This is calculated using the
 * correlation between the new patch and the false positive and falst negatives
 * such that:
 * 
 * confidence = dP / (dN + dP)
 * 
 * and dP = max(corr(patch,truePositives)) dP = max(corr(patch,falsePositives))
 * 
 * if no true positives have been seen, classify will always return 0 if not
 * false positives have been seen, classify will always return 1
 * 
 * classify is used by filter such that if the confidence of a patch is larger
 * than {@link #thetaTP} the patch is though to be a good patch for the object.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class NNClassifier {
	/**
	 * whether this stage is enabled
	 */
	public boolean enabled;

	/**
	 * Used as the lower bound of a historysis threshold (i.e. if a detection is
	 * made with a confidence over {@link #thetaTP}, the next detection can be a
	 * little worse by matching this)
	 */
	public float thetaFP;
	/**
	 * Used as the upper bound threshold
	 */
	public float thetaTP;
	ScaleIndexRectangle[] windows;
	DetectionResult detectionResult;
	List<NormalizedPatch> falsePositives;
	List<NormalizedPatch> truePositives;

	/**
	 * Sets thetaFP as 0.5f and thetaTP as .65f
	 */
	public NNClassifier() {
		thetaFP = .5f;
		thetaTP = .65f;

		truePositives = new ArrayList<NormalizedPatch>();
		falsePositives = new ArrayList<NormalizedPatch>();

	}

	/**
	 * clear falst positives and true positives
	 */
	public void release() {
		falsePositives.clear();
		truePositives.clear();
	}

	/**
	 * 
	 * @param f1
	 * @param f2
	 * @return correlation between two patches (assumed to be the same size)
	 *         calculated using {@link TemplateMatcherMode}
	 */
	private float ncc(FImage f1, FImage f2) {
		final float normcorr = TemplateMatcher.Mode.NORM_CORRELATION.computeMatchScore(f1.pixels, 0, 0, f2.pixels, 0, 0,
				f1.width, f1.height);
		return normcorr;
	}

	/**
	 * 
	 * @param patch
	 * @return The confidence that a given patch is a postive
	 */
	public float classifyPatch(NormalizedPatch patch) {

		if (truePositives.isEmpty()) {
			return 0;
		}

		if (falsePositives.isEmpty()) {
			return 1;
		}

		float ccorr_max_p = 0;
		// Compare patch to positive patches
		for (int i = 0; i < truePositives.size(); i++) {
			final float ccorr = ncc(truePositives.get(i).normalisedPatch, patch.normalisedPatch);
			if (ccorr > ccorr_max_p) {
				ccorr_max_p = ccorr;
			}
		}

		float ccorr_max_n = 0;
		// Compare patch to positive patches
		for (int i = 0; i < falsePositives.size(); i++) {
			final float ccorr = ncc(falsePositives.get(i).normalisedPatch, patch.normalisedPatch);
			if (ccorr > ccorr_max_n) {
				ccorr_max_n = ccorr;
			}
		}

		final float dN = 1 - ccorr_max_n;
		final float dP = 1 - ccorr_max_p;

		final float distance = dN / (dN + dP);
		return distance;
	}

	/**
	 * @param img
	 * @param bb
	 * @return confidence of the bb in image
	 */
	public float classifyBB(FImage img, Rectangle bb) {
		final NormalizedPatch patch = new NormalizedPatch();
		patch.source = img;
		patch.window = bb;
		patch.prepareNormalisedPatch();
		return classifyPatch(patch);

	}

	float classifyWindow(FImage img, int windowIdx) {

		final ScaleIndexRectangle bbox = windows[windowIdx];
		final NormalizedPatch patch = new NormalizedPatch();
		patch.window = bbox;
		patch.source = img;
		// here we reuse the scales images as the patch of the right
		// width/height and just write into it.
		patch.normalisedPatch = patch.zoomAndNormaliseTo(NormalizedPatch.SLUT_WORKSPACE);

		return classifyPatch(patch);
	}

	/**
	 * @param img
	 * @param windowIdx
	 * @return Filter a window by getting its confidence and returning true if
	 *         confidence > thetaTP
	 */
	public boolean filter(FImage img, int windowIdx) {
		if (!enabled)
			return true;

		final float conf = classifyWindow(img, windowIdx);

		if (conf < thetaTP) {
			return false;
		}

		return true;
	}

	/**
	 * Given a list of patches, classify each patch. If the patch is said to be
	 * positive and has a confidence lower than {@link #thetaTP} add the patch
	 * to the true positives If the patch is said to be negative and has a
	 * confidence higher than {@link #thetaFP} add the patch to the false
	 * positives
	 * 
	 * @param patches
	 */
	public void learn(List<NormalizedPatch> patches) {
		// TODO: Randomization might be a good idea here
		for (int i = 0; i < patches.size(); i++) {

			final NormalizedPatch patch = patches.get(i);
			// if the patch is a negative one, the image has not been normalised
			// etc yet!
			// it uses the prepared windows, so a held scale patch can be used
			if (!patch.positive) {
				patch.normalisedPatch = patch.zoomAndNormaliseTo(NormalizedPatch.SLUT_WORKSPACE);
			}

			final float conf = classifyPatch(patch);

			if (patch.positive && conf <= thetaTP) {
				truePositives.add(patch);
			}

			if (!patch.positive && conf >= thetaFP) {
				// We must handle the SLUT_WORKSPACE!
				// If we're negative we are using the slut, if we're planning to
				// keep this negative we must NOW clone the slut
				patch.normalisedPatch = patch.normalisedPatch.clone();
				falsePositives.add(patch);
			}
		}

	}

	/**
	 * @return the positively classified patches
	 */
	public List<NormalizedPatch> getPositivePatches() {
		return this.truePositives;
	}

}
