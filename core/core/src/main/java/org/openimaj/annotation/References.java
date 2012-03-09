package org.openimaj.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for multiple bibtex-style references inside the code.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface References {
	/**
	 * One or more {@link Reference} annotations
	 * @return One or more {@link Reference} annotations
	 */
	Reference [] references();
}
