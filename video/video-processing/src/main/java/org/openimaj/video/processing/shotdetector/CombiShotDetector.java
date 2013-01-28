/**
 * 
 */
package org.openimaj.video.processing.shotdetector;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;

/**
 *	A shot detector that uses evidence from a number of shot detectors
 *	to determine if a shot has been detected.
 *	<p>
 *	It runs the shot detection for each detector that is registered with the
 *	class and if it determines that a shot boundary occurred the score is increased
 *	by the weighting value for that detector.  The final score is divided by
 *	the number of detectors to get a probability value for the frame being
 *	a shot boundary. By default the threshold is set to 0.75 - that is, there must
 *	be a 3/4 correlation of shot boundary for it to be considered a shot boundary.  
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 28 Jan 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class CombiShotDetector extends VideoShotDetector<MBFImage>
{
	/** The detectors to use for evidence gathering */
	private Map<VideoShotDetector<MBFImage>,Double> detectors = null;
	
	/**
	 *	Default constructor that takes the video to be processed 
	 *	@param video The video
	 */
	public CombiShotDetector( final Video<MBFImage> video )
	{
		super( video );
		this.detectors = new HashMap<VideoShotDetector<MBFImage>, Double>();
		this.threshold = 0.75;
	}
	
	/**
	 * 	Add a shot detector that will be used in the evidence gathering.
	 *	@param detector The detector
	 * 	@param weight The weight to use for this detector 
	 */
	public void addVideoShotDetector( final VideoShotDetector<MBFImage> detector, 
			final double weight )
	{
		if( weight > 1 || weight < 0 )
			throw new IllegalArgumentException( 
					"Detector weight must be between "+
					"0 and 1 inclusive" );
		this.detectors.put( detector, weight );
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.processing.shotdetector.VideoShotDetector#getInterframeDistance(org.openimaj.image.Image)
	 */
	@Override
	protected double getInterframeDistance( final MBFImage thisFrame )
	{
		double score = 0;
		for( final VideoShotDetector<MBFImage> detector : this.detectors.keySet() )
		{
			detector.processFrame( thisFrame );
			if( detector.wasLastFrameBoundary() )
				score += this.detectors.get(detector);
		}
		
		return score/this.detectors.size();
	}	
}
