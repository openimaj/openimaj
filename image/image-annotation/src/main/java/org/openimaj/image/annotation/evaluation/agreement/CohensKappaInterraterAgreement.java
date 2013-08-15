/**
 *
 */
package org.openimaj.image.annotation.evaluation.agreement;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.ml.annotation.ScoredAnnotation;

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
	 * 	The input should be a {@link Map} for each rater where the
	 *	keys represent all the subjects that were rated by the raters and
	 *	the values represent the annotations given by the raters.
	 *	Agreement between the raters is determined by
	 *	{@link #equals(Object)} for the INSTANCE type. The scores in the
	 *	{@link ScoredAnnotation} are not used. Annotations for subjects
	 *	which are not in both sets are ignored.
	 *
	 *	@see "http://en.wikipedia.org/wiki/Cohen's_kappa"
	 *
	 * 	@param rater1 The annotations from rater 1
	 * 	@param rater2 The annotations from rater 2
	 *	@return Cohen's Kappa [0,1]
	 */
	public static <K,A> double calculate(
			final Map<K,ScoredAnnotation<A>> rater1,
			final Map<K,ScoredAnnotation<A>> rater2 )
	{
		int totalCount = 0;
		int agreementCount = 0;
		final Map<A,Integer> answerCountsR1 = new HashMap<A, Integer>();
		final Map<A,Integer> answerCountsR2 = new HashMap<A, Integer>();

		for( final K subjectKey : rater1.keySet() )
		{
			// We can only form an agreement if both raters rated this
			// specific subject, so let's check
			if( rater2.keySet().contains( subjectKey ) )
			{
				final ScoredAnnotation<A> r1a = rater1.get( subjectKey );
				final ScoredAnnotation<A> r2a = rater2.get( subjectKey );

				// It's possible that the key exists but is mapped to
				// a null value (for example, if majority voting was used
				// to generate the set and there was no majority).
				if( r1a == null || r2a == null ) continue;

				// Get the answers from the raters
				final A annotation1 = r1a.annotation;
				final A annotation2 = r2a.annotation;

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

		final double PrA = agreementCount / (double)totalCount;

		double PrE = 0;
		for( final A ann : answerCountsR1.keySet() )
		{
			final double PrAnnR1 = answerCountsR1.get(ann)/(double)totalCount;
			final double PrAnnR2 = answerCountsR2.get(ann)/(double)totalCount;
			PrE += PrAnnR1 * PrAnnR2;
		}

		final double kappa = (PrA - PrE) / (1d - PrE);

		return kappa;
	}
}
