/**
 * 
 */
package org.openimaj.vis.video;

import java.awt.Dimension;
import java.awt.Graphics;

import org.openimaj.audio.AudioStream;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.time.Timecode;
import org.openimaj.video.Video;
import org.openimaj.video.timecode.FrameNumberVideoTimecode;
import org.openimaj.vis.audio.AudioWaveformPlotter;
import org.openimaj.vis.timeline.Timeline.TimelineMarker;
import org.openimaj.vis.timeline.Timeline.TimelineMarkerType;
import org.openimaj.vis.timeline.TimelineObject;

/**
 *	Displays a block, or bar, which represents the video. The block will
 *	be scaled to fit the JPanel in which its drawn. The block will contain
 *	a visualisation of the video content. The visualisation of the content
 *	is determined by one of the subclasses of this class.
 *	<p>
 *	This class will process the video in a separate thread. 
 *	Obviously, it's not sensible to call this class with a 
 *	"live" video stream, such as from a VideoCapture object.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class VideoBarVisualisation extends TimelineObject
{
	/** */
	private static final long serialVersionUID = 1L;
	
	/**
	 *	A marker for marking video frames within the video bar
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 6 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public class VideoTimelineMarker extends TimelineMarker
	{
		/** The frame number in the video */
		public int frameNumber = 0;
	}

	/**
	 * 	Process a particular frame of the video. The frame and timecode
	 * 	of the frame are provided.
	 *	@param frame The frame to process
	 *	@param t The timecode.
	 */
	public abstract void processFrame( MBFImage frame, Timecode t );
	
	/**
	 * 	Forces a redraw of the specific visualisation onto the bar
	 * 	canvas.
	 *	@param vis The visualisation to update.
	 */
	public abstract void updateVis( MBFImage vis );

	/** The background colour of the bar */
	private Float[] barColour = new Float[]{0.3f,0.8f,1f};
	
	/** The background colour of the audio bar */
	private Float[] audioBarColour = new Float[]{0.7f,0.9f,1f};
	
	/** The video being displayed in the bar */
	private Video<MBFImage> video;
	
	/** The audio stream to show */
	private AudioStream audio;
	
	/** Whether to also show the audio waveform. */
	private boolean showAudio = false;
	
	/** If showAudio is true, this will be instantiated to draw the audio */
	private AudioWaveformPlotter waveformPlotter = null;
	
	/** The height to plot the audio */
	private int audioHeight = 50;
	
	/** The visualisation image */
	private MBFImage visualisation = null;

	/** Number of frames in the video in total */
	private long nFrames;
	
	/** The start position of the video (as a timeline object) */
	private long start = 0;
	
	/** The marker that's used for processing progress */
	private VideoTimelineMarker processingMarker = new VideoTimelineMarker();
	
	/**
	 *	Default constructor 
	 * 	@param video 
	 */
	protected VideoBarVisualisation( Video<MBFImage> video )
	{
		this( video, null );
	}
	
	/**
	 * 
	 *	@param video
	 *	@param audio
	 */
	protected VideoBarVisualisation( Video<MBFImage> video, AudioStream audio )
	{
		this.video = video;
		this.audio = audio;
		this.showAudio = audio != null;
		
		this.nFrames = this.video.countFrames();
		setPreferredSize( new Dimension(1,120+(this.showAudio?this.audioHeight:0)) );
		
		if( this.showAudio ) 
			this.waveformPlotter = new AudioWaveformPlotter();
	}

	/**
	 * 	Begin processing the video in a separate thread. The video will be
	 * 	reset after processing is complete.
	 */
	public void processVideo()
	{
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				VideoBarVisualisation.this.processVideoThread();
				VideoBarVisualisation.this.video.reset();
			}
		}).start();
		
		if( showAudio )
		{
			new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					waveformPlotter.plotAudioWaveformImage( 
							audio, getWidth(), audioHeight, new Float[]{0f,0f,0f,0f},
							new Float[]{0f,0f,0.6f,1f} );
					DisplayUtilities.display( waveformPlotter.lastGeneratedView );
					audio.reset();
				}			
			}).start();
		}
	}
	
	/**
	 * 	The processing method used in the processing thread.
	 */
	private void processVideoThread()
	{
		processingMarker = new VideoTimelineMarker();
		processingMarker.type = TimelineMarkerType.LABEL;
		
		// Iterate through the video to get each frame.
		int nFrame = 0;
		for( MBFImage frame : this.video )
		{
			processingMarker.frameNumber = nFrame;
			
			// Process the frame
			processFrame( frame, new FrameNumberVideoTimecode( nFrame, this.video.getFPS() ) );
			nFrame++;
			
			repaint();
		}
		
		processingMarker = null;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint( Graphics g )
	{
		// Resize the vis image if necessary
		int w = getWidth();
		int h = getHeight();
		if( visualisation == null ||
			(w > 0 && h > 0 && visualisation.getWidth() != w && 
				visualisation.getHeight() != h) )
			visualisation = new MBFImage( w, h, 3 );
		
		// Wipe out the vis.
		visualisation.fill( barColour );
		
		// Draw the audio?
		if( showAudio && waveformPlotter != null && waveformPlotter.lastGeneratedView != null )
		{
			visualisation.drawShapeFilled( 
				new Rectangle(0,h-audioHeight,w,audioHeight), audioBarColour );
			visualisation.drawImage( waveformPlotter.lastGeneratedView, 0,0 );
		}

		// Draw the vis specifics
		updateVis( visualisation );

		// Copy the vis to the Swing UI
		g.drawImage( ImageUtilities.createBufferedImage( visualisation ), 
			0, 0, null );

		// Draw the processing marker
		if( processingMarker != null )
		{
			double d = getTimePosition( processingMarker.frameNumber );
			processingMarker.label = String.format( "%.2f%%", 
				processingMarker.frameNumber / (float)nFrames * 100f );
			processingMarker.type.drawMarker( processingMarker, g, (int)d, h );
		}
	}
	
	/**
	 *	@return the barColour
	 */
	public Float[] getBarColour()
	{
		return barColour;
	}

	/**
	 *	@param barColour the barColour to set
	 */
	public void setBarColour( Float[] barColour )
	{
		this.barColour = barColour;
	}
	
	/**
	 * 	Return the video being shown by this bar.
	 *	@return The video.
	 */
	public Video<MBFImage> getVideo()
	{
		return video;
	}
	
	/**
	 * 	Returns the position of the given timecode at the scale of the
	 * 	current display. The position is given in pixels from the start of
	 * 	the bar.
	 * 
	 *	@param t the timecode for which to give the position.
	 *	@return The position in pixels of the timecode. 
	 */
	protected double getTimePosition( Timecode t )
	{
		double msLength = nFrames / this.video.getFPS() * 1000;
		return t.getTimecodeInMilliseconds() / msLength * getWidth();
	}

	/**
	 * 	Returns the position of the given frame at the scale of the current
	 * 	display. The position is given in pixel from the start of the bar.
	 *	@param nFrame The frame index
	 *	@return The position in pixels of the frame.
	 */
	protected double getTimePosition( int nFrame )
	{
		return nFrame / (double)nFrames * getWidth();
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.timeline.TimelineObject#getStartTimeMilliseconds()
	 */
	@Override
	public long getStartTimeMilliseconds()
	{
		return start;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.timeline.TimelineObject#getEndTimeMilliseconds()
	 */
	@Override
	public long getEndTimeMilliseconds()
	{
		return start + (long)(nFrames/this.video.getFPS()*1000);
	}
}
