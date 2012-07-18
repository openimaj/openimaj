package org.openimaj.experiment.evaluation;


public interface AnalysisResultAggregator<
	RESULT,
	AGGREGATED extends AnalysisResult
>
{
	public void add(RESULT result);
	
	public AGGREGATED getAggregatedResult();
}
