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
package org.openimaj.experiment.evaluation.retrieval.analysers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.lemurproject.ireval.IREval;
import org.lemurproject.ireval.RetrievalEvaluator;
import org.lemurproject.ireval.SetRetrievalEvaluator;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.util.pair.IndependentPair;

import com.googlecode.jatl.Html;

/**
 * An {@link AnalysisResult} that is bascked by a {@link SetRetrievalEvaluator} to
 * capture the results of a retrieval experiment.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class IREvalResult implements AnalysisResult {
	protected SetRetrievalEvaluator eval;
	
	/**
	 * Construct with the given {@link SetRetrievalEvaluator} result.
	 * @param sre the {@link SetRetrievalEvaluator}.
	 */
	public IREvalResult(SetRetrievalEvaluator sre) {
		this.eval = sre;
	}
	
	@Override
	public String toString() {
		return this.getSummaryReport();
	}

	void writeHTML(File file, final String title, final String info) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			
			new Html(fw) {{
				html();
					head();
						title(title);
					end();
					body();
						h1().text(title).end();
						div().text(info).end();
						hr();
						writeEvaluation();
						hr();
						h2().text("Individual Query Results:").end();
						writeIndividualQueries();
					endAll();
				done();
			}
			
			void writeIndividualQueries() {
				for( RetrievalEvaluator evaluator : eval.getEvaluators() ) {
	                String query = evaluator.queryName();
	                writeIndividual(query, evaluator);
	            }
			}
			
			void writeIndividual(String name, RetrievalEvaluator re) {
				table();
					tr();
						td().colspan("2");
							h3().text(name).end();
						end();
					end();
				
					// counts
					tr().td().text("num_ret").end().td().text(re.retrievedDocuments().size() + "").end().end();
					tr().td().text("num_rel").end().td().text(re.relevantDocuments().size() + "").end().end();
					tr().td().text("num_rel_ret").end().td().text(re.relevantRetrievedDocuments().size() + "").end().end();
					
					// aggregate measures
					tr().td().text("map").end().td().text(String.format("%3.4f", re.averagePrecision())).end().end();
					tr().td().text("ndcg").end().td().text(String.format("%3.4f", re.normalizedDiscountedCumulativeGain())).end().end();					
					tr().td().text("ndcg15").end().td().text(String.format("%3.4f", re.normalizedDiscountedCumulativeGain( 15 ))).end().end();
					tr().td().text("R-prec").end().td().text(String.format("%3.4f", re.rPrecision())).end().end();
					tr().td().text("bpref").end().td().text(String.format("%3.4f", re.binaryPreference())).end().end();
					tr().td().text("recip_rank").end().td().text(String.format("%3.4f", re.reciprocalRank())).end().end();

					// precision at fixed points
					int[] fixedPoints = RetrievalEvaluator.getFixedPoints();
					double [] vals = re.precisionAtFixedPoints();
					for( int i=0; i<fixedPoints.length; i++ ) {
		            int point = fixedPoints[i];
		            	tr().td().text("P" + point).end().td().text(String.format("%3.4f", vals[i])).end().end();
					}
					double[] precs = re.interpolatedPrecision();
		        	double prec = 0;
		        	for( int i=0; i<precs.length; i++ ) {
		        		tr().td().text(String.format("ircl_prn.%3.2f", prec)).end().td().text(String.format("%3.4f", precs[i])).end().end();
		        		prec += 0.1;
		        	}
		        end();
			}
			
			void writeEvaluation() {
				table();
					tr();
						td().colspan("2");
							h2().text("Summary Results:").end();
						end();
					end();
				
					// print summary data
					tr().td().text("num_q").end().td().text(eval.getEvaluators().size() + "").end().end();
					tr().td().text("num_ret").end().td().text(eval.numberRetrieved() + "").end().end();
					tr().td().text("num_rel").end().td().text(eval.numberRetrieved() + "").end().end();					
					tr().td().text("num_rel_ret").end().td().text(eval.numberRelevantRetrieved() + "").end().end();

					tr().td().text("map").end().td().text(String.format("%3.4f", eval.meanAveragePrecision())).end().end();
					tr().td().text("gm_ap").end().td().text(String.format("%3.4f", eval.geometricMeanAveragePrecision())).end().end();
					tr().td().text("ndcg").end().td().text(String.format("%3.4f", eval.meanNormalizedDiscountedCumulativeGain())).end().end();
					tr().td().text("R-prec").end().td().text(String.format("%3.4f", eval.meanRPrecision())).end().end();
					tr().td().text("bpref").end().td().text(String.format("%3.4f", eval.meanBinaryPreference())).end().end();		        
					tr().td().text("recip_rank").end().td().text(String.format("%3.4f", eval.meanReciprocalRank())).end().end();
		        
					// precision at fixed points
					int[] fixedPoints = SetRetrievalEvaluator.getFixedPoints();
		        	double [] precs = eval.precisionAtFixedPoints();

		        	for( int i=0; i<fixedPoints.length; i++ ) {
		        		int point = fixedPoints[i];
		        		tr().td().text("P" + point).end().td().text(String.format("%3.4f", precs[i])).end().end();
		        	}
		        	double prec = 0;
		        	precs = eval.interpolatedPrecision();
		        	for( int i=0; i<precs.length; i++ ) {
		        		tr().td().text(String.format("ircl_prn.%3.2f", prec)).end().td().text(String.format("%3.4f", precs[i])).end().end();
		        		prec += 0.1;
		        	}
		        end();
			}
		};
		} finally {
			if (fw != null) fw.close();
		}
	}
	
	/**
	 * @return a summary of the evaluation
	 */
	public List<IndependentPair<String, Number>> getSummaryData() {
		List<IndependentPair<String, Number>> data = new ArrayList<IndependentPair<String, Number>>();
		
		data.add(new IndependentPair<String, Number>("num_q", eval.getEvaluators().size()));
		data.add(new IndependentPair<String, Number>("num_ret", eval.numberRetrieved()));
		data.add(new IndependentPair<String, Number>("num_rel", eval.numberRetrieved()));					
		data.add(new IndependentPair<String, Number>("num_rel_ret", eval.numberRelevantRetrieved()));

		data.add(new IndependentPair<String, Number>("map", eval.meanAveragePrecision()));
		data.add(new IndependentPair<String, Number>("gm_ap", eval.geometricMeanAveragePrecision()));
		data.add(new IndependentPair<String, Number>("ndcg", eval.meanNormalizedDiscountedCumulativeGain()));
		data.add(new IndependentPair<String, Number>("R-prec", eval.meanRPrecision()));
		data.add(new IndependentPair<String, Number>("bpref", eval.meanBinaryPreference()));		        
		data.add(new IndependentPair<String, Number>("recip_rank", eval.meanReciprocalRank()));
		
		return data;
	}
	
	/**
	 * @return the interpolated PR data
	 */
	public List<IndependentPair<Double, Double>> getInterpolatedPRData() {
		List<IndependentPair<Double, Double>> data = new ArrayList<IndependentPair<Double, Double>>();
		
		double prec = 0;
    	double[] precs = eval.interpolatedPrecision();
    	for( int i=0; i<precs.length; i++ ) {
    		data.add(new IndependentPair<Double, Double>(prec, precs[i]));
    		prec += 0.1;
    	}		
		
		return data;
	}

	@Override
	public JasperPrint getSummaryReport(String title, String info) throws JRException {
		InputStream inputStream = IREvalResult.class.getResourceAsStream("IREvalSummaryReport.jrxml");
		ArrayList<IREvalResult> list = new ArrayList<IREvalResult>();
		list.add(this);
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(list);

		Map<String, Object> parameters = new HashMap<String, Object>();

		JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
		JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, beanColDataSource);
		
		return jasperPrint;
	}

	@Override
	public JasperPrint getDetailReport(String title, String info) {
		//FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSummaryReport() {
		return IREval.singleEvaluation(eval, false);
	}

	@Override
	public String getDetailReport() {
		return IREval.singleEvaluation(eval, true);
	}
}
