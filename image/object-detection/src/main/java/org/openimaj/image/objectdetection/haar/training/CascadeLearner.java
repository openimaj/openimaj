package org.openimaj.image.objectdetection.haar.training;

import org.openimaj.image.objectdetection.haar.StageTreeClassifier;

public class CascadeLearner {
	float maximumFPRPerStage;
	float minimumDRPerStage;
	float targetFPR;

	StageTreeClassifier learn() {
		final float overallFPR = 1.0f;
		final float overallDR = 1.0f;

		float previousFPR = overallFPR;
		final float previousDR = overallDR;
		for (int i = 0; overallFPR > targetFPR; i++) {

			for (int n = 0; overallFPR > maximumFPRPerStage * previousFPR; n++) {
				// perform adaboost step

				// evaluate on validation set (compute overallFPR and overallDR)

				// decrease current stage threshold to achieve overallDR >=
				// minimumDRPerStage*previousDR
				// (recompute overallFPR at the same time)
			}

			previousFPR = overallFPR;
		}

		return null;
	}
}
