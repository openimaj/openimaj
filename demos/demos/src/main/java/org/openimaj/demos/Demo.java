/**
 * 
 */
package org.openimaj.demos;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 	An annotation for types that are implementing an OpenIMAJ demo. Use this
 * 	annotation to specify that your class should be considered an OpenIMAJ
 * 	demo. The {@link Demos} GUI will look for types that are annotated with
 * 	this annotation for presenting to the user as a self-contained demo.
 * 	<p>
 * 	It is quite important that any classes that are annotated with this type
 * 	are fully self-contained and do not have hard-coded, local filenames. They
 * 	should use resources and the resources should be included.
 * 
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@created 2 November 2011
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Demo 
{
	/** The title of the demo. Should be short. No more than 30 characters */
	String   title();

	/** Name of the author of the demo */
	String   author();
	
	/** A description of the demo. As long as you like. */
	String   description();
	
	/** Keywords associated with the demo. */
	String[] keywords();
	
	/** The resource URI of an icon to represent the demo [optional] */
	String   icon() default "/defaults/demo.png";
	
	/** The resource URI of a screenshot of the demo [optional] */
	String   screenshot() default "/defaults/screenshot.png";
	
	/** Any arguments that need to be passed to the demo */
	String[] arguments() default {};
}
