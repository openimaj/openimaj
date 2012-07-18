package org.openimaj.experiment.evaluation.classification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.Evaluator;


/**
 * Implementation of an {@link Evaluator} for the evaluation 
 * of classification experiments.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <RESULT> Type of analysed data
 * @param <CLASS> Type of classes predicted by the classifier
 * @param <OBJECT> Type of objects classified by the classifier
 */
public class ClassificationEvaluator<
	RESULT extends AnalysisResult,  
	CLASS,
	OBJECT> 
implements Evaluator<
	Map<OBJECT, ClassificationResult<CLASS>>, RESULT> 
{
	protected Classifier<CLASS, OBJECT> classifier;
	protected ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser;
	protected Map<OBJECT, Set<CLASS>> actual;
	protected Collection<OBJECT> objects;
	
	/**
	 * Construct a new {@link ClassificationEvaluator} with the given classifier,
	 * set of objects to classify, ground truth ("actual") data and an
	 * {@link ClassificationAnalyser}.
	 * 
	 * @param classifier the classifier
	 * @param objects the objects to classify
	 * @param actual the ground truth
	 * @param analyser the analyser
	 */
	public ClassificationEvaluator(Classifier<CLASS, OBJECT> classifier, Collection<OBJECT> objects, Map<OBJECT, Set<CLASS>> actual, ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser) {
		this.classifier = classifier;
		this.objects = objects;
		this.actual = actual;
		this.analyser = analyser;
	}
	
	/**
	 * Construct a new {@link ClassificationEvaluator} with the given classifier,
	 * ground truth ("actual") data and an {@link ClassificationAnalyser}.
	 * The objects to classify are taken from the {@link Map#keySet()} of the
	 * ground truth.
	 * 
	 * @param classifier the classifier
	 * @param actual the ground truth
	 * @param analyser the analyser
	 */
	public ClassificationEvaluator(Classifier<CLASS, OBJECT> classifier, Map<OBJECT, Set<CLASS>> actual, ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser) {
		this.classifier = classifier;
		this.objects = actual.keySet();
		this.actual = actual;
		this.analyser = analyser;
	}
	
	/**
	 * Construct a new {@link ClassificationEvaluator} with the given classifier.
	 * The ground truth ("actual") data and objects to classify are held in a dataset.
	 * 
	 * @param classifier the classifier
	 * @param actual the dataset containing instances and ground truths
	 * @param analyser the analyser
	 */
	public ClassificationEvaluator(Classifier<CLASS, OBJECT> classifier, GroupedDataset<CLASS, ListDataset<OBJECT>,OBJECT> actual, ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser) {
		this.classifier = classifier;
		this.objects = DatasetAdaptors.asList(actual);
		this.actual = new HashMap<OBJECT,Set<CLASS>>();
		for (CLASS clazz : actual.getGroups()) {
			HashSet<CLASS> cset = new HashSet<CLASS>();
			cset.add(clazz);
			for (OBJECT instance : actual.getInstances(clazz)) {
				this.actual.put(instance, cset);
			}
		}
		this.analyser = analyser;
	}
	
	/**
	 * Construct a new {@link ClassificationEvaluator} with the given pre-classified 
	 * results, the ground truth ("actual") data and an {@link ClassificationAnalyser}.
	 * <p>
	 * Internally, this constructor wraps a simple {@link Classifier}
	 * implementation around the results.
	 * 
	 * @param results the pre-classified results
	 * @param actual the ground truth
	 * @param analyser the analyser
	 */
	public ClassificationEvaluator(final Map<OBJECT, ClassificationResult<CLASS>> results, Map<OBJECT, Set<CLASS>> actual, ClassificationAnalyser<RESULT, CLASS, OBJECT> analyser) {
		this.classifier = new Classifier<CLASS, OBJECT>() {
			@Override
			public ClassificationResult<CLASS> classify(OBJECT object) {
				return results.get(object);
			}
		};
		
		this.objects = actual.keySet();
		this.actual = actual;
		this.analyser = analyser;
	}
	
	@Override
	public Map<OBJECT, ClassificationResult<CLASS>> evaluate() {
		Map<OBJECT, ClassificationResult<CLASS>> results = new HashMap<OBJECT, ClassificationResult<CLASS>>();
		
		for (OBJECT object : objects) {
			results.put(object, classifier.classify(object));
		}
		
		return results;
	}

	@Override
	public RESULT analyse(Map<OBJECT, ClassificationResult<CLASS>> predicted) {
		return analyser.analyse(predicted, actual);
	}
}
