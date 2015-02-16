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
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
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
import com.xuggle.xuggler.io.URLProtocolManager;
import com.xuggle.xuggler.video.AConverter;
import com.xuggle.xuggler.video.BgrConverter;
import com.xuggle.xuggler.video.ConverterFactory;

/**
 * Wraps a Xuggle video reader into the OpenIMAJ {@link Video} interface.
 * <p>
 * <b>Some Notes:</b>
 * <p>
 * The {@link #hasNextFrame()} method must attempt to read the next packet in
 * the stream to determine if there is a next frame. That means that it incurs a
 * time penalty. It also means there's various logic in that method and the
 * {@link #getNextFrame()} method to avoid reading frames that have already been
 * read. It also means that, to avoid {@link #getCurrentFrame()} incorrectly
 * returning a new frame after {@link #hasNextFrame()} has been called, the
 * class may be holding two frames (the current frame and the next frame) after
 * {@link #hasNextFrame()} has been called.
 * <p>
 * The constructors have signatures that allow the passing of a boolean that
 * determines whether the video is looped or not. This has a different effect
 * than looping using the {@link VideoDisplay}. When the video is set to loop it
 * will loop indefinitely and the timestamp of frames will be consecutive. That
 * is, when the video loops the timestamps will continue to increase. This is in
 * contrast to setting the {@link VideoDisplay} end action (using
 * {@link VideoDisplay#setEndAction(org.openimaj.video.VideoDisplay.EndAction)}
 * where the looping will reset all timestamps when the video loops.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @created 1 Jun 2011
 */
public class XuggleVideo extends Video<MBFImage>
{
	private final static Logger logger = Logger.getLogger(XuggleVideo.class);

	static {
		// This allows us to read videos from jar: urls
		URLProtocolManager.getManager().registerFactory("jar", new JarURLProtocolHandlerFactory());

		// This converter converts the frames into MBFImages for us
		ConverterFactory.registerConverter(new ConverterFactory.Type(
				ConverterFactory.XUGGLER_BGR_24, MBFImageConverter.class,
				IPixelFormat.Type.BGR24, BufferedImage.TYPE_3BYTE_BGR));
	}

	/** The reader used to read the video */
	private IMediaReader reader = null;

	/** Used to tell, when reading packets, if we got enough for a new frame */
	private boolean currentFrameUpdated = false;

	/** The current frame - only ever one object that's reused */
	private MBFImage currentMBFImage;

	/** Whether the current frame is a key frame or not */
	private boolean currentFrameIsKeyFrame = false;

	/** The stream index that we'll be reading from */
	private int streamIndex = -1;

	/** Width of the video frame */
	private int width = -1;

	/** Height of the video frame */
	private int height = -1;

	/** A cache of the calculation of he total number of frames in the video */
	private long totalFrames = -1;

	/** A cache of the url of the video */
	private final String url;

	/** A cache of whether the video should be looped or not */
	private final boolean loop;

	/** The timestamp of the frame currently being decoded */
	private long timestamp;

	/** The offset to add to all timestamps (used for looping) */
	private long timestampOffset = 0;

	/** The number of frames per second */
	private double fps;

	/** The next frame in the stream */
	private MBFImage nextFrame = null;

	/** The timestamp of the next frame */
	public long nextFrameTimestamp = 0;

	/** Whether the next frame is a key frame or not */
	public boolean nextFrameIsKeyFrame = false;

	/**
	 * This implements the Xuggle MediaTool listener that will be called every
	 * time a video picture has been decoded from the stream. This class creates
	 * a BufferedImage for each video frame and updates the currentFrameUpdated
	 * boolean when one arrives.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 * @created 1 Jun 2011
	 */
	protected class FrameGetter extends MediaListenerAdapter
	{
		/**
		 * {@inheritDoc}
		 *
		 * @see com.xuggle.mediatool.MediaToolAdapter#onVideoPicture(com.xuggle.mediatool.event.IVideoPictureEvent)
		 */
		@Override
		public void onVideoPicture(final IVideoPictureEvent event)
		{
			// event.getPicture().getTimeStamp();
			if (event.getStreamIndex() == XuggleVideo.this.streamIndex)
			{
				XuggleVideo.this.currentMBFImage = ((MBFImageWrapper) event.getImage()).img;
				XuggleVideo.this.currentFrameIsKeyFrame = event.getMediaData().isKeyFrame();
				XuggleVideo.this.timestamp = (long) ((event.getPicture().getTimeStamp()
						* event.getPicture().getTimeBase().getDouble()) * 1000)
						+ XuggleVideo.this.timestampOffset;
				XuggleVideo.this.currentFrameUpdated = true;
			}
		}
	}

	/**
	 * Wrapper that created an MBFImage from a BufferedImage.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 * @created 1 Nov 2011
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
	 * Converter for converting IVideoPictures directly to MBFImages.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 * @created 1 Nov 2011
	 */
	protected static final class MBFImageConverter extends BgrConverter
	{
		private final MBFImageWrapper bimg = new MBFImageWrapper(null);
		private final byte[] buffer;

		public MBFImageConverter(
				final IPixelFormat.Type pictureType, final int pictureWidth,
				final int pictureHeight, final int imageWidth, final int imageHeight)
		{
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
				for (int y = 0, i = 0; y < h; y++) {
					for (int x = 0; x < w; x++, i += 3) {
						b[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[(this.buffer[i] & 0xFF)];
						g[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[(this.buffer[i + 1] & 0xFF)];
						r[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[(this.buffer[i + 2] & 0xFF)];
					}
				}

				return this.bimg;
			} finally {
				if (resamplePicture != null)
					resamplePicture.delete();
				if (ref.get() != null)
					ref.get().delete();
			}
		}
	}

	/**
	 * Default constructor that takes the video file to read.
	 *
	 * @param videoFile
	 *            The video file to read.
	 */
	public XuggleVideo(final File videoFile)
	{
		this(videoFile.toURI().toString());
	}

	/**
	 * Default constructor that takes the video file to read.
	 *
	 * @param videoFile
	 *            The video file to read.
	 * @param loop
	 *            should the video loop
	 */
	public XuggleVideo(final File videoFile, final boolean loop)
	{
		this(videoFile.toURI().toString(), loop);
	}

	/**
	 * Default constructor that takes the location of a video file to read. This
	 * can either be a filename or a URL.
	 *
	 * @param url
	 *            The URL of the file to read
	 */
	public XuggleVideo(final String url)
	{
		this(url, false);
	}

	/**
	 * Default constructor that takes the URL of a video file to read.
	 *
	 * @param url
	 *            The URL of the file to read
	 */
	public XuggleVideo(final URL url)
	{
		this(url.toString(), false);
	}

	/**
	 * Default constructor that takes the location of a video file to read. This
	 * can either be a filename or a URL. The second parameter determines
	 * whether the video will loop indefinitely. If so, {@link #getNextFrame()}
	 * will never return null; otherwise this method will return null at the end
	 * of the video.
	 *
	 * @param url
	 *            The URL of the file to read
	 * @param loop
	 *            Whether to loop the video indefinitely
	 */
	public XuggleVideo(final URL url, final boolean loop)
	{
		this(url.toString(), loop);
	}

	/**
	 * Default constructor that takes the location of a video file to read. This
	 * can either be a filename or a URL. The second parameter determines
	 * whether the video will loop indefinitely. If so, {@link #getNextFrame()}
	 * will never return null; otherwise this method will return null at the end
	 * of the video.
	 *
	 * @param url
	 *            The URL of the file to read
	 * @param loop
	 *            Whether to loop the video indefinitely
	 */
	public XuggleVideo(final String url, final boolean loop)
	{
		this.url = url;
		this.loop = loop;
		this.create(url);
	}

	/**
	 * Default constructor that takes an input stream. Note that only
	 * "streamable" video codecs can be used in this way.
	 *
	 * @param stream
	 *            The video data stream
	 */
	public XuggleVideo(final InputStream stream)
	{
		this.url = null;
		this.loop = false;
		this.create(stream);
	}

	/**
	 * Default constructor that takes a data input. Note that only "streamable"
	 * video codecs can be used in this way.
	 *
	 * @param input
	 *            The video data
	 */
	public XuggleVideo(final DataInput input)
	{
		this.url = null;
		this.loop = false;
		this.create(input);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#countFrames()
	 */
	@Override
	public long countFrames()
	{
		return this.totalFrames;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getNextFrame()
	 */
	@Override
	public MBFImage getNextFrame()
	{
		if (this.nextFrame != null)
		{
			// We've already read the next frame, so we simply move on.
			this.currentMBFImage = this.nextFrame;
			this.timestamp = this.nextFrameTimestamp;
			this.currentFrameIsKeyFrame = this.nextFrameIsKeyFrame;
			this.nextFrame = null;
		}
		else
		{
			// Read a frame from the stream.
			this.currentMBFImage = this.readFrame(false);
		}

		if (this.currentMBFImage != null)
		{
			// Increment frame counter
			this.currentFrame++;
		}

		return this.currentMBFImage;
	}

	/**
	 * Reads a frame from the stream, or returns null if no frame could be read.
	 * If preserveCurrent is true, then the frame is read into the nextFrame
	 * member rather than the currentMBFImage member and the nextFrame is
	 * returned (while currentMBFImage will still contain the previous frame).
	 * Note that if preserveCurrent is true, it will invoke a copy between
	 * images. If preserveCurrent is false and nextFrame is set, this method may
	 * have unexpected results as it does not swap current and next back. See
	 * {@link #getNextFrame()} which swaps back when a frame has been pre-read
	 * from the stream.
	 *
	 * @param preserveCurrent
	 *            Whether to preserve the current frame
	 * @return The frame that was read, or NULL if no frame could be read.
	 */
	synchronized private MBFImage readFrame(final boolean preserveCurrent)
	{
		// System.out.println( "readFrame( "+preserveCurrent+" )");

		if (this.reader == null)
			return null;

		// If we need to preserve the current frame, we need to copy the frame
		// because the readPacket() will cause the frame to be overwritten
		final long currentTimestamp = this.timestamp;
		final boolean currentKeyFrameFlag = this.currentFrameIsKeyFrame;
		if (preserveCurrent && this.nextFrame == null)
		{
			// We make a copy of the current image and set the current image
			// to point to that (thereby preserving it). We then set the next
			// frame image to point to the buffer that the readPacket() will
			// fill.
			if (this.currentMBFImage != null)
			{
				final MBFImage tmp = this.currentMBFImage.clone();
				this.nextFrame = this.currentMBFImage;
				this.currentMBFImage = tmp;
			}
		}
		// If nextFrame wasn't null, we can just write into it as must be
		// pointing to the current frame buffer

		IError e = null;
		boolean tryAgain = false;
		do
		{
			tryAgain = false;

			// Read packets until we have a new frame.
			while ((e = this.reader.readPacket()) == null && !this.currentFrameUpdated)
				;

			if (e != null && e.getType() == IError.Type.ERROR_EOF && this.loop)
			{
				// We're looping, so we update the timestamp offset.
				this.timestampOffset += (this.timestamp - this.timestampOffset);
				tryAgain = true;
				this.seekToBeginning();
			}
		} while (tryAgain);

		// Check if we're at the end of the file
		if (!this.currentFrameUpdated || e != null)
		{
			// Logger.error( "Got video demux error: "+e.getType() );
			return null;
		}

		// We've read a frame so we're done looping
		this.currentFrameUpdated = false;

		if (preserveCurrent)
		{
			// Swap the current values into the next-frame values
			this.nextFrameIsKeyFrame = this.currentFrameIsKeyFrame;
			this.currentFrameIsKeyFrame = currentKeyFrameFlag;
			this.nextFrameTimestamp = this.timestamp;
			this.timestamp = currentTimestamp;

			// Return the next frame
			if (this.nextFrame != null)
				return this.nextFrame;
			return this.currentMBFImage;
		}
		// Not preserving anything, so just return the frame
		else
			return this.currentMBFImage;
	}

	/**
	 * Returns a video timecode for the current frame.
	 *
	 * @return A video timecode for the current frame.
	 */
	public VideoTimecode getCurrentTimecode()
	{
		return new HrsMinSecFrameTimecode((long) (this.timestamp / 1000d * this.fps), this.fps);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getCurrentFrame()
	 */
	@Override
	public MBFImage getCurrentFrame()
	{
		if (this.currentMBFImage == null)
			this.currentMBFImage = this.getNextFrame();
		return this.currentMBFImage;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getWidth()
	 */
	@Override
	public int getWidth()
	{
		return this.width;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getHeight()
	 */
	@Override
	public int getHeight()
	{
		return this.height;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#hasNextFrame()
	 */
	@Override
	public boolean hasNextFrame()
	{
		if (this.nextFrame == null)
		{
			this.nextFrame = this.readFrame(true);
			return this.nextFrame != null;
		}
		else
			return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Note: if you created the video from a {@link DataInput} or
	 * {@link InputStream}, there is no way that it can be reset.
	 *
	 * @see org.openimaj.video.Video#reset()
	 */
	@Override
	synchronized public void reset()
	{
		if (this.reader == null) {
			if (this.url == null)
				return;

			this.create(url);
		} else {
			this.seekToBeginning();
		}
	}

	/**
	 * This is a convenience method that will seek the stream to be the
	 * beginning. As the seek method seems a bit flakey in some codec containers
	 * in Xuggle, we'll try and use a few different methods to get us back to
	 * the beginning. That means that this method may be slower than seek(0) if
	 * it needs to try multiple methods.
	 * <p>
	 * Note: if you created the video from a {@link DataInput} or
	 * {@link InputStream}, there is no way that it can be reset.
	 */
	synchronized public void seekToBeginning()
	{
		// if the video came from a stream, there is no chance of returning!
		if (this.url == null)
			return;

		// Try to seek to byte 0. That's the start of the file.
		this.reader.getContainer().seekKeyFrame(this.streamIndex,
				0, 0, 0, IContainer.SEEK_FLAG_BYTE);

		// Got to the beginning? We're done.
		if (this.timestamp == 0)
			return;

		// Try to seek to key frame at timestamp 0.
		this.reader.getContainer().seekKeyFrame(this.streamIndex,
				0, 0, 0, IContainer.SEEK_FLAG_FRAME);

		// Got to the beginning? We're done.
		if (this.timestamp == 0)
			return;

		// Try to seek backwards to timestamp 0.
		this.reader.getContainer().seekKeyFrame(this.streamIndex,
				0, 0, 0, IContainer.SEEK_FLAG_BACKWARDS);

		// Got to the beginning? We're done.
		if (this.timestamp == 0)
			return;

		// Try to seek to timestamp 0 any way possible.
		this.reader.getContainer().seekKeyFrame(this.streamIndex,
				0, 0, 0, IContainer.SEEK_FLAG_ANY);

		// Got to the beginning? We're done.
		if (this.timestamp == 0)
			return;

		// We're really struggling to get this container back to the start.
		// So, try recreating the whole reader again.
		this.reader.close();
		this.reader = null;
		this.create(url);

		this.getNextFrame();

		// We tried everything. It's either worked or it hasn't.
		return;
	}

	/**
	 * Create the necessary reader
	 */
	synchronized private void create(String urlstring)
	{
		setupReader();

		// Check whether the string we have is a valid URI
		IContainer container = null;
		int openResult = 0;
		try
		{
			// If it's a valid URI, we'll try to open the container using the
			// URI string.
			container = IContainer.make();
			openResult = container.open(urlstring, IContainer.Type.READ, null, true, true);

			// If there was an error trying to open the container in this way,
			// it may be that we have a resource URL (which ffmpeg doesn't
			// understand), so we'll try opening an InputStream to the resource.
			if (openResult < 0)
			{
				logger.trace("URL " + urlstring + " could not be opened by ffmpeg. " +
						"Trying to open a stream to the URL instead.");
				final InputStream is = new DataInputStream(new URL(urlstring).openStream());
				openResult = container.open(is, null, true, true);

				if (openResult < 0)
				{
					logger.error("Error opening container. Error " + openResult +
							" (" + IError.errorNumberToType(openResult).toString() + ")");
					return;
				}
			}
		} catch (final MalformedURLException e) {
			e.printStackTrace();
			return;
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		}

		setupReader(container);
	}

	/**
	 * Create the necessary reader
	 */
	synchronized private void create(InputStream stream)
	{
		setupReader();

		// Check whether the string we have is a valid URI
		final IContainer container = IContainer.make();
		final int openResult = container.open(stream, null, true, true);

		if (openResult < 0)
		{
			logger.error("Error opening container. Error " + openResult +
					" (" + IError.errorNumberToType(openResult).toString() + ")");
			return;
		}

		setupReader(container);
	}

	/**
	 * Create the necessary reader
	 */
	synchronized private void create(DataInput input)
	{
		setupReader();

		// Check whether the string we have is a valid URI
		final IContainer container = IContainer.make();
		final int openResult = container.open(input, null, true, true);

		if (openResult < 0)
		{
			logger.error("Error opening container. Error " + openResult +
					" (" + IError.errorNumberToType(openResult).toString() + ")");
			return;
		}

		setupReader(container);
	}

	private void setupReader() {
		// Assume we'll start at the beginning again
		this.currentFrame = 0;

		// If the reader is already open, we'll close it first and
		// reinstantiate it.
		if (this.reader != null && this.reader.isOpen())
		{
			this.reader.close();
			this.reader = null;
		}
	}

	private void setupReader(IContainer container) {
		// Set up a new reader using the container that reads the images.
		this.reader = ToolFactory.makeReader(container);
		this.reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		this.reader.addListener(new FrameGetter());

		// Find the video stream.
		IStream s = null;
		int i = 0;
		while (i < container.getNumStreams())
		{
			s = container.getStream(i);
			if (s != null && s.getStreamCoder().getCodecType() ==
					ICodec.Type.CODEC_TYPE_VIDEO)
			{
				// Save the stream index so that we only get frames from
				// this stream in the FrameGetter
				this.streamIndex = i;
				break;
			}
			i++;
		}

		if (container.getDuration() == Global.NO_PTS)
			this.totalFrames = -1;
		else
			this.totalFrames = (long) (s.getDuration() *
					s.getTimeBase().getDouble() * s.getFrameRate().getDouble());

		// If we found the video stream, set the FPS
		if (s != null)
			this.fps = s.getFrameRate().getDouble();

		// If we found a video stream, setup the MBFImage buffer.
		if (s != null)
		{
			final int w = s.getStreamCoder().getWidth();
			final int h = s.getStreamCoder().getHeight();
			this.width = w;
			this.height = h;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getTimeStamp()
	 */
	@Override
	public long getTimeStamp()
	{
		return this.timestamp;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getFPS()
	 */
	@Override
	public double getFPS()
	{
		return this.fps;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#getCurrentFrameIndex()
	 */
	@Override
	public synchronized int getCurrentFrameIndex()
	{
		return (int) (this.timestamp / 1000d * this.fps);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#setCurrentFrameIndex(long)
	 */
	@Override
	public void setCurrentFrameIndex(final long newFrame)
	{
		this.seekPrecise(newFrame / this.fps);
	}

	/**
	 * Implements a precise seeking mechanism based on the Xuggle seek method
	 * and the naive seek method which simply reads frames.
	 * <p>
	 * Note: if you created the video from a {@link DataInput} or
	 * {@link InputStream}, you can only seek forwards.
	 *
	 * @param timestamp
	 *            The timestamp to get, in seconds.
	 */
	public void seekPrecise(double timestamp)
	{
		// Use the Xuggle seek method first to get near the frame
		this.seek(timestamp);

		// The timestamp field is in milliseconds, so we need to * 1000 to
		// compare
		timestamp *= 1000;

		// Work out the number of milliseconds per frame
		final double timePerFrame = 1000d / this.fps;

		// If we're not in the right place, keep reading until we are.
		// Note the right place is the frame before the timestamp we're given:
		// |---frame 1---|---frame2---|---frame3---|
		// ^- given timestamp
		// ... so we should show frame2 not frame3.
		while (this.timestamp <= timestamp - timePerFrame && this.getNextFrame() != null)
			;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Note: if you created the video from a {@link DataInput} or
	 * {@link InputStream}, you can only seek forwards.
	 *
	 * @see org.openimaj.video.Video#seek(double)
	 */
	@Override
	synchronized public void seek(final double timestamp)
	{
		// Based on the code of this class:
		// http://www.google.com/codesearch#DzBPmFOZfmA/trunk/0.5/unstable/videoplayer/src/classes/org/jdesktop/wonderland/modules/videoplayer/client/VideoPlayerImpl.java&q=seekKeyFrame%20position&type=cs
		// using the timebase, calculate the time in timebase units requested
		// Check we've actually got a container
		if (this.reader == null) {
			if (this.url == null)
				return;

			this.create(url);
		}

		// Convert between milliseconds and stream timestamps
		final double timebase = this.reader.getContainer().getStream(
				this.streamIndex).getTimeBase().getDouble();
		final long position = (long) (timestamp / timebase);

		final long min = Math.max(0, position - 100);
		final long max = position;

		final int ret = this.reader.getContainer().seekKeyFrame(this.streamIndex,
				min, position, max, IContainer.SEEK_FLAG_ANY);

		if (ret >= 0)
			this.getNextFrame();
		else
			logger.error("Seek returned an error value: " + ret + ": "
					+ IError.errorNumberToType(ret));
	}

	/**
	 * Returns the duration of the video in seconds.
	 *
	 * @return The duraction of the video in seconds.
	 */
	public synchronized long getDuration()
	{
		final long duration = (this.reader.getContainer().
				getStream(this.streamIndex).getDuration());
		final double timebase = this.reader.getContainer().
				getStream(this.streamIndex).getTimeBase().getDouble();

		return (long) (duration * timebase);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.video.Video#close()
	 */
	@Override
	public synchronized void close()
	{
		if (this.reader != null)
		{
			synchronized (this.reader)
			{
				if (this.reader.isOpen())
				{
					this.reader.close();
					this.reader = null;
				}
			}
		}
	}
}
