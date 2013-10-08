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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.util.pair.ObjectFloatPair;

/**
 * Majority voting.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 14 Aug 2013
 * @version $Author$, $Revision$, $Date$
 */
public class MajorityVoting
{
	/**
	 * Returns the basic majority vote or null if there is no majority within
	 * the given list of annotations.
	 * 
	 * @param data
	 *            The list of annotations to score
	 * @return The majority annotation or NULL if there is no majority
	 */
	public static <A> ObjectFloatPair<A> calculateBasicMajorityVote(
			List<A> data)
	{
		// Count all the different annotations.
		final HashMap<A, Integer> count = new HashMap<A, Integer>();
		for (final A s : data)
		{
			if (count.get(s) == null)
				count.put(s, 1);
			else
				count.put(s, count.get(s) + 1);
		}

		// Find the maximum annotation
		A majority = null;
		int max = 0;
		for (final Entry<A, Integer> x : count.entrySet())
		{
			// If we found a new max, we have a majority
			if (x.getValue() > max)
			{
				max = x.getValue();
				majority = x.getKey();
			}
			else
			// If we found another one with the same count as max
			// then we no longer have a majority
			if (x.getValue() == max)
			{
				majority = null;
				// We mustn't reset max.
			}
		}

		// We'll return null if there's no majority
		if (majority == null)
			return null;

		// Otherwise we return the majority annotation
		return new ObjectFloatPair<A>(majority,
				count.get(majority) / (float) data.size());
	}

	/**
	 * Calculated the majority vote for a set of subjects, where each subject
	 * has a set of answers. Note that as the majority voting method may return
	 * null in the case of no majority, it is possible that some of the subjects
	 * in the return of this method will have a null map.
	 * 
	 * @param data
	 *            The data
	 * @return The subjects mapped to their majority vote.
	 */
	public static <A> Map<String, ObjectFloatPair<A>>
			calculateBasicMajorityVote(Map<String, List<A>> data)
	{
		final Map<String, ObjectFloatPair<A>> out =
				new HashMap<String, ObjectFloatPair<A>>();

		for (final Entry<String, List<A>> x : data.entrySet())
			out.put(x.getKey(), calculateBasicMajorityVote(x.getValue()));

		return out;
	}
}
