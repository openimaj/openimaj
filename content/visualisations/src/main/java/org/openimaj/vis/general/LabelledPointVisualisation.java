/**
 *
 */
package org.openimaj.vis.general;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.image.typography.hershey.HersheyFontStyle;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.vis.general.DotPlotVisualisation.ColouredDot;
import org.openimaj.vis.general.LabelledPointVisualisation.LabelledDot;

/**
 *	Plots dots with a label. This can be used as a
 *	visualisation in itself or used as an {@link ItemPlotter} in other visualisations.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jun 2013
 */
public class LabelledPointVisualisation extends XYPlotVisualisation<LabelledDot>
	implements ItemPlotter<LabelledDot,Float[],MBFImage>
{
	/**
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 11 Jun 2013
	 */
	public static class LabelledDot extends ColouredDot
	{
		/** The label */
		public String label;

		/**
		 * 	Create a labelled dot.
		 *	@param l The label
		 *	@param s The size
		 */
		public LabelledDot( final String l, final double s )
		{
			super( s, RGBColour.RED );
			this.label = l;
		}

		/**
		 * 	Create a labelled dot.
		 *	@param l The label
		 *	@param s The size
		 * 	@param colour The colour
		 */
		public LabelledDot( final String l, final double s, final Float[] colour )
		{
			super( s, colour );
			this.label = l;
		}
}

	/** */
	private static final long serialVersionUID = 1L;

	/** Whether to avoid drawing text that will overlap other text */
	private boolean avoidOverlaps = true;

	/** The bounds of the labels that have already been drawn */
	private final List<Rectangle> bounds = new ArrayList<Rectangle>();

	/**
	 *	Default constructor
	 */
	public LabelledPointVisualisation()
	{
		super( null );
		this.setItemPlotter( this );
	}

	/**
	 * 	Constructor that takes the width and height of the visualisation
	 *
	 *	@param width The width of the visualisation in pixels
	 *	@param height The height of the visualisation in pixels
	 */
	public LabelledPointVisualisation( final int width, final int height )
	{
		super( width, height, null );
		this.setItemPlotter( this );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.ItemPlotter#renderRestarting()
	 */
	@Override
	public void renderRestarting()
	{
		this.bounds.clear();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.ItemPlotter#plotObject(org.openimaj.image.Image, org.openimaj.vis.general.XYPlotVisualisation.LocatedObject, org.openimaj.vis.general.AxesRenderer)
	 */
	@Override
	public void plotObject( final MBFImage visImage,
			final XYPlotVisualisation.LocatedObject<LabelledDot> object,
			final AxesRenderer<Float[],MBFImage> renderer )
	{
		// Get the position where we're going to place the dot
		Point2d pos = renderer.calculatePosition( visImage, object.x, object.y );

		// Draw the dot
		visImage.createRenderer().drawShapeFilled(
				new Circle( pos,(float)(object.object.size * renderer.getxUnitSizePx()) ),
					object.object.colour );

		// Get the position where we're going the place the text
		pos = renderer.calculatePosition( visImage, object.x + object.object.size, object.y );

		// Create the font and font style
		final HersheyFont f = HersheyFont.TIMES_MEDIUM;
		final HersheyFontStyle<Float[]> fs = f.createStyle( visImage.createRenderer() );
		fs.setFontSize( 14 );

		// Calculate the bounding box of the text we're going to draw.
		final Rectangle b = fs.getRenderer( visImage.createRenderer() ).getBounds(
				object.object.label, fs );

		// Bounding box is 0,0,width,height, so move it into position.
		b.translate( 0, b.height );
		b.translate( pos.getX() + 4, pos.getY() );
//		System.out.println( b );

		boolean overlap = false;
		for( final Rectangle bb : this.bounds ) {
			if( bb.isOverlapping( b ) ) {
				overlap = true; break;
			}
		}

		this.bounds.add( b );

		if( !overlap )
			visImage.createRenderer().drawText(
					object.object.label, (int)b.x, (int)b.y + (int)b.height,
					f, 14, object.object.colour );

//		visImage.drawShape( b, RGBColour.GREEN );
	}

	/**
	 *	@return the avoidOverlaps
	 */
	public boolean isAvoidOverlaps()
	{
		return this.avoidOverlaps;
	}

	/**
	 *	@param avoidOverlaps the avoidOverlaps to set
	 */
	public void setAvoidOverlaps( final boolean avoidOverlaps )
	{
		this.avoidOverlaps = avoidOverlaps;
	}

	/**
	 * 	Main method to demonstrate the vis.
	 *	@param args command-line args (not used)
	 */
	public static void main( final String[] args )
	{
		final LabelledPointVisualisation dpv = new LabelledPointVisualisation( 1000, 600 );

		for( int i = 0; i < 10; i++ )
		{
			final double v = Math.random()/10d;
			dpv.addPoint( (Math.random()-0.5)*2, (Math.random()-0.5)*2,
					new LabelledDot( "Dot "+i, v, ColourMap.Cool.apply( (float)v*10f ) ) );
		}

		dpv.updateVis();
		DisplayUtilities.display( dpv.visImage );
	}
}
