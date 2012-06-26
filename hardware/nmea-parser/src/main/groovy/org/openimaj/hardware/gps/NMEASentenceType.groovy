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
package org.openimaj.hardware.gps;

import org.joda.time.DateTime;

/**
 * 	Groovy enumerator that defines all the sentence types for NMEA
 * 	with implementations that convert the tokens into objects for
 * 	easier handling.
 * 	<p>
 * 	The list of NMEA sentences was derived from 
 * 	http://www.gpsinformation.org/dale/nmea.htm
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 12 Jul 2011
 */
@SuppressWarnings("all")
public enum NMEASentenceType
{
	AAM,
	ALM,
	APA,
	APB,
	BOD,
	BWC,
	DTM,
	GGA({ tokens ->
			NMEAMessage m = new NMEAMessage(
				type: "fix",
				timestamp: parseTime( tokens[1] ),
				lat: parseLatLong( tokens[2] as double ),
				lng: parseLatLong( tokens[4] as double ),
				fixQuality: tokens[6] as int,
				numberOfSatellites: tokens[7] as int,
				horizontalDilution: tokens[8] as double,
				altitude: tokens[9] as double,
				heightOfSeaLevel: tokens[11] as double,
				checksum: tokens[14]
			);

			if( tokens[3] == "S" )
				m.lat = -m.lat;
			if( tokens[5] == "W" )
				m.lng = -m.lng;
			return m;
		}),
	GLL,
	GRS,
	GSA({ tokens ->
		NMEAMessage m = new NMEAMessage(
			type: "satStatus"
		);
	
		return m;
	}),
	GST,
	GSV,
	MSK,
	MSS,
	RMA,
	RMB,
	RMC({ tokens ->
			NMEAMessage m = new NMEAMessage(
				type: "recMinimum",
				timestamp: parseDateTime( tokens[1], tokens[9] ),
				lat: parseLatLong( tokens[3] as double ),
				lng: parseLatLong( tokens[5] as double ),
				speed: tokens[7] as double,
				trackAngle: tokens[8] as double,
				checksum: tokens[12]
			);
		
			if( tokens[4] == "S" )
				m.lat = -m.lat;
			if( tokens[6] == "W" )
				m.lng = -m.lng;
				
			if( !tokens[10]?.isEmpty() )
			{
				m.magneticVariation = tokens[10] as double;
				if( tokens[11] == "W" )
					m.magneticVariation = -m.magneticVariation;
			}
			return m;
		} ),
	RTE,
	TRF,
	STN,
	VBW,
	VTG,
	WCV,
	WPL,
	XTC,
	XTE,
	ZTG,
	ZDA
	
	def fn
	
	// This constructor allows the closure to be used
	public NMEASentenceType( Closure c ) 
	{ 
		fn = c 
	}
	
	// This constructor allows the "as NMEASententeType" groovy sugar
	public NMEASentenceType( String s, Integer i )
	{
		super( s, i );
	}
	 
	// Override the call method so we can call fn directly
	def call( args ) 
	{
		if( fn != null )
			fn( args )
	}
	
	// Converts 5056.1909 into a decimal equivalent -> 50.98 (or whatever it is)
	static public double parseLatLong( double latOrLong )
	{
		double val = (int)(latOrLong/100);
		double min = latOrLong - val*100;
		double min2 = min * (10d/6d);
		val += min2/100;
		return val;
	}
	
	static public DateTime parseTime( String timeString )
	{
		// TODO: Parse time
		return new DateTime();
	}
	
	static public DateTime parseDateTime( String timeString, String dateString )
	{
		// TODO: Parse date/time
		return new DateTime();
	}
	
}
