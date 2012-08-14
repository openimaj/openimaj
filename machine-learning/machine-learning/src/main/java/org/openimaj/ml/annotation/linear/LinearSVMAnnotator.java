package org.openimaj.ml.annotation.linear;

import gov.sandia.cognition.learning.algorithm.svm.PrimalEstimatedSubGradient;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.learning.data.InputOutputPair;
import gov.sandia.cognition.learning.function.categorization.LinearBinaryCategorizer;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.math.matrix.Vectorizable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.Annotator;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.utils.AnnotatedListHelper;

/**
 * An {@link Annotator} based on a set of linear SVMs (one per annotation).
 * <p>
 * The SVMs use the PEGASOS algorithm implemented by the
 * {@link PrimalEstimatedSubGradient} class.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object being annotated
 * @param <ANNOTATION>
 *            Type of annotation
 * @param <EXTRACTOR>
 *            Type of feature extractor
 */
public class LinearSVMAnnotator<OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<? extends FeatureVector, OBJECT>>
		extends
		BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR>
{
	private final Map<ANNOTATION, LinearBinaryCategorizer> classifiers = new HashMap<ANNOTATION, LinearBinaryCategorizer>();
	private Set<ANNOTATION> annotations;
	private ANNOTATION negativeClass;

	/**
	 * Construct a new {@link LinearSVMAnnotator} with the given extractor and
	 * the specified negative class. The negative class is excluded from the
	 * predicted annotations.
	 * 
	 * @param extractor
	 *            the extractor
	 * @param negativeClass
	 *            the negative class to exclude from predictions
	 */
	public LinearSVMAnnotator(EXTRACTOR extractor, ANNOTATION negativeClass) {
		super(extractor);
		this.negativeClass = negativeClass;
	}

	/**
	 * Construct a new {@link LinearSVMAnnotator} with the given extractor.
	 * 
	 * @param extractor
	 *            the extractor
	 */
	public LinearSVMAnnotator(EXTRACTOR extractor) {
		this(extractor, null);
	}

	@Override
	public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
		final AnnotatedListHelper<OBJECT, ANNOTATION> helper = new AnnotatedListHelper<OBJECT, ANNOTATION>(data);

		annotations = helper.getAnnotations();

		for (final ANNOTATION annotation : annotations) {
			final PrimalEstimatedSubGradient pegasos = new PrimalEstimatedSubGradient();

			final List<? extends FeatureVector> positive = helper.extractFeatures(annotation,
					(FeatureExtractor<? extends FeatureVector, OBJECT>) extractor);
			final List<? extends FeatureVector> negative = helper.extractFeaturesExclude(annotation,
					(FeatureExtractor<? extends FeatureVector, OBJECT>) extractor);

			pegasos.learn(convert(positive, negative));
			classifiers.put(annotation, pegasos.getResult());
		}
	}

	private Collection<? extends InputOutputPair<? extends Vectorizable, Boolean>>
			convert(List<? extends FeatureVector> positive, List<? extends FeatureVector> negative)
	{
		final Collection<InputOutputPair<Vectorizable, Boolean>> data =
				new ArrayList<InputOutputPair<Vectorizable, Boolean>>(positive.size() + negative.size());

		for (final FeatureVector p : positive) {
			data.add(new DefaultInputOutputPair<Vectorizable, Boolean>(convert(p), true));
		}
		for (final FeatureVector n : negative) {
			data.add(new DefaultInputOutputPair<Vectorizable, Boolean>(convert(n), false));
		}

		return data;
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return annotations;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		final List<ScoredAnnotation<ANNOTATION>> results = new ArrayList<ScoredAnnotation<ANNOTATION>>();

		for (final ANNOTATION annotation : annotations) {
			// skip the negative class
			if (annotation.equals(negativeClass))
				continue;

			final FeatureVector feature = extractor.extractFeature(object);
			final Vector vector = convert(feature);

			final double result = classifiers.get(annotation).evaluateAsDouble(vector);

			if (result > 0) {
				results.add(new ScoredAnnotation<ANNOTATION>(annotation, (float) Math.abs(result)));
			}
		}

		return results;
	}

	private Vector convert(FeatureVector feature) {
		return VectorFactory.getDenseDefault().copyArray(feature.asDoubleVector());
	}
}
