package org.openimaj.experiment.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openimaj.citation.annotation.Reference;

@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface DatasetDescription {
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
