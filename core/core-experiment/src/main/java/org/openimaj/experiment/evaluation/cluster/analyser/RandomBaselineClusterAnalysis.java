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

import org.openimaj.experiment.evaluation.AnalysisResult;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * The result of a {@link TestRandomBaselineClusterAnalyser} which wraps the
 * baseline result and result of an AnalysisResult
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class RandomBaselineClusterAnalysis<T extends RandomBaselineWrappable & AnalysisResult> implements AnalysisResult,RandomBaselineWrappable{

	private T score;
	private double randscore;

	/**
	 * @param score
	 * @param randscore
	 */
	public RandomBaselineClusterAnalysis(T score, double randscore) {
		this.score = score;
		this.randscore = randscore;
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
	public String getSummaryReport() {
		return String.format("s=(%s),b=%2.5f,d=%2.4f",this.score.getSummaryReport(),this.randscore,score());
	}

	@Override
	public String getDetailReport() {
		return getSummaryReport();
	}
	
	@Override
	public String toString() {
		return this.getSummaryReport();
	}
	
	@Override
	public double score() {
		return this.score.score() - this.randscore;
	}
	
	/**
	 * @return the unmodified analysis
	 */
	public T getUnmodified(){
		return this.score;
	}

}
