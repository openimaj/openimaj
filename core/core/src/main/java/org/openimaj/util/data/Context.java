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
package org.openimaj.util.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A Context is a {@link Map} which can give typed elements and can fail
 * gracefully when elements don't exist.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class Context extends HashMap<String, Object> implements Cloneable {
	private static final long serialVersionUID = 1888665727867672296L;

	private boolean failfast = false;

	/**
	 * Default constructor to make an empty {@link Context}. The Context is
	 * configured to return <code>null</code> from {@link #getTyped(String)} if
	 * there is a casting issue or the object doesn't exist.
	 */
	public Context() {

	}

	/**
	 * A conveniance function where nameval.length % 2 is assumed to == 0 and
	 * nameval contains pairs of "key" : value instances
	 * 
	 * @param nameval
	 */
	public Context(Object... nameval) {
		for (int i = 0; i < nameval.length; i += 2) {
			this.put(nameval[i].toString(), nameval[i + 1]);
		}
	}

	/**
	 * Construct an empty {@link Context}. The Context can optionally be
	 * configured to either return <code>null</code> from
	 * {@link #getTyped(String)} or throw a {@link RuntimeException} if there is
	 * a casting issue or the object doesn't exist.
	 *
	 * @param failfast
	 *            forces the getTyped to throw a runtime exception if set and
	 *            the object does not exist or is not the correct type
	 */
	public Context(boolean failfast) {
		this.failfast = failfast;
	}

	/**
	 * Get the object from the context with the given key and coerce the type.
	 *
	 * @param key
	 *            the key
	 * @return the object with the given key coerced to the specific return type
	 */
	public <T> T getTyped(String key) {
		final Object retUntyped = this.get(key);

		if (retUntyped == null) {
			if (failfast)
				throw new RuntimeException(new NoSuchElementException("Object not found"));
			return null;
		}

		try {
			@SuppressWarnings("unchecked")
			final T ret = (T) retUntyped;

			return ret;
		} catch (final Throwable t) {
			if (failfast)
				throw new RuntimeException(t);

			return null;
		}
	}

	@Override
	public Context clone() {
		final Context c = new Context();
		for (final java.util.Map.Entry<String, Object> es : this.entrySet()) {
			c.put(es.getKey(), es.getValue());
		}
		return c;
	}

	/**
	 * Combine this {@link Context} with another context by modifying any shared
	 * keys of both contexts to be prefixed with the given prefixes and then
	 * copying all the data from the given {@link Context} into this one.
	 * <p>
	 * If both prefixes are the same then the data being copied from the other
	 * context will have precedence. The prefixes can be <code>null</code>.
	 *
	 * @param that
	 *            the context to combine with this
	 * @param thisprefix
	 *            the prefix for keys in this context
	 * @param thatprefix
	 *            the prefix for keys in the other context
	 * @return combined context
	 */
	public Context combine(Context that, String thisprefix, String thatprefix) {
		final Context combined = new Context();

		final HashSet<String> sharedKeys = new HashSet<String>(this.keySet());
		sharedKeys.retainAll(that.keySet());

		final HashSet<String> thiskeys = new HashSet<String>(this.keySet());
		thiskeys.removeAll(sharedKeys);

		final HashSet<String> thatkeys = new HashSet<String>(that.keySet());
		thatkeys.removeAll(sharedKeys);

		if (thisprefix == null)
			thisprefix = "";
		if (thatprefix == null)
			thatprefix = "";

		// Add the prefix
		for (final String key : sharedKeys) {
			combined.put(thisprefix + key, this.get(key));
			combined.put(thatprefix + key, that.get(key));
		}

		for (final String key : thatkeys) {
			combined.put(key, that.get(key));
		}

		for (final String key : thiskeys) {
			combined.put(key, this.get(key));
		}

		return combined;
	}
}
