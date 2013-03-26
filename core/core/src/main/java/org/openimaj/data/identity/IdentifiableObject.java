package org.openimaj.data.identity;

/**
 * A simple implementation of {@link Identifiable} that wraps another object.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 */
public class IdentifiableObject<T> implements Identifiable {
	/**
	 * The identity
	 */
	public final String identity;

	/**
	 * The data
	 */
	public final T data;

	/**
	 * Construct with the given identity and data.
	 * 
	 * @param ident
	 *            the identity
	 * @param data
	 *            the data to wrap
	 */
	public IdentifiableObject(String ident, T data) {
		this.identity = ident;
		this.data = data;
	}

	@Override
	public String getID() {
		return identity;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Identifiable))
			return false;

		return identity.equals(((Identifiable) obj).getID());
	}
}
