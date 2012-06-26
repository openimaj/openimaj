package org.openimaj.experiment.evaluation.classification;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;

import java.util.HashSet;
import java.util.Set;

/**
 * A basic implementation of a {@link ClassificationResult} that
 * internally maintains a map of classes to confidences.
 * <p>
 * A threshold is used to determine whether a class has a high-enough
 * confidence to be considered part of the predicted set of
 * classes.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <CLASS> type of class predicted by the {@link Classifier}
 */
public class BasicClassificationResult<CLASS> implements ClassificationResult<CLASS> {
	private TObjectDoubleHashMap<CLASS> data = new TObjectDoubleHashMap<CLASS>();
	private double threshold = 0.5;
	
	/**
	 * Construct with a default threshold of 0.5. 
	 */
	public BasicClassificationResult() {
		
	}
	
	/**
	 * Construct with the given threshold. 
	 * @param threshold the threshold
	 */
	public BasicClassificationResult(double threshold) {
		this.threshold = 0.5;
	}
	
	/**
	 * Add a class/confidence pair.
	 * 
	 * @param clz the class
	 * @param confidence the confidence
	 */
	public void put(CLASS clz, double confidence) {
		data.put(clz, confidence);
	}
	
	@Override
	public double getConfidence(CLASS clazz) {
		return data.get(clazz);
	}
	
	@Override
	public Set<CLASS> getPredictedClasses() {
		final Set<CLASS> keys = new HashSet<CLASS>();
		
		data.forEachEntry(new TObjectDoubleProcedure<CLASS>() {
			@Override
			public boolean execute(CLASS key, double confidence) {
				if (confidence > threshold) keys.add(key);
				return true;
			}
		});
		
		return keys;
	}
}
