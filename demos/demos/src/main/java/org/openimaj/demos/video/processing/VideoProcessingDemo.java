/**
 * 
 */
package org.openimaj.demos.video.processing;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.Mode;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 28 Sep 2011
 */
public class VideoProcessingDemo extends JPanel implements VideoDisplayListener<MBFImage>
{
	/** */
    private static final long serialVersionUID = 1L;
	private Video<MBFImage> video;
	private VideoDisplay<MBFImage> videoDisplay;
	private ImageComponent ic;
	private JButton stopButton;
	private JButton playButton;
	private JButton pawsButton;
	private Thread videoThread;
	private ProcessingPanel processingPanel;
	private JLabel fps;
	private long startTime;

    /**
     * @throws IOException 
     * 
     */
	public VideoProcessingDemo() throws IOException
    {
		video = new VideoCapture( 320, 240 );
		ic = new ImageComponent( true );
		ic.setPreferredSize( new Dimension(320,240) );
		init();
		useWebcam();
    }
	
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
			public void actionPerformed( ActionEvent e )
			{
				videoDisplay.setMode( Mode.STOP );
			}
		});
		playButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				videoDisplay.setMode( Mode.PLAY );
			}
		});
		pawsButton.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				videoDisplay.setMode( Mode.PAUSE );
			}
		});
		
//		this.add( stopButton, gbc );
//		gbc.gridx++;
		this.add( playButton, gbc );
		gbc.gridx++;
		this.add( pawsButton, gbc );
		
		gbc.gridx = t; gbc.weightx = 1;
		this.add( fps = new JLabel(""), gbc );
		fps.setHorizontalTextPosition( JLabel.CENTER );
	}
	
	/**
	 * 	Return the image component used for displaying the video image.
	 *  @return
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
		stopVideo();
		video = new VideoCapture( 320, 240 );
		videoDisplay = new VideoDisplay<MBFImage>( video, ic );
		addListeners();
		videoThread = new Thread(videoDisplay);
		videoThread.start();
	}
	
	/**
	 * 	Set the processing source to be the file
	 *  @param f
	 */
	public void useFile( File f )
	{
		stopVideo();
		video = new XuggleVideo( f );
		videoDisplay = new VideoDisplay<MBFImage>( video, ic );	
		addListeners();
		videoThread = new Thread(videoDisplay);
		videoThread.start();
	}
	
	private void stopVideo()
	{
		if( video instanceof VideoCapture )
			((VideoCapture)video).stopCapture();
		if( videoDisplay != null )
			videoDisplay.setMode( Mode.STOP );
	}
	
	private void addListeners()
	{
		videoDisplay.addVideoListener( this );
		videoDisplay.addVideoListener( processingPanel );
	}
	
	@Override
    public void afterUpdate( VideoDisplay<MBFImage> display )
    {
		double diff = System.currentTimeMillis() - startTime;
		double d = Math.round(1d/(diff/10000d))/10d;
		
		fps.setText( ""+d+" fps" );
    }

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
	        JFrame f = new JFrame( "Video Processing Demo" );
	        f.getContentPane().add( new VideoProcessingDemo() );
	        f.pack();
	        f.setVisible( true );
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
