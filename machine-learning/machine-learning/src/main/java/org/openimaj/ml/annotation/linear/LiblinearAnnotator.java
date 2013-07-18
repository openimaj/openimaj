package org.openimaj.ml.annotation.linear;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.utils.AnnotatedListHelper;
import org.openimaj.ml.annotation.utils.LiblinearHelper;

import de.bwaldvogel.liblinear.DenseLinear;
import de.bwaldvogel.liblinear.DenseProblem;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

/**
 * Annotator based on linear classifiers learned using Liblinear (see
 * {@link Linear}) or {@link DenseLinear} depending on the density of the
 * features. Two modes of operation are available depending on whether the
 * problem is multiclass or multilabel. Binary classification can be achieved
 * with either mode, although multiclass mode is more efficient in this case.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object being annotated
 * @param <ANNOTATION>
 *            Type of annotation
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Fan, Rong-En", "Chang, Kai-Wei", "Hsieh, Cho-Jui", "Wang, Xiang-Rui", "Lin, Chih-Jen" },
		title = "LIBLINEAR: A Library for Large Linear Classification",
		year = "2008",
		journal = "J. Mach. Learn. Res.",
		pages = { "1871", "", "1874" },
		url = "http://dl.acm.org/citation.cfm?id=1390681.1442794",
		month = "june",
		publisher = "JMLR.org",
		volume = "9",
		customData = {
				"date", "6/1/2008",
				"issn", "1532-4435",
				"numpages", "4",
				"acmid", "1442794"
		})
public class LiblinearAnnotator<OBJECT, ANNOTATION>
		extends
		BatchAnnotator<OBJECT, ANNOTATION>
{
	/**
	 * The classifier mode; either multiclass or multilabel. Multiclass mode
	 * will use liblinear's internal multiclass support, whereas multilabel mode
	 * will create a set of one-versus-all (OvA) classifiers for each class.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public enum Mode {
		/**
		 * Multiclass mode using liblinear's internal multiclass support
		 */
		MULTICLASS,
		/**
		 * Multilabel mode, using an ensemble of one-versus-all binary
		 * classifiers (class/not-class) which are used to determine the labels
		 */
		MULTILABEL;
	}

	static abstract class InternalModel<OBJECT, ANNOTATION> {
		ArrayList<ANNOTATION> annotationsList;
		Set<ANNOTATION> annotations;
		FeatureExtractor<? extends FeatureVector, OBJECT> extractor;
		boolean dense;

		public abstract void train(List<? extends Annotated<OBJECT, ANNOTATION>> data);

		public abstract void train(GroupedDataset<ANNOTATION, ListDataset<OBJECT>, OBJECT> dataset);

		public abstract List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object);

		Feature[] computeFeature(OBJECT object) {
			final FeatureVector feature = extractor.extractFeature(object);

			return LiblinearHelper.convert(feature);
		}

		double[] computeFeatureDense(OBJECT object) {
			final FeatureVector feature = extractor.extractFeature(object);

			return LiblinearHelper.convertDense(feature);
		}
	}

	static class Multiclass<OBJECT, ANNOTATION> extends InternalModel<OBJECT, ANNOTATION> {
		private Parameter parameter;
		private Model model;

		public Multiclass(SolverType solver, double C, double eps, boolean dense) {
			parameter = new Parameter(solver, C, eps);
			this.dense = dense;
		}

		@Override
		public void train(GroupedDataset<ANNOTATION, ListDataset<OBJECT>, OBJECT> dataset) {
			annotationsList = new ArrayList<ANNOTATION>(dataset.getGroups());

			final int nItems = dataset.numInstances();
			final int featureLength = extractor.extractFeature(dataset.getRandomInstance()).length();

			if (dense) {
				final DenseProblem problem = new DenseProblem();
				problem.l = nItems;
				problem.n = featureLength;
				problem.x = new double[nItems][];
				problem.y = new double[nItems];

				int i = 0;
				for (final ANNOTATION annotation : dataset.getGroups()) {
					for (final OBJECT object : dataset.get(annotation)) {
						problem.y[i] = annotationsList.indexOf(annotation) + 1;
						problem.x[i] = computeFeatureDense(object);
						i++;
					}
				}

				model = DenseLinear.train(problem, parameter);
			} else {
				final Problem problem = new Problem();
				problem.l = nItems;
				problem.n = featureLength;
				problem.x = new Feature[nItems][];
				problem.y = new double[nItems];

				int i = 0;
				for (final ANNOTATION annotation : dataset.getGroups()) {
					for (final OBJECT object : dataset.get(annotation)) {
						problem.y[i] = annotationsList.indexOf(annotation) + 1;
						problem.x[i] = computeFeature(object);
						i++;
					}
				}

				model = Linear.train(problem, parameter);
			}
		}

		@Override
		public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
			final AnnotatedListHelper<OBJECT, ANNOTATION> helper = new AnnotatedListHelper<OBJECT, ANNOTATION>(data);
			annotations = helper.getAnnotations();
			annotationsList = new ArrayList<ANNOTATION>(annotations);

			final int nItems = data.size();
			final int featureLength = extractor.extractFeature(data.get(0).getObject()).length();

			if (dense) {
				final DenseProblem problem = new DenseProblem();
				problem.l = nItems;
				problem.n = featureLength;
				problem.x = new double[nItems][];
				problem.y = new double[nItems];

				for (int i = 0; i < nItems; i++) {
					final Annotated<OBJECT, ANNOTATION> object = data.get(i);

					if (object.getAnnotations().size() != 1)
						throw new IllegalArgumentException(
								"A multiclass problem cannot have more than one class per instance");

					final ANNOTATION annotation = object.getAnnotations().iterator().next();

					problem.y[i] = annotationsList.indexOf(annotation) + 1;
					problem.x[i] = computeFeatureDense(object.getObject());
				}

				model = DenseLinear.train(problem, parameter);
			} else {
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
		}

		@Override
		public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
			final double clz;
			final double prob;

			if (dense) {
				final double[] feature = computeFeatureDense(object);

				if (parameter.getSolverType().isLogisticRegressionSolver()) {
					final double[] probs = new double[annotations.size()];
					clz = DenseLinear.predictProbability(model, feature, probs) - 1;
					prob = probs[(int) clz];
				} else {
					clz = DenseLinear.predict(model, feature) - 1;
					prob = 1;
				}
			} else {
				final Feature[] feature = computeFeature(object);

				if (parameter.getSolverType().isLogisticRegressionSolver()) {
					final double[] probs = new double[annotations.size()];
					clz = Linear.predictProbability(model, feature, probs) - 1;
					prob = probs[(int) clz];
				} else {
					clz = Linear.predict(model, feature) - 1;
					prob = 1;
				}
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
	static class Multilabel<OBJECT, ANNOTATION> extends InternalModel<OBJECT, ANNOTATION> {
		private Parameter parameter;
		private Model[] models;

		private static final int NEGATIVE_CLASS = 1;
		private static final int POSTIVE_CLASS = 2;

		public Multilabel(SolverType solver, double C, double eps, boolean dense) {
			parameter = new Parameter(solver, C, eps);
			this.dense = dense;
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
				final List<? extends FeatureVector> positive = helper.extractFeatures(annotation, extractor);
				final List<? extends FeatureVector> negative = helper.extractFeaturesExclude(annotation, extractor);

				if (dense) {
					final DenseProblem problem = new DenseProblem();
					problem.l = positive.size() + negative.size();
					problem.n = featureLength;
					problem.x = new double[problem.l][];
					problem.y = new double[problem.l];

					for (int j = 0; j < negative.size(); j++) {
						problem.x[j] = LiblinearHelper.convertDense(negative.get(j));
						problem.y[j] = NEGATIVE_CLASS;
					}

					for (int j = negative.size(), k = 0; k < positive.size(); j++, k++) {
						problem.x[j] = LiblinearHelper.convertDense(positive.get(k));
						problem.y[j] = POSTIVE_CLASS;
					}

					models[i] = DenseLinear.train(problem, parameter);
				} else {
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
		}

		@Override
		public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
			final List<ScoredAnnotation<ANNOTATION>> result = new ArrayList<ScoredAnnotation<ANNOTATION>>();

			if (dense) {
				final double[] feature = computeFeatureDense(object);

				for (int i = 0; i < annotationsList.size(); i++) {
					final double clz;
					final double prob;
					if (parameter.getSolverType().isLogisticRegressionSolver()) {
						final double[] probs = new double[annotations.size()];
						clz = DenseLinear.predictProbability(models[i], feature, probs);
						prob = probs[(int) clz - 1];
					} else {
						clz = DenseLinear.predict(models[i], feature);
						prob = 1;
					}

					if (clz == POSTIVE_CLASS) {
						result.add(new ScoredAnnotation<ANNOTATION>(annotationsList.get(i), (float) prob));
					}
				}
			} else {
				final Feature[] feature = computeFeature(object);

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
			}

			return result;
		}

		@Override
		public void train(GroupedDataset<ANNOTATION, ListDataset<OBJECT>, OBJECT> dataset) {
			train(AnnotatedObject.createList(dataset));
		}
	}

	InternalModel<OBJECT, ANNOTATION> internal;

	/**
	 * Default constructor. Assumes sparse features.
	 * 
	 * @param extractor
	 *            the feature extractor
	 * @param mode
	 *            the mode
	 * @param solver
	 *            the liblinear solver
	 * @param C
	 *            the C parameter (usually 1 or larger)
	 * @param eps
	 *            the epsilon value
	 */
	public LiblinearAnnotator(FeatureExtractor<? extends FeatureVector, OBJECT> extractor, Mode mode, SolverType solver,
			double C, double eps)
	{
		this(extractor, mode, solver, C, eps, false);
	}

	/**
	 * Default constructor.
	 * 
	 * @param extractor
	 *            the feature extractor
	 * @param mode
	 *            the mode
	 * @param solver
	 *            the liblinear solver
	 * @param C
	 *            the C parameter (usually 1 or larger)
	 * @param eps
	 *            the epsilon value
	 * @param dense
	 *            are the features dense? If so the dense variant of liblinear
	 *            will be used to drastically reduce memory usage
	 */
	public LiblinearAnnotator(FeatureExtractor<? extends FeatureVector, OBJECT> extractor, Mode mode, SolverType solver,
			double C, double eps, boolean dense)
	{
		switch (mode) {
		case MULTICLASS:
			this.internal = new Multiclass<OBJECT, ANNOTATION>(solver, C, eps, dense);
			break;
		case MULTILABEL:
			this.internal = new Multilabel<OBJECT, ANNOTATION>(solver, C, eps, dense);
			break;
		default:
			throw new RuntimeException("Unhandled mode");
		}

		this.internal.extractor = extractor;
	}

	@Override
	public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
		internal.train(data);
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return internal.annotations;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		return internal.annotate(object);
	}

	@Override
	public void train(GroupedDataset<ANNOTATION, ListDataset<OBJECT>, OBJECT> dataset) {
		internal.train(dataset);
	}
}
