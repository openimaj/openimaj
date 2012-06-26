package org.openimaj.experiment.evaluation.retrieval.analysers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.lemurproject.ireval.RetrievalEvaluator;
import org.lemurproject.ireval.SetRetrievalEvaluator;
import org.openimaj.experiment.evaluation.AnalysisResult;

import com.googlecode.jatl.Html;

/**
 * An {@link AnalysisResult} that is bascked by a {@link SetRetrievalEvaluator} to
 * capture the results of a retrieval experiment.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class IREvalResult implements AnalysisResult {
	SetRetrievalEvaluator eval;
	
	/**
	 * Construct with the given {@link SetRetrievalEvaluator} result.
	 * @param sre the {@link SetRetrievalEvaluator}.
	 */
	public IREvalResult(SetRetrievalEvaluator sre) {
		this.eval = sre;
	}
	
	@Override
	public String toString() {
		return eval.toString();
	}

	@Override
	public void writeHTML(File file, final String title, final String info) throws IOException {
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
}
