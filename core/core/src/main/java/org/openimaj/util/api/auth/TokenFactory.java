package org.openimaj.util.api.auth;

/**
 * Interface describing a class capable of providing API tokens. See the
 * {@link DefaultTokenFactory} for an implementation that stores tokens in the
 * Java Preference system and can interactively query the user for the required
 * parameters if they don't have a saved token.
 * <p>
 * The tokens produced by a {@link TokenFactory} must be instances of a class
 * which is annotated with {@link Token}, and with fields annotated with
 * {@link Parameter}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface TokenFactory {
	/**
	 * Get the default token for the given class.
	 * <p>
	 * If a predefined token is unavailable, the token might be built by the
	 * underlying implementation; for example by interacting with the user.
	 * 
	 * @param tokenClass
	 *            the class of the token to build
	 * @return the token
	 */
	<T> T getToken(Class<T> tokenClass);

	/**
	 * Get the token for the given class, tagged with a specific name. This is
	 * useful if you have are multiple authentication tokens for the same API
	 * and want to refer to a specific one.
	 * <p>
	 * If a predefined token is unavailable, the token might be built by the
	 * underlying implementation; for example by interacting with the user.
	 * 
	 * @param tokenClass
	 *            the class of the token to build
	 * @param name
	 *            the name of the token
	 * @return the token
	 */
	<T> T getToken(Class<T> tokenClass, String name);
}
