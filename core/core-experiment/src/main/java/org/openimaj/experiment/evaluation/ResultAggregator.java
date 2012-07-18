package org.openimaj.experiment.evaluation;


public interface ResultAggregator<
	RESULT,
	AGGREGATED extends AnalysisResult
>
{
	public void add(RESULT result);
	
	public AGGREGATED getAggregatedResult();
}
