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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * A Swing timeline object.
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
		private String label;

		/** List of objects in this track */
		private List<TimelineObject> objects = new ArrayList<TimelineObject>();

		/** Markers for the track */
		private List<TimelineMarker> markers = new ArrayList<TimelineMarker>();

		/**
		 * Instantiate a new track with the given label.
		 * 
		 * @param label The label of the track
		 */
		public TimelineTrack( String label )
		{
			setBackground( new Color( 60, 60, 60 ) );

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
		public void addTimelineObject( TimelineObject obj )
		{
			this.objects.add( obj );
			this.add( obj );
			this.fixSizes();
		}

		/**
		 * Add a track marker to this time.
		 * 
		 * @param time The time to add the track marker
		 */
		public void addTrackMarker( long time )
		{
			TimelineMarker tm = new TimelineMarker();
			tm.time = time;
			tm.colour = Color.yellow;
			markers.add( tm );
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint( Graphics g )
		{
			super.paint( g );

			for( TimelineMarker m : markers )
			{
				int x = getTimePosition( m.time );
				m.type.drawMarker( m, g, x, getHeight() );
			}
		}

		/**
		 * The size of all the timeline objects are determined by the time
		 * scalar (see #getTimeScalar} and this method resets the sizes and
		 * positions of all the objects on the timeline.
		 */
		private void fixSizes()
		{
			for( TimelineObject o : objects )
			{
				o.setBounds(
						(int) (o.getStartTimeMilliseconds() * getTimeScalar() / 1000d),
						0,
						(int) ((o.getEndTimeMilliseconds() - o
								.getStartTimeMilliseconds()) * getTimeScalar() / 1000d),
						o.getPreferredSize().height );
			}
			revalidate();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized( ComponentEvent e )
		{
			fixSizes();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentMoved( ComponentEvent e )
		{
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentShown( ComponentEvent e )
		{
			fixSizes();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentHidden( ComponentEvent e )
		{
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
			public void drawMarker( TimelineMarker m, Graphics g, int x, int h )
			{
				g.drawLine( x, 0, x, h );

				FontMetrics metrics = g.getFontMetrics();
				int fw = metrics.stringWidth( m.label );
				int fh = metrics.getHeight() + metrics.getDescent();
				
				g.setColor( m.colour );
				g.fillRect( x, 4, fw+4, fh + 4 );
				
				g.setColor( getOpposingColour( m.colour ) );
				g.drawString( m.label, x+2, metrics.getHeight() + 4 );
			}
		},
		/**
		 * Draws a marker with a little flag at the top
		 */
		FLAG
		{
			private int fs = 10;

			@Override
			public void drawMarker( TimelineMarker m, Graphics g, int x, int h )
			{
				g.drawLine( x, 0, x, h );
				g.fillPolygon( new int[]
				{ x, x + fs * 2, x }, new int[]
				{ 0, fs / 2, fs }, 3 );
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
		static private Color getOpposingColour( Color c )
		{
			float[] hsv = new float[3];
			Color.RGBtoHSB( c.getRed(), 255 - c.getGreen(), c.getBlue(), hsv );
			float H = (float)( hsv[0] + 0.5 );
			if( H > 1 ) H -= 1;
			return new Color( Color.HSBtoRGB( hsv[0], hsv[1], hsv[2] ) );
		}
	}

	/** Width of the sidebar in pixels */
	private int sidebarWidth = 150;

	/** List of tracks in this timeline */
	private List<TimelineTrack> tracks = new ArrayList<Timeline.TimelineTrack>();

	/** List of markers on this timeline */
	private List<TimelineMarker> markers = new ArrayList<Timeline.TimelineMarker>();

	/** The constraints to use for the tracks */
	private GridBagConstraints gbc = new GridBagConstraints();

	/** Panel containing the ruler */
	private JPanel rulerPanel;

	/** The panel containing the tracks */
	private JPanel tracksPanel;

	/** The sidebar panel */
	private JPanel sidebarPanel;

	/** The default time scalar is 50 pixels per second */
	private double timeScalar = 5;

	/**
	 * Default constructor
	 */
	public Timeline()
	{
		this.setLayout( new GridBagLayout() );
		this.setBackground( new Color( 80, 80, 80 ) );

		rulerPanel = new JPanel();
		rulerPanel.setBackground( Color.BLACK );

		tracksPanel = new JPanel();
		tracksPanel.setLayout( new GridBagLayout() );

		sidebarPanel = new JPanel();
		sidebarPanel.setLayout( new GridBagLayout() );
		sidebarPanel.setSize( sidebarWidth, getHeight() );
		sidebarPanel
				.setPreferredSize( new Dimension( sidebarWidth, getHeight() ) );

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		this.add( rulerPanel, gbc );

		// Add the sidebar
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.gridwidth = 1;
		gbc.gridy++;
		this.add( sidebarPanel, gbc );

		// Add the tracks
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx++;
		gbc.weightx = 1;
		this.add( new JScrollPane( tracksPanel ), gbc );

		// Set up the grid bag constraints for the tracks
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
	}

	/**
	 * Add a new track to the timeline. Will be given the label "Track n" where
	 * n is the number of the track.
	 * 
	 * @return The timeline track that was added.
	 */
	public TimelineTrack addTrack()
	{
		return addTrack( "Track " + tracks.size() + 1 );
	}

	/**
	 * Add a track with the given label.
	 * 
	 * @param label The label
	 * @return The timeline track that was added
	 */
	public TimelineTrack addTrack( String label )
	{
		return addTrack( new TimelineTrack( label ) );
	}

	/**
	 * Add a new track to the timeline.
	 * 
	 * @param tt The track to add.
	 * @return The timeline track that was added
	 */
	public TimelineTrack addTrack( TimelineTrack tt )
	{
		JLabel sb = new JLabel( tt.label );
		sb.setOpaque( true );
		sb.setBackground( new Color( 60, 60, 60 ) );
		sb.setForeground( Color.white );
		sb.setSize( sidebarWidth, 30 );
		sb.setPreferredSize( new Dimension( sidebarWidth, 30 ) );
		sb.setHorizontalAlignment( SwingConstants.CENTER );
		gbc.insets = new Insets( 4, 4, 4, 4 );
		sidebarPanel.add( sb, gbc );

		tracksPanel.add( tt, gbc );
		tracks.add( tt );

		gbc.gridy++;
		gbc.insets = new Insets( 0, 0, 0, 0 );

		revalidate();
		return tt;
	}

	/**
	 * Add a new marker with a label.
	 * 
	 * @param timeMilliseconds The time at which the marker should be added.
	 * @param label The label to put on the marker.
	 * @return The created timeline marker object.
	 */
	public TimelineMarker addMarker( long timeMilliseconds, String label )
	{
		TimelineMarker m = new TimelineMarker();
		m.type = TimelineMarkerType.LABEL;
		m.time = timeMilliseconds;
		m.label = label;
		markers.add( m );
		repaint();
		return m;
	}

	/**
	 * Add a new marker
	 * 
	 * @param timeMilliseconds The time at which to add the marker
	 * @return The created timeline marker object
	 */
	public TimelineMarker addMarker( long timeMilliseconds )
	{
		TimelineMarker m = new TimelineMarker();
		m.type = TimelineMarkerType.FLAG;
		m.time = timeMilliseconds;
		markers.add( m );
		repaint();
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
	public void setTimeScalar( double ts )
	{
		this.timeScalar = ts;
		repaint();
	}

	/**
	 * Find the position of the given time on the panel.
	 * 
	 * @param milliseconds the number of milliseconds
	 * @return The pixel position
	 */
	public int getTimePosition( long milliseconds )
	{
		return (int) (milliseconds / getTimeScalar());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint( Graphics g )
	{
		super.paint( g );

		for( TimelineMarker m : markers )
		{
			int x = getTimePosition( m.time );
			m.type.drawMarker( m, g, x, getHeight() );
		}
	}
}
