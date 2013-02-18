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
package org.openimaj.rdf.owl2java;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.rdf.serialize.RelationList;
import org.openimaj.rdf.serialize.TripleList;
import org.openimaj.util.pair.IndependentPair;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;

/**
 *	Top level class that all top-level superclasses generated from an ontology
 *	will inherit from. This allows us to add functionality to all generated
 *	classes in one single place.
 *	<p>
 *	This class currently provides a single access point for:
 *	<ul>
 *	<li>retrieving and setting the URI of an instance</li>
 *	<li>adding unvalidated triples</li>
 *	<li>adding unvalidated relations</li>
 *	</ul>
 *	<p>
 *	During serialization, an unvalidated triples list will be sent directly
 *	to the triple sink and the field avoided. Unvalidated relations will be
 *	serialized in turn and sent to triple sink.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 15 Nov 2012
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class Something
{
	/** The URI of this instance */
	private String uri;
	
	/** Whether unvalidated tuples are allowed to be added to the class */
	private boolean allowUnvalidatedTriples = true;
	
	/** Any unvalidated tuples that have been added to this class */
	@TripleList
	private final List<Statement> unvalidatedTriples = new ArrayList<Statement>();
	
	/** Whether to allow unvalidated graphs to be linked to this class */
	private boolean allowUnvalidatedRelations = true;
	
	/** Any unvalidated relations that have been added to this class */
	@RelationList
	private final List<IndependentPair<URI,Object>> unvalidatedRelations =
			new ArrayList<IndependentPair<URI,Object>>();
	
	/**
	 * 	Get the URI of this instance
	 *	@return The URI of this instance
	 */
	public String getURI()
	{
		return this.uri;
	}
	
	/**
	 * 	Set the URI of this instance.
	 *	@param uri The URI of this instance
	 */
	public void setURI( final String uri )
	{
		this.uri = uri;
	}
	
	/**
	 * 	Add an unvalidated tuple to this subgraph.
	 *	@param predicate The predicate
	 *	@param object The object
	 * 	@throws IllegalArgumentException if this subgraph does not allow
	 * 		unvalidated tuples to be added
	 */
	public void addUnvalidatedTuple( final URI predicate, final Value object ) 
			throws IllegalArgumentException
	{
		if( this.allowUnvalidatedTriples ) 
			this.unvalidatedTriples.add( new StatementImpl( new URIImpl( this.uri ), 
				predicate, object ) );
		else	throw new IllegalArgumentException( "Adding tuples to a validated subgraph" );
	}
	
	/**
	 * 	Set whether to allow unvalidated triples in the subgraph.
	 *	@param tf TRUE to allow unvalidated triples, FALSE otherwise
	 */
	public void setAllowUnvalidatedTriples( final boolean tf )
	{
		this.allowUnvalidatedTriples = tf;
	}
	
	/**
	 * 	Add an unvalidated relation to this subgraph.
	 *	@param predicate The predicate
	 *	@param object The object
	 * 	@throws IllegalArgumentException if this subgraph does not allow
	 * 		unvalidated tuples to be added
	 */
	public void addUnvalidatedRelation( final URI predicate, final Object object ) 
			throws IllegalArgumentException
	{
		if( this.allowUnvalidatedRelations ) 
			this.unvalidatedRelations.add( new IndependentPair<URI, Object>( 
						predicate, object ) );
		else	throw new IllegalArgumentException( "Adding relations to a validated subgraph" );
	}
		
	/**
	 * 	Set whether to allow unvalidated relations in the subgraph.
	 *	@param tf TRUE to allow unvalidated relations, FALSE otherwise
	 */
	public void setAllowUnvalidatedRelations( final boolean tf )
	{
		this.allowUnvalidatedRelations = tf;
	}
}
