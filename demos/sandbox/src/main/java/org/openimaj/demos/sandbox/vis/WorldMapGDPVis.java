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
 *	A demonstration of the World visualisation. It downloads a CSV file from the web,
 *	parses it and selects some data from the table. It then steps through all the years
 *	in the particular dataset and colours the countries based on the values of the data.
 *	<p>
 *	You can supply an indicator name on the command line (when you run it, it will print out
 *	all available indicators). If you get the name wrong you'll get an NPE probably.
 *	<p>
 *	It demonstrates the {@link WorldVis} interface as well as using the {@link VideoVisualisation}
 *	API to convert a {@link VisualisationImpl} into a {@link Video} which is displayed using a
 *	standard {@link VideoDisplay}.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 12 Jun 2013
 */
public class WorldMapGDPVis
{
	private static final HashMap<String,HashMap<Integer,HashMap<String,Double>>> indicators =
			new HashMap<String, HashMap<Integer, HashMap<String,Double>>>();

	/**
	 * 	Main Method
	 *	@param args Command-line args. You can provide an indicator name here to override the default
	 * 	@throws IOException If the CSV cannot be downloaded
	 * 	@throws MalformedURLException Should not be thrown
	 */
	public static void main( final String[] args ) throws MalformedURLException, IOException
	{
		// Create a parser for a CSV file
		// The file gives values by country for a large set of indicators over a set of years.
		// Years are in columns: the first column is the country, the second column the indicator type.
		// We are using the ExcelCSVParser to parse this data for us.
		final LabeledCSVParser l = new LabeledCSVParser(
			new ExcelCSVParser(
				new URL(
					"http://users.ecs.soton.ac.uk/dpd/projects/openimaj/GDPgrowth-countries.csv"
				).openStream()
			) );

		// Get each line in turn and make a data structure
		while( l.getLine() != null )
		{
			// Get the country and indicator (data) type.
			final String c = l.getValueByLabel( "Country" );
			final String t = l.getValueByLabel( "IndicatorName" );

			// The data is between years 1971 and 2011, so we'll get each of the years
			// for every country and indicator and store it away in a big hashmap.
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

		// List the indicators so we can choose one.
		System.out.println( "Possible indicators: "+WorldMapGDPVis.indicators.keySet() );

		// The labelled point vis is used to plot country names.
		final LabelledPointVisualisation dpv = new LabelledPointVisualisation();

		// The world map vis to show the world
		final WorldMap<LabelledDot> wm = new WorldMap<LabelledDot>( 1280, 720, dpv );

		// Set the colours (and we'll remove the long/lat axes)
		wm.setDefaultCountryLandColour( RGBColour.BLACK );
		wm.getAxesRenderer().setDrawXAxis( false );
		wm.getAxesRenderer().setDrawYAxis( false );

		// This is the indicator we'll use
		String showIndicator = "Agriculture, hunting, forestry, fishing (ISIC A-B)";
		if( args.length > 0 )
			showIndicator = args[0];

		// Create a video from the visualisation.
		final VideoVisualisation vv = new VideoVisualisation( wm );
		VideoDisplay.createVideoDisplay( vv );

		// Loop through all the years and update the visualisation to show
		// the values for each year. We do this once a second.
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
	 * 	Gets the data for a specific year and updates the visualisation.
	 *
	 *	@param wm The visualisation to update.
	 *	@param showIndicator Which indicator to show
	 *	@param year Which year to show for
	 */
	private static void updateYear( final WorldMap<LabelledDot> wm,
			final String showIndicator, final int year )
	{
		// Get the dataset for the given indicator and year.
		final HashMap<String, Double> set = WorldMapGDPVis.indicators.get( showIndicator ).get( year );

		// Calculate the max and mean. Only values above the mean will be shown
		double max = 0;
		double mean = 0;
		for( final double d : set.values() )
		{
			max = Math.max( max, d );
			mean += d;
		}
		mean /= set.values().size();

		// Loop through all the countries
		for( final String c : set.keySet() )
		{
			// Get the value
			double v = set.get( c );

			// Clip the data (just in case)
			if( v < 0 ) v = 0;

			// Get the country from the world vis (some won't work just to naming differences)
			final String cc = wm.getCountryCodeByName( c );

			// If it didn't work, we will ignore it.
			if( cc != null )
			{
				// Get the position of the country (for plotting it's name label)
				final Point2d cl = wm.getCountryLocation(cc);

				// Highlight the country with a colour based on its value
				wm.addHighlightCountry( cc, ColourMap.Autumn.apply( (float)(v/max) ) );

				// Only if it's above the mean will we plot its name (just to keep the vis tidier)
				if( v > mean )
					wm.addPoint( cl.getX(), cl.getY(), new LabelledDot( c+": "+v, 1, RGBColour.WHITE ) );
			}
			else	System.out.println( "country "+c+" unknown");
		}

	}
}
