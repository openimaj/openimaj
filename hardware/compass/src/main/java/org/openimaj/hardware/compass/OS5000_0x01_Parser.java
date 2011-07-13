package org.openimaj.hardware.compass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 	Parser for strings of data from the OceanServer serial compass.
 * 
 *  @author Jon Hare <jsh2@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 13 Jul 2011
 */
public class OS5000_0x01_Parser
{
	// $Chhh.hPpp.pRrr.rTtt.tMx0.000My0.000Mz0.000Ax000.0Ay000.0Az000.0*cc
	private final static Pattern COMPASS = Pattern.compile( "C(\\d+\\.\\d+)" );
	private final static Pattern PITCH = Pattern.compile( "P(-??\\d+\\.\\d+)" );
	private final static Pattern ROLL = Pattern.compile( "R(-??\\d+\\.\\d+)" );
	private final static Pattern TEMPERATURE = Pattern.compile( "T(-??\\d+\\.\\d+)" );
	private final static Pattern MX = Pattern.compile( "Mx(-??\\d+\\.\\d+)" );
	private final static Pattern MY = Pattern.compile( "My(-??\\d+\\.\\d+)" );
	private final static Pattern MZ = Pattern.compile( "Mz(-??\\d+\\.\\d+)" );
	private final static Pattern AX = Pattern.compile( "Ax(-??\\d+\\.\\d+)" );
	private final static Pattern AY = Pattern.compile( "Ay(-??\\d+\\.\\d+)" );
	private final static Pattern AZ = Pattern.compile( "Az(-??\\d+\\.\\d+)" );

	/**
	 * 	Given a line of data, parses the line into a {@link CompassData} object.
	 * 
	 *  @param line The line to parse.
	 *  @return A {@link CompassData} object
	 */
	public static CompassData parseLine( String line )
	{
		CompassData data = new CompassData();

		data.compass = parse( COMPASS, line );
		data.pitch = parse( PITCH, line );
		data.roll = parse( ROLL, line );
		data.temperature = parse( TEMPERATURE, line );
		data.mx = parse( MX, line );
		data.my = parse( MY, line );
		data.mz = parse( MZ, line );
		data.ax = parse( AX, line );
		data.ay = parse( AY, line );
		data.az = parse( AZ, line );

		return data;
	}

	/**
	 * 	Helper function to parse individual lines.
	 * 
	 *  @param p The pattern to match against
	 *  @param line The line to parse.
	 *  @return a double value parsed out of the string
	 */
	private static double parse( Pattern p, String line )
	{
		Matcher m = p.matcher( line );
		if( m.find() ) return Double.parseDouble( m.group( 1 ) );
		return 0;
	}
}
