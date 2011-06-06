/**
 * 
 */
package org.openimaj.video.xuggle;

import java.awt.HeadlessException;
import java.io.File;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.parts.FacePipeline;
import org.openimaj.image.processing.face.parts.FacialDescriptor;
import org.openimaj.image.processing.face.parts.FacialDescriptor.FacialPartDescriptor;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

/**
 * 	A simple test harness for reading videos through the Xuggle Video
 * 	implementation, performing face detection and displaying them on 
 * 	the OpenIMAJ VideoDisplay.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 31 May 2011
 */
public class XuggleVideoTester implements VideoDisplayListener<MBFImage>
{
	private FacePipeline engine = new FacePipeline();
	
	public XuggleVideoTester()
    {
		try
        {
//	        XuggleVideo v = new XuggleVideo( new File( "src/test/resources/06041609-rttr-16k-news18-rttr-16k.mpg") );
//	        XuggleVideo v = new XuggleVideo( new File( "src/test/resources/20110307_rttr.wmv") );
	        XuggleVideo v = new XuggleVideo( new File( "src/test/resources/fma.flv") );
	        VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay( v );
			vd.addVideoListener(this);
        }
        catch( HeadlessException e )
        {
	        e.printStackTrace();
        }
    }
	
	public static void main( String[] args )
	{
		new XuggleVideoTester();
	}
	
	public void beforeUpdate(MBFImage frame) 
	{
		LocalFeatureList<FacialDescriptor> faces = engine.extractFaces(
				Transforms.calculateIntensityNTSC( frame ) );
		
		for(FacialDescriptor face : faces)
		{
			frame.drawPolygon( face.bounds.asPolygon(), RGBColour.RED );
			for(FacialPartDescriptor part: face.faceParts)
				frame.drawPoint( part.position, RGBColour.GREEN, 3 );
		}
	}

	public void afterUpdate( VideoDisplay<MBFImage> display )
    {
    }
}
