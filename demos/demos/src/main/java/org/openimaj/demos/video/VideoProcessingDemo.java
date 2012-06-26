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
package org.openimaj.demos.video;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openimaj.demos.Demo;
import org.openimaj.demos.video.utils.NumberKeySeekListener;
import org.openimaj.demos.video.utils.ProcessingPanel;
import org.openimaj.demos.video.utils.SourcePanel;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * 	A demo of the video functions and video processing functions in OpenIMAJ.
 * 	This demo shows a window which allows the user to select between webcam
 * 	video or video from a file. It also provides a set of pre-defined processing
 * 	operators which can be turned on and off for the video.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 28 Sep 2011
 */
@Demo(
	author = "David Dupplaw", 
	description = "A simple GUI that demonstrates various video " +
			"processing functionalities " +
			"in OpenIMAJ and allows processing of both file and live videos.", 
	keywords = { "video" }, 
	title = "Video Processing"
)
public class VideoProcessingDemo extends JPanel implements VideoDisplayListener<MBFImage>
{
	/** */
    private static final long serialVersionUID = 1L;
    
    /** The video */
	private Video<MBFImage> video;
	
	/** The video display which will play the video */
	private VideoDisplay<MBFImage> videoDisplay;
	
	/** The image component into which the video is being painted (reused) */
	private ImageComponent ic;
	
	/** Button to stop the video */
	private JButton stopButton;
	
	/** Button to play the video */
	private JButton playButton;
	
	/** Button to pause the video */
	private JButton pawsButton;
	
	/** The thread which is running the video playback */
	private Thread videoThread;
	
	/** The panel which provides the processing functions */
	private ProcessingPanel processingPanel;
	
	/** A label to show the number of frames per second being processed */
	private JLabel fps;
	
	/** The time a frame started to be processed. */
	private long startTime;

    /**
     * 	Default constructor.
     * 
     * 	@throws IOException 
     */
	public VideoProcessingDemo() throws IOException
    {
		ic = new ImageComponent( true );
		ic.setPreferredSize( new Dimension(320,240) );
		init();
    }
	
	/**
	 * 	Sets up all the graphical widgets.
	 */
	private void init()
	{
		this.setLayout( new GridBagLayout() );
		
		// --------------------------------------------------------
		// Video display
		// --------------------------------------------------------
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = gbc.weighty = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = 3;
		
		this.add( ic, gbc );
		
		// --------------------------------------------------------
		// Controls panels
		// --------------------------------------------------------
		gbc.gridx += gbc.gridwidth; gbc.gridwidth = 1;
		JPanel p = new JPanel( new GridBagLayout() );
		
		GridBagConstraints sgbc = new GridBagConstraints();
		sgbc.fill = GridBagConstraints.BOTH;
		sgbc.weightx = 0; sgbc.weighty = 1;
		sgbc.gridx = sgbc.gridy = 0;
		sgbc.gridwidth = 1;
		
		p.add( new SourcePanel(this), sgbc );
		sgbc.gridy++;
		p.add( processingPanel = new ProcessingPanel(), sgbc );
		
		this.add( p, gbc );
		int t = gbc.gridx;
		
		// --------------------------------------------------------
		// Navigation buttons
		// --------------------------------------------------------
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		stopButton = new JButton( "STOP" );
		playButton = new JButton( "PLAY" );
		pawsButton = new JButton( "PAUSE" );
		
		stopButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				videoDisplay.setMode( Mode.STOP );
			}
		});
		playButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				videoDisplay.setMode( Mode.PLAY );
			}
		});
		pawsButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				videoDisplay.setMode( Mode.PAUSE );
			}
		});
		
		this.add( playButton, gbc );
		gbc.gridx++;
		this.add( pawsButton, gbc );
		
		gbc.gridx = t; gbc.weightx = 1;
		this.add( fps = new JLabel(""), gbc );
		fps.setHorizontalTextPosition( JLabel.CENTER );
	}
	
	/**
	 * 	@return The image component used for displaying the video image.
	 */
	public ImageComponent getImageComponent()
	{
		return ic;
	}

	/**
	 * 	Set the video source to be the webcam
	 *  @throws IOException
	 */
	public void useWebcam() throws IOException
	{
		// Stop any existing video
		stopVideo();
		
		// Setup a new video from the VideoCapture class
		video = new VideoCapture( 320, 240 );
		
		// Reset the video displayer to use the capture class
		videoDisplay = new VideoDisplay<MBFImage>( video, ic );
		
		// Make sure the listeners are sorted
		addListeners();
		
		// Start the new video playback thread
		videoThread = new Thread(videoDisplay);
		videoThread.start();
	}
	
	/**
	 * 	Set the processing source to be the file
	 *  @param f
	 */
	public void useFile( File f )
	{
		// Stop any existing video
		stopVideo();
		
		// Setup a new video from the video file
		video = new XuggleVideo( f , false);
		
		// Reset the video displayer to use the file video
		videoDisplay = new VideoDisplay<MBFImage>( video, ic );
		videoDisplay.setStopOnVideoEnd(false);
		
		// Make sure all the listeners are added to this new display
		addListeners();
		addVideoFileListeners();
		
		// Start the new video playback thread
		videoThread = new Thread(videoDisplay);
		videoThread.start();
	}
	
	private void addVideoFileListeners() {
		long eventMask = AWTEvent.KEY_EVENT_MASK;
		final NumberKeySeekListener keyEventListener = new NumberKeySeekListener(videoDisplay);
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				switch (event.getID()) {
					case KeyEvent.KEY_PRESSED:
						KeyEvent kevent = (KeyEvent) event;
						keyEventListener.keyPressed(kevent);
						break;
				};
			}
		}, eventMask);
//		rp.addKeyListener(new NumberKeySeekListener(videoDisplay));
	}

	/**
	 * 	Stops the current video.
	 */
	private void stopVideo()
	{
		if( video instanceof VideoCapture )
			((VideoCapture)video).stopCapture();
		if( videoDisplay != null )
			videoDisplay.setMode( Mode.STOP );
	}
	
	/**
	 * 	Adds the default listeners to the video display
	 */
	private void addListeners()
	{
		videoDisplay.addVideoListener( this );
		videoDisplay.addVideoListener( processingPanel );
	}
	
	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
	 */
	@Override
    public void afterUpdate( VideoDisplay<MBFImage> display )
    {
		double diff = System.currentTimeMillis() - startTime;
		double d = Math.round(1d/(diff/10000d))/10d;
		
		fps.setText( ""+d+" fps" );
    }

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	@Override
    public void beforeUpdate( MBFImage frame )
    {
		startTime = System.currentTimeMillis();
    }

	/**
	 * 
	 *  @param args
	 */
	public static void main( String[] args )
    {
	    try
        {
	    	VideoProcessingDemo demo = new VideoProcessingDemo() ;
	        JFrame f = new JFrame( "Video Processing Demo" );
	        f.getContentPane().add(demo );
	        f.pack();
	        f.setVisible( true );
//	        demo.useFile(new File("/Users/ss/Downloads/20070701_185500_bbcthree_doctor_who_confidential.ts"));
        }
        catch( HeadlessException e )
        {
	        e.printStackTrace();
        }
        catch( IOException e )
        {
	        e.printStackTrace();
        }
    }
}
