/**
 *
 */
package org.openimaj.experiment.evaluation.agreement;

import java.util.Map;

/**
 * Calculates the percentage interrater agreement for a given dataset between
 * two (and only two) raters.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 12 Aug 2013
 */
public class PercentageInterraterAgreement
{
	/**
	 * The input should be a {@link Map} for each rater where the keys represent
	 * all the subjects that were rated by the raters and the values represent
	 * the annotations given by the raters. Agreement between the raters is
	 * determined by {@link #equals(Object)} for the INSTANCE type. Annotations
	 * for subjects which are not in both sets are ignored.
	 * 
	 * @param rater1
	 *            The annotations from rater 1
	 * @param rater2
	 *            The annotations from rater 2
	 * @return The percentage agreement
	 */
	public static <K, A> double calculate(
			final Map<K, A> rater1,
			final Map<K, A> rater2)
	{
		int totalCount = 0;
		int agreementCount = 0;

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

				// Keep a running total
				totalCount++;
			}
		}

		final double PrA = agreementCount / (double) totalCount;
		return PrA;
	}
}
