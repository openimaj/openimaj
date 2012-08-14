package org.openimaj.experiment.evaluation.classification;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.util.pair.ObjectDoublePair;

/**
 * A basic implementation of a {@link ClassificationResult} that internally
 * maintains a map of classes to confidences.
 * <p>
 * A threshold is used to determine whether a class has a high-enough confidence
 * to be considered part of the predicted set of classes.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <CLASS>
 *            type of class predicted by the {@link Classifier}
 */
public class BasicClassificationResult<CLASS> implements ClassificationResult<CLASS> {
	private final TObjectDoubleHashMap<CLASS> data = new TObjectDoubleHashMap<CLASS>();
	private double threshold = 0.5;

	/**
	 * Construct with a default threshold of 0.5.
	 */
	public BasicClassificationResult() {

	}

	/**
	 * Construct with the given threshold.
	 * 
	 * @param threshold
	 *            the threshold
	 */
	public BasicClassificationResult(double threshold) {
		this.threshold = 0.5;
	}

	/**
	 * Add a class/confidence pair.
	 * 
	 * @param clz
	 *            the class
	 * @param confidence
	 *            the confidence
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
		// predicted classes are sorted by decreasing confidence

		final List<ObjectDoublePair<CLASS>> toSort = new ArrayList<ObjectDoublePair<CLASS>>();

		data.forEachEntry(new TObjectDoubleProcedure<CLASS>() {
			@Override
			public boolean execute(CLASS key, double confidence) {
				if (confidence > threshold)
					toSort.add(new ObjectDoublePair<CLASS>(key, confidence));
				return true;
			}
		});

		Collections.sort(toSort, new Comparator<ObjectDoublePair<CLASS>>() {
			@Override
			public int compare(ObjectDoublePair<CLASS> o1, ObjectDoublePair<CLASS> o2) {
				return -1 * Double.compare(o1.second, o2.second);
			}
		});

		final Set<CLASS> keys = new LinkedHashSet<CLASS>(toSort.size());

		for (final ObjectDoublePair<CLASS> p : toSort)
			keys.add(p.first);

		return keys;
	}
}
