package org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.experiment.evaluation.classification.ClassificationAnalyser;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;

import gov.sandia.cognition.learning.data.DefaultTargetEstimatePair;
import gov.sandia.cognition.learning.data.TargetEstimatePair;
import gov.sandia.cognition.learning.performance.categorization.ConfusionMatrixPerformanceEvaluator;

public class CMAnalyser< 
	OBJECT, 
	CLASS> 
implements ClassificationAnalyser<
	CMResult<CLASS>, 
	CLASS, 
	OBJECT> 
{
	ConfusionMatrixPerformanceEvaluator<?, CLASS> eval = new ConfusionMatrixPerformanceEvaluator<Object, CLASS>();
	
	@Override
	public CMResult<CLASS> analyse(
			Map<OBJECT, ClassificationResult<CLASS>> predicted,
			Map<OBJECT, Set<CLASS>> actual) {
		
		List<TargetEstimatePair<CLASS, CLASS>> data = new ArrayList<TargetEstimatePair<CLASS,CLASS>>();
		
		for (OBJECT obj : predicted.keySet()) {
			Set<CLASS> pclasses = predicted.get(obj).getPredictedClasses();
			Set<CLASS> aclasses = actual.get(obj);
			
//			HashSet<CLASS> allClasses = new HashSet<CLASS>();
//			allClasses.addAll(pclasses);
//			allClasses.addAll(aclasses);
//			
//			for (CLASS clz : allClasses) {
//				CLASS target = aclasses.contains(clz) ? clz : null;
//				CLASS estimate = pclasses.contains(clz) ? clz : null;
//				
//				data.add(DefaultTargetEstimatePair.create(target, estimate));
//			}
			
			data.add(DefaultTargetEstimatePair.create(
					new ArrayList<CLASS>(aclasses).get(0),
					new ArrayList<CLASS>(pclasses).get(0)
				));
		}
		
		return new CMResult<CLASS>(eval.evaluatePerformance(data));
	}
}
