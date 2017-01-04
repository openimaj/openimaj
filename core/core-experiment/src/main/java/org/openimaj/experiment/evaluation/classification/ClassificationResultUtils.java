package org.openimaj.experiment.evaluation.classification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openimaj.util.pair.ObjectDoublePair;

/**
 * Utility methods for working with {@link ClassificationResult}s
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class ClassificationResultUtils {

	private ClassificationResultUtils() {
	}

	/**
	 * Get the class with the highest confidence
	 *
	 * @param result
	 *            the {@link ClassificationResult} to work with
	 * @return the class with the highest confidence
	 */
	public static <CLASS> CLASS getHighestConfidenceClass(ClassificationResult<CLASS> result) {
		CLASS bestClass = null;
		double bestConfidence = 0;
		for (final CLASS s : result.getPredictedClasses())
		{
			if (result.getConfidence(s) >= bestConfidence)
			{
				bestClass = s;
				bestConfidence = result.getConfidence(s);
			}
		}

		return bestClass;
	}

	/**
	 * Get the all classes and confidences, sorted by decreasing confidence
	 *
	 * @param result
	 *            the {@link ClassificationResult} to work with
	 * @return the sorted classes and confidences
	 */
	public static <CLASS>
			List<ObjectDoublePair<CLASS>>
			getSortedClassesAndConfidences(ClassificationResult<CLASS> result)
	{
		final List<ObjectDoublePair<CLASS>> list = new ArrayList<>();

		for (final CLASS clz : result.getPredictedClasses()) {
			list.add(new ObjectDoublePair<>(clz, result.getConfidence(clz)));
		}

		Collections.sort(list, new Comparator<ObjectDoublePair<CLASS>>() {
			@Override
			public int compare(ObjectDoublePair<CLASS> o1, ObjectDoublePair<CLASS> o2) {
				return -1 * Double.compare(o1.second, o2.second);
			}
		});

		return list;
	}
}
