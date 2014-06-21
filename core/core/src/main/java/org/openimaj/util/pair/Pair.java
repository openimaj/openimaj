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
package org.openimaj.util.pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Pair represents a generic pair of objects.
 * 
 * @author Jonathon Hare
 * 
 * @param <T>
 *            the class of objects in the pair
 */
public class Pair<T> extends IndependentPair<T, T> {
	/**
	 * Constructs a Pair object with two objects obj1 and obj2
	 * 
	 * @param obj1
	 *            first object in pair
	 * @param obj2
	 *            second objec in pair
	 */
	public Pair(T obj1, T obj2) {
		super(obj1, obj2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return o1 + " -> " + o2;
	}

	/**
	 * Create a pair from the given objects.
	 * 
	 * @param <T>
	 *            Type of objects.
	 * @param t
	 *            The first object.
	 * @param q
	 *            The second object.
	 * @return The pair.
	 */
	public static <T> Pair<T> pair(final T t, final T q) {
		return new Pair<T>(t, q);
	}

	/**
	 * Create a pair list from the given objects.
	 * 
	 * @param <T>
	 *            Type of objects.
	 * @param t
	 *            The list of first objects.
	 * @param q
	 *            The list of second objects.
	 * @return The list of pairs.
	 */
	public static <T> List<Pair<T>> pairList(final List<T> t, final List<T> q) {
		final List<Pair<T>> list = new ArrayList<Pair<T>>(t.size());

		for (int i = 0; i < t.size(); i++) {
			list.add(new Pair<T>(t.get(i), q.get(i)));
		}

		return list;
	}
}
