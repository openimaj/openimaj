/**
 *
 */
package org.openimaj.demos.sandbox.vis;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.video.VideoDisplay;
import org.openimaj.vis.VideoVisualisation;
import org.openimaj.vis.general.LabelledPointVisualisation;
import org.openimaj.vis.general.LabelledPointVisualisation.LabelledDot;
import org.openimaj.vis.world.WorldMap;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Jun 2013
 */
public class WorldMapGDPVis
{
	private static final HashMap<String,HashMap<Integer,HashMap<String,Double>>> indicators =
			new HashMap<String, HashMap<Integer, HashMap<String,Double>>>();

	/**
	 *	@param args
	 * 	@throws IOException
	 * 	@throws MalformedURLException
	 */
	public static void main( final String[] args ) throws MalformedURLException, IOException
	{
		// Create a parser for a CSV file
		final LabeledCSVParser l = new LabeledCSVParser(
			new ExcelCSVParser(
				new URL(
					"http://users.ecs.soton.ac.uk/dpd/projects/openimaj/GDPgrowth-countries.csv"
				).openStream()
			) );

		// Get each line in turn and make a data structure
		while( l.getLine() != null )
		{
			final String c = l.getValueByLabel( "Country" );
			final String t = l.getValueByLabel( "IndicatorName" );

			for( int year = 1971; year <= 2011; year++ )
			{
				final String s = l.getValueByLabel( ""+year );
				if( !s.isEmpty() )
				{
					final Double v = Double.parseDouble( s );

					HashMap<Integer,HashMap<String, Double>> yi = WorldMapGDPVis.indicators.get( t );
					if( yi == null )
						WorldMapGDPVis.indicators.put( t, yi = new HashMap<Integer,HashMap<String,Double>>() );

					HashMap<String, Double> ci = yi.get( year );
					if( ci == null )
						yi.put( year, ci = new HashMap<String,Double>() );

					ci.put( c, v );
				}
			}
		}

		System.out.println( "Possible indicators: "+WorldMapGDPVis.indicators.keySet() );

		final LabelledPointVisualisation dpv = new LabelledPointVisualisation();
		final WorldMap<LabelledDot> wm = new WorldMap<LabelledDot>( 1280, 720, dpv );
		wm.setDefaultCountryLandColour( RGBColour.BLACK );

		final String showIndicator = "Agriculture, hunting, forestry, fishing (ISIC A-B)";

		final VideoVisualisation vv = new VideoVisualisation( wm );
		VideoDisplay.createVideoDisplay( vv );

		for( int year = 1971; year < 2011; year++ )
		{
			wm.clearData();
			WorldMapGDPVis.updateYear( wm, showIndicator, year );
			try
			{
				Thread.sleep( 1000 );
			}
			catch( final InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 *	@param wm
	 *	@param showIndicator
	 *	@param year
	 */
	private static void updateYear( final WorldMap<LabelledDot> wm,
			final String showIndicator, final int year )
	{
		final HashMap<String, Double> set = WorldMapGDPVis.indicators.get( showIndicator ).get( year );

		System.out.println( set );

		double max = 0;
		double mean = 0;
		for( final double d : set.values() )
		{
			max = Math.max( max, d );
			mean += d;
		}
		mean /= set.values().size();

		for( final String c : set.keySet() )
		{
			double v = set.get( c );
			if( v < 0 ) v = 0;
			final String cc = wm.getCountryCodeByName( c );
			if( cc != null )
			{
				final Point2d cl = wm.getCountryLocation(cc);
				wm.addHighlightCountry( cc, ColourMap.Autumn.apply( (float)(v/max) ) );

				if( v > mean )
					wm.addPoint( cl.getX(), cl.getY(), new LabelledDot( c+": "+v, 1, RGBColour.WHITE ) );
			}
			else	System.out.println( "country "+c+" unknown");
		}

	}
}
