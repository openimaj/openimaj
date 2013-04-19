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
package org.openimaj.rdf.serialize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation for members of classes that can be serialized as relationships
 * in an RDF graph by the {@link RDFSerializer}, this annotation should provide
 * the URI of the predicate to use for the field.
 * <p>
 * For example:
 * <code><pre>
 * 		class PersonClass
 * 		{
 * 			@Predicate("http://example.com/hasFirstName")
 * 			public String firstName = "Jon";
 * 		}
 * </pre></code>
 * ...will be encoded to:
 * <code><pre>
 * 		http://example.com/PersonClass
 * 			http://example.com/hasFirstName "Jon".
 * </pre></code>
 * <p>
 * If this annotation is not used, the {@link RDFSerializer} will, by default,
 * ignore the field.  If the {@link RDFSerializer} is set to automatically create
 * predicates, the predicate will end up being called
 * {@code http://example.com/MyClass_hasFirstName}.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 11 Sep 2012
 * @version $Author$, $Revision$, $Date$
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Predicate {
	/** The URI of the predicate */
	public String value();
}
