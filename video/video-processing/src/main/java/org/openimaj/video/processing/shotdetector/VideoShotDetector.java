/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.algorithm.HistogramProcessor;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.processor.VideoProcessor;
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
	/** A list of the calculated keyframes */
	private List<VideoKeyframe<T>> keyframes = 
		new ArrayList<VideoKeyframe<T>>();
	
	/** The list of shot boundaries */
	private List<ShotBoundary> shotBoundaries = 
		new ArrayList<ShotBoundary>();
	
	/** Differences between consecutive frames */
	private List<Double> differentials = new ArrayList<Double>();
	
	/** The last processed histogram - stored to allow future comparison */
	private Histogram lastHistogram = null;
	
	/** The frame we're at within the video */
	private int frameCounter = 0;
	
	/** The shot boundary distance threshold */
	private double threshold = 5000;
	
	/** The video being processed */
	private Video<T> video = null;
	
	/** Whether to find keyframes */
	private boolean findKeyframes = true;
	
	/** Whether to store all frame differentials */
	private boolean storeAllDiffs = false;
	
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
	 * 	Return the last shot boundary in the list.
	 *	@return The last shot boundary in the list.
	 */
	public ShotBoundary getLastShotBoundary()
	{
		if( this.shotBoundaries.size() == 0 )
			return null;
		return this.shotBoundaries.get( this.shotBoundaries.size()-1 );
	}
	
	/**
	 * 	Returns the last video keyframe that was generated.
	 *	@return The last video keyframe that was generated.
	 */
	public VideoKeyframe<T> getLastKeyframe()
	{
		if( this.keyframes.size() == 0 )
			return null;
		return this.keyframes.get( this.keyframes.size()-1 );
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
		
		if( storeAllDiffs )
			differentials.add( dist );
		
		// We generate a shot boundary if the threshold is exceeded or we're
		// at the very start of the video.
		if( dist > threshold || this.lastHistogram == null )
		{
			// The timecode of this frame
			VideoTimecode tc = new HrsMinSecFrameTimecode( frameCounter, video.getFPS() );
			
			// The last shot boundary we created
			ShotBoundary sb = getLastShotBoundary();
			System.out.println( tc+":    -> Last shot boundary was "+sb );
			
			// If this frame is sequential to the last
			if( sb != null &&
				tc.getFrameNumber() - sb.getTimecode().getFrameNumber() < 4  )
			{
				System.out.println( tc+":    -> Consecutive boundary detected." );

				// If the shot boundary is a fade, we simply change the end 
				// timecode, otherwise we replace the given shot boundary 
				// with a new one.
				if( sb instanceof FadeShotBoundary )
				{
						((FadeShotBoundary)sb).setEndTimecode( tc );
						System.out.println( tc+":    -> Updating fade end timecode");
				}
				else
				{
					// Remove the old one.
					shotBoundaries.remove( sb );
					
					// Change it to a fade.
					FadeShotBoundary fsb = new FadeShotBoundary( sb );
					fsb.setEndTimecode( tc );
					shotBoundaries.add( fsb );

					System.out.println( tc+":    -> Creating a fade");
				}

				if( findKeyframes )
				{
					keyframes.remove( getLastKeyframe() );
					keyframes.add( new VideoKeyframe<T>( tc, frame.clone()) );
				}
			}
			else
			{
				// Create a new shot boundary
				shotBoundaries.add( new ShotBoundary( tc ) );
				
				if( findKeyframes )
					keyframes.add( new VideoKeyframe<T>( tc, frame.clone()) );
				
				System.out.println( tc+": Shot boundary" );
			}
		}
		
		this.lastHistogram = newHisto;
		frameCounter++;
    }
	
	/**
	 * 	Get the list of shot boundaries that have been extracted so far.
	 *  @return The list of shot boundaries.
	 */
	public List<ShotBoundary> getShotBoundaries()
	{
		return shotBoundaries;
	}
	
	/**
	 * 	Get the list of the keyframes found in this video.
	 *	@return The list of keyframes found.
	 */
	public List<VideoKeyframe<T>> getKeyframes()
	{
		return keyframes;
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
	 * 	Set whether to store keyframes of boundaries when they
	 * 	have been found.
	 * 
	 *	@param k TRUE to store keyframes; FALSE otherwise
	 */
	public void setFindKeyframes( boolean k )
	{
		this.findKeyframes = k;
	}
	
	/**
	 * 	Set whether to store differentials during the processing
	 * 	stage.
	 * 
	 *	@param d TRUE to store all differentials; FALSE otherwise
	 */
	public void setStoreAllDifferentials( boolean d )
	{
		this.storeAllDiffs = d;
	}
	
	/**
	 * 	Get the differentials between frames (if storeAllDiff is true).
	 *	@return The differentials between frames as a List of Double.
	 */
	public DoubleFV getDifferentials()
	{
		double d[] = new double[ this.differentials.size() ];
		int x = 0;
		for( Double dd : this.differentials )
			d[x++] = dd;
		return new DoubleFV( d );
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
