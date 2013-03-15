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

	class Multilabel implements InternalModel<OBJECT, ANNOTATION> {
		private Parameter parameter;
		private Model[] models;

		public Multilabel(SolverType solver, double C, double eps) {
			parameter = new Parameter(solver, C, eps);
		}

		@Override
		public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
			final AnnotatedListHelper<OBJECT, ANNOTATION> helper = new AnnotatedListHelper<OBJECT, ANNOTATION>(data);
			annotations = helper.getAnnotations();
			annotationsList = new ArrayList<ANNOTATION>(annotations);

			final int nItems = data.size();
			final int featureLength = extractor.extractFeature(data.get(0).getObject()).length();

			models = new Model[annotationsList.size()];

			for (int i = 0; i < annotationsList.size(); i++) {
				final Problem problem = new Problem();
				problem.l = nItems;
				problem.n = featureLength;
				problem.x = new Feature[nItems][];
				problem.y = new double[nItems];

				final ANNOTATION annotation = annotationsList.get(i);
				positive = helper.extractFeatures(annotation, extractor);

				// for (int i = 0; i < nItems; i++) {
				// final Annotated<OBJECT, ANNOTATION> object = data.get(i);
				//
				// if (object.getAnnotations().size() != 1)
				// throw new IllegalArgumentException(
				// "A multiclass problem cannot have more than one class per instance");
				//
				// final ANNOTATION annotation =
				// object.getAnnotations().iterator().next();
				//
				// problem.y[i] = annotationsList.indexOf(annotation) + 1;
				// problem.x[i] = computeFeature(object.getObject());
				// }
				//
				// model = Linear.train(problem, parameter);
			}
		}

		@Override
		public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	InternalModel<OBJECT, ANNOTATION> internal;
	private Set<ANNOTATION> annotations;
	private ArrayList<ANNOTATION> annotationsList;

	public LiblinearAnnotator(EXTRACTOR extractor) {
		super(extractor);

		// FIXME
		this.internal = new Multiclass(SolverType.L2R_LR, 1.0, 0.01);
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
				new IdentityFeatureExtractor<DoubleFV>());

		final List<Annotated<DoubleFV, String>> trainingData = new ArrayList<Annotated<DoubleFV, String>>();
		trainingData.add(AnnotatedObject.create(new DoubleFV(new double[] { -1.0 }), "neg"));
		trainingData.add(AnnotatedObject.create(new DoubleFV(new double[] { 1.0 }), "pos"));
		ann.train(trainingData);

		System.out.println(ann.annotate(new DoubleFV(new double[] { -10.0 })));
	}
}
