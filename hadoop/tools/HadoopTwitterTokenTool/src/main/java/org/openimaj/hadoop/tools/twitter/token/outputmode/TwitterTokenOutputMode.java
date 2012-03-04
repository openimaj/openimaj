package org.openimaj.hadoop.tools.twitter.token.outputmode;

import java.io.Writer;
import java.util.List;

import org.openimaj.util.pair.IndependentPair;

/**
 * @author ss
 *
 */
public interface TwitterTokenOutputMode {
	/**
	 * Accept a vector. The vector may be sparse (every feature might not be present)
	 * @param vectorName the vector's name
	 * @param featureValues a list of feature:value pairs
	 */
	public abstract void acceptVect(String vectorName, List<IndependentPair<String,Double>> featureValues);
	/**
	 * Accept a feature. The feature may be sparse (every vector might not be present)
	 * @param featureName the feature's name
	 * @param timeIDF a list of vector:value pairs
	 */
	public abstract void acceptFeat(String featureName, List<IndependentPair<String, Double>> timeIDF);
	
	/**
	 * @param output where the output is going
	 */
	public void write(Writer output);
}
