package org.openimaj.experiment.evaluation.retrieval.analysers;

import java.io.File;
import java.io.IOException;

import gnu.trove.TObjectDoubleHashMap;

import org.openimaj.experiment.evaluation.AnalysisResult;

public class PrecisionAtNScores<Q> implements AnalysisResult {
	TObjectDoubleHashMap<Q> allScores;

	@Override
	public void writeHTML(File file, String title, String info) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
