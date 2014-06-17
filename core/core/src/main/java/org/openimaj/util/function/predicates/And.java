package org.openimaj.util.function.predicates;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.function.Predicate;

/**
 * "And" together 2 or more predicates
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the input type of the predicates
 */
public class And<T> implements Predicate<T> {
	List<Predicate<T>> predicates;

	/**
	 * Construct an empty "and"
	 */
	public And() {
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
	public And(Predicate<T> p1, Predicate<T> p2) {
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
	public And(Predicate<T> p1, Predicate<T> p2, Predicate<T> p3) {
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
	public And(List<Predicate<T>> predicates) {
		this.predicates = predicates;
	}

	/**
	 * Add a new predicate to this "and"
	 * 
	 * @param p
	 *            the predicate to add
	 * @return this
	 */
	public And<T> add(Predicate<T> p) {
		predicates.add(p);
		return this;
	}

	@Override
	public boolean test(T object) {
		for (final Predicate<T> p : predicates)
			if (!p.test(object))
				return false;
		return true;
	}
}
