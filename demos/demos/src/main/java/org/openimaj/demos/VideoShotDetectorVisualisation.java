package org.openimaj.demos;

import java.awt.Toolkit;
import java.io.File;

import org.openimaj.feature.DoubleFV;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.processing.shotdetector.VideoKeyframe;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class VideoShotDetectorVisualisation
{
	/**
	 * 	Testing code.
	 *  @param args
	 */
	public static void main( String[] args )
	{
		int threshold = 8000;
		VideoShotDetector<MBFImage> vsd = new VideoShotDetector<MBFImage>( new XuggleVideo(new File( "/Users/ss/dwhelper/OldNeil.mpg") ), true );
		vsd.setStoreAllDifferentials( true );
		vsd.setFindKeyframes( true );
		vsd.setThreshold( threshold );
		vsd.process();

		System.out.println( "Found these boundaries: "+vsd.getShotBoundaries() );

		// Calculate the various variables required to draw the visualisation.
		DoubleFV dfv = vsd.getDifferentials();
		double max = Double.MIN_VALUE;
		for( int x = 0; x < dfv.length(); x++ )
			max = Math.max( max, dfv.get(x) );
		int th = 64;
		int tw = 64;
		int h = 200;
		int w = Toolkit.getDefaultToolkit().getScreenSize().width - tw;
		MBFImage m = new MBFImage( w+tw, h, 3 );

		// Draw all the keyframes found onto the image
		ResizeProcessor rp = new ResizeProcessor( tw, th, true );
		for( VideoKeyframe<MBFImage> kf : vsd.getKeyframes() )
		{
			int fn = kf.getTimecode().getFrameNumber();
			int x = fn * w / dfv.length();
			
			// We draw the keyframes along the top of the visualisation.
			// So we draw a line to the frame to match it up to the differential
			m.drawLine( x, h, x, 0, new Float[]{0.3f,0.3f,0.3f} );			
			m.drawImage( kf.getImage().process( rp ), x+1, 0);
		}

		// This is the threshold line drawn onto the image.
		m.drawLine( 0, (int)(h - h/max*threshold), w, (int)(h - h/max*threshold), RGBColour.RED );
		
		// Now draw all the differentials
		int x = 0;
		for( int z = 0; z < dfv.length(); z++ )
		{
			x = z * w/dfv.length();
			m.drawLine( x, h, x, (int)(h - h/max*dfv.get(z)), RGBColour.WHITE );
		}

		// Display the visualisation
		DisplayUtilities.display( m );

		// System.out.println( "Keyframes: "+keyframes );
		// DisplayUtilities.display( "Keyframes: ", keyframes.toArray( new Image<?,?>[0] ) );
	}
}
