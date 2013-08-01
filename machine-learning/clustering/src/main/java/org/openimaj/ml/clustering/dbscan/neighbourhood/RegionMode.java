package org.openimaj.ml.clustering.dbscan.neighbourhood;

import java.util.List;

/**
 * {@link RegionMode} instances can provide Neighbours of the n'th data
 * point given all the data points
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <PAIRTYPE>
 */
public interface RegionMode<PAIRTYPE>{
	/**
	 * @param index
	 * @return a list of neighbours
	 */
	public List<PAIRTYPE> regionQuery(int index);
	
	/**
	 * @return whether the provided region is valid
	 */
	public boolean validRegion(List<PAIRTYPE> region);
}