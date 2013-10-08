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
 * Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FullMEAnalysis implements AnalysisResult{

	/**
	 * A measure of how pure each cluster is.
	 * P = 1/N Sigma_k max_j | w_k AND c_j |
	 *
	 * Count the true classes of all the elements in a class, make a count of the largest group from each cluster,
	 * divide by number of elements in all clusters.
	 *
	 * High means: most of the clusters had a high number of a single class
	 * Low means: most of the clusters had a roughly equal spread of all the classes
	 */
	public PurityAnalysis purity;
	
	/**
	 * the {@link NMIAnalysis} instance
	 */
	public NMIAnalysis nmi;

	/**
	 * The {@link DecisionAnalysis} instance
	 */
	public DecisionAnalysis decision;
	
	/**
	 * The {@link FScoreAnalysis} instance
	 */
	public FScoreAnalysis fscore;
	
	/**
	 * The {@link RandomIndexAnalysis} instance
	 */
	public RandomIndexAnalysis randIndex;
	
	/**
	 * 
	 */
	public AnalysisResult adjRandInd;


	@Override
	public String getSummaryReport() {
		return String.format("(%s,%s,%s,%s,%s,%s)",purity,nmi,decision,fscore,randIndex,adjRandInd);
	}

	@Override
	public String getDetailReport() {
		return "";
	}


	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {throw new UnsupportedOperationException();}

	@Override
	public JasperPrint getDetailReport(String title, String info)throws JRException {throw new UnsupportedOperationException();}

}