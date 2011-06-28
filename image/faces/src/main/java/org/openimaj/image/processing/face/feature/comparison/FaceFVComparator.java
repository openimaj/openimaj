package org.openimaj.image.processing.face.feature.comparison;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.processing.face.feature.FacialFeature;

public class FaceFVComparator<T extends FacialFeature & FeatureVectorProvider<FloatFV>> implements FacialFeatureComparator<T> {
	FloatFVComparison comp;
	
	public FaceFVComparator() {
		comp = FloatFVComparison.EUCLIDEAN;
	}
	
	public FaceFVComparator(FloatFVComparison comp) {
		this.comp = comp;
	}
	
	@Override
	public double compare(T query, T target) {
		return comp.compare(query.getFeatureVector(), target.getFeatureVector());
	}

	@Override
	public boolean isAscending() {
		FloatFV f1 = new FloatFV(new float[]{ 1, 0 });
		FloatFV f2 = new FloatFV(new float[]{ 0, 1 });
		
		return comp.compare(f1, f1) < comp.compare(f1, f2);
	}

}
