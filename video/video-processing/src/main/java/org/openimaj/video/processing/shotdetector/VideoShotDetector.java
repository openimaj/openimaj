/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.algorithm.HistogramProcessor;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.processor.VideoProcessor;
import org.openimaj.video.timecode.FrameNumberVideoTimecode;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.video.timecode.VideoTimecode;

/**
 * 	Video shot detector class implemented as a video display listener. This
 * 	means that shots can be detected as the video plays. The class also
 * 	supports direct processing of a video file (with no display).  The default
 * 	shot boundary threshold is 5000.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class VideoShotDetector<T extends Image<?,T>> 
	extends VideoProcessor<Video<T>,T>
	implements VideoDisplayListener<T>
{
	/** The list of shot boundaries */
	private List<ShotBoundary> shotBoundaries = new ArrayList<ShotBoundary>();
	
	/** The last processed histogram - stored to allow future comparison */
	private Histogram lastHistogram = null;
	
	/** The frame we're at within the video */
	private int frameCounter = 0;
	
	/** The shot boundary distance threshold */
	private double threshold = 5000;
	
	/** The video being processed */
	private Video<T> video = null;
	
	/**
	 * 	Constructor that takes the video file to process.
	 * 
	 *  @param videoFile The video to process.
	 */
	public VideoShotDetector( Video<T> video )
	{
		this( video, false );
	}

	/**
	 * 	Default constructor that takes the video file to process and
	 * 	whether or not to display the video as it's being processed.
	 * 
	 *  @param v The video to process
	 *  @param display Whether to display the video during processing.
	 */
	public VideoShotDetector( Video<T> video, boolean display )
    {
		this.video = video;
		if( display )
		{
			try
	        {
		        VideoDisplay<T> vd = VideoDisplay.createVideoDisplay( video );
				vd.addVideoListener( this );
				vd.setStopOnVideoEnd( true );
	        }
	        catch( HeadlessException e )
	        {
		        e.printStackTrace();
	        }
		}
    }
	
	/**
	 * 	Process the video.
	 */
	public void process()
	{
		super.process( video );
		mergeBoundaries();
	}

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
	 */
	public void afterUpdate( VideoDisplay<T> display )
    {
    }

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	public void beforeUpdate( T frame )
    {
		checkForShotBoundary( frame );
    }
	
	/**
	 * 	Looks through the shot boundaries and removes consecutive ones,
	 * 	keeping only the last.
	 */
	private void mergeBoundaries()
	{
		List<ShotBoundary> toRemove = new ArrayList<ShotBoundary>();
		ShotBoundary last = null;
		for( Iterator<ShotBoundary> i = shotBoundaries.iterator(); i.hasNext(); )
		{
			ShotBoundary sb = i.next();
			if( sb.timecode instanceof FrameNumberVideoTimecode )
			{
				FrameNumberVideoTimecode tc = (FrameNumberVideoTimecode)sb.timecode;
				
				if( last != null && tc.getFrameNumber() == 
					((FrameNumberVideoTimecode)last.timecode).getFrameNumber()+1 )
				{
					toRemove.add( last );
					last = sb;
				}
			}
		}
		
		for( ShotBoundary ssb : toRemove )
			shotBoundaries.remove( ssb );
	}
	
	/**
	 * 	Checks whether a shot boundary occurred between the given frame
	 * 	and the previous frame, and if so, it will add a shot boundary
	 * 	to the shot boundary list.
	 * 
	 *  @param frame The new frame to process.
	 */
	private void checkForShotBoundary( T frame )
	{
		// Get the histogram for the frame.
		HistogramProcessor hp = new HistogramProcessor( 64 );
		if( ((Object)frame) instanceof MBFImage )
			hp.processImage( ((MBFImage)(Object)frame).getBand(0), 
					(Image<?,?>[])(Object)null );
		Histogram newHisto = hp.getHistogram();
		
		double dist = 0;
		
		// If we have a last histogram, compare against it.
		if( this.lastHistogram != null )
			dist = newHisto.compare( lastHistogram, DoubleFVComparison.EUCLIDEAN );
		
		// We generate a shot boundary if the threshold is exceeded or we're
		// at the very start of the video.
		if( dist > threshold || this.lastHistogram == null )
		{
			VideoTimecode tc = new HrsMinSecFrameTimecode( frameCounter, video.getFPS() );
			shotBoundaries.add( new ImageShotBoundary<T>( tc, frame.clone() ) );


			System.out.println( "Shot boundary at "+tc );
// 				System.out.println( tc+" -> "+dist );
//				System.out.println( " ------------------------------------------------ ");
//				System.out.println( " ------- S H O T   B O U N D A R Y  ------------- ");
//				System.out.println( " ------------------------------------------------ ");
		}
		
		this.lastHistogram = newHisto;
		frameCounter++;
    }
	
	/**
	 * 	Get the list of shot boundaries that have been extracted so far.
	 * 
	 *  @return The list of shot boundaries.
	 */
	public List<ShotBoundary> getShotBoundaries()
	{
		return shotBoundaries;
	}

	/**
	 * 	Set the threshold that will determine a shot boundary.
	 * 
	 *  @param threshold The new threshold.
	 */
	public void setThreshold( double threshold )
	{
		this.threshold = threshold;
	}

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.processor.VideoProcessor#processFrame(org.openimaj.image.Image)
	 */
	@Override
    public void processFrame( T frame )
    {
		checkForShotBoundary( frame );
    }
}
