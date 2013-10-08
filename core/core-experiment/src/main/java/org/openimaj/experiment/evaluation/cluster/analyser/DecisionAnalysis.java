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
 * Counting the number of true positives, true negatives, flase postitives and false negatives
 * one can produce various cluster quality metrics including the fscore and randindex
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DecisionAnalysis implements AnalysisResult{

	/**
	 * The total number of pairs in a cluster which belong to the same class
	 */
	public long TP;
	/**
	 * The total number of pairs in a cluster which do not belong to the same class
	 */
	public long FP;
	/**
	 * The total number of pairs in different clusters which belong to different classes
	 */
	public long TN;
	/**
	 * The total number of pairs in different clusters which belong to the same class
	 */
	public long FN;

	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JasperPrint getDetailReport(String title, String info) throws JRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSummaryReport() {
		return toString();
	}

	@Override
	public String getDetailReport() {
		return toString();
	}
	
	@Override
	public String toString() {
		return String.format("{tp=%d,fp=%d,tn=%d,fn=%d,P=%2.2f,R=%2.5f}",TP,FP,TN,FN,precision(),recall());
	}
	
	/**
	 * @return The number of true positives as a proportion of overall positives 
	 */
	public double precision(){
		if( TP + FP == 0) return 0;
		return TP / (double)(TP + FP);
	};

	/**
	 * @return The number of true positives as a porportion of true positives and false negatives
	 */
	public double recall(){
		if( TP + FN == 0) return 0;
		return TP / (double)(TP + FN);
	};

}
