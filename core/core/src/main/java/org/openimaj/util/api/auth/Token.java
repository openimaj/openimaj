package org.openimaj.util.api.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata describing an API token
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
public @interface Token {
	/**
	 * Get the name of the API with which this token is used
	 * 
	 * @return the name of the API with which this token is used
	 */
	String name();

	/**
	 * Get any additional information about this token or the mechanism by which
	 * a user should go about getting the parameters. This might displayed to
	 * the user by a {@link TokenFactory} when querying the user for the
	 * {@link Parameter}s required to access the API.
	 * 
	 * @return additional information about the API and getting the required
	 *         {@link Parameter}s.
	 */
	String extraInfo() default "";

	/**
	 * Get the URL at which the API parameters can be obtained (usually by the
	 * user registering for an account, etc).
	 * 
	 * @return the URL at which the API parameters can be obtained.
	 */
	String url();
}
