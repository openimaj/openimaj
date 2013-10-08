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
package org.openimaj.web.flickr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

/**
 *	A wrapper for Flickr images, and their URLs and what not. This is different
 *	to using the Photo class from com.aetrion as it specifically does not
 *	require access to Flickr and will not attempt to get any information from
 *	Flickr itself (except the image through the {@link #getImage()} method).
 *	It's just a placeholder for any information known about a Flickr image. 
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Aug 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class FlickrImage
{
	/** The allowable size suffixes */
	public static final String sizeSuffixes = "sqtnm-zcbo"; 
	
	/** The photo id */
	private long id;
	
	/** The URI of the image (generated) */
	private URI url;
	
	/** The farm ID */
	private int farm;
	
	/** The server ID */
	private int server;
	
	/** The image secret */
	private String secret;
	
	/** The size identifier */
	private char size;
	
	/** Whether it's the original image or not */
	private boolean isOriginal;
	
	/** The original image file extension */
	private String extension;
	
	/** If the file is cached locally, then this is its location */
	private File localFile;

	/**
	 *	@param id
	 * 	@param farm 
	 * 	@param server 
	 * 	@param secret 
	 * 	@param size 
	 * 	@param extension 
	 */
	public FlickrImage( long id, int farm, int server, String secret, 
			char size, String extension )
	{
		this.id = id;
		this.farm = farm;
		this.server = server;
		this.secret = secret;
		this.size = size;
		this.extension = extension;
		this.url = createURI();
		
		if( !sizeSuffixes.contains( ""+size ) )
			throw new IllegalArgumentException( "Size suffixes must be one "+
					"of "+sizeSuffixes );
	}
	
	/**
	 * 	Parses a Flickr URL and updates this object. 
	 *	@param url The URL
	 * 	@return The new FlickrImage instance
	 */
	public static FlickrImage create( URL url )
	{
		int farm = 0;
		long id = 0;
		int server = 0;
		String secret = null;
		char size = 'o';
		String extension = "jpg";
		
		// Patterns from http://www.flickr.com/services/api/misc.urls.html
		Pattern p1 = Pattern.compile( "http://farm([0-9]+).static.flickr.com/([0-9]+)/"
				+ "([0-9]+)_([0-9a-f]+).jpg" );
		Pattern p2 = Pattern.compile( "http://farm([0-9]+).static.flickr.com/([0-9]+)/"
				+ "([0-9]+)_([0-9a-f]+)_([mstzb]).jpg" );
		Pattern p3 = Pattern.compile( "http://farm([0-9]+).static.flickr.com/([0-9]+)/"
				+ "([0-9]+)_([0-9a-f]+)_o.(jpg|png|gif)" );
		Pattern p4 = Pattern.compile( "http://flic.kr/p/([0-9A-Za-z]+)" );
		
		FlickrImage f = null;
		Matcher m = null;
		if( (m = p1.matcher( url.toString() )).find() )
		{
			farm = Integer.parseInt( m.group(1) );
			server = Integer.parseInt( m.group(2) );
			id = Long.parseLong( m.group(3) );
			secret = m.group(4);
			f = new FlickrImage( id, farm, server, secret, size, extension );
		}
		else
		if( (m = p2.matcher( url.toString() )).find() )
		{
			farm = Integer.parseInt( m.group(1) );
			server = Integer.parseInt( m.group(2) );
			id = Long.parseLong( m.group(3) );
			secret = m.group(4);
			size = m.group(5).charAt(0);
			f = new FlickrImage( id, farm, server, secret, size, extension );
		}
		else
		if( (m = p3.matcher( url.toString() )).find() )
		{
			farm = Integer.parseInt( m.group(1) );
			server = Integer.parseInt( m.group(2) );
			id = Long.parseLong( m.group(3) );
			secret = m.group(4);
			size = 'o';
			extension = m.group(5);
			f = new FlickrImage( id, farm, server, secret, size, extension );
			f.setOriginal( true );
		}
		else
		if( p4.matcher( url.toString() ).find() )
		{
			id = FlickrBaseEncoder.decode( url.toString() );
			f = new FlickrImage( id, 0, 0, null, 'z', null );
		}
		else
		{
			System.err.println( "WARNING: No pattern matched the Flickr "+
					"URL "+url.toString() );
		}

		return f;
	}
	
	/**
	 * 
	 *	@return
	 */
	private URI createURI()
	{
		URI url = null;
		if( size == 'o' )
		{
			// http://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}_[mstzb].jpg
			url = URI.create( "http://farm"+farm+".staticflickr.com/"+server+"/"
					+id+"_"+secret+"_"+size+"."+extension );
		}
		else
		{
			// http://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}_o.[jpg|gif|png]
			url = URI.create( "http://farm"+farm+".staticflickr.com/"+server
					+"/"+id+"_"+secret+"_"+size+".jpg" );
		}
		
		return url;
	}
	
	/**
	 *	@return the id
	 */
	public long getId()
	{
		return id;
	}

	/**
	 *	@param id the id to set
	 */
	public void setId( long id )
	{
		this.id = id;
	}

	/**
	 *	@return the url
	 */
	public URI getUrl()
	{
		return url;
	}

	/**
	 *	@param url the url to set
	 */
	public void setUrl( URI url )
	{
		this.url = url;
	}

	/**
	 *	@return the farm
	 */
	public int getFarm()
	{
		return farm;
	}

	/**
	 *	@param farm the farm to set
	 */
	public void setFarm( int farm )
	{
		this.farm = farm;
	}

	/**
	 *	@return the server
	 */
	public int getServer()
	{
		return server;
	}

	/**
	 *	@param server the server to set
	 */
	public void setServer( int server )
	{
		this.server = server;
	}

	/**
	 *	@return the secret
	 */
	public String getSecret()
	{
		return secret;
	}

	/**
	 *	@param secret the secret to set
	 */
	public void setSecret( String secret )
	{
		this.secret = secret;
	}

	/**
	 *	@return the size
	 */
	public char getSize()
	{
		return size;
	}

	/**
	 *	@param size the size to set
	 */
	public void setSize( char size )
	{
		this.size = size;
	}

	/**
	 *	@return the isOriginal
	 */
	public boolean isOriginal()
	{
		return isOriginal;
	}

	/**
	 *	@param isOriginal the isOriginal to set
	 */
	public void setOriginal( boolean isOriginal )
	{
		this.isOriginal = isOriginal;
	}

	/**
	 *	@return the localFile
	 */
	public File getLocalFile()
	{
		return localFile;
	}

	/**
	 *	@param localFile the localFile to set
	 */
	public void setLocalFile( File localFile )
	{
		this.localFile = localFile;
	}
	
	/**	
	 * 	Reads the actual flickr image. If it's cached then it will read
	 * 	the cache, otherwise it will contact Flickr.
	 *	@return The image
	 *	@throws IOException
	 */
	public MBFImage getImage() throws IOException
	{
		if( localFile != null )
				return ImageUtilities.readMBF( localFile );
		else	return ImageUtilities.readMBF( url.toURL() );
	}
}
