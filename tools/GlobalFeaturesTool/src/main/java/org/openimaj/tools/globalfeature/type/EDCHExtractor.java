package org.openimaj.tools.globalfeature.type;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.EdgeDirectionCoherenceVector;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;

/**
 * EDCH
 * @see EdgeDirectionCoherenceVector
 */
public class EDCHExtractor extends GlobalFeatureExtractor {
	@Override
	public FeatureVector extract(MBFImage image, FImage mask) {
		EdgeDirectionCoherenceVector cldo = new EdgeDirectionCoherenceVector();
		image.flatten().analyseWith(cldo);
		
		if (mask != null)
			System.err.println("Warning: EDGE_DIRECTION_COHERENCE_HISTOGRAM doesn't support masking");
		
		return cldo.getFeatureVector();
	}
}
