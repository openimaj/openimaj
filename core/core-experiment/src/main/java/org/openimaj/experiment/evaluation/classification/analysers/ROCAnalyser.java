package org.openimaj.experiment.evaluation.classification.analysers;

import gov.sandia.cognition.statistics.method.ReceiverOperatingCharacteristic;
import gov.sandia.cognition.util.DefaultPair;
import gov.sandia.cognition.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.experiment.evaluation.classification.ClassificationAnalyser;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;

/**
 * A {@link ClassificationAnalyser} capable of producing 
 * a Receiver Operating Characteristic curve and associated
 * statistics.  
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <CLASS> Type of classes
 * @param <OBJECT> Type of objects
 */
public class ROCAnalyser< 
	OBJECT, 
	CLASS> 
implements ClassificationAnalyser<
	ROCAnalysisResult<CLASS>, 
	CLASS, 
	OBJECT> 
{

	@Override
	public ROCAnalysisResult<CLASS> analyse(Map<OBJECT, ClassificationResult<CLASS>> predicted, Map<OBJECT, Set<CLASS>> actual) {
		//get all the classes
		Set<CLASS> allClasses = new HashSet<CLASS>();
		for (OBJECT o : predicted.keySet()) {
			allClasses.addAll(actual.get(o));
		}
		
		//for each class compute a ROC curve
		Map<CLASS, ReceiverOperatingCharacteristic> output = new HashMap<CLASS, ReceiverOperatingCharacteristic>();
		for (CLASS clz : allClasses) {
			List<Pair<Boolean, Double>> data = new ArrayList<Pair<Boolean, Double>>();
			
			for (OBJECT o : predicted.keySet()) {
				double score = predicted.get(o).getConfidence(clz);
				boolean objIsClass = actual.get(o).contains(clz);
				
				data.add(new DefaultPair<Boolean, Double>(objIsClass, score));
			}
			
			output.put(clz, ReceiverOperatingCharacteristic.createFromTargetEstimatePairs(data));
		}
		
		return new ROCAnalysisResult<CLASS>(output);
	}

}
