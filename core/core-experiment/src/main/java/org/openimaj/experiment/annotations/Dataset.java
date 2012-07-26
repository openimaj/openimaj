package org.openimaj.experiment.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.openimaj.citation.annotation.Reference;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface Dataset {
	String name();
	String description();
	String creator() default "";
	String url() default "";
	String contact() default "";
	
	/**
	 * One or more {@link Reference} annotations
	 * @return One or more {@link Reference} annotations
	 */
	Reference [] references() default {};
}
