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
 *	An annotation that marks a member of a class that must be an instance of
 *	Collection<IndependentPair<URI,Object>>
 *	to be treated specially during serialisation. When it is encountered,
 *	the relations stored in the list will be added to the graph without validation.
 *	The first object in the pair should be the predicate URI and the second object
 *	will be serialised by the {@link RDFSerializer} to its own subgraph.
 *	<p>
 *	For example:
 *	<code><pre>
 *		class MyObject
 *		{
 *			@RelationList
 *			public ArrayList<IndependentPair<URI,Object>> unvalidatedRelations;
 *
 *			MyObject()
 *			{
 *				unvalidatedRelations = new ArrayList<>();
 *				unvalidatedRelations.add( new IndependentPair<>(
 *					new URIImpl( "http://example.com/hasThingy" ), "Thingy" ) );
 *				unvalidatedRelations.add( new IndependentPair<>(
 *					new URIImpl( "http://example.com/hasBlah" ), "Blah" ) );
 *			}
 *		}
 *	</pre></code>
 *	...would be serialised as follows:
 *	<code><pre>
 *		http://example.com/MyObject
 *			http://example.com/hasThingy "Thingy";
 *			http://example.com/hasBlah "Blah".
 *	</pre></code>
 *	<p>
 *	Note that these are not unserialised again when deserialising the object
 *	from RDF (there is no way for the deserialiser to know that these triples
 *	came from this object).
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 16 Nov 2012
 *	@version $Author$, $Revision$, $Date$
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RelationList
{

}
