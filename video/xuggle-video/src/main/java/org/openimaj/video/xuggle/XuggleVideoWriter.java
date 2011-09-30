/**
 * 
 */
package org.openimaj.video.xuggle;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.awt.image.BufferedImage;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoWriter;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

/**
 *	An implementation of the video writer class that uses the Xuggler
 *	video API to write videos.  Note that Xuggler will resize any images
 *	to fit within the given output size during its write process. 
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 27 Jul 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class XuggleVideoWriter extends VideoWriter<MBFImage>
{
	/** The media writer we'll use to write the video */
	private IMediaWriter writer = null;
	
	/** The filename to write the video to */
	private String filename = "output.mp4";
	
	/** Keep track of the timecode of the next frame */
	private long nextFrameTime = 0;

	/** This is the time between each frame in milliseconds */
	private long interFrameTime = 400;

	/**
	 * 	Default constructor that takes the frame size and frame rate
	 * 	of the resulting video.
	 * 
	 * 	@param filename The filename to write the video to
	 *	@param width The width of the video frame in pixels
	 *	@param height The height of the video frame in pixels
	 *	@param frameRate The frame rate of the resulting video
	 */
	public XuggleVideoWriter( String filename, int width, 
			int height, double frameRate )
	{
		super( width, height, frameRate );
		
		this.filename = filename;
		this.initialise();

		long sd = (long)(1000/frameRate);
		this.interFrameTime  = DEFAULT_TIME_UNIT.convert(sd, MILLISECONDS);
	}
	
	/**
	 * 	Initialise the writer
	 */
	public void initialise()
	{
		if( writer == null )
		{
			// First, let's make a IMediaWriter to write the file.
			this.writer  = ToolFactory.makeWriter( filename );
			 
			// We tell it we're going to add one video stream, with id 0,
			// at position 0
			this.writer.addVideoStream(0, 0, width, height);
			
			// Reset the next frame's timecode
			this.nextFrameTime = 0;
		}
	}
	
	/**
	 *	@inheritDoc
	 * 	@see org.openimaj.video.processor.VideoProcessor#processingComplete()
	 */
	@Override
	public void processingComplete()
	{
		this.close();
	}
	
	/**
	 * 	Close the video stream
	 */
	public void close()
	{
		this.writer.close();
	}
	
	/** 
	 *	@inheritDoc
	 * 	@see org.openimaj.video.VideoWriter#addFrame(org.openimaj.image.Image)
	 */
	@Override
	public void addFrame( MBFImage frame )
	{
		// Create a buffered image for Xuggler
        BufferedImage f = ImageUtilities.createBufferedImage( frame );
        f = convertToType( f, BufferedImage.TYPE_3BYTE_BGR );
        
        // Encode the image to the video stream
        this.writer.encodeVideo( 0, f, nextFrameTime, DEFAULT_TIME_UNIT );

        // Set up the next timecode
		this.nextFrameTime += this.interFrameTime;
	}
	
	/**
	 * Convert a {@link BufferedImage} of any type, to {@link BufferedImage} of
	 * a specified type. If the source image is the same type as the target
	 * type, then original image is returned, otherwise new image of the correct
	 * type is created and the content of the source image is copied into the
	 * new image.
	 * 
	 * @param sourceImage the image to be converted
	 * @param targetType the desired BufferedImage type
	 * 
	 * @return a BufferedImage of the specified target type.
	 */
	public static BufferedImage convertToType( BufferedImage sourceImage, 
			int targetType )
	{
		BufferedImage image;

		// if the source image is already the target type, return the source image
		if( sourceImage.getType() == targetType )
			image = sourceImage;
		// otherwise create a new image of the target type and draw the new image
		else
		{
			image = new BufferedImage( sourceImage.getWidth(), 
					sourceImage.getHeight(), targetType );
			image.getGraphics().drawImage( sourceImage, 0, 0, null );
		}

		return image;
	}

	@Override
	public void reset() 
	{
		// Cannot reset the Xuggle video writer.
	}
}
