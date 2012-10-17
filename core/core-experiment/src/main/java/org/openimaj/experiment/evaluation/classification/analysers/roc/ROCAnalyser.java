/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.experiment.evaluation.classification.analysers.roc;

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
	ROCResult<CLASS>, 
	CLASS, 
	OBJECT> 
{

	@Override
	public ROCResult<CLASS> analyse(Map<OBJECT, ClassificationResult<CLASS>> predicted, Map<OBJECT, Set<CLASS>> actual) {
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
				if (predicted.get(o) != null) {
					double score = predicted.get(o).getConfidence(clz);
					boolean objIsClass = actual.get(o).contains(clz);

					data.add(new DefaultPair<Boolean, Double>(objIsClass, score));
				} else {
					data.add(new DefaultPair<Boolean, Double>(false, 1.0));
				}
			}
			
			output.put(clz, ReceiverOperatingCharacteristic.createFromTargetEstimatePairs(data));
		}
		
		return new ROCResult<CLASS>(output);
	}

}
