/**
 * 
 */
package org.openimaj.demos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 	An annotation for annotating packages that contain demos. This provides a
 * 	means for categorizing demos by placing them in annotated packages.
 * 	<p>
 * 	The annotation itself is set within the package-info.java
 * 
 * 	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@created 3 November 2011
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface DemoPackage 
{
	/** The display name of the demo package. No more than 30 characters */
	String   title();

	/** A description of the demo. As long as you like. */
	String   description();
	
	/** Keywords associated with the demo. */
	String[] keywords();
	
	/** The resource URI of an icon to represent the demo [optional] */
	String   icon() default "/defaults/demo.png";
}
