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
package org.openimaj.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.samples.SampleBufferFactory;
import org.openimaj.audio.timecode.AudioTimecode;

/**
 * A basic audio mixer that takes a number of {@link AudioStream}s and mixes
 * then with some gain compensation into a single audio stream.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 23rd November 2011
 */
public class AudioMixer extends AudioStream
{
	/**
	 * A listener for objects that wish to be informed of a mix event. The mix
	 * event provides the sample buffers of all the channels and the sample
	 * buffer of the mixed stream. It is called before the mixed stream chunk is
	 * returned from each mix event.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * 
	 * @created 29 Nov 2011
	 */
	public interface MixEventListener
	{
		/**
		 * Callback for a mix event.
		 * 
		 * @param channels
		 *            the channels being mixed
		 * @param mix
		 *            the mixed channel
		 */
		public void mix(SampleBuffer[] channels, SampleBuffer mix);
	}

	/** A list of the audio streams to mix in this mixer */
	private final List<AudioStream> streams = new ArrayList<AudioStream>();
	private final List<Float> gain = new ArrayList<Float>();
	private final Map<AudioStream, AudioStream> streamMap = new HashMap<AudioStream, AudioStream>();

	/** The currently processed sample in the mixer */
	private SampleBuffer currentSample = null;

	/** The size of each mix - the sample buffer size */
	private int bufferSize = 256;

	/**
	 * If set to TRUE, this will cause the mixer to run even when there are not
	 * streams to play. It does this by returning empty sample chunks.
	 */
	private boolean alwaysRun = true;

	/** The time the mixer started */
	private long startMillis = -1;

	/** The timecode we're using */
	private AudioTimecode timecode = null;

	/** Listeners of the mix event */
	private final List<MixEventListener> mixEventListeners =
			new ArrayList<MixEventListener>();

	/** Whether or not to fire mix events */
	private boolean fireMixEvents = false;

	/**
	 * Default constructor that takes the format for the samples. All streams
	 * added to this mixer must conform to that sample format.
	 * 
	 * @param af
	 *            The {@link AudioFormat}
	 */
	public AudioMixer(final AudioFormat af)
	{
		this.setFormat(af);

		// Create the current sample chunk that we'll reuse
		this.currentSample = SampleBufferFactory.createSampleBuffer(af, this.bufferSize);

		this.timecode = new AudioTimecode(0);
	}

	/**
	 * The timecode object
	 * 
	 * @param tc
	 *            The timecode object.
	 */
	public void setTimecodeObject(final AudioTimecode tc)
	{
		this.timecode = tc;
	}

	/**
	 * Add an {@link AudioStream} to this mixer. It must conform to the same
	 * format as this mixer. If not, an {@link IllegalArgumentException} will be
	 * thrown.
	 * 
	 * @param as The {@link AudioStream} to add to this mixer.
	 * @param defaultGain
	 *            The default gain of this stream.
	 */
	public void addStream(final AudioStream as, final float defaultGain)
	{
		if (as.format.equals(this.getFormat()))
		{
			AudioStream stream = as;

			// It's important that the incoming sample chunks from
			// the input streams are equal in length, so we wrap them
			// all in FixedSampleSizeAudioProcessor. However, before we
			// do we check whether they already are fixed sized chunks.
			// We can't check with just a instanceof because that will also
			// be true for subclasses and we can't be sure they're doing more.
			// So, we must check ONLY for instances of EXACTLY
			// FixedSampleSizeAudioProcessors.
			if (stream.getClass().getName().equals(
					FixedSizeSampleAudioProcessor.class.getName()))
			{
				// Get the underlying stream.
				stream = ((AudioProcessor) as).getUnderlyingStream();
			}

			// Set the gain
			this.gain.add(defaultGain);

			// Add the stream wrapped in a fixed size audio processor.
			synchronized (this.streams)
			{
				// Wrap the stream in a FixedSizeSampleAudioProcessor
				final FixedSizeSampleAudioProcessor fssap =
						new FixedSizeSampleAudioProcessor(stream, this.bufferSize);
				this.streams.add(fssap);
				this.streamMap.put(as, fssap);
			}
		}
		else
			throw new IllegalArgumentException("Format of added stream is " +
					"incompatible with the mixer.");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.AudioStream#nextSampleChunk()
	 */
	@Override
	synchronized public SampleChunk nextSampleChunk()
	{
		// If there are no streams attached to this mixer, then
		// we return null - end of mixer stream.
		if (this.streams.size() == 0 && !this.alwaysRun)
			return null;

		// Set the time the mixer started
		if (this.startMillis == -1)
			this.startMillis = System.currentTimeMillis();

		// Get the next sample chunk from each stream.
		final SampleBuffer sb = this.currentSample;
		SampleChunk sc = null;

		final List<SampleBuffer> chunkList = new ArrayList<SampleBuffer>();
		synchronized (this.streams)
		{
			for (int stream = 0; stream < this.streams.size(); stream++)
			{
				// We can do this because the sample chunks from all the streams
				// are forced to be the same size!
				sc = this.streams.get(stream).nextSampleChunk();

				// Get the next chunk and add it to a list for going through
				// later
				if (sc != null)
				{
					// System.out.println(
					// this+" Stream "+stream+" size "+sc.getNumberOfSamples()
					// );
					chunkList.add(sc.getSampleBuffer());
				}
				else
				{
					// Got to the end of the stream, so we'll remove it
					this.streams.remove(stream);
					this.gain.remove(stream);
				}
			}

			// System.out.println( chunkList +" -> "+this.gain );

			// Now create the new sample chunk by averaging the samples
			// at each point from all streams
			for (int i = 0; i < sb.size(); i++)
			{
				float Z = 0;
				for (int stream = 0; stream < chunkList.size(); stream++)
					if (chunkList.get(stream) != null)
						Z += chunkList.get(stream).get(i) * this.gain.get(stream);

				// Set the value in the new sample buffer
				sb.set(i, Z);
			}
		}

		// Fire the mix event
		if (this.fireMixEvents)
			for (final MixEventListener mel : this.mixEventListeners)
				mel.mix(chunkList.toArray(new SampleBuffer[0]), sb);

		// Create a SampleChunk for our mix stream
		sc = sb.getSampleChunk();

		this.timecode.setTimecodeInMilliseconds(System.currentTimeMillis() -
				this.startMillis);
		sc.setStartTimecode(this.timecode);

		return sc;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.AudioStream#reset()
	 */
	@Override
	public void reset()
	{
		// No implementation
	}

	/**
	 * Set the size of the buffer that the mixer will mix. Note that this must
	 * be done before any streams are added to the mixer.
	 * 
	 * @param bufferSize
	 *            The buffer size in samples per channel.
	 */
	public void setBufferSize(final int bufferSize)
	{
		this.bufferSize = bufferSize;
		this.currentSample = SampleBufferFactory.createSampleBuffer(
				this.format, bufferSize);
	}

	/**
	 * Whether to run the mixer when there are no audio streams to mix.
	 * 
	 * @param alwaysRun
	 *            TRUE to make the mixer always run.
	 */
	public void setAlwaysRun(final boolean alwaysRun)
	{
		this.alwaysRun = alwaysRun;
	}

	/**
	 * Add a mix event listener to this AudioMixer.
	 * 
	 * @param mel
	 *            The {@link MixEventListener} to add
	 */
	public void addMixEventListener(final MixEventListener mel)
	{
		this.mixEventListeners.add(mel);
	}

	/**
	 * Remove the given {@link MixEventListener} from this mixer.
	 * 
	 * @param mel
	 *            The {@link MixEventListener} to remove
	 */
	public void removeMixEventListener(final MixEventListener mel)
	{
		this.mixEventListeners.remove(mel);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.audio.AudioStream#getLength()
	 */
	@Override
	public long getLength()
	{
		return -1;
	}

	/**
	 * Whether to fire mix events or not (default is that the mixer doesn't)
	 * 
	 * @param tf
	 *            TRUE to fire mix events.
	 */
	public void setMixEvents(final boolean tf)
	{
		this.fireMixEvents = tf;
	}

	/**
	 * Remove the given audio stream from the mixer.
	 * 
	 * @param as The audio stream to remove
	 */
	public void removeStream(final AudioStream as)
	{
		synchronized (this.streams)
		{
			AudioStream aas = this.streamMap.get(as);
			if (aas == null)
				aas = as;

			System.out.println("Removing " + aas + " from " + this.streams);
			this.gain.remove(this.streams.indexOf(aas));
			this.streams.remove(this.streams.indexOf(aas));
		}
	}
}
