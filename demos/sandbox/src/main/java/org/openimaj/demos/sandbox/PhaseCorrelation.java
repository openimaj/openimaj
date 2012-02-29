/**
 * 
 */
package org.openimaj.demos.sandbox;

import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;
import flanagan.complex.Complex;
import flanagan.complex.ComplexMatrix;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 29 Feb 2012
 */
public class PhaseCorrelation
{
	/**
	 *  @param args
	 * 	@throws IOException 
	 */
	public static void main( String[] args ) throws IOException
    {
		VideoCapture vc = new VideoCapture( 320, 240 );
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( vc );
		vd.addVideoListener( new VideoDisplayListener<MBFImage>()
		{
			private MBFImage lastFrame = null;
			
			@Override
            public void afterUpdate( VideoDisplay<MBFImage> display )
            {
            }

			@Override
            public void beforeUpdate( MBFImage frame )
            {
				if( lastFrame != null )
				{					
					lastFrame = frame.clone();
					
					int gw = 20;
					int gh = 20;
					int nx = frame.getWidth()/gw;
					int ny = frame.getHeight()/gh;
					
					for( int cy = 0; cy < ny; cy++ )
					{
						for( int cx = 0; cx < nx; cx++ )
						{
							MBFImage i1 = frame.extractROI( cx*gw, cy*gh, gw, gh );
							MBFImage i2 = lastFrame.extractROI( cx*gw, cy*gh, gw, gh );
							
							Point2d mv = calculateMotionVector( 
								Transforms.calculateIntensity( i1 ),
								Transforms.calculateIntensity( i2 ) );
							
							System.out.println( "("+cx+","+cy+") = "+mv );
							
							int boxCentreX = cx*gw + gw/2;
							int boxCentreY = cy*gh + gh/2;
							frame.drawLine( boxCentreX, boxCentreY,
									(int)(boxCentreX + mv.getX()), 
									(int)(boxCentreY + mv.getY()),
									2, new Float[]{1f,0f,0f} );
						}
					}
				}
				else
					lastFrame = frame.clone();
            }			
		});
    }
	
	/**
	 * 	Calculate the estimated motion vector between <code>img1</code> 
	 * 	which is first in the sequence and <code>img2</code> which is 
	 * 	second in the sequence. This method uses phase correlation - the
	 * 	fact that translations in space can be seen as shifts in phase
	 * 	in the frequency domain. The returned vector will have a maximum
	 * 	horizontal displacement of <code>img1.getWidth()/2</code> and 
	 * 	a minimum displacement of <code>-img1.getWidth()/2</code>
	 * 	and similarly for the vertical displacement and height.
	 * 
	 *  @param img1 The first image in the sequence
	 *  @param img2 The second image in the sequence
	 *  @return the estimated motion vector as a {@link Point2d} in absolute
	 *  	x and y coordinates.
	 */
	public static Point2d calculateMotionVector( FImage img1, FImage img2 )
    {	
		// The images must have comparable shapes and must be square
		if( img1.getRows() != img2.getRows() || 
			img1.getCols() != img2.getCols() ||
			img1.getCols() != img2.getRows() )
			return new Point2dImpl(0,0);
		
	    // Prepare and perform an FFT for each of the incoming images.
	    int h = img1.getRows();
	    int w = img1.getCols();
	    
	    System.out.println( "Image width and height: "+w+"x"+h );
	    
	    try
	    {
		    FloatFFT_2D fft1 = new FloatFFT_2D( h, w );
		    FloatFFT_2D fft2 = new FloatFFT_2D( h, w );
		    float[][] data1 = FourierTransform.prepareData( img1, h, w, false );
		    float[][] data2 = FourierTransform.prepareData( img2, h, w, false );
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
		    cmat.times( 1/det.abs() );
		    
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
		    
		    // DisplayUtilities.display( img1.normalise() );
	
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
}
