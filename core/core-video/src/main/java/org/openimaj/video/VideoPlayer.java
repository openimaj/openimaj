/**
 * 
 */
package org.openimaj.video;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.openimaj.audio.AudioStream;
import org.openimaj.content.animation.animator.LinearTimeBasedIntegerValueAnimator;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.Image;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;

/**
 * 	This class is an extension of the {@link VideoDisplay} class that provides
 * 	GUI elements for starting, stopping, pausing and rewinding video. 
 * 	<p>
 * 	The class relies on the underlying {@link VideoDisplay}to actually provide 
 * 	the main functionality for video playing and indeed still allows those
 * 	methods to be used. This class then provides a simple API for starting,
 * 	pausing and stopping video.
 * 	<p>
 * 	Unlike {@link VideoDisplay}, the VideoPlayer class does not create a frame
 * 	when the {@link #createVideoPlayer(Video)} methods are called. Use the
 * 	{@link #showFrame()} method to produce a visible frame. 
 * 
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 * 	@created 10 Aug 2012
 * 	@version $Author$, $Revision$, $Date$
 * 	@param <T> The type of the video frame
 */
public class VideoPlayer<T extends Image<?, T>> extends VideoDisplay<T>
implements VideoDisplayStateListener
{
	/**
	 * 	The video player components encapsulates the buttons and their
	 * 	functionalities, as well as animating buttons, etc.
	 * 
	 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * 	@created 10 Aug 2012
	 * 	@version $Author$, $Revision$, $Date$
	 */
	protected class VideoPlayerComponent extends JPanel
	{
		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * 	This class represents the widgets in the video player
		 * 
		 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
		 * 	@created 10 Aug 2012
		 * 	@version $Author$, $Revision$, $Date$
		 */
		protected class ButtonsPanel extends JPanel implements VideoDisplayListener<T>
		{
			/** */
			private static final long serialVersionUID = 1L;

			/* The graphic for the play button */
			private final static String PLAY = "/play.png";
			private final static String STOP = "/stop.png";
			private final static String PAUSE = "/pause.png";
//			private final static String STEP_BACK = "/step-backward.png";
//			private final static String STEP_FORWARD = "/step-forward.png";
			
			/** The list of buttons in order of their display */
			private final String[] buttons = new String[]{
//					ButtonsPanel.STEP_BACK,
					ButtonsPanel.PAUSE,
//					ButtonsPanel.STEP_FORWARD,
					ButtonsPanel.PLAY,
					ButtonsPanel.STOP,
			};
			
			/** The methods to use for each of the buttons */
			private Method[] methods = null;
			
			/** Insets */
			private final int inset = 2;
			
			/** Progress bar */
			private final JProgressBar progress = new JProgressBar( 0 , 100 );
			
			/** The background image */
			private BufferedImage img = null;
			
			/** Label showing the current position */
			private final JLabel label = new JLabel("0:00:00/0:00:00");
			
			/**
			 * 	Construct a new buttons panel
			 *	@throws SecurityException
			 *	@throws NoSuchMethodException
			 */
			public ButtonsPanel() throws SecurityException, NoSuchMethodException
			{
				this.methods = new Method[]{
//						VideoPlayer.this.getClass().getMethod( "stepBack" ),
						VideoPlayer.this.getClass().getMethod( "pause" ),
//						VideoPlayer.this.getClass().getMethod( "stepForward" ),
						VideoPlayer.this.getClass().getMethod( "play" ),
						VideoPlayer.this.getClass().getMethod( "stop" ),
				};

				try
				{
					this.img = ImageIO.read( this.getClass().getResource( 
							"/brushed-metal.png" ) );
				}
				catch( final IOException e )
				{
					e.printStackTrace();
				}
				
				this.init();
				this.setPreferredSize( new Dimension( 
						(100+this.inset)*this.buttons.length, 
						100+this.inset ) );
				this.setSize( this.getPreferredSize() );
				
				VideoPlayer.this.addVideoListener( this );
			}

			/**
			 * 
			 */
			private void init()
			{
				this.setLayout( new GridBagLayout() );
				this.setOpaque( false );
				
				final GridBagConstraints gbc = new GridBagConstraints();
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = gbc.weighty = 0;
				gbc.gridx = gbc.gridy = 1;
				gbc.insets = new Insets( this.inset, this.inset, this.inset, this.inset );

				// ------------------------------------------------------------
				// Progress bar
				// ------------------------------------------------------------
				gbc.gridy = 0;
				gbc.weightx = 1;
				gbc.gridwidth = this.buttons.length;
				this.add( this.progress, gbc );
				this.progress.addMouseListener( new MouseAdapter()
				{
					@Override
					public void mouseClicked( final MouseEvent e )
					{
						System.out.println( "Clicked at "+e.getX() );
						VideoPlayer.this.setPosition( e.getX() * 100 /
								ButtonsPanel.this.getWidth() );
					}
				} );

				// ------------------------------------------------------------
				// Navigation Buttons
				// ------------------------------------------------------------
				final JPanel buttonsPanel = new JPanel( new GridBagLayout() );
				buttonsPanel.setBorder( BorderFactory.createEmptyBorder() );
				buttonsPanel.setOpaque( false );
				
				gbc.weightx = gbc.weighty = 0;
				gbc.gridx = gbc.gridy = 1;
				gbc.gridwidth = 1;
				for( int i = 0; i < this.buttons.length; i++ )
				{
					final String b = this.buttons[i];
					final ImageIcon buttonIcon = new ImageIcon( this.getClass()
							.getResource( b ) );
					final JLabel button = new JLabel( buttonIcon );
					button.setBorder( BorderFactory.createEmptyBorder() );							
					final int j = i;
					button.addMouseListener( new MouseAdapter()
					{
						@Override
						public void mouseClicked( final MouseEvent e ) 
						{
							try
							{
								ButtonsPanel.this.methods[j].invoke( 
										VideoPlayer.this );
							}
							catch( final IllegalArgumentException e1 )
							{
								e1.printStackTrace();
							}
							catch( final IllegalAccessException e1 )
							{
								e1.printStackTrace();
							}
							catch( final InvocationTargetException e1 )
							{
								e1.printStackTrace();
							}
						};
						
						@Override
						public void mouseEntered(final MouseEvent e) 
						{
							button.setBorder( BorderFactory.createLineBorder( Color.yellow ) );
						};
						
						@Override
						public void mouseExited(final MouseEvent e) 
						{
							button.setBorder( BorderFactory.createEmptyBorder() );							
						};
					} );
					buttonsPanel.add( button, gbc );
					gbc.gridx++;
				}
				buttonsPanel.add( this.label, gbc );
				
				gbc.gridy = 2;
				gbc.gridx = 1;
				this.add( buttonsPanel, gbc );
			}

			@Override
			public void paint( final Graphics g )
			{
				g.drawImage( this.img, 0, 0, null );
				super.paint( g );
			}
			
			/**
			 * 	Set the progress (0-100)
			 *	@param pc The %age value
			 */
			public void setProgress( final double pc )
			{
				this.progress.setValue( (int)pc );
			}

			@Override
			public void afterUpdate( final VideoDisplay<T> display )
			{
				this.setProgress( display.getPosition() );
				
				// The end timecode
				final HrsMinSecFrameTimecode end = new HrsMinSecFrameTimecode( 
						VideoPlayer.this.getVideo().countFrames(), 
						VideoPlayer.this.getVideo().getFPS() );
				
				final HrsMinSecFrameTimecode current = new HrsMinSecFrameTimecode( 
						VideoPlayer.this.getVideo().currentFrame, 
						VideoPlayer.this.getVideo().getFPS() );
				
				this.label.setText( current.toString()+"/"+end.toString() );
			}

			@Override
			public void beforeUpdate( final T frame )
			{
			}
		}
		
		/**
		 *	Class used to animate the buttons panel on and off the screen. 
		 *
		 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
		 *  @created 14 Aug 2012
		 *	@version $Author$, $Revision$, $Date$
		 */
		public class AnimatorThread implements Runnable
		{
			public boolean stopNow = false;
			public boolean buttonValue;
			
			/**
			 * 	Create a new animator thread. If the thread succeeds the
			 * 	showButtons value will be set to the tf value given.
			 *	@param tf Whether the buttons are shown (TRUE) or hidden
			 */
			public AnimatorThread( final boolean tf )
			{
				this.buttonValue = tf;
			}
			
			@Override
			public void run()
			{
				// Animate the buttons
				while( !this.stopNow && VideoPlayerComponent.this.animator != null &&
						!VideoPlayerComponent.this.animator.isComplete() )
				{
					VideoPlayerComponent.this.bp.setBounds( 
						VideoPlayerComponent.this.bp.getBounds().x, 
						VideoPlayerComponent.this.animator.nextValue(),
						VideoPlayerComponent.this.bp.getBounds().width,
						VideoPlayerComponent.this.bp.getBounds().height );
					try
					{
						// Sleep for 40ms - animates at roughly 25fps
						Thread.sleep( 40 );
					}
					catch( final InterruptedException e )
					{
					}
				}
				
				if( !this.stopNow )
					VideoPlayerComponent.this.showButtons = this.buttonValue;
			}			
		}

		/** The buttons panel */
		private ButtonsPanel bp = null;
		
		/** Whether to show the buttons */
		private boolean showButtons = true;
		
		/** The current mode of the buttons */
		private Mode currentMode = Mode.PLAY;
		
		/** The animator used to animate the buttons */
		private LinearTimeBasedIntegerValueAnimator animator = null;
		
		/** The animator thread */
		private AnimatorThread animatorThread = null;

		/**
		 * Create a new player component using the display component
		 * 
		 * @param ic The video display component
		 */
		public VideoPlayerComponent( final ImageComponent ic )
		{
			try
			{
				this.init( ic );
			}
			catch( final SecurityException e )
			{
				e.printStackTrace();
			}
			catch( final NoSuchMethodException e )
			{
				e.printStackTrace();
			}
		}

		/**
		 * Set up the widgets
		 * 
		 * @param ic The video display component
		 * @throws NoSuchMethodException 
		 * @throws SecurityException 
		 */
		private void init( final ImageComponent ic ) 
				throws SecurityException, NoSuchMethodException
		{
			this.setLayout( null );
			
			// Add the buttons
			this.bp = new ButtonsPanel();
			this.add( this.bp );

			// Add the video
			this.add( ic );
			
			// Set the size of the components based on the video component
			this.setPreferredSize( ic.getSize() );
			this.setSize( ic.getSize() );

			// Position the buttons panel
			this.bp.setBounds( 0, this.getHeight()-this.bp.getSize().height, 
					this.getWidth(), 
					this.bp.getSize().height );

			this.showButtons = true;

			// Add a mouse listener to toggle the button display.
			final MouseAdapter ma = new MouseAdapter()
			{
				@Override
				public void mouseEntered(final MouseEvent e) 
				{
					VideoPlayerComponent.this.setShowButtons( true );
				};
				
				@Override
				public void mouseExited(final MouseEvent e) 
				{
					if( !VideoPlayerComponent.this.getVisibleRect().contains( 
							e.getPoint() ) )
					{
						VideoPlayerComponent.this.setShowButtons( false );
					}
				};
			};
			ic.addMouseListener( ma );
			this.bp.addMouseListener( ma );
		}

		/**
		 * 	Reset the button states to the current state of the video player
		 */
		public void updateButtonStates()
		{
			// If we're changing mode
			if( this.currentMode != VideoPlayer.this.getMode() )
			{
				// Pop the buttons up if the mode changes.
				this.showButtons = true;
				
				// TODO: Update the graphics depending on the mode
				switch( VideoPlayer.this.getMode() )
				{
					case PLAY:
						break;
					case STOP:
						break;
					case PAUSE:
						break;
					default:
						break;
				}
				
				// Update the buttons to reflect the current video player mode
				this.currentMode = VideoPlayer.this.getMode();
			}
		}
		
		/**
		 * 	Set whether the buttons are in view or not.
		 *	@param tf TRUE to show the buttons
		 */
		public void setShowButtons( final boolean tf )
		{
			// Only need to do anything if the buttons are different to what
			// we want.
			if( tf != this.showButtons )
			{
				// Kill the current thread if there is one
				if( this.animatorThread != null )
				{
					this.animatorThread.stopNow = true;
					this.animatorThread = null;
				}
				
				// Create an animator to animate the buttons over 1/2 second
				// Animates from the current position to either off the screen
				// or on the screen depending on the value of tf
				this.animator = new LinearTimeBasedIntegerValueAnimator( 
						this.bp.getBounds().y, 
						this.getHeight()-(tf?this.bp.getSize().height:0), 
						500 );
				
				// Start the thread
				this.animatorThread = new AnimatorThread( tf );
				new Thread( this.animatorThread ).start();
				
				this.showButtons = tf;
			}
		}
	}

	/** The frame showing the player */
	private JFrame frame = null;

	/** The player component */
	private VideoPlayerComponent component = null;

	/**
	 * Create the video player to play the given video.
	 * 
	 * @param v The video to play
	 */
	public VideoPlayer( final Video<T> v )
	{
		this( v, null, new ImageComponent() );
	}

	/**
	 * Create the video player to play the given video.
	 * 
	 * @param v The video to play
	 * @param audio The audio to play
	 */
	public VideoPlayer( final Video<T> v, final AudioStream audio )
	{
		this( v, audio, new ImageComponent() );
	}

	/**
	 * Created the video player for the given video on the given image
	 * component.
	 * 
	 * @param v The video
	 * @param audio The audio
	 * @param screen The screen to draw the video to.
	 */
	protected VideoPlayer( final Video<T> v, final AudioStream audio, final ImageComponent screen )
	{
		super( v, audio, screen );

		screen.setSize( v.getWidth(), v.getHeight() );
		screen.setPreferredSize( new Dimension( v.getWidth(), v.getHeight() ) );
		screen.setAllowZoom( false );
		screen.setAllowPanning( false );
		screen.setTransparencyGrid( false );
		screen.setShowPixelColours( false );
		screen.setShowXYPosition( false );
		
		this.component = new VideoPlayerComponent( screen );
		this.component.setShowButtons( false );
		this.addVideoDisplayStateListener( this );
	}

	/**
	 * Creates a new video player in a new thread and starts it running
	 * (initially in pause mode).
	 * 
	 * @param video The video
	 * @return The video player
	 */
	public static <T extends Image<?, T>> VideoPlayer<T> createVideoPlayer(
			final Video<T> video )
	{
		final VideoPlayer<T> vp = new VideoPlayer<T>( video );
//		vp.pause();
		new Thread( vp ).start();
		return vp;
	}
	
	/**
	 * Creates a new video player in a new thread and starts it running
	 * (initially in pause mode).
	 * 
	 * @param video The video
	 * @param audio The udio
	 * @return The video player
	 */
	public static <T extends Image<?, T>> VideoPlayer<T> createVideoPlayer(
			final Video<T> video, final AudioStream audio )
	{
		final VideoPlayer<T> vp = new VideoPlayer<T>( video, audio );
//		vp.pause();
		new Thread( vp ).start();
		return vp;
	}

	/**
	 * Shows the video player in a frame. If a frame already exists it will be
	 * made visible.
	 */
	public void showFrame()
	{
		if( this.frame == null )
		{
			this.frame = new JFrame();
			this.frame.add( this.component );
			this.frame.pack();
		}

		this.frame.setVisible( true );
	}

	/**
	 * Play the video.
	 */
	public void play()
	{
		this.setMode( Mode.PLAY );
	}

	/**
	 * Stop the video
	 */
	public void stop()
	{
		this.setMode( Mode.STOP );
	}

	/**
	 * Pause the video
	 */
	public void pause()
	{
		this.setMode( Mode.PAUSE );
	}

	/**
	 * Step back a frame.
	 */
	public void stepBack()
	{

	}

	/**
	 * Step forward a frame.
	 */
	public void stepForward()
	{

	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoStopped(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoStopped( final VideoDisplay<?> v )
	{
		// If this is called it means the video mode was changed and the video
		// has stopped playing. We must let our buttons know that this has happened.
		this.component.updateButtonStates();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoPlaying(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoPlaying( final VideoDisplay<?> v )
	{
		// If this is called it means the video mode was changed and the video
		// has started playing. We must let our buttons know that this has happened.
		this.component.updateButtonStates();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoPaused(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoPaused( final VideoDisplay<?> v )
	{
		// If this is called it means the video mode was changed and the video
		// has been paused. We must let our buttons know that this has happened.
		this.component.updateButtonStates();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.VideoDisplayStateListener#videoStateChanged(org.openimaj.video.VideoDisplay.Mode, org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void videoStateChanged(
			final org.openimaj.video.VideoDisplay.Mode mode,
			final VideoDisplay<?> v )
	{
		// As we've implemented the other methods in this listener, so
		// we don't need to implement this one too.
	}
}
