package org.openimaj.experiment.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark that you want the execution time
 * of a method to be recorded during an experiment.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface Time {
	/**
	 * Get the identifier of the method. If left as default, this will
	 * be translated to the name of the class and method the annotation is
	 * attached to.
	 * @return the identifier for the method.
	 */
	String identifier() default "";
}
