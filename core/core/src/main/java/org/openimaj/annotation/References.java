package org.openimaj.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for multiple bibtex-style references inside the code.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(value = { ElementType.METHOD, ElementType.TYPE })
public @interface References {
	/**
	 * One or more {@link Reference} annotations
	 * @return One or more {@link Reference} annotations
	 */
	Reference [] references();
}
