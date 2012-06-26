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
package org.openimaj.video.analysis.motion;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.Video;
import org.openimaj.video.VideoFrame;
import org.openimaj.video.analyser.VideoAnalyser;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;
import flanagan.complex.Complex;
import flanagan.complex.ComplexMatrix;

/**
 *	A motion estimator will estimate the motion of parts of a video frame.
 *	This class includes a set of algorithms for calculating the
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 1 Mar 2012
 *	
 */
public abstract class MotionEstimator extends VideoAnalyser<FImage>
{
	/**
	 *	A set of algorithms for the motion estimator.	
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 1 Mar 2012
	 *	
	 */
	public enum MotionEstimatorAlgorithm
	{
		/**
		 * 	Basic phase correlation algorithm that finds peaks in the 
		 * 	cross-power spectrum between two images. This is the basic
		 * 	implementation without sub-pixel accuracy.
		 */
		PHASE_CORRELATION
		{
			/**
			 * 	Calculate the estimated motion vector between <code>images</code> 
			 * 	which [0] is first in the sequence and <code>img2</code> which is 
			 * 	second in the sequence. This method uses phase correlation - the
			 * 	fact that translations in space can be seen as shifts in phase
			 * 	in the frequency domain. The returned vector will have a maximum
			 * 	horizontal displacement of <code>img2.getWidth()/2</code> and 
			 * 	a minimum displacement of <code>-img2.getWidth()/2</code>
			 * 	and similarly for the vertical displacement and height.
			 * 
			 *  @param img2 The second image in the sequence
			 *  @param images The previous image in the sequence
			 *  @return the estimated motion vector as a {@link Point2d} in absolute
			 *  	x and y coordinates.
			 */
			@Override
			protected Point2d estimateMotion( VideoFrame<FImage> img2, 
					VideoFrame<FImage>... images )
			{
				// The previous image will be the first in the images array
				FImage img1 = images[0].frame;
				
				// No previous frame?
				if( img1 == null )
					return new Point2dImpl(0,0);
				
				// The images must have comparable shapes and must be square
				if( img1.getRows() != img2.frame.getRows() || 
					img1.getCols() != img2.frame.getCols() ||
					img1.getCols() != img2.frame.getRows() )
					return new Point2dImpl(0,0);

			    // Prepare and perform an FFT for each of the incoming images.
			    int h = img1.getRows();
			    int w = img1.getCols();
			    
			    try
			    {
				    FloatFFT_2D fft1 = new FloatFFT_2D( h, w );
				    FloatFFT_2D fft2 = new FloatFFT_2D( h, w );
				    float[][] data1 = FourierTransform.prepareData( img1, h, w, false );
				    float[][] data2 = FourierTransform.prepareData( img2.frame, h, w, false );
				    fft1.complexForward( data1 );
				    fft2.complexForward( data2 );
				    
				    // Multiply (element-wise) the fft and the conjugate of the fft.
				    Complex[][] cfft = new Complex[h][w];
				    for( int y = 0; y < h; y++ )
				    {
				    	for( int x = 0; x < w; x++ )
				    	{				
				    		float re1 = data1[y][x*2];
				    		float im1 = data1[y][1 + x*2];
				    		float re2 = data2[y][x*2];
				    		float im2 = data2[y][1 + x*2];
			
				    		Complex c1 = new Complex( re1, im1 );
				    		Complex c2 = new Complex( re2, -im2 );
				    		cfft[y][x] = c1.times( c2 ); 
				    	}
				    }
				    
				    // Normalise by the determinant
				    ComplexMatrix cmat = new ComplexMatrix(cfft);
				    Complex det = cmat.determinant();
				    cmat.times( 1d/det.abs() );
				    
				    // Convert back to an array for doing the inverse FFTing
				    cfft = cmat.getArray();
				    for( int y = 0; y < h; y++ )
				    {
				    	for( int x = 0; x < w; x++ )
				    	{
				    		data1[y][x*2] = (float)cfft[y][x].getReal();
				    		data1[y][1+x*2] = (float)cfft[y][x].getImag();
				    	}
				    }
				    
				    // Perform the inverse FFT
				    fft1.complexInverse( data1, false );
			
				    // Get the data back out
				    FourierTransform.unprepareData( data1, img1, false );
				    
				    // Get the estimated motion vector from the peak in the space
				    FValuePixel p = img1.maxPixel();
				    return new Point2dImpl( 
				    	-(p.x > w/2 ? p.x - w : p.x),
				    	-(p.y > w/2 ? p.y - w : p.y) );
			    }
			    catch( Exception e )
			    {
			    	return new Point2dImpl(0,0);
			    }
			}
		};
		
		/**
		 * 	Estimate the motion to the given image, <code>img1</code> from
		 * 	the previous frames. The previous frames will be given in reverse
		 * 	order so that images[0] will be the previous frame, images[1] the
		 * 	frame before that, etc. The number of frames given will be at most
		 * 	that given by {@link #requiredNumberOfFrames()}. It could be less
		 * 	if at the beginning of the video. If you require more frames, return
		 * 	an empty motion vector - that is (0,0). 
		 *  
		 *	@param img1 The image to which we want to estimate the motion.
		 *	@param images The previous frames in reverse order
		 *	@return The estimated motion vector.
		 */
		protected abstract Point2d estimateMotion( VideoFrame<FImage> img1, 
				VideoFrame<FImage>... images );
		
		/**
		 * 	The required number of frames required for the given motion estimation
		 * 	algorithm to work. The default is 1 which means the algorithm will
		 * 	only receive the previous frame. If more are required, override this
		 * 	method and return the required number.
		 *  
		 *	@return The required number of frames to pass to the algorithm.
		 */
		protected int requiredNumberOfFrames()
		{
			return 1;
		}
	}
	
	/** The estimator to use */
	private MotionEstimatorAlgorithm estimator = null;
	
	/** The old frame stack. It's a queue so the oldest frame is popped off */
	private Queue<VideoFrame<FImage>> oldFrames = null;
	
	/** The estimated motion vectors for the last analysed frame */
	public Map<Point2d,Point2d> motionVectors = null;
	
	/**
	 * 	Constructor a new motion estimator using the given algorithm.
	 *	@param alg The algorithm to use to estimate motion.
	 */
	public MotionEstimator( MotionEstimatorAlgorithm alg )
	{
		this.estimator = alg;
		oldFrames = new LinkedList<VideoFrame<FImage>>();
	}
	
	/**
	 * 	Create a chainable motion estimator.
	 *	@param v The video to chain to 
	 *	@param alg The algorithm to use to estimate motion
	 */
	public MotionEstimator( Video<FImage> v, MotionEstimatorAlgorithm alg )
	{
		super(v);
		this.estimator = alg;
		oldFrames = new LinkedList<VideoFrame<FImage>>();
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.analyser.VideoAnalyser#analyseFrame(org.openimaj.image.Image)
	 */
	@SuppressWarnings( "unchecked" )
	@Override
	public void analyseFrame( FImage frame )
	{
		VideoFrame<FImage> vf = new VideoFrame<FImage>( frame,
				new HrsMinSecFrameTimecode( getTimeStamp(), getFPS() ) );
		
		motionVectors = estimateMotionField( estimator, vf, 
				oldFrames.toArray( new VideoFrame[0] ) );
		
		oldFrames.offer( vf );
		
		// Make sure there's never too many frames in the queue
		if( oldFrames.size() > estimator.requiredNumberOfFrames() )
			oldFrames.poll();
	}
	
	/**
	 * 	Return the estimated motion vectors for the last processed frame.
	 *	@return The estimated motion vectors
	 */
	public Map<Point2d,Point2d> getMotionVectors()
	{
		return motionVectors;
	}

	/**
	 * 	This method needs to be overridden for specific layouts of motion
	 * 	field within the image.	
	 * 
	 *	@param frame The current frame
	 *	@param array The list of previous frames (based on the estimator)
	 *	@return The motion field
	 */
	protected abstract Map<Point2d, Point2d> estimateMotionField( 
			MotionEstimatorAlgorithm estimator, VideoFrame<FImage> frame, 
			VideoFrame<FImage>[] array );
}