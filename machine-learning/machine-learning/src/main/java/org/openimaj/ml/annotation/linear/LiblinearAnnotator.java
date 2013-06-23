package org.openimaj.ml.annotation.linear;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.annotation.Annotated;
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
				final List<? extends FeatureVector> positive = helper.extractFeatures(annotation, extractor);
				final List<? extends FeatureVector> negative = helper.extractFeaturesExclude(annotation, extractor);

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
	private FeatureExtractor<? extends FeatureVector, OBJECT> extractor;

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
	 */
	public LiblinearAnnotator(FeatureExtractor<? extends FeatureVector, OBJECT> extractor, Mode mode, SolverType solver,
			double C, double eps)
	{
		this.extractor = extractor;

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
}
