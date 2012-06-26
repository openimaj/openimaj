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
/*
 * Main.java
 *
 * Created on November 30, 2006, 9:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lemurproject.ireval;

import org.lemurproject.ireval.RetrievalEvaluator.Document;
import org.lemurproject.ireval.RetrievalEvaluator.Judgment;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * A trec_eval style tool in pure java.
 * 
 * @author Trevor Strohman
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class IREval {
    /**
     * Loads a TREC judgments file.
     *
     * @param filename The filename of the judgments file to load.
     * @return Maps from query numbers to lists of judgments for each query.
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static TreeMap< String, ArrayList<Judgment> > loadJudgments( String filename ) throws IOException, FileNotFoundException {
        // open file
        BufferedReader in = new BufferedReader(new FileReader( filename ));
        String line = null;
        TreeMap< String, ArrayList<Judgment> > judgments = new TreeMap< String, ArrayList<Judgment> >();
        String recentQuery = null;
        ArrayList<Judgment> recentJudgments = null;
        
        while( (line = in.readLine()) != null ) {
            // allow for multiple whitespace characters between fields
            String[] fields = line.split( "\\s+" );
            
            String number = fields[0];
            @SuppressWarnings("unused")
			String unused = fields[1];
            String docno = fields[2];
            String judgment = fields[3];
            int jVal = 0;
            try {
                jVal = Integer.valueOf( judgment );
            } catch (NumberFormatException e) {
                jVal = (int)Math.round(Double.valueOf( judgment ));
            }
            
            Judgment j = new Judgment( docno, jVal );
            
            if( recentQuery == null || !recentQuery.equals( number ) ) {
                if( !judgments.containsKey( number ) ) {
                    judgments.put( number, new ArrayList<Judgment>() );
                }
                
                recentJudgments = judgments.get( number );
                recentQuery = number;
            }
            
            recentJudgments.add( j );
        }

        in.close();
        return judgments;
    }
    
    /**
     * Reads in a TREC ranking file.
     *
     * @param filename The filename of the ranking file.
     * @return A map from query numbers to document ranking lists.
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    @SuppressWarnings("unused")
	public static TreeMap< String, ArrayList<Document> > loadRanking( String filename ) throws IOException, FileNotFoundException {
        // open file
        BufferedReader in = new BufferedReader(new FileReader( filename ));
        String line = null;
        TreeMap< String, ArrayList<Document> > ranking = new TreeMap< String, ArrayList<Document> >();
        ArrayList<Document> recentRanking = null;
        String recentQuery = null;
        
        while( (line = in.readLine()) != null ) {
            // allow for multiple whitespace characters between fields
            String[] fields = line.split( "\\s+" );
            
            // 1 Q0 WSJ880711-0086 39 -3.05948 Exp
                    
            String number = fields[0];
            String unused = fields[1];
            String docno = fields[2];
            String rank = fields[3];
            String score = fields[4];
            String runtag = fields[5];
            // lemur can output nan (or NaN)
            double scoreNumber;
            try {
                scoreNumber = Double.valueOf( score );
            } catch (NumberFormatException ex) {
                scoreNumber = 0.0;
            }
            
            
            Document document = new Document( docno, Integer.valueOf( rank ), scoreNumber );
            
            if( recentQuery == null || !recentQuery.equals( number ) ) {
                if( !ranking.containsKey( number ) ) {
                    ranking.put( number, new ArrayList<Document>() );
                }
                
                recentQuery = number;
                recentRanking = ranking.get( number );       
            }
            
            recentRanking.add( document );
        }
        
        in.close();
        return ranking;
    }

    /**
     * Creates a SetRetrievalEvaluator from data from loadRanking and loadJudgments.
     * @param allRankings 
     * @param allJudgments 
     * @return the evaluation result 
     */
    public static SetRetrievalEvaluator create( TreeMap< String, ArrayList<Document> > allRankings, TreeMap< String, ArrayList<Judgment> > allJudgments ) {
        // Map query numbers into Integer to get proper sorting.
        TreeMap< String, RetrievalEvaluator > evaluators = new TreeMap<String, RetrievalEvaluator>(new java.util.Comparator<String>() {
                @Override
				public int compare(String a, String b) {
                    try {
                        Integer a1 = new Integer(a);
                        Integer b1 = new Integer(b);
                        return a1.compareTo(b1);
                    } catch (NumberFormatException e) {
                        // not an integer
                        return a.compareTo(b);
                    }}});

        for( String query : allRankings.keySet() ) {
            ArrayList<Judgment> judgments = allJudgments.get( query );
            ArrayList<Document> ranking = allRankings.get( query );

            /* resort ranking on score, renumber ranks */
            java.util.Collections.sort(ranking, new java.util.Comparator<Document>() {
                    @Override
					public int compare(Document a, Document b) 
                    {
                        if (a.score < b.score) return 1;
                        if (a.score == b.score) return 0;
                        return -1;
                    }
                });
            int i = 1;
            for (Document d : ranking) {
                d.rank = i++;
            }
            
            if( judgments == null || ranking == null ) {
                continue;
            }
            
            RetrievalEvaluator evaluator = new RetrievalEvaluator( query, ranking, judgments );
            evaluators.put( query, evaluator );
        }
        
        return new SetRetrievalEvaluator( evaluators.values() );
    }

    /**
     * Returns an output string very similar to that of trec_eval.  
     * @param query 
     * @param evaluator 
     * @return the result as a {@link String}
     */
    public static String singleQuery( String query, RetrievalEvaluator evaluator ) {
        StringWriter s = new StringWriter();
        PrintWriter out = new PrintWriter(s);
        String formatString = "%2$-25s\t%1$5s\t";
        // print trec_eval relational-style output
        // counts
        out.format( formatString + "%3$6d\n",        query, "num_ret",     evaluator.retrievedDocuments().size() );
        out.format( formatString + "%3$6d\n",        query, "num_rel",     evaluator.relevantDocuments().size() );
        out.format( formatString + "%3$6d\n",        query, "num_rel_ret", evaluator.relevantRetrievedDocuments().size() );

        // aggregate measures
        out.format( formatString + "%3$6.4f\n",     query, "map",         evaluator.averagePrecision() );
        out.format( formatString + "%3$6.4f\n",     query, "ndcg",        evaluator.normalizedDiscountedCumulativeGain() );
        out.format( formatString + "%3$6.4f\n",     query, "ndcg15",      evaluator.normalizedDiscountedCumulativeGain( 15 ) );
        out.format( formatString + "%3$6.4f\n",     query, "R-prec",      evaluator.rPrecision() );
        out.format( formatString + "%3$6.4f\n",     query, "bpref",       evaluator.binaryPreference() );
        out.format( formatString + "%3$6.4f\n",     query, "recip_rank",  evaluator.reciprocalRank() );

        // precision at fixed points
        int[] fixedPoints = RetrievalEvaluator.getFixedPoints();
        double [] vals = evaluator.precisionAtFixedPoints();
        for( int i=0; i<fixedPoints.length; i++ ) {
            int point = fixedPoints[i];
            out.format( formatString + "%3$6.4f\n", query, "P" + point,  vals[i] );
        }
        double[] precs = evaluator.interpolatedPrecision();
        double prec = 0;
        for( int i=0; i<precs.length; i++ ) {
            out.format( "ircl_prn.%3$3.2f%2$-18s\t%1$5s\t%4$6.4f\n", query, " ", prec, precs[i]  );
            prec += 0.1;
        }
        out.format("\n");
        return s.toString();
    }

    /**
     * Returns an output string very similar to that of trec_eval.  
     * @param setEvaluator 
     * @param showIndividual 
     * @return the result as a {@link String}
     */
    public static String singleEvaluation( SetRetrievalEvaluator setEvaluator, boolean showIndividual ) {
        StringWriter s = new StringWriter();
        PrintWriter out = new PrintWriter(s);
        String formatString = "%2$-25s\t%1$5s\t";
        // print trec_eval relational-style output
        if (showIndividual) {
            for( RetrievalEvaluator evaluator : setEvaluator.getEvaluators() ) {
                String query = evaluator.queryName();
                out.print(singleQuery(query, evaluator));
            }
        }
        // print summary data
        out.format( formatString + "%3$6d\n",      "all", "num_q",     setEvaluator.getEvaluators().size() );
        out.format( formatString + "%3$6d\n",      "all", "num_ret",     setEvaluator.numberRetrieved() );
        out.format( formatString + "%3$6d\n",      "all", "num_rel",     setEvaluator.numberRelevant() );
        out.format( formatString + "%3$6d\n",      "all", "num_rel_ret", setEvaluator.numberRelevantRetrieved() );

        out.format( formatString + "%3$6.4f\n",   "all", "map",         setEvaluator.meanAveragePrecision() );
        out.format( formatString + "%3$6.4f\n",   "all", "gm_ap",         setEvaluator.geometricMeanAveragePrecision() );
        out.format( formatString + "%3$6.4f\n",   "all", "ndcg",        setEvaluator.meanNormalizedDiscountedCumulativeGain() );
        out.format( formatString + "%3$6.4f\n",   "all", "R-prec",      setEvaluator.meanRPrecision() );
        out.format( formatString + "%3$6.4f\n",   "all", "bpref",       setEvaluator.meanBinaryPreference() );
        out.format( formatString + "%3$6.4f\n",   "all", "recip_rank",  setEvaluator.meanReciprocalRank() );

        // precision at fixed points
        int[] fixedPoints = SetRetrievalEvaluator.getFixedPoints();
        double [] precs = setEvaluator.precisionAtFixedPoints();

        for( int i=0; i<fixedPoints.length; i++ ) {
            int point = fixedPoints[i];
            out.format( formatString + "%3$6.4f\n", "all", "P" + point,   precs[i] );
        }
        double prec = 0;
        precs = setEvaluator.interpolatedPrecision();
        for( int i=0; i<precs.length; i++ ) {
            out.format( "ircl_prn.%3$3.2f%2$-18s\t%1$5s\t%4$6.4f\n", "all", " ", prec, precs[i]  );
            prec += 0.1;
        }
        out.format("\n");
        return s.toString();
    }

    /**
     * Compare two sets of retrieval results.
     * 
     * @param baseline
     * @param treatment
     * @param baselineName
     * @param treatmentName
     * @return the result as a {@link String}.
     */
    public static String comparisonEvaluation( SetRetrievalEvaluator baseline, SetRetrievalEvaluator treatment, String baselineName, String treatmentName ) {

        StringWriter s = new StringWriter();
        PrintWriter out = new PrintWriter(s);
        String[] metrics = { "averagePrecision", "rPrecision", "ndcg", "bpref", "P5", "P10", "P20" };
        String formatString = "%1$-20s%2$-30s%3$6.4f\n";
        String integerFormatString = "%1$-20s%2$-30s%3$6d\n";
        out.println("Comparing baseline: " + baselineName + " to treatment: " + treatmentName + "\n");
        if (treatment == null) return "NOPE";
        for( String metric : metrics ) {
            Map<String, Double> baselineMetric = baseline.evaluateAll( metric );
            Map<String, Double> treatmentMetric = treatment.evaluateAll( metric );

            SetRetrievalComparator comparator = new SetRetrievalComparator( baselineMetric, treatmentMetric );

            out.format( formatString, metric, baselineName, comparator.meanBaselineMetric() );
            out.format( formatString, metric, treatmentName, comparator.meanTreatmentMetric() );

            out.format( integerFormatString, metric, "basebetter", comparator.countBaselineBetter() );
            out.format( integerFormatString, metric, "treatbetter", comparator.countTreatmentBetter() );
            out.format( integerFormatString, metric, "equal", comparator.countEqual() );

            out.format( formatString, metric, "ttest", comparator.pairedTTest() );
            out.format( formatString, metric, "randomized", comparator.randomizedTest() );
            out.format( formatString, metric, "signtest", comparator.signTest() );
        }

        return s.toString();
    }

    
    /**
     * Print tool usage
     */
    public static void usage( ) {
        System.err.println( "ireval: " );
        System.err.println( "   There are two ways to use this program.  First, you can evaluate a single ranking: " );
        System.err.println( "      java -jar ireval.jar TREC-Ranking-File TREC-Judgments-File" );
        System.err.println( "   or, you can use it to compare two rankings with statistical tests: " );
        System.err.println( "      java -jar ireval.jar TREC-Baseline-Ranking-File TREC-Improved-Ranking-File TREC-Judgments-File" );
        System.exit(-1);
    }
    
    /**
     * Tool main method.
     * @param args
     * @throws IOException
     */
    public static void main( String[] args ) throws IOException {
        try {
            if( args.length == 3 ) {
                TreeMap< String, ArrayList<Document> > baselineRanking = loadRanking( args[0] );
                TreeMap< String, ArrayList<Document> > treatmentRanking = loadRanking( args[1] );
                TreeMap< String, ArrayList<Judgment> > judgments = loadJudgments( args[2] );

                SetRetrievalEvaluator baseline = create( baselineRanking, judgments );
                SetRetrievalEvaluator treatment = create( treatmentRanking, judgments );

                // adjust these to file names.
                System.out.println(comparisonEvaluation( baseline, treatment, "baseline", "treatment" ));
            } else if( args.length == 2 ) {
                TreeMap< String, ArrayList<Document> > ranking = loadRanking( args[0] );
                TreeMap< String, ArrayList<Judgment> > judgments = loadJudgments( args[1] );

                SetRetrievalEvaluator setEvaluator = create( ranking, judgments );
                System.out.println(singleEvaluation( setEvaluator, true ));
            } else {
                usage();
            }
        } catch( Exception e ) {
            e.printStackTrace();
            usage();
        }
    }
}
