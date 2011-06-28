package org.openimaj.image.processing.face.feature.comparison;

import org.openimaj.image.processing.face.feature.FacialFeature;

public interface FacialFeatureComparator<T extends FacialFeature> {
	/**
	 * Compare this feature against a the given feature and return
	 * a score.
	 * @param feature the feature
	 * @return the score for the match. 
	 */
	public abstract double compare(T query, T target);
	
	/**
	 * Is the score returned by {@link #compare(Object, Object)} 
	 * ascending or descending? Ascending means that smaller numbers
	 * imply a "better" match.
	 *  
	 * @return true if ascending; false otherwise.
	 */
	public boolean isAscending();
}
