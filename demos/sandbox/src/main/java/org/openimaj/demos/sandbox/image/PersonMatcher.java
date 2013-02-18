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
package org.openimaj.demos.sandbox.image;

import net.billylieurance.azuresearch.AbstractAzureSearchQuery.AZURESEARCH_FORMAT;
import net.billylieurance.azuresearch.AzureSearchImageQuery;
import net.billylieurance.azuresearch.AzureSearchImageResult;
import net.billylieurance.azuresearch.AzureSearchResultSet;


/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 5 Feb 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class PersonMatcher
{
	/** Bing App Id */
	private static final String APPID = "S27Lmrra8fEPf4mvSN9iPsWKZTUayXVTdaMIbP5uRiQ=";

	/** The query string that was used */
	private String query = "";

	/**
	 * 	Constructor that takes a query string.
	 *	@param query The query string
	 */
	public PersonMatcher( final String query )
    {
		this.query = query;
		PersonMatcher.retrieveImages( query, false );
    }

	/**
	 * 	Retrieves a set of images from Bing that match the query.
	 */
	private static void retrieveImages( final String query, final boolean facesOnly )
    {
		final AzureSearchImageQuery aq = new AzureSearchImageQuery();
		aq.setAppid( PersonMatcher.APPID );
		aq.setFormat( AZURESEARCH_FORMAT.JSON );
		aq.setMarket( "en-us" );
		aq.setQuery( query );
		aq.doQuery();

		final AzureSearchResultSet<AzureSearchImageResult> result = aq.getQueryResult();
		System.out.println( result );
	}

	/**
	 *	@param args
	 */
	public static void main( final String[] args )
    {
	    new PersonMatcher( "Barack Obama" );
    }
}
