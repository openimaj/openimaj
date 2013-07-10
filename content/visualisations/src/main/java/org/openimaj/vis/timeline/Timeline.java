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
package org.openimaj.vis.timeline;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.vis.AnimatedVisualisationProvider;
import org.openimaj.vis.general.AxesRenderer2D;
import org.openimaj.vis.general.DiversityAxis;
import org.openimaj.vis.general.ItemPlotter;
import org.openimaj.vis.general.LabelTransformer;

/**
 * 	A timeline visualisation. The timeline consists of a set of {@link TimelineTrack}s and you
 * 	can add these with {@link #addTrack(TimelineTrack)} and add objects to those tracks using
 * 	{@link #addTimelineObject(TimelineTrack, TimelineObject)}.
 * 	<p>
 * 	This class is an implementation of the {@link DiversityAxis} visualisation.
 *
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@created 3 Jul 2012 / 25 Jun 2013
 */
public class Timeline extends DiversityAxis<TimelineObject>
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 *	This is an item plotter for {@link TimelineObject}s. The size of each band needs
	 *	to be updated here when the diversity axis band size changes.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 25 Jun 2013
	 *	@version $Author$, $Revision$, $Date$
	 */
	protected static class TimelineObjectPlotter implements ItemPlotter<TimelineObject, Float[], MBFImage>
	{
		/** The size of the bands in the diversity axis */
		private int bandSize = 100;

		/**
		 *	{@inheritDoc}
		 * 	@see org.openimaj.vis.general.ItemPlotter#renderRestarting()
		 */
		@Override
		public void renderRestarting()
		{
		}

		/**
		 *	{@inheritDoc}
		 * 	@see org.openimaj.vis.general.ItemPlotter#plotObject(org.openimaj.image.Image, org.openimaj.vis.general.XYPlotVisualisation.LocatedObject, org.openimaj.vis.general.AxesRenderer2D)
		 */
		@Override
		public void plotObject( final MBFImage visImage,
				final org.openimaj.vis.general.XYPlotVisualisation.LocatedObject<TimelineObject> object,
				final AxesRenderer2D<Float[], MBFImage> renderer )
		{
			// Work out where we're going to plot this timeline object.
			final Point2d p = renderer.calculatePosition( object.x, object.y );

			// Reset its size, if we need to then update the visualisation
			object.object.setRequiredSize( new Dimension(
					object.object.getRequiredSize().width, this.bandSize ) );
			object.object.updateVis();

			// Now get the image and draw it in the correct place.
			final MBFImage i = object.object.getVisualisationImage();
			if( i != null )
				visImage.drawImage( i, (int)p.getX(), (int)p.getY() );
		}

		/**
		 *	Set the size of a band.
		 *	@param bandSize The size of a band.
		 */
		public void setBandSize( final int bandSize )
		{
			this.bandSize = bandSize;
		}
	}

	/**
	 * Represents a track in the timeline. A track has a name and a number.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 3 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static class TimelineTrack
	{
		/** The number of this track */
		private int number = 0;

		/** The label */
		private final String label;

		/** Markers for the track */
		private final List<TimelineMarker> markers = new ArrayList<TimelineMarker>();

		/**
		 * Instantiate a new track with the given label.
		 *
		 * @param label The label of the track
		 * @param number The track number
		 */
		public TimelineTrack( final String label, final int number )
		{
			this.number = number;
			this.label = label;
		}

		/**
		 * 	Return the number of this track.
		 *	@return The track number
		 */
		public int getTrackNumber()
		{
			return this.number;
		}

		/**
		 * Add a track marker to this time.
		 *
		 * @param time The time to add the track marker
		 */
		public void addTrackMarker( final long time )
		{
			final TimelineMarker tm = new TimelineMarker();
			tm.time = time;
			tm.colour = RGBColour.YELLOW;
			this.markers.add( tm );
		}

		/**
		 * 	Get the name of the track
		 *	@return the label
		 */
		public String getLabel()
		{
			return this.label;
		}
	}

	/**
	 * Timeline markers that are drawn onto a timeline
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 3 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static class TimelineMarker
	{
		/** The marker type */
		public TimelineMarkerType type = TimelineMarkerType.FLAG;

		/** The time of the marker */
		public long time = 0;

		/** The label of the marker, if it has one */
		public String label;

		/** The colour of the marker */
		public Float[] colour = RGBColour.BLACK;
	}

	/**
	 * Different type of markers.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 3 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static enum TimelineMarkerType
	{
		/**
		 * Draws a marker with a little flag showing some text
		 */
		LABEL
		{
			@Override
			public void drawMarker( final TimelineMarker m, final MBFImage image, final int x, final int h )
			{
				image.drawLine( x, 0, x, h, m.colour );

				final int fw = 20;
				final int fh = 10;
				image.drawShapeFilled( new Rectangle( x, 4, fw+4, fh + 4 ), m.colour );

				// TODO: Need to draw the label
			}
		},
		/**
		 * Draws a marker with a little flag at the top
		 */
		FLAG
		{
			private final int fs = 10;

			@Override
			public void drawMarker( final TimelineMarker m, final MBFImage image, final int x, final int h )
			{
				image.drawLine( x, 0, x, h, m.colour );
				image.drawShapeFilled(
					new Triangle(
							new Point2dImpl( x, 0 ),
							new Point2dImpl( x+this.fs*2, this.fs/2 ),
							new Point2dImpl( x, this.fs ) ),
						m.colour );
			}
		};

		/**
		 * Draws this marker into the given graphics context at the given
		 * position and with the given length.
		 *
		 * @param m The marker
		 * @param image The image to draw to
		 * @param xPos The position
		 * @param height The length to draw the marker
		 */
		public abstract void drawMarker( TimelineMarker m, MBFImage image,
				int xPos, int height );

		/**
		 * Returns an opposing colour
		 *
		 * @param c The colour to oppose
		 * @return An opposing colour
		 */
		static protected Float[] getOpposingColour( final Float[] c )
		{
			// TODO: This isn't really getting a good colour
			return new Float[]{ 1-c[0], 1-c[1], 1-c[2], 1f };
		}
	}

	/** Width of the sidebar in pixels */
	private final int sidebarWidth = 150;

	/** List of markers on this timeline */
	private final List<TimelineMarker> markers = new ArrayList<Timeline.TimelineMarker>();

	/** The sidebar panel */
	private MBFImage sidebarPanel = null;

	/** The default time scalar is 100 milliseconds per pixel */
	private double timeScalar = 100;

	/** The number of tracks */
	private int nTracks = 1;

	/** The tracks */
	private final HashMap<Integer,TimelineTrack> tracks = new HashMap<Integer, Timeline.TimelineTrack>();

	/** The plotter used to plot timeline objects */
	private final TimelineObjectPlotter plotter = new TimelineObjectPlotter();

	/**
	 * 	Default constructor
	 * 	@param w Width of the visualisation
	 * 	@param h Height of the visualisation
	 */
	public Timeline( final int w, final int h )
	{
		super( w, h, null );
		super.setItemPlotter( this.plotter );
		this.init();
	}

	/**
	 *
	 */
	private void init()
	{
		this.sidebarPanel = new MBFImage( this.sidebarWidth,
				this.visImage.getHeight(), 4 );

		// Setup the axis renderer (the timeline's ruler);
		this.axesRenderer2D.setAxisPaddingLeft( this.sidebarWidth );
		this.axesRenderer2D.setDrawYAxis( false );
		this.axesRenderer2D.setDrawXAxisName( false );
		this.axesRenderer2D.setMajorTickColour( RGBColour.WHITE );
		this.axesRenderer2D.setMinorTickColour( RGBColour.WHITE );
		this.axesRenderer2D.setMajorTickColour( RGBColour.WHITE );
		this.axesRenderer2D.setMinorTickColour( RGBColour.WHITE );
		this.axesRenderer2D.setxAxisNameColour( RGBColour.WHITE );
		this.axesRenderer2D.setxTickLabelColour( RGBColour.WHITE );
		this.axesRenderer2D.setxAxisColour( RGBColour.WHITE );
		this.axesRenderer2D.setxMajorTickSpacing( 10000 );
		this.axesRenderer2D.setxMinorTickSpacing( 1000 );
		this.axesRenderer2D.setxLabelSpacing( 10000 );
		this.axesRenderer2D.setxAxisLabelTransformer( new LabelTransformer()
		{
			@Override
			public String transform( final double value )
			{
				// Convert milliseconds to seconds
				return ""+Math.round(value/1000d);
			}
		} );

		// Set the default time scalar
		this.setTimeScalar( 50 );

		// Do the precalcs for the axes renderer
		this.axesRenderer2D.precalc( );
	}

	/**
	 * Add a new track to the timeline. Will be given the label "Track n" where
	 * n is the number of the track.
	 *
	 * @return The timeline track that was added.
	 */
	public TimelineTrack addTrack()
	{
		return this.addTrack( "Track " + this.data.size() + 1 );
	}

	/**
	 * Add a track with the given label.
	 *
	 * @param label The label
	 * @return The timeline track that was added
	 */
	public TimelineTrack addTrack( final String label )
	{
		return this.addTrack( new TimelineTrack( label, this.nTracks++ ) );
	}

	/**
	 * Add a new track to the timeline.
	 *
	 * @param tt The track to add.
	 * @return The timeline track that was added
	 */
	public TimelineTrack addTrack( final TimelineTrack tt )
	{
		this.tracks.put( tt.getTrackNumber(), tt );
		return tt;
	}

	/**
	 *	Add an object to a track.
	 *
	 *	@param tt The track to add the object to
	 *	@param obj The timeline objec to add
	 *	@return The timeline track
	 */
	public TimelineTrack addTimelineObject( final TimelineTrack tt, final TimelineObject obj )
	{
		super.addObject( tt.getTrackNumber(), obj.getStartTimeMilliseconds(), obj );

		if( obj instanceof AnimatedVisualisationProvider )
			((AnimatedVisualisationProvider)obj).addAnimatedVisualisationListener( this );

		obj.setDataPixelTransformer(
			this.axesRenderer2D.getRelativePixelTransformer(
				(int)this.axesRenderer2D.calculatePosition(
						obj.getStartTimeMilliseconds(), 0 ).getX(),
				tt.getTrackNumber() ) );

		return tt;
	}

	/**
	 * Add a new marker with a label.
	 *
	 * @param timeMilliseconds The time at which the marker should be added.
	 * @param label The label to put on the marker.
	 * @return The created timeline marker object.
	 */
	public TimelineMarker addMarker( final long timeMilliseconds, final String label )
	{
		final TimelineMarker m = new TimelineMarker();
		m.type = TimelineMarkerType.LABEL;
		m.time = timeMilliseconds;
		m.label = label;
		this.markers.add( m );
		this.repaint();
		return m;
	}

	/**
	 * Add a new marker
	 *
	 * @param timeMilliseconds The time at which to add the marker
	 * @return The created timeline marker object
	 */
	public TimelineMarker addMarker( final long timeMilliseconds )
	{
		final TimelineMarker m = new TimelineMarker();
		m.type = TimelineMarkerType.FLAG;
		m.time = timeMilliseconds;
		this.markers.add( m );
		this.repaint();
		return m;
	}

	/**
	 * Returns the scalar between milliseconds and pixels. If this was to return
	 * 1 then it would mean one pixel represented 1 millisecond; if this was to
	 * return 1000 then it would mean one pixel represented one second.
	 *
	 * @return The time scalar (milliseconds per pixel)
	 */
	public double getTimeScalar()
	{
		return this.timeScalar;
	}

	/**
	 * Set the time scalar of this timeline. The units are milliseconds per
	 * pixel.
	 *
	 * @param ts The new time scalar
	 */
	public void setTimeScalar( final double ts )
	{
		this.timeScalar = ts;
		this.axesRenderer2D.setMaxXValue( this.timeScalar * (this.visImage.getWidth()-this.sidebarWidth) );

		this.updateVis();
	}

	/**
	 * Find the position of the given time on the panel.
	 *
	 * @param milliseconds the number of milliseconds
	 * @return The pixel position
	 */
	public int getTimePosition( final long milliseconds )
	{
		return (int) (milliseconds / this.getTimeScalar());
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImpl#update()
	 */
	@Override
	public void update()
	{
		// Draw the sidebar
		this.visImage.drawImage( this.sidebarPanel, 0, 0 );

		super.update();

		// Draw all the timeline markers
		for( final TimelineMarker m : this.markers )
		{
			final int x = this.getTimePosition( m.time );
			m.type.drawMarker( m, this.visImage, x, this.getHeight() );
		}
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.general.DiversityAxis#bandSizeKnown(int)
	 */
	@Override
	public void bandSizeKnown( final int bandSize )
	{
		this.plotter.setBandSize( bandSize );
		super.bandSizeKnown( bandSize );
	}

	/**
	 * 	Main method test for the timeline
	 *	@param args The command-line args (unused)
	 */
	public static void main( final String[] args )
	{
		final Timeline t = new Timeline( 1200, 400 );
		t.showWindow( "Timeline" );
	}
}
