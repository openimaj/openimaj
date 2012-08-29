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
package org.openimaj.video.xuggle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.video.Video;
import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
import org.openimaj.video.timecode.VideoTimecode;

import com.xuggle.ferry.JNIReference;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.AConverter;
import com.xuggle.xuggler.video.BgrConverter;
import com.xuggle.xuggler.video.ConverterFactory;

/**
 * 	Wraps a Xuggle video reader into the OpenIMAJ {@link Video} interface.
 * 
 * 	This class requires that you have Xuggle already installed on your system.
 * 
 * 	If you have trouble running this within Eclipse you may need to set your
 * 	Eclipse environment to include LD_LIBRARY_PATH (or DYLIB_LIBRARY_PATH on
 * 	Mac) to point to your $XUGGLE_HOME/lib directory.
 * 
 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *  @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *	
 *	@created 1 Jun 2011
 */
public class XuggleVideo extends Video<MBFImage>
{
	/** The reader used to read the video */
	private IMediaReader reader = null;

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

	/** The timestamp of the frame currently being decoded */
	private long timestamp;

	/** The number of frames per second */
	private double fps;

	/**
	 * 	This implements the Xuggle MediaTool listener that will be called
	 * 	every time a video picture has been decoded from the stream. This class
	 * 	creates a BufferedImage for each video frame and updates the
	 * 	currentFrameUpdated boolean when one arrives.
	 * 
	 *  @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *  @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *	
	 *	@created 1 Jun 2011
	 */
	protected class FrameGetter extends MediaListenerAdapter
	{
		/**
		 *  {@inheritDoc}
		 *  @see com.xuggle.mediatool.MediaToolAdapter#onVideoPicture(com.xuggle.mediatool.event.IVideoPictureEvent)
		 */
		@Override
		public void onVideoPicture( final IVideoPictureEvent event )
		{
			event.getPicture().getTimeStamp();
			if( event.getStreamIndex() == XuggleVideo.this.streamIndex )
			{
				XuggleVideo.this.currentMBFImage = ((MBFImageWrapper)event.getImage()).img;
				XuggleVideo.this.currentFrameUpdated = true;
				XuggleVideo.this.timestamp = (long) ((event.getPicture().getTimeStamp()
						* event.getPicture().getTimeBase().getDouble()) * 1000);
			}
		}
	}

	/**
	 * 	Wrapper that created an MBFImage from a BufferedImage.
	 *
	 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *	
	 *	@created 1 Nov 2011
	 */
	protected static final class MBFImageWrapper extends BufferedImage
	{
		MBFImage img;

		public MBFImageWrapper(final MBFImage img)
		{
			super(1, 1, BufferedImage.TYPE_INT_RGB);
			this.img = img;
		}
	}

	/**
	 * 	Converter for converting IVideoPictures directly to MBFImages.
	 *
	 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *	
	 *	@created 1 Nov 2011
	 */
	protected static final class MBFImageConverter extends BgrConverter {
		private final MBFImageWrapper bimg = new MBFImageWrapper(null);
		private final byte[] buffer;

		public MBFImageConverter(final IPixelFormat.Type pictureType, final int pictureWidth, final int pictureHeight, final int imageWidth, final int imageHeight) {
			super(pictureType, pictureWidth, pictureHeight, imageWidth, imageHeight);

			this.bimg.img = new MBFImage(imageWidth, imageHeight, ColourSpace.RGB);
			this.buffer = new byte[imageWidth * imageHeight * 3];
		}

		@Override
		public BufferedImage toImage(IVideoPicture picture) {
			// test that the picture is valid
			this.validatePicture(picture);

			// resample as needed
			IVideoPicture resamplePicture = null;
			final AtomicReference<JNIReference> ref = new AtomicReference<JNIReference>(null);
			try
			{
				if (this.willResample())
				{
					resamplePicture = AConverter.resample(picture, this.mToImageResampler);
					picture = resamplePicture;
				}

				// get picture parameters
				final int w = picture.getWidth();
				final int h = picture.getHeight();

				final float[][] r = this.bimg.img.bands.get(0).pixels;
				final float[][] g = this.bimg.img.bands.get(1).pixels;
				final float[][] b = this.bimg.img.bands.get(2).pixels;

				picture.getDataCached().get(0, this.buffer, 0, this.buffer.length);
				for (int y=0, i=0; y<h; y++) {
					for (int x=0; x<w; x++, i+=3) {
						b[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[(this.buffer[i] & 0xFF)];
						g[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[(this.buffer[i+1] & 0xFF)];
						r[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[(this.buffer[i+2] & 0xFF)];
					}
				}

				return this.bimg;
			} finally {
				if (resamplePicture!=null)
					resamplePicture.delete();
				if (ref.get()!=null)
					ref.get().delete();
			}
		}
	}


	/**
	 * 	Default constructor that takes the video file to read.
	 * 
	 *  @param videoFile The video file to read.
	 */
	public XuggleVideo( final File videoFile )
	{
		this( videoFile.getPath() );
	}

	/**
	 * 	Default constructor that takes the video file to read.
	 * 
	 *  @param videoFile The video file to read.
	 *  @param loop should the video loop
	 */
	public XuggleVideo( final File videoFile , final boolean loop )
	{
		this( videoFile.getPath() , loop);
	}

	/**
	 * 	Default constructor that takes the location of a video file
	 * 	to read. This can either be a filename or a URL.
	 * 
	 *  @param url The URL of the file to read
	 */
	public XuggleVideo( final String url )
	{
		this( url, false );
	}

	/**
	 * 	Default constructor that takes the URL of a video file
	 * 	to read.
	 * 
	 *  @param url The URL of the file to read
	 */
	public XuggleVideo( final URL url )
	{
		this( url.toString(), false );
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
	public XuggleVideo( final URL url, final boolean loop )
	{
		this( url.toString(), loop );
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
	public XuggleVideo( final String url, final boolean loop )
	{
		this.url = url;
		this.loop = loop;
		this.create();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.Video#countFrames()
	 */
	@Override
	public long countFrames()
	{
		return this.totalFrames;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.Video#getNextFrame()
	 */
	@Override
	public MBFImage getNextFrame()
	{
		if( this.reader == null ) return null;

		// Read packets until we have a new frame.
		IError e = null;
		synchronized( this.reader )
		{
			while( this.reader != null && (e = this.reader.readPacket()) == null
					&& !this.currentFrameUpdated );
		}

		// Check if we're at the end of the file
		if( !this.currentFrameUpdated || e != null )
		{
			System.err.println( "Got video demux error: "+e.getDescription() );
			return null;
		}

		// We've read a frame so we're done looping
		this.currentFrameUpdated = false;

		// Increment frame counter
		this.currentFrame++;

		return this.currentMBFImage;
	}

	/**
	 *	Returns a video timecode for the current frame.
	 *  @return A video timecode for the current frame.
	 */
	public VideoTimecode getCurrentTimecode()
	{
		return new HrsMinSecFrameTimecode( (long)(this.timestamp/1000d*this.fps), this.fps );
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.Video#getCurrentFrame()
	 */
	@Override
	public MBFImage getCurrentFrame()
	{
		if( this.currentMBFImage == null )
			this.currentMBFImage = this.getNextFrame();
		return this.currentMBFImage;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.Video#getWidth()
	 */
	@Override
	public int getWidth()
	{
		return this.width;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.Video#getHeight()
	 */
	@Override
	public int getHeight()
	{
		return this.height;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.Video#hasNextFrame()
	 */
	@Override
	public boolean hasNextFrame()
	{
		return this.reader.isOpen();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.Video#reset()
	 */
	@Override
	public void reset()
	{
		if( this.reader != null && this.reader.isOpen() )
			this.seek(0);
		else	this.create();
	}

	/**
	 * 	Create the necessary reader
	 */
	private void create()
	{
		// This converter converts the frames into MBFImages for us
		ConverterFactory.registerConverter( new ConverterFactory.Type(
				ConverterFactory.XUGGLER_BGR_24, MBFImageConverter.class,
				IPixelFormat.Type.BGR24, BufferedImage.TYPE_3BYTE_BGR));

		// Assume we'll start at the beginning again
		this.currentFrame = 0;

		// If the reader is already open, we'll close it first and
		// reinstantiate it.
		if( this.reader != null && this.reader.isOpen() )
			this.reader.close();

		// Open the container from an input stream
		InputStream inputStream = null;
		final IContainer container = IContainer.make();
		int openResult = 0;
		try
		{
			inputStream = new URL(this.url).openStream();
			openResult = container.open( inputStream, null, true, true );
		}
		catch( final MalformedURLException e )
		{
			System.out.println( "Maybe not a URL? : '"+this.url+"'");
			try
			{
				openResult = container.open(
						new RandomAccessFile( this.url, "r" ),
						IContainer.Type.READ, null );
			}
			catch( final FileNotFoundException e1 )
			{
				System.out.println( "Nope, not a file either. *shrug*");
				e1.printStackTrace();
				return;
			}
		}
		catch( final IOException e )
		{
			e.printStackTrace();
			return;
		}

		// If the open failed, let's tell someone and quit
		if( openResult < 0 )
		{
			System.out.println( "Error opening container: "+openResult );
			System.out.println( IError.errorNumberToType( openResult ).toString() );
			return;
		}

		// Set up a new reader using the container that reads the images.
		this.reader = ToolFactory.makeReader( container );
		this.reader.setBufferedImageTypeToGenerate( BufferedImage.TYPE_3BYTE_BGR );
		this.reader.addListener( new FrameGetter() );
		this.reader.setCloseOnEofOnly( !this.loop );

		// Find the video stream.
		IStream s = null;
		int i = 0;
		while( i < container.getNumStreams() )
		{
			s = container.getStream( i );
			if( s != null && s.getStreamCoder().getCodecType() ==
					ICodec.Type.CODEC_TYPE_VIDEO )
			{
				// Save the stream index so that we only get frames from
				// this stream in the FrameGetter
				this.streamIndex = i;
				break;
			}
			i++;
		}

		if( container.getDuration() == Global.NO_PTS )
			this.totalFrames = -1;
		else	this.totalFrames = (long) (s.getDuration() *
				s.getTimeBase().getDouble() * s.getFrameRate().getDouble());

		// If we found the video stream, set the FPS
		if( s != null )
			this.fps = s.getFrameRate().getDouble();

		// If we found a video stream, setup the MBFImage buffer.
		if( s != null )
		{
			final int w = s.getStreamCoder().getWidth();
			final int h = s.getStreamCoder().getHeight();
			this.width = w;
			this.height = h;
		}
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.Video#getTimeStamp()
	 */
	@Override
	public long getTimeStamp()
	{
		return this.timestamp;
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.video.Video#getFPS()
	 */
	@Override
	public double getFPS()
	{
		return this.fps;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.Video#getCurrentFrameIndex()
	 */
	@Override
	public synchronized int getCurrentFrameIndex()
	{
		return (int)(this.timestamp/1000d * this.fps);
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.Video#setCurrentFrameIndex(long)
	 */
	@Override
	public void setCurrentFrameIndex( final long newFrame )
	{
		this.seekPrecise( newFrame / this.fps );
	}

	/**
	 * 	Implements a precise seeking mechanism based on the Xuggle seek method
	 * 	and the naive seek method which simply reads frames.
	 * 
	 *	@param timestamp The timestamp to get, in seconds.
	 */
	public void seekPrecise( double timestamp )
	{
		// Use the Xuggle seek method first to get near the frame
		this.seek( timestamp );

		// The timestamp field is in milliseconds, so we need to * 1000 to compare
		timestamp *= 1000;

		// Work out the number of milliseconds per frame
		final double timePerFrame = 1000d / this.fps;

		// If we're not in the right place, keep reading until we are.
		// Note the right place is the frame before the timestamp we're given:
		// |---frame 1---|---frame2---|---frame3---|
		//                   ^- given timestamp
		// ... so we should show frame2 not frame3.
		while( this.timestamp <= timestamp-timePerFrame && this.getNextFrame() != null );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.Video#seek(double)
	 */
	@Override
	public void seek( final double timestamp )
	{
		// Based on the code of this class: http://www.google.com/codesearch#DzBPmFOZfmA/trunk/0.5/unstable/videoplayer/src/classes/org/jdesktop/wonderland/modules/videoplayer/client/VideoPlayerImpl.java&q=seekKeyFrame%20position&type=cs
		// using the timebase, calculate the time in timebase units requested
		synchronized( this.reader )
		{
			// Check we've actually got a container
			if( this.reader == null || this.reader.getContainer() == null ||
					this.reader.getContainer().getStream(this.streamIndex) == null )
				this.create();

			// Convert between milliseconds and stream timestamps
			final double timebase = this.reader.getContainer().getStream(
					this.streamIndex).getTimeBase().getDouble();
			final long position = (long)(timestamp/timebase);

			final long min = position - 100;
			final long max = position;

			final int ret = this.reader.getContainer().seekKeyFrame( this.streamIndex,
					min, position, max, 0 );
			if(ret >= 0)
			{
				this.getNextFrame();
			}
		}
	}

	/**
	 * 	Returns the duration of the video in seconds.
	 *	@return The duraction of the video in seconds.
	 */
	public long getDuration()
	{
		final long duration = (this.reader.getContainer().
				getStream(this.streamIndex).getDuration());
		final double timebase = this.reader.getContainer().
				getStream(this.streamIndex).getTimeBase().getDouble();

		return (long) (duration * timebase);
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.video.Video#close()
	 */
	@Override
	public synchronized void close()
	{
		synchronized( this.reader )
		{
			if (this.reader != null && this.reader.isOpen())
			{
				this.reader.close();
				this.reader = null;
			}
		}
	}
}
