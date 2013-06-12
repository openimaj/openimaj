/**
 *
 */
package org.openimaj.vis.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.vis.general.AxesRenderer;
import org.openimaj.vis.general.ItemPlotter;
import org.openimaj.vis.general.LabelledPointVisualisation;
import org.openimaj.vis.general.LabelledPointVisualisation.LabelledDot;
import org.openimaj.vis.general.XYPlotVisualisation;

import Jama.Matrix;

/**
 *	Draws a world map visualisation as an XY plot. The XY coordinates are scaled
 *	to be equal to longitude and latitude.
 *
 *	@author Sina Samangooei (ss@ecs.soton.ac.uk)
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@param <T> The type of data to plot on the image
 *  @created 11 Jun 2013
 */
public class WorldMap<T> extends XYPlotVisualisation<T>
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The colour to use for the sea */
	private Float[] seaColour = new Float[]{0.4f,0.5f,1f,1f};

	/** The colour to outline the countries with */
	private Float[] defaultCountryOutlineColour = new Float[]{0f,0f,0f,1f};

	/** The colour to fill the land with */
	private Float[] defaultCountryLandColour = new Float[]{0f,1f,0f,1f};

	/** The colour to fill the land when highlighted */
	private Float[] highlightCountryLandColour = new Float[]{1f,0f,0f,1f};

	/** The polygons that represent the countries in the world */
	private WorldPolygons worldPolys;

	/** List of countries which should be highlighted */
	private final Set<String> activeCountries = new HashSet<String>();

	/** These are overrides for the default country highglight colour */
	private final HashMap<String,Float[]> countryHighlightColours = new HashMap<String,Float[]>();


	/**
	 *	@param width Width of the visualisation
	 *	@param height Height of the visualisation
	 *	@param plotter The plotter to plot data with
	 */
	public WorldMap( final int width, final int height,
			final ItemPlotter<T, Float[], MBFImage> plotter )
	{
		super( width, height, plotter );
		this.init();
	}

	/**
	 * 	Initialise
	 */
	private void init()
	{
		super.axesRenderer.setMinXValue( -180 );
		super.axesRenderer.setMaxXValue( 180 );
		super.axesRenderer.setMinYValue( -90 );
		super.axesRenderer.setMaxYValue( 90 );
		super.axesRenderer.setAxisPaddingLeft( 50 );
		super.axesRenderer.setAxisPaddingBottom( 50 );
		super.axesRenderer.setAxisPaddingRight( 50 );
		super.axesRenderer.setAxisPaddingTop( 50 );
		super.axesRenderer.setxMajorTickSpacing( 10 );
		super.axesRenderer.setyMajorTickSpacing( 10 );
		super.axesRenderer.setxMinorTickSpacing( 5 );
		super.axesRenderer.setyMinorTickSpacing( 5 );
		super.axesRenderer.setxLabelSpacing( 90 );
		super.axesRenderer.setyLabelSpacing( 45 );
		super.axesRenderer.setxAxisColour( RGBColour.WHITE );
		super.axesRenderer.setyAxisColour( RGBColour.WHITE );
		super.axesRenderer.setyTickLabelColour( RGBColour.WHITE );
		super.axesRenderer.setxTickLabelColour( RGBColour.WHITE );
		super.axesRenderer.setDrawXAxisName( false );
		super.axesRenderer.setDrawYAxisName( false );
		this.worldPolys = new WorldPolygons();
	}

	/**
	 * 	Add a country to highlight
	 *	@param countryCode The country code to highlight
	 */
	public void addHighlightCountry( final String countryCode )
	{
		this.activeCountries.add( countryCode );
	}

	/**
	 * 	Add a country to highlight
	 *	@param countryCode The country code to highlight
	 * 	@param colour The colour to highlight the country
	 */
	public void addHighlightCountry( final String countryCode, final Float[] colour )
	{
		this.activeCountries.add( countryCode );
		this.countryHighlightColours.put( countryCode, colour );
	}

	/**
	 * 	Remove a highlighted country
	 *	@param countryCode The country code to remove
	 */
	public void removeHighlightCountry( final String countryCode )
	{
		this.activeCountries.remove( countryCode );
		this.countryHighlightColours.remove( countryCode );
	}

	/**
	 * 	Fill the image with the sea's colour. Uses the member
	 * 	seaColour to determine this. If you want another sea
	 * 	texture, override this method.
	 */
	protected void drawSea( final MBFImage img )
	{
		img.fill( this.seaColour );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.XYPlotVisualisation#beforeAxesRender(org.openimaj.image.MBFImage, org.openimaj.vis.general.AxesRenderer)
	 */
	@Override
	public void beforeAxesRender( final MBFImage visImage,
			final AxesRenderer<Float[], MBFImage> axesRenderer )
	{
		// Fill the image with the sea colour.
		// We'll draw the countries on top of this.
		this.drawSea( this.visImage );

		// Make the image fit into the axes centred around 0,0 long/lat
		final Point2d mid = axesRenderer.calculatePosition( visImage, 0, 0 );
		final Point2d dateLine0 = axesRenderer.calculatePosition( visImage, 180, 0 );
		final Point2d northPole = axesRenderer.calculatePosition( visImage, 0, -90 );
		final double scaleX = (dateLine0.getX()-mid.getX()) / 180d;
		final double scaleY = (northPole.getY()-mid.getY()) / 90d;
		Matrix trans = Matrix.identity(3, 3);
		trans = trans.times(
			TransformUtilities.scaleMatrixAboutPoint(
				scaleX, -scaleY, mid
			)
		);

		// Translate to 0,0
		trans = trans.times(
			TransformUtilities.translateMatrix(mid.getX(), mid.getY())
		);

		// Now draw the countries onto the sea. We transform each of the shapes
		// by the above transform matrix prior to plotting them to the image.
		for( final WorldPlace wp : this.worldPolys.getShapes() )
		{
			// Each place may have more than one polygon.
			final List<Shape> shapes = wp.getShapes();

			// For each of the polygons... draw them to the image.
			for( Shape s : shapes )
			{
				s = s.transform(trans);

				if( this.activeCountries.contains( wp.getISOA2() ) )
				{
					final Float[] col = this.countryHighlightColours.get( wp.getISOA2() );
					visImage.drawShapeFilled( s, col == null ? this.highlightCountryLandColour : col );
				}
				else
				{
					this.visImage.drawShapeFilled( s, this.defaultCountryLandColour );
				}

				// Draw the outline shape of the country
				this.visImage.drawShape( s, 1, this.defaultCountryOutlineColour );
			}
		}
	}

	/**
	 *	@return the seaColour
	 */
	public Float[] getSeaColour()
	{
		return this.seaColour;
	}

	/**
	 *	@param seaColour the seaColour to set
	 */
	public void setSeaColour( final Float[] seaColour )
	{
		this.seaColour = seaColour;
	}

	/**
	 *	@return the defaultCountryOutlineColour
	 */
	public Float[] getDefaultCountryOutlineColour()
	{
		return this.defaultCountryOutlineColour;
	}

	/**
	 *	@param defaultCountryOutlineColour the defaultCountryOutlineColour to set
	 */
	public void setDefaultCountryOutlineColour( final Float[] defaultCountryOutlineColour )
	{
		this.defaultCountryOutlineColour = defaultCountryOutlineColour;
	}

	/**
	 *	@return the defaultCountryLandColour
	 */
	public Float[] getDefaultCountryLandColour()
	{
		return this.defaultCountryLandColour;
	}

	/**
	 *	@param defaultCountryLandColour the defaultCountryLandColour to set
	 */
	public void setDefaultCountryLandColour( final Float[] defaultCountryLandColour )
	{
		this.defaultCountryLandColour = defaultCountryLandColour;
	}

	/**
	 *	@return the highlightCountryLandColour
	 */
	public Float[] getHighlightCountryLandColour()
	{
		return this.highlightCountryLandColour;
	}

	/**
	 *	@param highlightCountryLandColour the highlightCountryLandColour to set
	 */
	public void setHighlightCountryLandColour( final Float[] highlightCountryLandColour )
	{
		this.highlightCountryLandColour = highlightCountryLandColour;
	}

	/**
	 * 	Returns a country code for a given country name.
	 *	@param countryName The country name
	 *	@return the country code
	 */
	public String getCountryCodeByName( final String countryName )
	{
		final WorldPlace p = this.worldPolys.byCountry( countryName );
		if( p == null ) return null;
		return p.getISOA2();
	}

	/**
	 * 	Returns the lat/long of a country given its country code
	 *	@param countryCode The country code
	 *	@return The lat long as a point2d
	 */
	public Point2d getCountryLocation( final String countryCode )
	{
		final WorldPlace wp = this.worldPolys.byCountryCode( countryCode );
		if( wp == null ) return null;
		return new Point2dImpl( wp.getLongitude(), wp.getLatitude() );
	}

	/**
	 * 	Demonstration method.
	 *	@param args command-line args (unused)
	 */
	public static void main( final String[] args )
	{
		final WorldMap<LabelledDot> wp = new WorldMap<LabelledDot>(
				1200, 800, new LabelledPointVisualisation() );

		wp.addPoint( -67.271667, -55.979722, new LabelledDot( "Cape Horn", 1d, RGBColour.WHITE ) );
		wp.addPoint( -0.1275, 51.507222, new LabelledDot( "London", 1d, RGBColour.WHITE ) );
		wp.addPoint( 139.6917, 35.689506, new LabelledDot( "Tokyo", 1d, RGBColour.WHITE ) );
		wp.addHighlightCountry( "cn" );
		wp.addHighlightCountry( "us", new Float[]{0f,0.2f,1f,1f} );

		wp.showWindow( "World" );
	}
}
