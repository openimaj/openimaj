/**
 * 
 */
package org.openimaj.experiment.evaluation.agreement;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates Fleiss inter-rater agreement - a version of Cohen's kappa that
 * works with multiple raters. Note that it is technially not a kappa as it does
 * not reduce to Cohen's kappa when used with only 2 raters.
 * 
 * @see "http://en.wikipedia.org/wiki/Fleiss'_kappa"
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 16 Aug 2013
 */
public class FleissInterraterAgreement
{
	/**
	 * Calculate Fleiss interrater agreement
	 * 
	 * @see "http://en.wikipedia.org/wiki/Fleiss'_kappa"
	 * @param data
	 *            The rater's annotations
	 * @return The Fleiss Kappa
	 */
	public static <K, A> double calculate(
			List<Map<K, A>> data)
	{
		// Map from subject to category count (the table)
		final Map<K, TObjectIntHashMap<A>> table = new HashMap<K, TObjectIntHashMap<A>>();

		// Total assignments to each category, Pj*Nn
		final TObjectDoubleHashMap<A> cats = new TObjectDoubleHashMap<A>();

		// Total ratings made
		int totalCount = 0;

		// Generate the counts table
		// Loop through all the raters
		for (final Map<K, A> ratings : data)
		{
			// Loop through the subjects that this rater rated
			for (final K subject : ratings.keySet())
			{
				final A annotation = ratings.get(subject);
				if (annotation != null)
				{
					TObjectIntHashMap<A> count = table.get(subject);
					if (count == null)
					{
						count = new TObjectIntHashMap<A>();
						table.put(subject, count);
					}

					count.putIfAbsent(annotation, 0);
					count.increment(annotation);

					cats.putIfAbsent(annotation, 0);
					cats.increment(annotation);

					totalCount++;
				}
			}
		}

		// Normalise Pj by N*n (totalCount) and square (part of working out
		// PeBar)
		final TDoubleArrayList Pj = new TDoubleArrayList();
		for (final A x : cats.keySet())
			Pj.add((cats.get(x) / totalCount) *
					(cats.get(x) / totalCount));

		final int n = data.size();

		final TDoubleArrayList Pis = new TDoubleArrayList();
		for (final K subject : table.keySet())
		{
			double Pi = 0;
			for (final A cat : table.get(subject).keySet())
			{
				final int nij = table.get(subject).get(cat);
				Pi += nij * (nij - 1);
			}
			Pi /= (n * (n - 1));
			Pis.add(Pi);
		}

		final double Pbar = Pis.sum() / Pis.size();
		final double PeBar = Pj.sum();

		final double kappa = (Pbar - PeBar) / (1 - PeBar);
		return kappa;
	}
}
