/**
 * 
 */
package org.openimaj.image.annotation.evaluation.agreement;

import java.util.Map;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;

/**
 *	Calculates the percentage interrater agreement for a given dataset between two
 *	(and only two) raters. 
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Aug 2013
 */
public class PercentageInterraterAgreement
{
	/**
	 * 	The input should be a {@link Map} where the
	 *	keys represent all the subjects that were rated by the raters. Each
	 *	group should contain an {@link IndependentPair}. It is expected
	 *	that the order of the pair is the same for every group, rater 1 followed
	 *	by rater 2. Agreement between the raters is determined by 
	 *	{@link #equals(Object)} for the INSTANCE type. The scores in the	
	 *	{@link ScoredAnnotation} are not used.
	 *
 	 *	@param data
	 *	@return
	 */
	public static <K,A> double calculate( 
			Map<K,ScoredAnnotation<A>> rater1,
			Map<K,ScoredAnnotation<A>> rater2 )
	{
		int totalCount = 0;
		int agreementCount = 0;

		for( K subjectKey : rater1.keySet() )
		{
			// We can only form an agreement if both raters rated this
			// specific subject, so let's check
			if( rater2.keySet().contains( subjectKey ) )
			{
				// Get the answers from the raters
				A annotation1 = rater1.get( subjectKey ).annotation;
				A annotation2 = rater2.get( subjectKey ).annotation;
				
				// Count the agreements
				if( annotation1.equals( annotation2 ) )
					agreementCount++;

				// Keep a running total
				totalCount++;
			}
		}
			
		double PrA = agreementCount / (double)totalCount;
		return PrA;
	}
}
