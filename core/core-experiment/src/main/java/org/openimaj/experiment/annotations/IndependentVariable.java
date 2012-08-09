package org.openimaj.experiment.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openimaj.experiment.ExperimentContext;
import org.openimaj.experiment.ExperimentRunner;
import org.openimaj.experiment.RunnableExperiment;

/**
 * Annotation for marking independent or controlled variables within a
 * {@link RunnableExperiment} instance. These variables and their values will be
 * recorded automatically within an {@link ExperimentContext} when the
 * {@link RunnableExperiment} is run with the {@link ExperimentRunner}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface IndependentVariable {
	/**
	 * The identifier of the variable. If not set, then it will
	 * be replaced with the name of the field the annotation is
	 * attached to.
	 * @return the identifier of the variable
	 */
	String identifier() default "";
}
