package org.openimaj.ml.annotation.linear;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.utils.AnnotatedListHelper;
import org.openimaj.ml.annotation.utils.LiblinearHelper;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

/**
 * Annotator based on linear classifiers learned using Liblinear (see
 * {@link Linear}). Two modes of operation are available depending on whether
 * the problem is multiclass or multilabel. Binary classification can be
 * achieved with either mode, although multiclass mode is more efficient in this
 * case.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 * @param <ANNOTATION>
 * @param <EXTRACTOR>
 */
public class LiblinearAnnotator<OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<? extends FeatureVector, OBJECT>>
		extends
		BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR>
{
	public enum Mode {
		MULTICLASS, MULTILABEL;
	}

	interface InternalModel<OBJECT, ANNOTATION> {
		public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data);

		public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object);
	}

	class Multiclass implements InternalModel<OBJECT, ANNOTATION> {
		private Parameter parameter;
		private Model model;

		public Multiclass(SolverType solver, double C, double eps) {
			parameter = new Parameter(solver, C, eps);
		}

		@Override
		public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
			final AnnotatedListHelper<OBJECT, ANNOTATION> helper = new AnnotatedListHelper<OBJECT, ANNOTATION>(data);
			annotations = helper.getAnnotations();
			annotationsList = new ArrayList<ANNOTATION>(annotations);

			final int nItems = data.size();
			final int featureLength = extractor.extractFeature(data.get(0).getObject()).length();

			final Problem problem = new Problem();
			problem.l = nItems;
			problem.n = featureLength;
			problem.x = new Feature[nItems][];
			problem.y = new double[nItems];

			for (int i = 0; i < nItems; i++) {
				final Annotated<OBJECT, ANNOTATION> object = data.get(i);

				if (object.getAnnotations().size() != 1)
					throw new IllegalArgumentException(
							"A multiclass problem cannot have more than one class per instance");

				final ANNOTATION annotation = object.getAnnotations().iterator().next();

				problem.y[i] = annotationsList.indexOf(annotation) + 1;
				problem.x[i] = computeFeature(object.getObject());
			}

			model = Linear.train(problem, parameter);
		}

		@Override
		public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
			final Feature[] feature = computeFeature(object);

			final double clz;
			final double prob;
			if (parameter.getSolverType().isLogisticRegressionSolver()) {
				final double[] probs = new double[annotations.size()];
				clz = Linear.predictProbability(model, feature, probs) - 1;
				prob = probs[(int) clz];
			} else {
				clz = Linear.predict(model, feature) - 1;
				prob = 1;
			}

			final List<ScoredAnnotation<ANNOTATION>> result = new ArrayList<ScoredAnnotation<ANNOTATION>>(1);
			result.add(new ScoredAnnotation<ANNOTATION>(annotationsList.get((int) clz), (float) prob));
			return result;
		}
	}

	/**
	 * Multi-label classifier built from multiple binary classifiers.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	class Multilabel implements InternalModel<OBJECT, ANNOTATION> {
		private Parameter parameter;
		private Model[] models;

		private static final int NEGATIVE_CLASS = 1;
		private static final int POSTIVE_CLASS = 2;

		public Multilabel(SolverType solver, double C, double eps) {
			parameter = new Parameter(solver, C, eps);
		}

		@Override
		public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
			final AnnotatedListHelper<OBJECT, ANNOTATION> helper = new AnnotatedListHelper<OBJECT, ANNOTATION>(data);
			annotations = helper.getAnnotations();
			annotationsList = new ArrayList<ANNOTATION>(annotations);

			final int featureLength = extractor.extractFeature(data.get(0).getObject()).length();

			models = new Model[annotationsList.size()];

			for (int i = 0; i < annotationsList.size(); i++) {
				final ANNOTATION annotation = annotationsList.get(i);
				final List<? extends FeatureVector> positive = helper.extractFeatures(annotation,
						(FeatureExtractor<? extends FeatureVector, OBJECT>) extractor);
				final List<? extends FeatureVector> negative = helper.extractFeaturesExclude(annotation,
						(FeatureExtractor<? extends FeatureVector, OBJECT>) extractor);

				final Problem problem = new Problem();
				problem.l = positive.size() + negative.size();
				problem.n = featureLength;
				problem.x = new Feature[problem.l][];
				problem.y = new double[problem.l];

				for (int j = 0; j < negative.size(); j++) {
					problem.x[j] = LiblinearHelper.convert(negative.get(j));
					problem.y[j] = NEGATIVE_CLASS;
				}

				for (int j = negative.size(), k = 0; k < positive.size(); j++, k++) {
					problem.x[j] = LiblinearHelper.convert(positive.get(k));
					problem.y[j] = POSTIVE_CLASS;
				}

				models[i] = Linear.train(problem, parameter);
			}
		}

		@Override
		public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
			final Feature[] feature = computeFeature(object);
			final List<ScoredAnnotation<ANNOTATION>> result = new ArrayList<ScoredAnnotation<ANNOTATION>>();

			for (int i = 0; i < annotationsList.size(); i++) {
				final double clz;
				final double prob;
				if (parameter.getSolverType().isLogisticRegressionSolver()) {
					final double[] probs = new double[annotations.size()];
					clz = Linear.predictProbability(models[i], feature, probs);
					prob = probs[(int) clz - 1];
				} else {
					clz = Linear.predict(models[i], feature);
					prob = 1;
				}

				if (clz == POSTIVE_CLASS) {
					result.add(new ScoredAnnotation<ANNOTATION>(annotationsList.get(i), (float) prob));
				}
			}

			return result;
		}
	}

	InternalModel<OBJECT, ANNOTATION> internal;
	private Set<ANNOTATION> annotations;
	private ArrayList<ANNOTATION> annotationsList;

	public LiblinearAnnotator(EXTRACTOR extractor, Mode mode, SolverType solver, double C, double eps) {
		super(extractor);

		switch (mode) {
		case MULTICLASS:
			this.internal = new Multiclass(solver, C, eps);
			break;
		case MULTILABEL:
			this.internal = new Multilabel(solver, C, eps);
			break;
		default:
			throw new RuntimeException("Unhandled mode");
		}
	}

	private Feature[] computeFeature(OBJECT object) {
		final FeatureVector feature = extractor.extractFeature(object);

		return LiblinearHelper.convert(feature);
	}

	@Override
	public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
		internal.train(data);
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return annotations;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		return internal.annotate(object);
	}

	public static void main(String[] args) {
		final LiblinearAnnotator<DoubleFV, String, IdentityFeatureExtractor<DoubleFV>> ann = new LiblinearAnnotator<DoubleFV, String, IdentityFeatureExtractor<DoubleFV>>(
				new IdentityFeatureExtractor<DoubleFV>(), Mode.MULTICLASS, SolverType.L2R_LR, 1.0, 0.01);

		final List<Annotated<DoubleFV, String>> trainingData = new ArrayList<Annotated<DoubleFV, String>>();
		trainingData.add(AnnotatedObject.create(new DoubleFV(new double[] { -1.0 }), "neg"));
		trainingData.add(AnnotatedObject.create(new DoubleFV(new double[] { 1.0 }), "pos"));
		ann.train(trainingData);

		System.out.println(ann.annotate(new DoubleFV(new double[] { 10.0 })));
	}
}
