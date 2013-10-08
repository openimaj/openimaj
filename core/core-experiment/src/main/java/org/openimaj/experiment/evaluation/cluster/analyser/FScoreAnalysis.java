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
package org.openimaj.experiment.evaluation.cluster.analyser;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.openimaj.experiment.evaluation.AnalysisResult;

/**
 * Uses a decision analysis to produce the random index result
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class FScoreAnalysis implements AnalysisResult, RandomBaselineWrappable{
	private DecisionAnalysis ann;

	/**
	 * @param ann
	 */
	public FScoreAnalysis(DecisionAnalysis ann) {
		this.ann = ann;
	}
	@Override
	public String getSummaryReport() {
		return String.format("f1=%2.5f",fscore(1));
	}

	@Override
	public String getDetailReport() {
		return this.getSummaryReport();
	}
	
	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JasperPrint getDetailReport(String title, String info)throws JRException {
		throw new UnsupportedOperationException();
	}
	@Override
	public double score() {
		return fscore(1);
	}
	
	/**
	 * @return the proportion of true decisions made as compared to all decisions made
	 */
	public double randIndex() {
		return (ann.TP + ann.TN) / (double)(ann.TP + ann.FP + ann.TN + ann.FN);
	}
	
	@Override
	public String toString() {
		return this.getSummaryReport();
	}
	/**
	 * @param beta
	 * @return the f-score which weights up or down the relative importance of a false positive against false negatives
	 */
	public double fscore(double beta){
		double beta2 = beta * beta;
		double P = ann.precision();
		double R = ann.recall();
		if(P + R == 0) return 0;
		return ((beta2 + 1) * P * R)/(beta2 * P + R);
	}
	
	/**
	 * @return the underlying decision analysis
	 */
	public DecisionAnalysis getDecisionAnalysis(){
		return this.ann;
	}

}
