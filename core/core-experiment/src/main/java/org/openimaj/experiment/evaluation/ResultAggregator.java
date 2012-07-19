package org.openimaj.experiment.evaluation;


/**
 * A {@link ResultAggregator} aggregates multiple results
 * into a single {@link AnalysisResult}. A primary use for
 * this is in cross-validation where the results from each
 * round must be aggregated to form the final analysis.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <RESULT> Type of the results being aggregated
 * @param <AGGREGATED> Type of the aggregated result
 */
public interface ResultAggregator<
	RESULT,
	AGGREGATED extends AnalysisResult
>
{
	/**
	 * Add a new result to this aggregation
	 * @param result the result to add
	 */
	public void add(RESULT result);
	
	/**
	 * Get the aggregated result
	 * @return the aggregated result
	 */
	public AGGREGATED getAggregatedResult();
}
