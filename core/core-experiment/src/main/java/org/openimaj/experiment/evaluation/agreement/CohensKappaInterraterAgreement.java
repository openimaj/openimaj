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
/**
 *
 */
package org.openimaj.experiment.evaluation.agreement;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Map;

/**
 * Calculates the interrater agreement for a given dataset between two (and only
 * two) raters.
 * <p>
 * Cohen's Kappa is defined as: (PrA - PrE) / (1 - PrE) where PrA is the
 * percentage agreement, and PrE is the probability of random agreement. PrA =
 * agreement / total and PrE = PrX + PrY where PrX and PrY are the probability
 * of both agreeing on X or both agreeing on Y randomly (that is,
 * Pr(r1,x)*Pr(r2,x) ... )
 * 
 * @see "http://en.wikipedia.org/wiki/Cohen's_kappa"
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 12 Aug 2013
 */
public class CohensKappaInterraterAgreement
{
	/**
	 * The input should be a {@link Map} for each rater where the keys represent
	 * all the subjects that were rated by the raters and the values represent
	 * the annotations given by the raters. Agreement between the raters is
	 * determined by {@link #equals(Object)} for the INSTANCE type. Annotations
	 * for subjects which are not in both sets are ignored.
	 * 
	 * @see "http://en.wikipedia.org/wiki/Cohen's_kappa"
	 * 
	 * @param rater1
	 *            The annotations from rater 1
	 * @param rater2
	 *            The annotations from rater 2
	 * @return Cohen's Kappa [0,1]
	 */
	public static <K, A> double calculate(
			final Map<K, A> rater1,
			final Map<K, A> rater2)
	{
		int totalCount = 0;
		int agreementCount = 0;
		final TObjectIntHashMap<A> answerCountsR1 = new TObjectIntHashMap<A>();
		final TObjectIntHashMap<A> answerCountsR2 = new TObjectIntHashMap<A>();

		for (final K subjectKey : rater1.keySet())
		{
			// We can only form an agreement if both raters rated this
			// specific subject, so let's check
			if (rater2.keySet().contains(subjectKey))
			{
				final A r1a = rater1.get(subjectKey);
				final A r2a = rater2.get(subjectKey);

				// It's possible that the key exists but is mapped to
				// a null value (for example, if majority voting was used
				// to generate the set and there was no majority).
				if (r1a == null || r2a == null)
					continue;

				// Get the answers from the raters
				final A annotation1 = r1a;
				final A annotation2 = r2a;

				// Count the agreements
				if (annotation1.equals(annotation2))
					agreementCount++;

				// Count each of the answers for each of the raters
				answerCountsR1.putIfAbsent(annotation1, 0);
				answerCountsR2.putIfAbsent(annotation2, 0);
				answerCountsR1.increment(annotation1);
				answerCountsR2.increment(annotation2);

				// Keep a running total
				totalCount++;
			}
		}

		System.out.println(answerCountsR1);

		final double PrA = agreementCount / (double) totalCount;
		System.out.println(PrA);

		double PrE = 0;
		for (final A ann : answerCountsR1.keySet())
		{
			final Integer i = answerCountsR2.get(ann);
			final double PrAnnR1 = answerCountsR1.get(ann) / (double) totalCount;
			final double PrAnnR2 = (i == null ? 0 : i) / (double) totalCount;
			PrE += PrAnnR1 * PrAnnR2;
		}
		System.out.println(PrE);

		final double kappa = (PrA - PrE) / (1d - PrE);

		return kappa;
	}
}
