/**
 * 
 */
package org.openimaj.video.translator;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.video.Video;

/**
 *	Converts an MBFImage video into an FImage video using intensity.	
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 1 Mar 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MBFImageToFImageVideoTranslator 
	extends VideoTranslator<MBFImage,FImage>
{
	/**
	 * 	Construct using the input video.
	 *	@param in The input video
	 */
	public MBFImageToFImageVideoTranslator( Video<MBFImage> in )
	{
		super( in );
	}

	/**
	 * 	Translates the MBFImage video frame to an FImage video frame
	 * 	using {@link Transforms#calculateIntensity(MBFImage)}.
	 * 
	 * 	@see org.openimaj.video.translator.VideoTranslator#translateFrame(org.openimaj.image.Image)
	 */
	@Override
	public FImage translateFrame( MBFImage nextFrame )
	{
		return Transforms.calculateIntensity( nextFrame );
	}
}
