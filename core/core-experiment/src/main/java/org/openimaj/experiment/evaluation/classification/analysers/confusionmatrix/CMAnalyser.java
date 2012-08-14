package org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix;

import gov.sandia.cognition.learning.data.DefaultTargetEstimatePair;
import gov.sandia.cognition.learning.data.TargetEstimatePair;
import gov.sandia.cognition.learning.performance.categorization.ConfusionMatrixPerformanceEvaluator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.experiment.evaluation.classification.ClassificationAnalyser;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.Classifier;

/**
 * A {@link ClassificationAnalyser} that creates Confusion Matrices.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <CLASS>
 *            The type of classes produced by the {@link Classifier}
 * @param <OBJECT>
 *            The type of object classifed by the {@link Classifier}
 */
public class CMAnalyser<OBJECT, CLASS>
		implements ClassificationAnalyser<
		CMResult<CLASS>,
		CLASS,
		OBJECT>
{
	/**
	 * Strategies for building confusion matrices
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static enum Strategy {
		/**
		 * Strategy to use when there is exactly one actual class and one
		 * predicted class.
		 */
		SINGLE {
			@Override
			protected <CLASS> void add(
					List<TargetEstimatePair<CLASS, CLASS>> data,
					Set<CLASS> predicted, Set<CLASS> actual)
			{
				data.add(DefaultTargetEstimatePair.create(
						actual.size() == 0 ? null : new ArrayList<CLASS>(actual).get(0),
						predicted.size() == 0 ? null : new ArrayList<CLASS>(predicted).get(0)
						));
			}
		},
		/**
		 * Strategy for multiple possible actual classes and predicted classes.
		 * Deals with:
		 * <ol>
		 * <li>true positives (a class present in both the predicted and actual
		 * set</li>
		 * <li>false positives (a predicted class not being in the actual set)</li>
		 * <li>false negatives (an actual class not being in the predicted set)</li>
		 * </ol>
		 * False positives and negatives are dealt with by using
		 * <code>null</code> values for the actual/predicted class respectively.
		 */
		MULTIPLE {
			@Override
			protected <CLASS> void add(
					List<TargetEstimatePair<CLASS, CLASS>> data,
					Set<CLASS> predicted, Set<CLASS> actual)
			{
				final HashSet<CLASS> allClasses = new HashSet<CLASS>();
				allClasses.addAll(predicted);
				allClasses.addAll(actual);

				for (final CLASS clz : allClasses) {
					final CLASS target = actual.contains(clz) ? clz : null;
					final CLASS estimate = predicted.contains(clz) ? clz : null;

					data.add(DefaultTargetEstimatePair.create(target, estimate));
				}
			}
		},
		/**
		 * Strategy for multiple possible actual classes and predicted classes
		 * in the case the predictions and actual classes are ordered and there
		 * is a one-to-one correspondence.
		 * <p>
		 * A {@link RuntimeException} will be thrown if the sets are not the
		 * same size and both instances of {@link LinkedHashSet}.
		 */
		MULTIPLE_ORDERED {
			@SuppressWarnings("unchecked")
			@Override
			protected <CLASS> void add(
					List<TargetEstimatePair<CLASS, CLASS>> data,
					Set<CLASS> predicted, Set<CLASS> actual)
			{
				final LinkedHashSet<CLASS> op = (LinkedHashSet<CLASS>) predicted;
				final LinkedHashSet<CLASS> ap = (LinkedHashSet<CLASS>) actual;

				if (op.size() != ap.size())
					throw new RuntimeException("Sets are not the same size!");

				final Object[] opa = op.toArray();
				final Object[] apa = ap.toArray();

				for (int i = 0; i < opa.length; i++)
					data.add(new DefaultTargetEstimatePair<CLASS, CLASS>((CLASS) opa[i], (CLASS) apa[i]));
			}
		};

		protected abstract <CLASS> void add(List<TargetEstimatePair<CLASS, CLASS>> data, Set<CLASS> predicted,
				Set<CLASS> actual);
	}

	protected Strategy strategy;
	ConfusionMatrixPerformanceEvaluator<?, CLASS> eval = new ConfusionMatrixPerformanceEvaluator<Object, CLASS>();

	/**
	 * Construct with the given strategy for building the confusion matrix
	 * 
	 * @param strategy
	 *            the strategy
	 */
	public CMAnalyser(Strategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public CMResult<CLASS> analyse(
			Map<OBJECT, ClassificationResult<CLASS>> predicted,
			Map<OBJECT, Set<CLASS>> actual)
	{
		final List<TargetEstimatePair<CLASS, CLASS>> data = new ArrayList<TargetEstimatePair<CLASS, CLASS>>();

		for (final OBJECT obj : predicted.keySet()) {
			final Set<CLASS> pclasses = predicted.get(obj).getPredictedClasses();
			final Set<CLASS> aclasses = actual.get(obj);

			strategy.add(data, pclasses, aclasses);
		}

		return new CMResult<CLASS>(eval.evaluatePerformance(data));
	}
}
