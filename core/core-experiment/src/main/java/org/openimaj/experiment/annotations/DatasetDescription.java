package org.openimaj.experiment.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openimaj.experiment.ExperimentContext;
import org.openimaj.experiment.dataset.Dataset;

/**
 * An annotation for marking up a specific {@link Dataset} subclass
 * with metadata about the dataset.
 * <p>
 * The {@link ExperimentContext} can use this metadata in any reports
 * that it generates.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface DatasetDescription {
	/**
	 * @return the name of the dataset 
	 */
	String name();
	
	/**
	 * @return a description of the dataset
	 */
	String description();
	
	/**
	 * @return the creator of the dataset
	 */
	String creator() default "";
	
	/**
	 * @return a URL to information about the dataset
	 */
	String url() default "";
	
	/**
	 * @return a contact person/site for the dataset
	 */
	String contact() default "";
}
