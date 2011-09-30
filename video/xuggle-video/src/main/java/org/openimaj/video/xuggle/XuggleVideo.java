/**
 * 
 */
package org.openimaj.video.xuggle;

import java.awt.image.BufferedImage;
import java.io.File;

import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IStream;

/**
 * 	Wraps a Xuggle video reader into the OpenIMAJ {@link Video} interface.
 * 
 * 	This class requires that you have Xuggle already installed on your system.
 * 
 * 	If you have trouble running this within Eclipse you may need to set your
 * 	Eclipse environment to include LD_LIBRARY_PATH (or DYLIB_LIBRARY_PATH on
 * 	Mac) to point to your $XUGGLE_HOME/lib directory.
 * 
 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 1 Jun 2011
 */
public class XuggleVideo extends Video<MBFImage>
{
	/** The reader used to read the video */
	private IMediaReader reader = null;
	
	/** The listener used to create buffered images each time a frame is read */
	private FrameGetter frameGetter = null;
	
	/** Used to tell, when reading packets, if we got enough for a new frame */
	private boolean currentFrameUpdated = false;
	
	/** The current frame - only ever one object that's reused */
	private MBFImage currentMBFImage;
	
	/** The stream index that we'll be reading from */
	private int streamIndex = -1;
	
	/** Width of the video frame */
	private int width = -1;
	
	/** Height of the video frame */
	private int height = -1;

	/** A cache of the calculation of he total number of frames in the video */ 
	private long totalFrames = -1;
	
	/** A cache of the url of the video */
	private String url = null;
	
	/** A cache of whether the video should be looped or not */
	private boolean loop = false;
	
	/**
	 * 	This implements the Xuggle MediaTool listener that will be called
	 * 	every time a video picture has been decoded from the stream. This class
	 * 	creates a BufferedImage for each video frame and updates the
	 * 	currentFrameUpdated boolean when one arrives.
	 * 
	 *  @author David Dupplaw <dpd@ecs.soton.ac.uk>
	 *	@version $Author$, $Revision$, $Date$
	 *	@created 1 Jun 2011
	 */
	protected class FrameGetter extends MediaToolAdapter
	{
		/** The current frame */
		private BufferedImage currentFrame = null;
		
		/**
		 *  @inheritDoc
		 *  @see com.xuggle.mediatool.MediaToolAdapter#onVideoPicture(com.xuggle.mediatool.event.IVideoPictureEvent)
		 */
		@Override
		public void onVideoPicture( IVideoPictureEvent event )
		{
			if( event.getStreamIndex() == streamIndex )
				setCurrentFrame( event.getImage() );
			
			super.onVideoPicture(event);
		}

		/**
		 * 	Set the current frame.
		 *  @param currentFrame The current frame
		 */
		protected void setCurrentFrame( BufferedImage currentFrame )
        {
	        this.currentFrame = currentFrame;
	        currentFrameUpdated = true;
        }
		
		/**
		 * 	Get the current frame
		 *  @return The current frame
		 */
		public BufferedImage getCurrentFrame()
        {
	        return currentFrame;
        }
	}
	
	/**
	 * 	Default constructor that takes the video file to read.
	 * 
	 *  @param videoFile The video file to read.
	 */
	public XuggleVideo( File videoFile )
    {
		this( videoFile.getPath() );
    }
	
	/**
	 * 	Default constructor that takes the location of a video file
	 * 	to read. This can either be a filename or a URL.
	 * 
	 *  @param url The URL of the file to read
	 */
	public XuggleVideo( String url )
	{
		this( url, false );
	}
	
	/**
	 * 	Default constructor that takes the location of a video file
	 * 	to read. This can either be a filename or a URL. The second
	 * 	parameter determines whether the video will loop indefinitely.
	 * 	If so, {@link #getNextFrame()} will never return null; otherwise
	 * 	this method will return null at the end of the video.
	 * 
	 *  @param url The URL of the file to read
	 *  @param loop Whether to loop the video indefinitely
	 */
	public XuggleVideo( String url, boolean loop )
	{
		this.url = url;
		this.loop = loop;
		this.reset();
    }
	
	@Override
	public long countFrames(){
		return this.totalFrames;
	}
	
	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.Video#getNextFrame()
	 */
	@Override
    public MBFImage getNextFrame()
    {		
		// Read packets until we have a new frame.
		IError e = null;
		while( (e = reader.readPacket()) == null && !currentFrameUpdated );
		
		// Check if we're at the end of the file
		if( !currentFrameUpdated ||	e != null )
		{
			// System.err.println( "Got error: "+e );
			return null;
		}
		
		// We've read a frame so we're done looping
		currentFrameUpdated = false;
		
		// Push the BufferedImage data into the MBFImage buffer image.
	    BufferedImage bimg = frameGetter.getCurrentFrame();
		int [] data = bimg.getRGB(0, 0, bimg.getWidth(), 
				bimg.getHeight(), null, 0, bimg.getWidth());
		
		currentMBFImage.internalAssign( data, currentMBFImage.getWidth(), 
				currentMBFImage.getHeight() );
		
		// Increment frame counter
		this.currentFrame++;
		
		return currentMBFImage;
    }

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.Video#getCurrentFrame()
	 */
	@Override
    public MBFImage getCurrentFrame()
    {
	    return currentMBFImage;
    }

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.Video#getWidth()
	 */
	@Override
    public int getWidth()
    {
	    return width;
    }

	/**
	 *  @inheritDoc
	 *  @see org.openimaj.video.Video#getHeight()
	 */
	@Override
    public int getHeight()
    {
	    return height;
    }

	@Override
	public boolean hasNextFrame() 
	{
		return reader.isOpen();
	}

	@Override
	public void reset() 
	{
		// Set up a new reader that creates BufferdImages.
		this.reader = ToolFactory.makeReader( this.url );
		this.reader.setBufferedImageTypeToGenerate( BufferedImage.TYPE_3BYTE_BGR );
		this.reader.addListener( frameGetter = new FrameGetter() );
		this.reader.setCloseOnEofOnly( !this.loop );
		
		// We need to open the reader so that we can read the container information
		this.reader.open();
		
		// Find the video stream.
		IStream s = null;
		int i = 0;
		while( i < reader.getContainer().getNumStreams() )
		{
			s = reader.getContainer().getStream( i );
			if( s != null && s.getStreamCoder().getCodecType() == 
				ICodec.Type.CODEC_TYPE_VIDEO )
			{
				// Save the stream index so that we only get frames from
				// this stream in the FrameGetter
				streamIndex = i;
				break;
			}
			i++;
		}
		
		if( reader.getContainer().getDuration() == Global.NO_PTS )
				this.totalFrames = -1;
		else	this.totalFrames = (long) ((reader.getContainer().getDuration() 
				* s.getTimeBase().getDouble()));
			
		// If we found the video stream, set the FPS
		if( s != null )
			super.fps = s.getFrameRate().getDouble();
		
		System.out.println( "FPS is "+super.fps );
		System.out.println("Container duration: " + 
				reader.getContainer().getDuration());
		System.out.println("Stream duration: " + s.getDuration());
		System.out.println("Time base: " + s.getTimeBase().getDouble());
		
		// If we found a video stream, setup the MBFImage buffer.
		if( s != null )
		{
			int w = s.getStreamCoder().getWidth();
			int h = s.getStreamCoder().getHeight();
			currentMBFImage = new MBFImage( w, h, 3 );
			this.width = w;
			this.height = h;
			System.out.println( "Created new buffer frame "+w+"x"+h );
		}
	}
}
