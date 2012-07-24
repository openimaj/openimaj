package org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix;

import org.openimaj.experiment.evaluation.ResultAggregator;

/**
 * A {@link ResultAggregator} for collecting multiple {@link CMResult}s
 * and producing a single unified report.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <CLASS> The type of the classes represented by the {@link CMResult}s
 */
public class CMAggregator<CLASS> 
	implements
		ResultAggregator<CMResult<CLASS>, AggregatedCMResult<CLASS>> 
{
	AggregatedCMResult<CLASS> aggregated = new AggregatedCMResult<CLASS>();
	
	@Override
	public void add(CMResult<CLASS> result) {
		aggregated.matrices.add(result);
	}

	@Override
	public AggregatedCMResult<CLASS> getAggregatedResult() {
		return aggregated;
	}
}
