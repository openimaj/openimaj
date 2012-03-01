/**
 * 
 */
package org.openimaj.video.analysis.motion;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.Video;
import org.openimaj.video.VideoFrame;

/**
 *	Estimates the motion field over a grid.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 1 Mar 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class GridMotionEstimator extends MotionEstimator
{
	private int x, y;
	private boolean fixed;

	/**
	 * 	Construct a grid-based motion estimator. If <code>fixed</code> is
	 * 	true, the x and y values represent the width and height of the pixel
	 * 	blocks. If <code>fixed</code> is false, the x and y represent the number
	 * 	of grid elements to spread evenly across the frame.
	 * 
	 *	@param alg The estimator algorithm to use
	 *	@param x The x value
	 *	@param y The y value
	 *	@param fixed Whether x and y represent pixels or grid count.
	 */
	public GridMotionEstimator( MotionEstimatorAlgorithm alg, 
			int x, int y, boolean fixed )
	{
		super( alg );
		this.x = x; this.y = y;
		this.fixed = fixed;
	}

	/**
	 * 	Construct a chained grid-based motion estimator. If <code>fixed</code> is
	 * 	true, the x and y values represent the width and height of the pixel
	 * 	blocks. If <code>fixed</code> is false, the x and y represent the number
	 * 	of grid elements to spread evenly across the frame.
	 *
	 *	@param v The video to chain to
	 *	@param alg The estimator algorithm to use
	 *	@param x The x value
	 *	@param y The y value
	 *	@param fixed Whether x and y represent pixels or grid count.
	 */
	public GridMotionEstimator( Video<FImage> v, MotionEstimatorAlgorithm alg, 
			int x, int y, boolean fixed )
	{
		super( v, alg );
		this.x = x; this.y = y;
		this.fixed = fixed;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.analysis.motion.MotionEstimator#estimateMotionField(org.openimaj.video.analysis.motion.MotionEstimator.MotionEstimatorAlgorithm, org.openimaj.image.FImage, org.openimaj.image.FImage[])
	 */
	@Override
	protected Map<Point2d, Point2d> estimateMotionField(
			MotionEstimatorAlgorithm estimator, VideoFrame<FImage> vf, 
			VideoFrame<FImage>[] array )
	{
		if( array.length < 1 )
			return new HashMap<Point2d,Point2d>();
		
		int gw = 0, gh = 0;
		if( fixed )
		{
			gw = x;
			gh = y;
		}
		else
		{
			gw = vf.frame.getWidth()/x;
			gh = vf.frame.getHeight()/y;
		}
		
		Map<Point2d,Point2d> out = new HashMap<Point2d, Point2d>();
		
		@SuppressWarnings( "unchecked" )
		VideoFrame<FImage>[] otherFrames = new VideoFrame[array.length];
		
		for( int yy = 0; yy < vf.frame.getHeight(); yy += gh )
		{
			for( int xx = 0; xx < vf.frame.getWidth(); xx += gw )
			{
				for( int ff = 0; ff < array.length; ff++ )
					otherFrames[ff] = new VideoFrame<FImage>(
							array[ff].frame.extractROI( xx, yy, gw, gh ), 
							array[ff].timecode );
				
				// vf.frame.drawShape( new Rectangle(xx,yy,gw,gh), 1, 0f );
				
				out.put( new Point2dImpl(xx+gw/2f,yy+gh/2f), 
						estimator.estimateMotion( new VideoFrame<FImage>( 
							vf.frame.extractROI( xx, yy, gw, gh ), 
							vf.timecode ), 
							otherFrames ) );
			}
		}
		
		return out;
	}
}
