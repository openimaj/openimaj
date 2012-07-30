/**
 * 
 */
package org.openimaj.vis.video;

import java.awt.Dimension;
import java.awt.Graphics;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.time.Timecode;
import org.openimaj.video.Video;
import org.openimaj.video.timecode.FrameNumberVideoTimecode;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.vis.timeline.Timeline.TimelineMarker;
import org.openimaj.vis.timeline.Timeline.TimelineMarkerType;
import org.openimaj.vis.timeline.TimelineObject;

/**
 *	Displays a block, or bar, which represents the data. The block will
 *	be scaled to fit the JPanel in which its drawn. The block will contain
 *	a visImage of the data content. The visImage of the content
 *	is determined by one of the subclasses of this class.
 *	<p>
 *	This class will process the data in a separate thread. 
 *	Obviously, it's not sensible to call this class with a 
 *	"live" data stream, such as from a VideoCapture object.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 3 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public abstract class VideoBarVisualisation extends TimelineObject<Video<MBFImage>>
{
	/** */
	private static final long serialVersionUID = 1L;
	
	/**
	 *	A marker for marking data frames within the data bar
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 6 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public class VideoTimelineMarker extends TimelineMarker
	{
		/** The frame number in the data */
		public int frameNumber = 0;
	}

	/**
	 * 	Process a particular frame of the data. The frame and timecode
	 * 	of the frame are provided.
	 *	@param frame The frame to process
	 *	@param t The timecode.
	 */
	public abstract void processFrame( MBFImage frame, Timecode t );
	
	/**
	 * 	Forces a redraw of the specific visImage onto the bar
	 * 	canvas.
	 *	@param vis The visImage to update.
	 */
	public abstract void updateVis( MBFImage vis );
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.Visualisation#update()
	 */
	public void update()
	{
		updateVis( visImage );
	}

	/** The background colour of the bar */
	private Float[] barColour = new Float[]{0.3f,0.5f,0.7f};
	
	/** Whether to also show the audio waveform. */
	private boolean showAudio = false;
	
	/** The height to plot the audio */
	private int audioHeight = 50;
	
	/** Number of frames in the data in total */
	private long nFrames;
	
	/** The start position of the data (as a timeline object) */
	private long start = 0;
	
	/** The marker that's used for processing progress */
	private VideoTimelineMarker processingMarker = new VideoTimelineMarker();
	
	/**
	 * 
	 *	@param data
	 */
	protected VideoBarVisualisation( Video<MBFImage> video )
	{
		this.data = video;
		
		this.nFrames = this.data.countFrames();
		setPreferredSize( new Dimension(1,120+(this.showAudio?this.audioHeight:0)) );
	}

	/**
	 * 	Begin processing the data in a separate thread. The data will be
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
				VideoBarVisualisation.this.data.reset();
			}
		}).start();		
	}
	
	/**
	 * 	The processing method used in the processing thread.
	 */
	private void processVideoThread()
	{
		processingMarker = new VideoTimelineMarker();
		processingMarker.type = TimelineMarkerType.LABEL;
		
		// Iterate through the data to get each frame.
		int nFrame = 0;
		for( MBFImage frame : this.data )
		{
			processingMarker.frameNumber = nFrame;
			
			// Process the frame
			processFrame( frame, new FrameNumberVideoTimecode( nFrame, this.data.getFPS() ) );
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
		int w = Math.min( getWidth(), getViewSize().width );
		int h = Math.min( getHeight(), getViewSize().height );
		
		// Create a new vis image if the current image is the wrong size,
		// or the image is not yet instantiated.
		if( visImage == null ||
			(w > 0 && h > 0 && visImage.getWidth() != w && 
				visImage.getHeight() != h) )
			visImage = new MBFImage( w, h, 3 );
		
		// Wipe out the vis.
		visImage.fill( barColour );
		
		// Draw the vis specifics
		updateVis( visImage );

		// Copy the vis to the Swing UI
		g.drawImage( ImageUtilities.createBufferedImage( visImage ), 
			0, 0, null );

		// Draw the processing marker
		if( processingMarker != null )
		{
			double d = getTimePosition( processingMarker.frameNumber );
			HrsMinSecFrameTimecode tc = new HrsMinSecFrameTimecode( 
					processingMarker.frameNumber, data.getFPS() );
			processingMarker.label = String.format( "%.2f%% %s",
				processingMarker.frameNumber / (float)nFrames * 100f, tc.toString() );
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
	 * 	Return the data being shown by this bar.
	 *	@return The data.
	 */
	public Video<MBFImage> getVideo()
	{
		return data;
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
		double msLength = nFrames / this.data.getFPS() * 1000;
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
	 * 	Set the start time of this data object.
	 *	@param t The start time.
	 */
	public void setStartTimeMilliseconds( long t )
	{
		this.start = t;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.timeline.TimelineObject#getEndTimeMilliseconds()
	 */
	@Override
	public long getEndTimeMilliseconds()
	{
		return start + (long)(nFrames/this.data.getFPS()*1000);
	}
}
