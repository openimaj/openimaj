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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.timecode.AudioTimecode;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.io.URLProtocolManager;

/**
 * A wrapper for the Xuggle audio decoding system into the OpenIMAJ audio
 * system.
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 8 Jun 2011
 *
 */
public class XuggleAudio extends AudioStream
{
	static Logger logger = Logger.getLogger(XuggleAudio.class);

	static {
		URLProtocolManager.getManager().registerFactory("jar", new JarURLProtocolHandlerFactory());
	}

	/** The reader used to read the video */
	private IMediaReader reader = null;

	/** The stream index that we'll be reading from */
	private int streamIndex = -1;

	/** The current sample chunk - note this is reused */
	private SampleChunk currentSamples = null;

	/** Whether we've read a complete chunk */
	private boolean chunkAvailable = false;

	/** The timecode of the current sample chunk */
	private final AudioTimecode currentTimecode = new AudioTimecode(0);

	/** The length of the media */
	private long length = -1;

	/** The URL being read */
	private final String url;

	/** Whether to loop the file */
	private final boolean loop;

	/**
	 * Whether this class was constructed from a stream. Some functions are
	 * unavailable
	 */
	private boolean constructedFromStream = false;

	/**
	 *
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 8 Jun 2011
	 *
	 */
	protected class ChunkGetter extends MediaToolAdapter
	{
		/**
		 * {@inheritDoc}
		 *
		 * @see com.xuggle.mediatool.MediaToolAdapter#onAudioSamples(com.xuggle.mediatool.event.IAudioSamplesEvent)
		 */
		@Override
		public void onAudioSamples(final IAudioSamplesEvent event)
		{
			// Get the samples
			final IAudioSamples aSamples = event.getAudioSamples();
			final byte[] rawBytes = aSamples.getData().
					getByteArray(0, aSamples.getSize());
			XuggleAudio.this.currentSamples.setSamples(rawBytes);

			// Set the timecode of these samples
			// double timestampMillisecs =
			// rawBytes.length/format.getNumChannels() /
			// format.getSampleRateKHz();
			final long timestampMillisecs = TimeUnit.MILLISECONDS.convert(
					event.getTimeStamp().longValue(), event.getTimeUnit());

			XuggleAudio.this.currentTimecode.setTimecodeInMilliseconds(
					timestampMillisecs);

			XuggleAudio.this.currentSamples.setStartTimecode(
					XuggleAudio.this.currentTimecode);

			XuggleAudio.this.currentSamples.getFormat().setNumChannels(
					XuggleAudio.this.getFormat().getNumChannels());

			XuggleAudio.this.currentSamples.getFormat().setSigned(
					XuggleAudio.this.getFormat().isSigned());

			XuggleAudio.this.currentSamples.getFormat().setBigEndian(
					XuggleAudio.this.getFormat().isBigEndian());

			XuggleAudio.this.currentSamples.getFormat().setSampleRateKHz(
					XuggleAudio.this.getFormat().getSampleRateKHz());

			XuggleAudio.this.chunkAvailable = true;
		}
	}

	/**
	 * Default constructor that takes the file to read.
	 *
	 * @param file
	 *            The file to read.
	 */
	public XuggleAudio(final File file)
	{
		this(file.toURI().toString(), false);
	}

	/**
	 * Default constructor that takes the file to read.
	 *
	 * @param file
	 *            The file to read.
	 * @param loop
	 *            Whether to loop indefinitely
	 */
	public XuggleAudio(final File file, final boolean loop)
	{
		this(file.toURI().toString(), loop);
	}

	/**
	 * Default constructor that takes the location of a file to read. This can
	 * either be a filename or a URL.
	 *
	 * @param u
	 *            The URL of the file to read
	 */
	public XuggleAudio(final URL u)
	{
		this(u.toString(), false);
	}

	/**
	 * Default constructor that takes the location of a file to read. This can
	 * either be a filename or a URL.
	 *
	 * @param u
	 *            The URL of the file to read
	 * @param loop
	 *            Whether to loop indefinitely
	 */
	public XuggleAudio(final URL u, final boolean loop)
	{
		this(u.toString(), loop);
	}

	/**
	 * Default constructor that takes the location of a file to read. This can
	 * either be a filename or a URL.
	 *
	 * @param url
	 *            The URL of the file to read
	 */
	public XuggleAudio(final String url)
	{
		this(url, false);
	}

	/**
	 * Default constructor that takes the location of a file to read. This can
	 * either be a filename or a URL. The second parameter determines whether
	 * the file will loop indefinitely. If so, {@link #nextSampleChunk()} will
	 * never return null; otherwise this method will return null at the end of
	 * the video.
	 *
	 * @param u
	 *            The URL of the file to read
	 * @param loop
	 *            Whether to loop indefinitely
	 */
	public XuggleAudio(final String u, final boolean loop)
	{
		this.url = u;
		this.loop = loop;
		this.create(null);
	}

	/**
	 * Construct a xuggle audio object from the stream.
	 *
	 * @param stream
	 *            The stream
	 */
	public XuggleAudio(final InputStream stream)
	{
		this.url = "stream://local";
		this.loop = false;
		this.constructedFromStream = true;
		this.create(stream);
	}

	/**
	 * Create the Xuggler reader
	 *
	 * @param stream
	 *            Can be NULL; else the stream to create from.
	 */
	private void create(final InputStream stream)
	{
		// If the reader is already open, we'll close it first and
		// reinstantiate it.
		if (this.reader != null && this.reader.isOpen())
		{
			this.reader.close();
			this.reader = null;
		}

		// Check whether the string we have is a valid URI
		IContainer container = null;
		int openResult = 0;
		try
		{
			// Create the container to read our audio file
			container = IContainer.make();

			// If we have a stream, we'll create from the stream...
			if (stream != null)
			{
				openResult = container.open(stream, null, true, true);

				if (openResult < 0)
					logger.info("XuggleAudio could not open InputStream to audio.");
			}
			// otherwise we'll use the URL in the class
			else
			{
				final URI uri = new URI(this.url);

				// If it's a valid URI, we'll try to open the container using
				// the URI string.
				openResult = container.open(uri.toString(),
						IContainer.Type.READ, null, true, true);

				// If there was an error trying to open the container in this
				// way,
				// it may be that we have a resource URL (which ffmpeg doesn't
				// understand), so we'll try opening an InputStream to the
				// resource.
				if (openResult < 0)
				{
					logger.trace("URL " + this.url + " could not be opened by ffmpeg. " +
							"Trying to open a stream to the URL instead.");
					final InputStream is = uri.toURL().openStream();
					openResult = container.open(is, null, true, true);

					if (openResult < 0)
					{
						logger.error("Error opening container. Error " + openResult +
								" (" + IError.errorNumberToType(openResult).toString() + ")");
						return;
					}
				}
				else
					logger.info("Opened XuggleAudio stream ok: " + openResult);
			}
		} catch (final URISyntaxException e2)
		{
			e2.printStackTrace();
			return;
		} catch (final MalformedURLException e)
		{
			e.printStackTrace();
			return;
		} catch (final IOException e)
		{
			e.printStackTrace();
			return;
		}

		// Set up a new reader using the container that reads the images.
		this.reader = ToolFactory.makeReader(container);
		this.reader.addListener(new ChunkGetter());
		this.reader.setCloseOnEofOnly(!this.loop);

		// Find the audio stream.
		IStream s = null;
		int i = 0;
		while (i < container.getNumStreams())
		{
			s = container.getStream(i);
			if (s != null &&
					s.getStreamCoder().getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
			{
				// Save the stream index so that we only get frames from
				// this stream in the FrameGetter
				this.streamIndex = i;
				break;
			}
			i++;
		}
		logger.info("Using audio stream " + this.streamIndex);

		if (container.getDuration() == Global.NO_PTS)
			this.length = -1;
		else
			this.length = (long) (s.getDuration() *
					s.getTimeBase().getDouble() * 1000d);

		// Get the coder for the audio stream
		final IStreamCoder aAudioCoder = container.
				getStream(this.streamIndex).getStreamCoder();

		logger.info("Using stream code: " + aAudioCoder);

		// Create an audio format object suitable for the audio
		// samples from Xuggle files
		final AudioFormat af = new AudioFormat(
				(int) IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
				aAudioCoder.getSampleRate() / 1000d,
				aAudioCoder.getChannels());
		af.setSigned(true);
		af.setBigEndian(false);
		super.format = af;

		logger.info("XuggleAudio using audio format: " + af);

		this.currentSamples = new SampleChunk(af.clone());
	}

	// protected int retries = 0;
	// protected int maxRetries = 0;
	//
	// /**
	// * Set the maximum allowed number of retries in case of an error reading a
	// * packet. Only use this on live streams; if you do it on a file-based
	// * stream it might cause looping at the end of file.
	// *
	// * @param retries
	// * maximum number of retries
	// */
	// public void setMaxRetries(int retries) {
	// this.maxRetries = retries;
	// }

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.audio.AudioStream#nextSampleChunk()
	 */
	@Override
	public SampleChunk nextSampleChunk()
	{
		try
		{
			IError e = null;
			while ((e = this.reader.readPacket()) == null && !this.chunkAvailable)
				;

			if (!this.chunkAvailable) {
				this.reader.close();
				this.reader = null;
				return null;
			}

			if (e != null)
			{
				this.reader.close();
				this.reader = null;

				// // We might be reading from a live stream & if we hit an
				// error
				// // we'll retry
				// if (e != null && e.getType() != IError.Type.ERROR_EOF)
				// {
				// logger.error("Got audio demux error " + e.getDescription());
				// this.create(null);
				// this.retries++;
				// }
				// logger.info("Closing audio stream " + this.url);
				return null;
			}

			this.chunkAvailable = false;
			return this.currentSamples;
		} catch (final Exception e) {
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.audio.AudioStream#reset()
	 */
	@Override
	public void reset()
	{
		if (this.constructedFromStream)
		{
			logger.info("Cannot reset a stream of audio.");
			return;
		}

		if (this.reader == null || this.reader.getContainer() == null)
			this.create(null);
		else
			this.seek(0);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.audio.AudioStream#getLength()
	 */
	@Override
	public long getLength()
	{
		return this.length;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.openimaj.audio.AudioStream#seek(long)
	 */
	@Override
	public void seek(final long timestamp)
	{
		if (this.constructedFromStream)
		{
			logger.info("Cannot seek within a stream of audio.");
			return;
		}

		if (this.reader == null || this.reader.getContainer() == null)
			this.create(null);

		// Convert from milliseconds to stream timestamps
		final double timebase = this.reader.getContainer().getStream(
				this.streamIndex).getTimeBase().getDouble();
		final long position = (long) (timestamp / timebase);

		final long min = Math.max(0, position - 100);
		final long max = position;

		// logger.info( "Timebase: "+timebase+" of a second second");
		// logger.info( "Position to seek to (timebase units): "+position
		// );
		// logger.info( "max: "+max+", min: "+min );

		final int i = this.reader.getContainer().seekKeyFrame(this.streamIndex,
				min, position, max, 0);

		// Check for errors
		if (i < 0)
			logger.error("Audio seek error (" + i + "): " + IError.errorNumberToType(i));
		else
			this.nextSampleChunk();
	}

	/**
	 * Close the audio stream.
	 */
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
