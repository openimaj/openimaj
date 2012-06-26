/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
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
	
	/** Any JVM arguments that need to be passed to the demo */
	String[] vmArguments() default {};
}
