/**
 * 
 */
package org.openimaj.image.annotation.evaluation.agreement;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;

/**
 *	Calculates the interrater agreement for a given dataset between two
 *	(and only two) raters. 
 *	<p>
 *	Cohen's Kappa is defined as:
 *		(PrA - PrE) / (1 - PrE)
 *	where PrA is the percentage agreement, and PrE is the probability of
 *	random agreement. 
 *		PrA = agreement / total
 *	and
 *		PrE = PrX + PrY
 *	where PrX and PrY are the probability of both agreeing on X or both
 *	agreeing on Y randomly (that is, Pr(r1,x)*Pr(r2,x) ... ) 
 *
 *	@see "http://en.wikipedia.org/wiki/Cohen's_kappa"
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Aug 2013
 */
public class CohensKappaInterraterAgreement
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
	 *	@see "http://en.wikipedia.org/wiki/Cohen's_kappa"
	 *
	 * 	@param rater1 
	 * 	@param rater2 
	 *	@return
	 */
	public static <K,A> double calculate( 
			Map<K,ScoredAnnotation<A>> rater1,
			Map<K,ScoredAnnotation<A>> rater2 )
	{
		int totalCount = 0;
		int agreementCount = 0;
		Map<A,Integer> answerCountsR1 = new HashMap<A, Integer>();
		Map<A,Integer> answerCountsR2 = new HashMap<A, Integer>();

		for( K subjectKey : rater1.keySet() )
		{
			// We can only form an agreement if both raters rated this
			// specific subject, so let's check
			if( rater2.keySet().contains( subjectKey ) )
			{
				// Get the answers from the raters
				A annotation1 = rater1.get( subjectKey ).annotation;
				A annotation2 = rater2.get( subjectKey ).annotation;
				
				System.out.println( totalCount+": Comparing: "+annotation1+" <-> "+annotation2 );

				// Count the agreements
				if( annotation1.equals( annotation2 ) )
					agreementCount++;
				
				// Count each of the answers for each of the raters
				// First rater 1
				Integer a;
				if( (a = answerCountsR1.get(annotation1)) == null )
						answerCountsR1.put( annotation1, 1 );
				else	answerCountsR1.put( annotation1, a+1 );
				// then rater 2
				if( (a = answerCountsR2.get(annotation2)) == null )
						answerCountsR2.put( annotation2, 1 );
				else	answerCountsR2.put( annotation2, a+1 );
				
				// Keep a running total
				totalCount++;
			}
		}
		
		double PrA = agreementCount / (double)totalCount;
		
		double PrE = 0;
		for( A ann : answerCountsR1.keySet() )
		{
			double PrAnnR1 = answerCountsR1.get(ann)/(double)totalCount;
			double PrAnnR2 = answerCountsR2.get(ann)/(double)totalCount;
			PrE += PrAnnR1 * PrAnnR2;
		}
		
		double kappa = (PrA - PrE) / (1d - PrE);
		
		return kappa;
	}
}
