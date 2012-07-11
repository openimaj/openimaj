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
		
		/** The preferred size of the track */
		private int preferredTrackHeight = 0;
		
		/** Used to avoid infinite loop of resizing */
		private boolean fixingFlag = false;

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
			obj.setViewSize( Timeline.this.getSize(), 0 );
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
			if( fixingFlag ) return;
			
			fixingFlag = true;
			int max = 0;
			for( TimelineObject o : objects )
			{
				int s = (int)(o.getStartTimeMilliseconds() / getTimeScalar()); 
				int w = (int)((o.getEndTimeMilliseconds() - o.getStartTimeMilliseconds()) 
						/ getTimeScalar() );
				o.setBounds( s, 0, w, o.getPreferredSize().height ); 
				max = Math.max( max, s+w );
			}
			this.setPreferredSize( new Dimension( max, preferredTrackHeight ) );
			this.setSize( max, preferredTrackHeight );
			this.setBounds( 0, 0, max, preferredTrackHeight );
//			revalidate();
			fixingFlag = false;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized( ComponentEvent e )
		{
			System.out.println( "Resize" );
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
			System.out.println( "Show" );
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
		
		/**
		 * 	Set the preferred size of this track
		 *	@param t The preferred size
		 */
		public void setPreferredTrackHeight( int t )
		{
			this.preferredTrackHeight = t;
			fixSizes();
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
		private double fps = 25;

		/** The left margin */
		private int leftMargin = 0;
		
		/** The right margin amount */
		private int rightMargin = 0;
		
		/** The offset of the main axis as a percentage of the total height */
		private double axisOffset = 0.25;
		
		/** The height of the minute ticks as a percentage of panel height */
		private double minuteTickHeight = 1.5;
		
		/** The height of the second ticks as a percentage of panel height */
		private double secondTickHeight = 0.2;
		
		/** The height of the frame ticks as a percentage of panel height */
		private double frameTickHeight = 0.06;

		/** Colour of the ruler's main axis */
		private Color rulerColour = Color.white;

		/** The colour of the minute ticks */
		private Color minuteTickColour = Color.white;
		
		/** The colour of the second ticks */
		private Color secondTickColour = new Color(200,200,200);
		
		/** The colour of the frame ticks */
		private Color frameTickColour = new Color(160,160,160);
		
		/**
		 * 	Default constructor
		 */
		public TimelineRuler()
        {
			setPreferredSize( new Dimension( 1000, 25 ) );
        }
		
        /**
         * 	The time scalar in use.
         *	@param scalar The scalar to use
         */
        public void setScalar( double scalar )
        {
        	this.scalar = scalar;
        }
        
        /**
         * 	Set the left margin position in pixels
         *	@param margin The left margin
         */
        public void setLeftMargin( int margin )
        {
        	this.leftMargin = margin;
        }
        
        /**
         *	{@inheritDoc}
         * 	@see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        @Override
        public void paint( Graphics g )
        {
            super.paint( g );
            
            // The mid (y) position
            int midPoint = getHeight() / 2;
            
            // Where to draw the axis (y position)
            int axisPosition = (int)(midPoint + (getHeight()*axisOffset));
            
            // If we should draw frames
            if( scalar < (250/fps) )
            {
            	// Draw the ticks
            	double step = (1000/fps) / scalar; // pixels per second
            	
            	int tickLength = (int)(getHeight() * frameTickHeight);
            	g.setColor( frameTickColour );
            	for( double x = leftMargin; x < getWidth()-rightMargin; x += step )
            		g.drawLine( (int)x, axisPosition-tickLength, 
            					(int)x, axisPosition+tickLength );            	
            }

            // If we should draw seconds
            if( scalar < 5000 )
            {
            	// Draw the ticks
            	double step = 1000 / scalar; // pixels per second
            	
            	// Every 10 seconds if we're a bit small
            	if( scalar >= 250 ) step *= 6;
            	
            	int tickLength = (int)(getHeight() * secondTickHeight);
            	g.setColor( secondTickColour );
            	for( double x = leftMargin; x < getWidth()-rightMargin; x += step )
            		g.drawLine( (int)x, axisPosition-tickLength, 
            					(int)x, axisPosition+tickLength );
            	
            	// We'll draw labels if we're very small
            	if( scalar < 100 )
            	{
            		HrsMinSecFrameTimecode tc = new HrsMinSecFrameTimecode( 0, fps );
            		int h = g.getFontMetrics().getHeight();
            		for( double x = leftMargin; x < getWidth()-rightMargin; x += step )
            		{
            			tc.setTimecodeInMilliseconds( (long)(x*scalar) );
            			g.drawString( tc.toString(), (int)x+2, h );
            		}
            	}
            }
            
            // If we should draw minutes
            if( scalar < 15000 )
            {
            	// Draw the ticks
            	double step = 60000 / scalar; // pixels per minute
            	
            	int tickLength = (int)(getHeight() * minuteTickHeight);
            	g.setColor( minuteTickColour );
            	for( double x = leftMargin; x < getWidth()-rightMargin; x+= step )
            		g.drawLine( (int)x, axisPosition-tickLength, 
            					(int)x, axisPosition+tickLength );

            	// We'll draw labels if we're very small
            	if( scalar < 5000 )
            	{
            		HrsMinSecFrameTimecode tc = new HrsMinSecFrameTimecode( 0, fps );
            		int h = g.getFontMetrics().getHeight();
            		for( double x = leftMargin; x < getWidth()-rightMargin; x += step )
            		{
            			tc.setTimecodeInMilliseconds( (long)(x*scalar) );
            			g.drawString( tc.toString(), (int)x+2, h );
            		}
            	}
            }
            
            // Draw the main axis
            g.setColor( this.rulerColour  );
            g.drawLine( leftMargin, axisPosition, getWidth()-rightMargin, 
            		axisPosition );
            
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
	private TimelineRuler rulerPanel;

	/** The panel containing the tracks */
	private JPanel tracksPanel;

	/** The sidebar panel */
	private JPanel sidebarPanel;
	
	/** The functions panel */
	private JPanel functionsPanel;

	/** The default time scalar is 50 milliseconds per pixel */
	private double timeScalar = 100;

	/**
	 * Default constructor
	 */
	public Timeline()
	{
		this.setLayout( new GridBagLayout() );
		this.setBackground( new Color( 80, 80, 80 ) );

		sidebarPanel = new JPanel( new GridBagLayout() );
		sidebarPanel.setSize( sidebarWidth, getHeight() );
		sidebarPanel.setPreferredSize( new Dimension( sidebarWidth, getHeight() ) );
		sidebarPanel.setBounds( 0, 0, sidebarWidth, getHeight() );
		
		tracksPanel = new JPanel( new GridBagLayout() );
		
		rulerPanel = new TimelineRuler();
		rulerPanel.setBackground( Color.BLACK );
		rulerPanel.setScalar( getTimeScalar() );
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		tracksPanel.add( rulerPanel, gbc );

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
		this.add( new JScrollPane( tracksPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS ), gbc );

		// Add the functions panel
		functionsPanel = new JPanel( new GridBagLayout() );
		
		JButton ziButton = new JButton( "Zoom In" );
		JButton zoButton = new JButton( "Zoom Out" );
		
		ziButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				setTimeScalar( getTimeScalar()/2 );
			}
		} );
		zoButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				setTimeScalar( getTimeScalar() *2 );
			}
		} );
		
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridwidth = 1; gbc2.gridx = gbc2.gridy = 0;
		functionsPanel.add( ziButton, gbc2 );
		gbc2.gridx++;
		functionsPanel.add( zoButton, gbc2 );
		
		gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy++;
		gbc.weightx = 1; gbc.weighty = 0;
		this.add( functionsPanel, gbc );
		
		// Set up the grid bag constraints for the tracks
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 1;
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
		// Create a side-bar for the new track
		JLabel sb = new JLabel( tt.label );
		sb.setOpaque( true );
		sb.setBackground( new Color( 60, 60, 60 ) );
		sb.setForeground( Color.white );
		sb.setSize( sidebarWidth, 30 );
		sb.setPreferredSize( new Dimension( sidebarWidth, 30 ) );
		sb.setHorizontalAlignment( SwingConstants.CENTER );
		
		// Add the sidebar
		gbc.weightx = gbc.weighty = 1;
		gbc.insets = new Insets( 1, 1, 1, 1 );
		sidebarPanel.add( sb, gbc );

		// Add the track
		gbc.weightx = gbc.weighty = 1;
		tracksPanel.add( tt, gbc );
		tracks.add( tt );
		
		for( TimelineTrack ttt : tracks )
			ttt.setPreferredTrackHeight( getHeight()/tracks.size() );

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
		this.rulerPanel.setScalar( ts );
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
