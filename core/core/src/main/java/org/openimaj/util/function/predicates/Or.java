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
package org.openimaj.util.function.predicates;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.function.Predicate;

/**
 * "Or" together 2 or more predicates
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the input type of the predicates
 */
public class Or<T> implements Predicate<T> {
	List<Predicate<T>> predicates;

	/**
	 * Construct an empty "or"
	 */
	public Or() {
		predicates = new ArrayList<Predicate<T>>();
	}

	/**
	 * Construct with the given predicates
	 * 
	 * @param p1
	 *            first predicate
	 * @param p2
	 *            second predicate
	 */
	public Or(Predicate<T> p1, Predicate<T> p2) {
		this();
		predicates.add(p1);
		predicates.add(p2);
	}

	/**
	 * Construct with the given predicates
	 * 
	 * @param p1
	 *            first predicate
	 * @param p2
	 *            second predicate
	 * @param p3
	 *            third predicate
	 */
	public Or(Predicate<T> p1, Predicate<T> p2, Predicate<T> p3) {
		this();
		predicates.add(p1);
		predicates.add(p2);
		predicates.add(p3);
	}

	/**
	 * Construct with the given predicates
	 * 
	 * @param predicates
	 *            the predicates
	 */
	public Or(List<Predicate<T>> predicates) {
		this.predicates = predicates;
	}

	/**
	 * Add a new predicate to this "or"
	 * 
	 * @param p
	 *            the predicate to add
	 * @return this
	 */
	public Or<T> add(Predicate<T> p) {
		predicates.add(p);
		return this;
	}

	@Override
	public boolean test(T object) {
		for (final Predicate<T> p : predicates)
			if (p.test(object))
				return true;
		return false;
	}
}
