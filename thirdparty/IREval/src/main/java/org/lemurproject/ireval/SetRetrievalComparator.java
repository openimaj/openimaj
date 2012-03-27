/*
 * SetRetrievalComparator.java
 *
 * Created on November 30, 2006, 4:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lemurproject.ireval;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.commons.math.distribution.TDistributionImpl;

/**
 *
 * @author trevor
 */
public class SetRetrievalComparator {
    double[] baseline;
    double[] treatment;
    
    /** Creates a new instance of SetRetrievalComparator */
    public SetRetrievalComparator( Map<String, Double> baseline, Map<String, Double> treatment ) {
        Set<String> commonQueries = new TreeSet<String>( baseline.keySet() );
        commonQueries.retainAll( treatment.keySet() );

        this.baseline = new double[commonQueries.size()];
        this.treatment = new double[commonQueries.size()];
        int i = 0;
        
        for( String key : commonQueries ) {
            this.baseline[i] = baseline.get(key);
            this.treatment[i] = treatment.get(key);
            i++;
        }
    }

    private double mean( double[] numbers ) {
        double sum = 0;
        for( int i=0; i<numbers.length; i++ ) {
            sum += numbers[i];
        }
        
        return sum / (double) numbers.length;
    }
    
    public double meanBaselineMetric() {
        return mean( baseline );
    }
    
    public double meanTreatmentMetric() {
        return mean( treatment );
    }

    public int countTreatmentBetter() {
        int better = 0;

        for( int i=0; i<baseline.length; i++ ) {
            if( baseline[i] < treatment[i] )
                better++;
        }

        return better;
    }

    public int countBaselineBetter() {
        int better = 0;

        for( int i=0; i<baseline.length; i++ ) {
            if( baseline[i] > treatment[i] )
                better++;
        }

        return better;
    }

    public int countEqual() {
        int same = 0;

        for( int i=0; i<baseline.length; i++ ) {
            if( baseline[i] == treatment[i] )
                same++;
        }

        return same;
    }
    
    public double pairedTTest() {       
        double sampleSum = 0;
        double sampleSumSquares = 0;
        int n = baseline.length;
        
        for( int i=0; i<baseline.length; i++ ) {
            double delta = treatment[i] - baseline[i];
            sampleSum += delta;
            sampleSumSquares += delta*delta;
        }
       
        double sampleVariance = sampleSumSquares / (n - 1);
        double sampleMean = sampleSum / baseline.length;
        
        double sampleDeviation = Math.sqrt(sampleVariance);
        double meanDeviation = sampleDeviation / Math.sqrt(n);
        double t = sampleMean / meanDeviation;
        
        try {
			return 1.0 - new TDistributionImpl(n).cumulativeProbability(t);
		} catch (MathException e) {
			throw new RuntimeException(e);
		}
    }
    
    public double signTest() {
        int treatmentIsBetter = 0;
        int different = 0;
        
        for( int i=0; i<treatment.length; i++ ) {
            if( treatment[i] > baseline[i] )
                treatmentIsBetter++;
            if( treatment[i] != baseline[i] )
                different++;
        }
        
        double pvalue;
		try {
			pvalue = 1 - new BinomialDistributionImpl(different, 0.5).cumulativeProbability(treatmentIsBetter - 1);
		} catch (MathException e) {
			throw new RuntimeException(e);
		}
        
        return pvalue;
    }

    public double randomizedTest() {
        double baseMean = mean( baseline );
        double treatmentMean = mean( treatment );
        double difference = treatmentMean - baseMean;
        int batch = 10000;
        
        final int maxIterationsWithoutMatch = 1000000;
        long iterations = 0;
        long matches = 0;
        
        double[] leftSample = new double[baseline.length];
        double[] rightSample = new double[baseline.length];
        Random random = new Random();
        double pValue = 0.0;
        
        while(true) {
            for( int i=0; i<batch; i++ ) {
                // create a sample from both distributions
                for( int j=0; j<baseline.length; j++ ) {
                    if( random.nextBoolean() ) {
                        leftSample[j] = baseline[j];
                        rightSample[j] = treatment[j];
                    } else {
                        leftSample[j] = treatment[j];
                        rightSample[j] = baseline[j];
                    }
                }

                double sampleDifference = mean( leftSample ) - mean( rightSample );

                if( difference <= sampleDifference )
                    matches++;
            }
            
            iterations += batch;

            // this is the current p-value estimate
            pValue = (double) matches / (double) iterations;
            
            // if we still haven't found a match, keep looking
            if( matches == 0 ) {
                if( iterations < maxIterationsWithoutMatch ) {
                    continue;
                } else {
                    break;
                }
            }
            
            // this is our accepted level of deviation in the p-value; we require:
            //      - accuracy at the fourth decimal place, and
            //      - less than 5% error in the p-value, or
            //      - accuracy at the sixth decimal place.
            
            double maxDeviation = Math.max( 0.0000005 / pValue, Math.min( 0.00005 / pValue, 0.05 ) );
            
            // this estimate is derived in Efron and Tibshirani, p.209.
            // this is the estimated number of iterations necessary for convergence, given
            // our current p-value estimate.
            double estimatedIterations = Math.sqrt( pValue * (1.0 - pValue) ) / maxDeviation;

	    //            if( estimatedIterations > iterations )
            if( iterations > estimatedIterations )
                break;

        }
   
        return pValue;
    }
}
