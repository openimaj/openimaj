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
