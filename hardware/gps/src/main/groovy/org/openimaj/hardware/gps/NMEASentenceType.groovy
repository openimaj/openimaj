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
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 12 Jul 2011
 */
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
				lat: tokens[3] as double,
				lng: tokens[5] as double,
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
