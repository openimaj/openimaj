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
