/**
 * 
 */
package org.openimaj.video.translator;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;

/**
 *	Converts an FImage video into an MBFImage video.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 1 Mar 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class FImageToMBFImageVideoTranslator extends VideoTranslator<FImage,MBFImage>
{
	/**
	 * 	Create a translator using the input video.
	 *	@param in The input video to translate
	 */
	public FImageToMBFImageVideoTranslator( Video<FImage> in )
	{
		super( in );
	}

	@Override
	public MBFImage translateFrame( FImage nextFrame )
	{
		return new MBFImage(nextFrame,nextFrame,nextFrame);
	}
}
