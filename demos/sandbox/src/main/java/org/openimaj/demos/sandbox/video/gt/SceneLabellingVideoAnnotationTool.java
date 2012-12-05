/**
 * 
 */
package org.openimaj.demos.sandbox.video.gt;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.openimaj.audio.AudioStream;
import org.openimaj.demos.sandbox.video.gt.VideoGroundTruth.IdentifiableVideoFrame;
import org.openimaj.demos.sandbox.video.gt.VideoGroundTruth.IdentifierProducer;
import org.openimaj.demos.sandbox.video.gt.VideoGroundTruth.StateProvider;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.video.timecode.VideoTimecode;

/**
 *	A tool for annotating scenes within videos as containing music, speech,
 *	both or neither.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 5 Dec 2012
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class SceneLabellingVideoAnnotationTool extends JPanel 
	implements IdentifierProducer, StateProvider
{
	/** */
	private static final long serialVersionUID = 1L;

	/** The video ground truth writer */
	private VideoGroundTruth groundTruth = null;

	/** The shot detector to use to analyse the video */
	private VideoShotDetector shotDetector = null;
	
	/** A list of the detected shot boundaries */
	private final Set<IdentifiableVideoFrame> shotBoundaries = 
			new HashSet<IdentifiableVideoFrame>();
	
	/** The last video shot boundary that was detected */
	private IdentifiableVideoFrame lastShotBoundary = null;
	
	/** The video being processed */
	private final Video<MBFImage> video;
	
	/** The JLabel that shows the last boundary frame */
	private final JLabel lastShotBoundaryFrameLabel = new JLabel();
	
	/** The JLabel that shows the last boundary timecode */
	private final JLabel lastShotBoundaryTimecodeLabel = new JLabel();
	
	/** The timecode of the previous frame to be processed */
	private VideoTimecode previousFrameTimecode = null;
	
	/** The frame for this UI */
	private JFrame frame;
	
	/** The list of state radio buttons */
	private AbstractButton[] radioButtons = null;
	
	/** A button group for the states */
	private final ButtonGroup stateButtonGroup = new ButtonGroup();

	/** The currently selected state */
	private String currentState;

	/**
	 * 	Returns a list of the states that are able to be labelled for any
	 * 	one scene.
	 *	@return The list of states
	 */
	public abstract String[] getStates();
	
	/**
	 *	@param video
	 *	@param audio
	 */
	public SceneLabellingVideoAnnotationTool( final Video<MBFImage> video, 
			final AudioStream audio )
	{
		this.video = video;
		this.groundTruth = new VideoGroundTruth( video, audio, this );
		this.shotDetector = new VideoShotDetector( video );
		this.init();
	}

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.demos.sandbox.video.gt.VideoGroundTruth.StateProvider#getCurrentState(org.openimaj.experiment.dataset.Identifiable)
	 */
	@Override
	public List<String> getCurrentState( final Identifiable id )
	{
		if( this.currentState == null ) return null;
		
		final List<String> list = new ArrayList<String>();
		list.add( this.currentState );
		return list;
	}

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.demos.sandbox.video.gt.VideoGroundTruth.IdentifierProducer#getIdentifiers()
	 */
	@Override
	public List<Identifiable> getIdentifiers()
	{
		final List<Identifiable> list = new ArrayList<Identifiable>();
		list.add( this.lastShotBoundary );
		return list;
	}
	
	/**
	 * 	Set up the UI
	 */
	private void init()
	{
		this.setLayout( new GridBagLayout() );
		
		// Set up a default constraints. We'll change this as we add
		// bits and bobs to the UI
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = gbc.weighty = 0;
		
		// Add the label that shows the last shot boundary frame
		gbc.insets = new Insets(4,4,4,4);
		gbc.weightx = 1;
		this.add( this.lastShotBoundaryFrameLabel, gbc );
		
		// Add the label that shows the timecode for the last shot boundary
		gbc.gridx++; gbc.weightx = 1;
		final Dimension d = this.lastShotBoundaryTimecodeLabel.getPreferredSize();
		d.width = 300;
		this.lastShotBoundaryTimecodeLabel.setPreferredSize( d );
		this.add( this.lastShotBoundaryTimecodeLabel, gbc );

		// Now add on radio buttons for all the possible states
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.insets = new Insets(1,1,1,1);
		final String[] states = this.getStates();
		this.radioButtons = new AbstractButton[ states.length ];
		for( int i = 0; i < states.length; i++ )
		{
			gbc.gridy++;
			this.add( this.radioButtons[i] = 
					new JToggleButton( states[i] ), gbc );
			this.stateButtonGroup.add( this.radioButtons[i] );
		}
		
		// Add an item listener that sets the current state when a button is pressed
		this.stateButtonGroup.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				SceneLabellingVideoAnnotationTool.this.currentState = 
						SceneLabellingVideoAnnotationTool.this.
							stateButtonGroup.getSelected().getText(); 
			}
		} );
		
		// Set up the video player.
		this.groundTruth.getVideoPlayer().setButtons( new String[]{"play","pause"} );
		this.groundTruth.getVideoPlayer().pause();
		this.groundTruth.getVideoPlayer().addVideoListener( 
				new VideoDisplayListener<MBFImage>()
		{
			@Override
			public void beforeUpdate( final MBFImage frame )
			{
				SceneLabellingVideoAnnotationTool.this.processFrame( frame );
			}
			
			@Override
			public void afterUpdate( final VideoDisplay<MBFImage> display )
			{
			}
		} );
		
		// Show the video player
		final JFrame f = this.groundTruth.getVideoPlayer().showFrame();
		
		// Show the tool
		final JFrame ff = this.showFrame();
		ff.setLocation( f.getLocation().x + f.getWidth(), f.getLocation().y );
		ff.setSize( ff.getWidth(), f.getHeight() );
	}
	
	/**
	 * 	Show the UI frame if it's not already being shown.
	 *	@return The UI frame
	 */
	private JFrame showFrame()
	{
		if( this.frame == null )
		{
			this.frame = new JFrame();
			this.frame.add( this );
			this.frame.pack();
		}

		this.frame.setVisible( true );
		return this.frame;
	}

	/**
	 * 	Process a frame from the video
	 *	@param frame The video frame
	 */
	protected void processFrame( final MBFImage frame )
	{
		// Pass the frame to our shot detector to see if the shot has changed.
		this.shotDetector.processFrame( frame );

		// Timecode for the current frame
		final HrsMinSecFrameTimecode tc = new HrsMinSecFrameTimecode( 
				this.video.getCurrentFrameIndex(), this.video.getFPS() );
		
		// If we're in to a new scene, we update the scene counter
		if( this.shotDetector.wasLastFrameBoundary() )
		{
			// If we already have a shot that we're dealing with, then
			// we'll create a region-based annotation for this shot and
			// give it to the video ground truth thingy
			if( this.lastShotBoundary != null )
			{
				// Add this as a data item to the ground truthed dataset
				this.groundTruth.updateIdentifiableRegion( this.lastShotBoundary, 
						this.lastShotBoundary.timecode, this.previousFrameTimecode );
			}
			
			// New shot boundary here
			this.lastShotBoundary = new IdentifiableVideoFrame( frame, tc );
			
			// Store this shot boundary
			this.shotBoundaries.add( this.lastShotBoundary );
			
			// Update the labels in the UI
			this.lastShotBoundaryFrameLabel.setIcon( 
				new ImageIcon( ImageUtilities.createBufferedImage( 
					frame.process( new ResizeProcessor( 200, 200, true ) ) ) ) );
			this.lastShotBoundaryTimecodeLabel.setText( tc.toString() );
		
			// Check whether there's a default state for our state buttons
			final String defaultState = this.getDefaultState();
			
			// Reset all the radio buttons - we don't want this new scene
			// to have the settings from the last scene
			for( final AbstractButton button : this.radioButtons )
			{
				if( defaultState == null || !button.getText().equals( defaultState ) )
						button.setSelected( false );
				else	button.setSelected( true );
			}
			
			this.currentState = null;
			
			// Make sure the UI gets updated
			this.repaint();
		}
		
		// Remember this timecode for the next processing loop
		this.previousFrameTimecode = tc;
	}

	/**
	 * 	Returns the default state to be set for each new scene. The method
	 * 	can return null to indicate that no default state should be used.
	 * 	If it returns a state, it should return a value that matches one of
	 * 	those returned by {@link #getStates()}.
	 * 
	 *	@return The default state
	 */
	public String getDefaultState()
	{
		return null;
	}	
}
