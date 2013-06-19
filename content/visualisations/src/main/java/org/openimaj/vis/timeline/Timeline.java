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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.openimaj.video.timecode.HrsMinSecFrameTimecode;

/**
 * A Swing timeline object.
 *
 * 	TODO: This needs to be retested as lots has changed since this was written and it probably doesn't work.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 3 Jul 2012
 * @version $Author$, $Revision$, $Date$
 */
public class Timeline extends JPanel
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Represents a track in the timeline. A track consists of a group of
	 * timeline objects which will be drawn all within the same bounds.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 3 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public class TimelineTrack extends JPanel implements ComponentListener
	{
		/** */
		private static final long serialVersionUID = 1L;

		/** The label */
		private final String label;

		/** List of objects in this track */
		private final List<TimelineObject> objects = new ArrayList<TimelineObject>();

		/** Markers for the track */
		private final List<TimelineMarker> markers = new ArrayList<TimelineMarker>();

		/** The preferred size of the track */
		private int preferredTrackHeight = 0;

		/** Used to avoid infinite loop of resizing */
		private boolean fixingFlag = false;

		/**
		 * Instantiate a new track with the given label.
		 *
		 * @param label The label of the track
		 */
		public TimelineTrack( final String label )
		{
			this.setBackground( new Color( 60, 60, 60 ) );

			this.label = label;

			// We'll position the timeline objects absolutely
			this.setLayout( null );

			// Add a component listener for resize events
			this.addComponentListener( this );
		}

		/**
		 * Adds a timeline to the track. Because timeline objects store their
		 * own start and end times, it's important that they are not reused on
		 * different tracks.
		 *
		 * @param obj The object to add
		 */
		public void addTimelineObject( final TimelineObjectAdapter<?> obj )
		{
			this.objects.add( obj );
			this.add( obj );
			obj.setViewSize( Timeline.this.getSize(), 0, 0 );
			this.fixSizes();
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
			tm.colour = Color.yellow;
			this.markers.add( tm );
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint( final Graphics g )
		{
			super.paint( g );

			for( final TimelineMarker m : this.markers )
			{
				final int x = Timeline.this.getTimePosition( m.time );
				m.type.drawMarker( m, g, x, this.getHeight() );
			}
		}

		/**
		 * The sizes of all the timeline objects are determined by the time
		 * scalar (see #getTimeScalar} and this method resets the sizes and
		 * positions of all the objects on the timeline.
		 */
		private void fixSizes()
		{
			if( this.fixingFlag ) return;

			this.fixingFlag = true;
			int max = 0;
			for( final TimelineObject o : this.objects )
			{
				final int s = (int)(o.getStartTimeMilliseconds() / Timeline.this.getTimeScalar());
				final int w = (int)((o.getEndTimeMilliseconds() - o.getStartTimeMilliseconds())
						/ Timeline.this.getTimeScalar() );
				o.setViewSize( o.getPreferredSize(), s, 0 );
				max = Math.max( max, s+w );
			}
			this.setPreferredSize( new Dimension( max, this.preferredTrackHeight ) );
			this.setSize( max, this.preferredTrackHeight );
			this.setBounds( 0, 0, max, this.preferredTrackHeight );
			this.fixingFlag = false;
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized( final ComponentEvent e )
		{
			this.fixSizes();
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentMoved( final ComponentEvent e )
		{
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentShown( final ComponentEvent e )
		{
			System.out.println( "Show" );
			this.fixSizes();
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentHidden( final ComponentEvent e )
		{
		}

		/**
		 * 	Set the preferred size of this track
		 *	@param t The preferred size
		 */
		public void setPreferredTrackHeight( final int t )
		{
			this.preferredTrackHeight = t;
			this.fixSizes();
		}
	}

	/**
	 * Markers are drawn directly onto the JPanel.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 3 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	static public class TimelineMarker
	{
		/** The marker type */
		public TimelineMarkerType type = TimelineMarkerType.FLAG;

		/** The time of the marker */
		public long time = 0;

		/** The label of the marker, if it has one */
		public String label;

		/** The colour of the marker */
		public Color colour = Color.black;
	}

	/**
	 * Different type of markers.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 3 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	static public enum TimelineMarkerType
	{
		/**
		 * Draws a marker with a little flag showing some text
		 */
		LABEL
		{
			@Override
			public void drawMarker( final TimelineMarker m, final Graphics g, final int x, final int h )
			{
				g.drawLine( x, 0, x, h );

				final FontMetrics metrics = g.getFontMetrics();
				final int fw = metrics.stringWidth( m.label );
				final int fh = metrics.getHeight() + metrics.getDescent();

				g.setColor( m.colour );
				g.fillRect( x, 4, fw+4, fh + 4 );

				g.setColor( TimelineMarkerType.getOpposingColour( m.colour ) );
				g.drawString( m.label, x+2, metrics.getHeight() + 4 );
			}
		},
		/**
		 * Draws a marker with a little flag at the top
		 */
		FLAG
		{
			private final int fs = 10;

			@Override
			public void drawMarker( final TimelineMarker m, final Graphics g, final int x, final int h )
			{
				g.drawLine( x, 0, x, h );
				g.fillPolygon( new int[]
				{ x, x + this.fs * 2, x }, new int[]
				{ 0, this.fs / 2, this.fs }, 3 );
			}
		};

		/**
		 * Draws this marker into the given graphics context at the given
		 * position and with the given length.
		 *
		 * @param m The marker
		 * @param g The graphics context
		 * @param xPos The position
		 * @param height The length to draw the marker
		 */
		public abstract void drawMarker( TimelineMarker m, Graphics g,
				int xPos, int height );

		/**
		 * Returns an opposing colour
		 *
		 * @param c The colour to oppose
		 * @return An opposing colour
		 */
		static private Color getOpposingColour( final Color c )
		{
			final float[] hsv = new float[3];
			Color.RGBtoHSB( c.getRed(), 255 - c.getGreen(), c.getBlue(), hsv );
			float H = (float)( hsv[0] + 0.5 );
			if( H > 1 ) H -= 1;
			return new Color( Color.HSBtoRGB( hsv[0], hsv[1], hsv[2] ) );
		}
	}

	/**
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 9 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public class TimelineRuler extends JPanel
	{
		/** */
        private static final long serialVersionUID = 1L;

        /** The time scalar in milliseconds per pixel */
		private double scalar = 100;

		/** The number of frames per second */
		private final double fps = 25;

		/** The left margin */
		private int leftMargin = 0;

		/** The right margin amount */
		private final int rightMargin = 0;

		/** The offset of the main axis as a percentage of the total height */
		private final double axisOffset = 0.25;

		/** The height of the minute ticks as a percentage of panel height */
		private final double minuteTickHeight = 1.5;

		/** The height of the second ticks as a percentage of panel height */
		private final double secondTickHeight = 0.2;

		/** The height of the frame ticks as a percentage of panel height */
		private final double frameTickHeight = 0.06;

		/** Colour of the ruler's main axis */
		private final Color rulerColour = Color.white;

		/** The colour of the minute ticks */
		private final Color minuteTickColour = Color.white;

		/** The colour of the second ticks */
		private final Color secondTickColour = new Color(200,200,200);

		/** The colour of the frame ticks */
		private final Color frameTickColour = new Color(160,160,160);

		/**
		 * 	Default constructor
		 */
		public TimelineRuler()
        {
			this.setPreferredSize( new Dimension( 1000, 25 ) );
        }

        /**
         * 	The time scalar in use.
         *	@param scalar The scalar to use
         */
        public void setScalar( final double scalar )
        {
        	this.scalar = scalar;
        }

        /**
         * 	Set the left margin position in pixels
         *	@param margin The left margin
         */
        public void setLeftMargin( final int margin )
        {
        	this.leftMargin = margin;
        }

        /**
         *	{@inheritDoc}
         * 	@see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        @Override
        public void paint( final Graphics g )
        {
            super.paint( g );

            // The mid (y) position
            final int midPoint = this.getHeight() / 2;

            // Where to draw the axis (y position)
            final int axisPosition = (int)(midPoint + (this.getHeight()*this.axisOffset));

            // If we should draw frames
            if( this.scalar < (250/this.fps) )
            {
            	// Draw the ticks
            	final double step = (1000/this.fps) / this.scalar; // pixels per second

            	final int tickLength = (int)(this.getHeight() * this.frameTickHeight);
            	g.setColor( this.frameTickColour );
            	for( double x = this.leftMargin; x < this.getWidth()-this.rightMargin; x += step )
            		g.drawLine( (int)x, axisPosition-tickLength,
            					(int)x, axisPosition+tickLength );
            }

            // If we should draw seconds
            if( this.scalar < 5000 )
            {
            	// Draw the ticks
            	double step = 1000 / this.scalar; // pixels per second

            	// Every 10 seconds if we're a bit small
            	if( this.scalar >= 250 ) step *= 6;

            	final int tickLength = (int)(this.getHeight() * this.secondTickHeight);
            	g.setColor( this.secondTickColour );
            	for( double x = this.leftMargin; x < this.getWidth()-this.rightMargin; x += step )
            		g.drawLine( (int)x, axisPosition-tickLength,
            					(int)x, axisPosition+tickLength );

            	// We'll draw labels if we're very small
            	if( this.scalar < 100 )
            	{
            		final HrsMinSecFrameTimecode tc = new HrsMinSecFrameTimecode( 0, this.fps );
            		final int h = g.getFontMetrics().getHeight();
            		for( double x = this.leftMargin; x < this.getWidth()-this.rightMargin; x += step )
            		{
            			tc.setTimecodeInMilliseconds( (long)(x*this.scalar) );
            			g.drawString( tc.toString(), (int)x+2, h );
            		}
            	}
            }

            // If we should draw minutes
            if( this.scalar < 15000 )
            {
            	// Draw the ticks
            	final double step = 60000 / this.scalar; // pixels per minute

            	final int tickLength = (int)(this.getHeight() * this.minuteTickHeight);
            	g.setColor( this.minuteTickColour );
            	for( double x = this.leftMargin; x < this.getWidth()-this.rightMargin; x+= step )
            		g.drawLine( (int)x, axisPosition-tickLength,
            					(int)x, axisPosition+tickLength );

            	// We'll draw labels if we're very small
            	if( this.scalar < 5000 )
            	{
            		final HrsMinSecFrameTimecode tc = new HrsMinSecFrameTimecode( 0, this.fps );
            		final int h = g.getFontMetrics().getHeight();
            		for( double x = this.leftMargin; x < this.getWidth()-this.rightMargin; x += step )
            		{
            			tc.setTimecodeInMilliseconds( (long)(x*this.scalar) );
            			g.drawString( tc.toString(), (int)x+2, h );
            		}
            	}
            }

            // Draw the main axis
            g.setColor( this.rulerColour  );
            g.drawLine( this.leftMargin, axisPosition, this.getWidth()-this.rightMargin,
            		axisPosition );

        }
	}

	/** Width of the sidebar in pixels */
	private final int sidebarWidth = 150;

	/** List of tracks in this timeline */
	private final List<TimelineTrack> tracks = new ArrayList<Timeline.TimelineTrack>();

	/** List of markers on this timeline */
	private final List<TimelineMarker> markers = new ArrayList<Timeline.TimelineMarker>();

	/** The constraints to use for the tracks */
	private final GridBagConstraints gbc = new GridBagConstraints();

	/** Panel containing the ruler */
	private TimelineRuler rulerPanel;

	/** The panel containing the tracks */
	private JPanel tracksPanel;

	/** The sidebar panel */
	private final JPanel sidebarPanel;

	/** The functions panel */
	private final JPanel functionsPanel;

	/** The default time scalar is 50 milliseconds per pixel */
	private double timeScalar = 100;

	/**
	 * Default constructor
	 */
	public Timeline()
	{
		this.setLayout( new GridBagLayout() );
		this.setBackground( new Color( 80, 80, 80 ) );

		this.sidebarPanel = new JPanel( new GridBagLayout() );
		this.sidebarPanel.setSize( this.sidebarWidth, this.getHeight() );
		this.sidebarPanel.setPreferredSize( new Dimension( this.sidebarWidth, this.getHeight() ) );
		this.sidebarPanel.setBounds( 0, 0, this.sidebarWidth, this.getHeight() );

		this.tracksPanel = new JPanel( new GridBagLayout() );

		this.rulerPanel = new TimelineRuler();
		this.rulerPanel.setBackground( Color.BLACK );
		this.rulerPanel.setScalar( this.getTimeScalar() );
		this.tracksPanel = new JPanel( new GridBagLayout() );
		this.rulerPanel = new TimelineRuler();
		this.rulerPanel.setBackground( Color.BLACK );
		this.rulerPanel.setScalar( this.getTimeScalar() );
		this.gbc.fill = GridBagConstraints.HORIZONTAL;
		this.gbc.weightx = 1;
		this.gbc.weighty = 0;
		this.gbc.gridx = 0;
		this.gbc.gridy = 0;
		this.tracksPanel.add( this.rulerPanel, this.gbc );

		// Add the sidebar
		this.gbc.fill = GridBagConstraints.VERTICAL;
		this.gbc.weightx = 0;
		this.gbc.weighty = 1;
		this.gbc.gridwidth = 1;
		this.gbc.gridy++;
		this.add( this.sidebarPanel, this.gbc );

		// Add the tracks
		this.gbc.fill = GridBagConstraints.BOTH;
		this.gbc.gridx++;
		this.gbc.weightx = 1;
		this.add( new JScrollPane( this.tracksPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS ), this.gbc );

		// Add the functions panel
		this.functionsPanel = new JPanel( new GridBagLayout() );

		final JButton ziButton = new JButton( "Zoom In" );
		final JButton zoButton = new JButton( "Zoom Out" );

		ziButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				Timeline.this.setTimeScalar( Timeline.this.getTimeScalar()/2 );
			}
		} );
		zoButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				Timeline.this.setTimeScalar( Timeline.this.getTimeScalar() *2 );
			}
		} );

		final GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridwidth = 1; gbc2.gridx = gbc2.gridy = 0;
		this.functionsPanel.add( ziButton, gbc2 );
		gbc2.gridx++;
		this.functionsPanel.add( zoButton, gbc2 );

		this.gbc.gridwidth = 2; this.gbc.gridx = 0; this.gbc.gridy++;
		this.gbc.weightx = 1; this.gbc.weighty = 0;
		this.add( this.functionsPanel, this.gbc );

		// Set up the grid bag constraints for the tracks
		this.gbc.fill = GridBagConstraints.BOTH;
		this.gbc.weightx = 1;
		this.gbc.weighty = 0;
		this.gbc.gridx = 0;
		this.gbc.gridy = 2;
	}

	/**
	 * Add a new track to the timeline. Will be given the label "Track n" where
	 * n is the number of the track.
	 *
	 * @return The timeline track that was added.
	 */
	public TimelineTrack addTrack()
	{
		return this.addTrack( "Track " + this.tracks.size() + 1 );
	}

	/**
	 * Add a track with the given label.
	 *
	 * @param label The label
	 * @return The timeline track that was added
	 */
	public TimelineTrack addTrack( final String label )
	{
		return this.addTrack( new TimelineTrack( label ) );
	}

	/**
	 * Add a new track to the timeline.
	 *
	 * @param tt The track to add.
	 * @return The timeline track that was added
	 */
	public TimelineTrack addTrack( final TimelineTrack tt )
	{
		// Create a side-bar for the new track
		final JLabel sb = new JLabel( tt.label );
		sb.setOpaque( true );
		sb.setBackground( new Color( 60, 60, 60 ) );
		sb.setForeground( Color.white );
		sb.setSize( this.sidebarWidth, 30 );
		sb.setPreferredSize( new Dimension( this.sidebarWidth, 30 ) );
		sb.setHorizontalAlignment( SwingConstants.CENTER );

		// Add the sidebar
		this.gbc.weightx = this.gbc.weighty = 1;
		this.gbc.insets = new Insets( 1, 1, 1, 1 );
		this.sidebarPanel.add( sb, this.gbc );

		// Add the track
		this.gbc.weightx = this.gbc.weighty = 1;
		this.tracksPanel.add( tt, this.gbc );
		this.tracks.add( tt );

		for( final TimelineTrack ttt : this.tracks )
			ttt.setPreferredTrackHeight( this.getHeight()/this.tracks.size() );

		this.gbc.gridy++;
		this.gbc.insets = new Insets( 0, 0, 0, 0 );

		this.revalidate();
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
	 * @return The time scalar
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
		this.rulerPanel.setScalar( ts );
		this.repaint();
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
	 * {@inheritDoc}
	 *
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint( final Graphics g )
	{
		super.paint( g );

		for( final TimelineMarker m : this.markers )
		{
			final int x = this.getTimePosition( m.time );
			m.type.drawMarker( m, g, x, this.getHeight() );
		}
	}
}
