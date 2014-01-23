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
package org.openimaj.vis.general;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourMap;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.image.renderer.RenderHints;
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
	 * 	@see org.openimaj.vis.general.ItemPlotter#plotObject(org.openimaj.image.Image, org.openimaj.vis.general.XYPlotVisualisation.LocatedObject, org.openimaj.vis.general.AxesRenderer2D)
	 */
	@Override
	public void plotObject( final MBFImage visImage,
			final XYPlotVisualisation.LocatedObject<LabelledDot> object,
			final AxesRenderer2D<Float[],MBFImage> renderer )
	{
		// Get the position where we're going to place the dot
		Point2d pos = renderer.calculatePosition( object.x, object.y );

		final MBFImageRenderer ir = visImage.createRenderer( RenderHints.ANTI_ALIASED );

		// Draw the dot
		ir.drawShapeFilled(
				new Circle( pos,
					(float)(renderer.scaleDimensions( object.object.size, object.object.size )[0] ) ),
					object.object.colour );

		// Get the position where we're going the place the text
		pos = renderer.calculatePosition( object.x + object.object.size, object.y );

		// Create the font and font style
		final HersheyFont f = HersheyFont.TIMES_MEDIUM;
		final HersheyFontStyle<Float[]> fs = f.createStyle( visImage.createRenderer() );
		fs.setFontSize( 14 );

		// Calculate the bounding box of the text we're going to draw.
		final Rectangle b = fs.getRenderer( visImage.createRenderer() ).getSize(
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
			ir.drawText(
					object.object.label, (int)b.x, (int)b.y + (int)b.height,
					f, 14, object.object.colour );

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
		dpv.getAxesRenderer().setxMajorTickSpacing( 0.2 );
		dpv.getAxesRenderer().setxMinorTickSpacing( 0.05 );
		dpv.getAxesRenderer().setyMajorTickSpacing( 0.2 );
		dpv.getAxesRenderer().setyMinorTickSpacing( 0.05 );

		for( int i = 0; i < 10; i++ )
		{
			final double v = Math.random()/10d;
			dpv.addPoint( (Math.random()-0.5)*2, (Math.random()-0.5)*2,
					new LabelledDot( "Dot "+i, v, ColourMap.Cool.apply( (float)v*10f ) ) );
		}

		dpv.updateVis();
		dpv.showWindow("Labelled Point Vis" );
	}
}
