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
package org.openimaj.rdf.serialize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation will force a collection object to be output as an
 * RDF collection (or sequence) rather than as individual triples when using
 * the {@link RDFSerializer} to serialise objects.
 * <p>
 * By default the {@link RDFSerializer} will output collections in an unordered
 * way, linked by the predicate for the field.
 * <code><pre>
 * 		@Predicate("http://example.com/hasString")
 * 		String[] strings = new String[] { "one", "two" };
 * </pre></code>
 * ...will be output, by default, as:
 * <code><pre>
 * 	http://example.com/object
 * 			http://example.com/hasString "one";
 * 			http://example.com/hasString "two".
 * </pre></code>
 * <p>
 * When the {@link RDFCollection} annotation is used.
 * the triples are encoded using RDF sequences;
 * that is as subgraphs where the items have the predicates
 * <code>rdf:_1, rdf:_2..., rdf:_n</code> and the type <code>rdf:Seq</code>.
 * This retains the same order for the collection as when serialised.
 * So the above example would be output as:
 * <code><pre>
 *	http://example.com/object
 *		http://example.com/hasString http://example.com/strings .
 *	http://example.com/strings
 *		rdf:type	rdf:Seq;
 *		rdf:_1	"one";
 *		rdf:_2	"two".
 * </pre></code>
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 15 Nov 2012
 *	@version $Author$, $Revision$, $Date$
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RDFCollection
{
}
