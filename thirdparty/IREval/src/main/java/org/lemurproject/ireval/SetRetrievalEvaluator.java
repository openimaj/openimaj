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
 * SetRetrievalEvaluator
 *
 * Use of this software is governed by the Creative Commons Attribution license:
 *    http://creativecommons.org/licenses/by/2.5/
 *
 * Trevor Strohman, September 23, 2005
 */

package org.lemurproject.ireval;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Computes summary statistics over a set of queries.
 *
 * @author Trevor Strohman
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SetRetrievalEvaluator {
    Collection<RetrievalEvaluator> _evaluators;
    
    /** 
     * Creates a new instance of SetRetrievalEvaluator 
     * @param evaluators 
     */
    public SetRetrievalEvaluator( Collection<RetrievalEvaluator> evaluators ) {
        _evaluators = evaluators;
    }
    
    /**
     * @return Returns a collection of evaluators.
     */
    public Collection<RetrievalEvaluator> getEvaluators() {
        return _evaluators;
    }
    
    /**
     * @return the levels at which precision is calculated
     */
    public static int[] getFixedPoints() {
    	return RetrievalEvaluator.getFixedPoints();
    }
    
    /**
     * @return the precision at fixed values
     */
    public double[] precisionAtFixedPoints() {
    	// precision at fixed points
    	int []fixedPoints = RetrievalEvaluator.getFixedPoints();
        double[] retVal = new double [fixedPoints.length];
        int i = 0;
        for( int point : fixedPoints ) {
            retVal[i++] = meanPrecision( point );
        }
        return retVal;
    }
    
    /**
     * @return the interpolated precision values
     */
    public double[] interpolatedPrecision() {
    	double[] precs = {0,0,0,0,0,0,0,0,0,0,0};
    	for (RetrievalEvaluator evaluator : _evaluators) {
            double[] vals = evaluator.interpolatedPrecision();
            for (int j = 0; j < vals.length; j++) precs[j] += vals[j];
    	}
    	for (int j = 0; j < precs.length; j++) precs[j] /= _evaluators.size();
    	return precs;
    }
    /**
     * @return the mean average precision; the mean of the average precision values for all queries.
     */
    public double meanAveragePrecision() {
        double sumAveragePrecision = 0;
        if (_evaluators.size() == 0) return 0;

        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumAveragePrecision += evaluator.averagePrecision();
        }
        
        return sumAveragePrecision / (double) _evaluators.size();
    }
    
    /**
     * @return the mean of the binary preference values for all queries.
     */
    public double meanBinaryPreference() {
        double sumBinaryPreference = 0;
        if (_evaluators.size() == 0) return 0;
        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumBinaryPreference += evaluator.binaryPreference();
        }
        
        return sumBinaryPreference / (double) _evaluators.size();
    }

    /**
     * @return the geometric mean of average precision values for all queries.
     */
    public double geometricMeanAveragePrecision() {
        double productAveragePrecision = 1.0;
        if (_evaluators.size() == 0) return 0;

        for( RetrievalEvaluator evaluator : _evaluators ) {
            double p = evaluator.averagePrecision();
            // don't let a 0 value into the product.
            if (p > 0.0)
                productAveragePrecision *= p;
        }
        return Math.pow( productAveragePrecision, 1.0 / _evaluators.size() );
    }
    
    /**
     * @param documentsRetrieved 
     * @return the mean of the precision values for all queries.
     */
    public double meanPrecision( int documentsRetrieved ) {
        double sumPrecision = 0;
        if (_evaluators.size() == 0) return 0;

        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumPrecision += evaluator.precision( documentsRetrieved );
        }
        
        return sumPrecision / _evaluators.size();
    }
    
    /** 
     * @return the mean of the reciprocal rank values for all queries.
     */
    public double meanReciprocalRank( ) {
        double sumRR = 0;
        if (_evaluators.size() == 0) return 0;
        
        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumRR += evaluator.reciprocalRank();
        }
        
        return sumRR / _evaluators.size();
    }
    
    /** 
     * @return the mean of the R-precision values for all queries.
     */
    public double meanRPrecision( ) {
        double sumRPrecision = 0;
        if (_evaluators.size() == 0) return 0;
        
        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumRPrecision += evaluator.rPrecision();
        }
        
        return sumRPrecision / _evaluators.size();
    }  
    
    /**
     * @return the mean of the NDCG values for all queries.
     */
    public double meanNormalizedDiscountedCumulativeGain( ) {
        double sumNDCG = 0;
        if (_evaluators.size() == 0) return 0;
        
        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumNDCG += evaluator.normalizedDiscountedCumulativeGain();
        }                                                             
        
        return sumNDCG / _evaluators.size();
    }

    /**
     * @param documentsRetrieved 
     * @return the mean of the NDCG values for all queries.
     */
    public double meanNormalizedDiscountedCumulativeGain( int documentsRetrieved ) {
        double sumNDCG = 0;
        if (_evaluators.size() == 0) return 0;
        
        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumNDCG += evaluator.normalizedDiscountedCumulativeGain( documentsRetrieved );
        }                                                             
        
        return sumNDCG / _evaluators.size();
    }

    /**
     * Get a {@link Map} containing a particular metric value for each query.
     * For instance, if metric == "averagePrecision", this returns 
     * a map where the keys are query identifiers and the values are the
     * average precision metric evaluated for each query.
     * 
     * @param metric the metric name.
     * 
     * @return A {@link Map} containing a particular metric value for each query.
     * @throws IllegalArgumentException 
     */
    public Map<String, Double> evaluateAll( String metric ) throws IllegalArgumentException {
        TreeMap<String, Double> result = new TreeMap<String, Double>();
        
        for( RetrievalEvaluator evaluator : _evaluators ) {
            double value = 0;
            
            if( metric.equals("averagePrecision") || metric.equals("ap") || metric.equals("map") ) {
                value = evaluator.averagePrecision();
            } else if( metric.equals("ndcg") ) {
                value = evaluator.normalizedDiscountedCumulativeGain();
            } else if( metric.equals("reciprocalRank") || metric.equals("mrr") ) {
                value = evaluator.reciprocalRank();
            } else if( metric.equals("rPrecision") ) {
                value = evaluator.rPrecision();
            } else if( metric.equals( "bpref" ) ) {
                value = evaluator.binaryPreference();
            } else if( metric.startsWith( "P" ) ) {
                value = evaluator.precision( Integer.parseInt( metric.substring(1) ) );
            } else if( metric.startsWith( "R" ) ) {
                value = evaluator.recall( Integer.parseInt( metric.substring(1) ) );
            } else {
                throw new IllegalArgumentException( "Unknown metric: " + metric );
            }
            
            result.put( evaluator.queryName(), value );
        }
        
        return result;
    }

    /**
     * @return The number of documents retrieved for all queries.
     */
    public int numberRetrieved() {
        int sumRetrieved = 0;
        
        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumRetrieved += evaluator.retrievedDocuments().size();
        }
        
        return sumRetrieved;
    }
    
    /**
     * @return The total number of relevant documents to any of the queries.
     */
    public int numberRelevant() {
        int sumRelevant = 0;
        
        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumRelevant += evaluator.relevantDocuments().size();
        }
        
        return sumRelevant;
    }
    
    /**
     * @return The total number of relevant documents retrieved for any of the queries.
     */
    public int numberRelevantRetrieved() {
        int sumRelevantRetrieved = 0;
        
        for( RetrievalEvaluator evaluator : _evaluators ) {
            sumRelevantRetrieved += evaluator.relevantRetrievedDocuments().size();
        }
        
        return sumRelevantRetrieved;
    }
    
    @Override
	public String toString() {
    	return IREval.singleEvaluation(this, true);
    }
}
