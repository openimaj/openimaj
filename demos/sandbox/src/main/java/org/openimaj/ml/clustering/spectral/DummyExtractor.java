package org.openimaj.ml.clustering.spectral;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DummyExtractor implements FeatureExtractor<DoubleFV, double[]>{

	@Override
	public DoubleFV extractFeature(double[] object) {
		return new DoubleFV(object);
	}

}
