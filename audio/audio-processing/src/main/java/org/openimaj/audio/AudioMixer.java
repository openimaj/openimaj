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
import java.util.List;

import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.timecode.AudioTimecode;


/**
 * 	A basic audio mixer that takes a number of {@link AudioStream}s and mixes
 * 	then with some gain compensation into a single audio stream.
 * 	
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@created 23rd November 2011
 */
public class AudioMixer extends AudioStream
{
	/**
	 * 	A listener for objects that wish to be informed of a mix event.
	 * 	The mix event provides the sample buffers of all the channels
	 * 	and the sample buffer of the mixed stream. It is called before
	 * 	the mixed stream chunk is returned from each mix event.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	
	 *	@created 29 Nov 2011
	 */
	public interface MixEventListener
	{
		/**
		 * Callback for a mix event.
		 * @param channels the channels being mixed
		 * @param mix the mixed channel
		 */
		public void mix( SampleBuffer[] channels, SampleBuffer mix );
	}
	
	/** A list of the audio streams to mix in this mixer */
	private List<AudioStream> streams = new ArrayList<AudioStream>();
	private List<Float> gain = new ArrayList<Float>(); 
	
	/** The currently processed sample in the mixer */
	private SampleChunk currentSample = null;
	
	/** The size of each mix - the sample buffer size */
	private int bufferSize = 256;
	
	/** 
	 * 	If set to TRUE, this will cause the mixer to run even when there are
	 *  not streams to play. It does this by returning empty sample chunks.
	 */
	private boolean alwaysRun = true;
	
	/** The time the mixer started */
	private long startMillis = -1;
	
	/** The timecode we're using */
	private AudioTimecode timecode = null;
	
	/** Listeners of the mix event */
	private List<MixEventListener> mixEventListeners = 
		new ArrayList<MixEventListener>();
	
	/**
	 * 	Default constructor that takes the format for
	 * 	the samples. All streams added to this mixer
	 * 	must conform to that sample format.
	 * 
	 * 	@param af The {@link AudioFormat}
	 */
	public AudioMixer( AudioFormat af )
	{
		this.setFormat( af );
		
		// Create the current sample chunk that we'll reuse
		currentSample = new SampleChunk( af );
		currentSample.setSamples( new byte[bufferSize*af.getNumChannels()] );
		
		timecode = new AudioTimecode( 0 );
	}
	
	/**
	 * 	The timecode object
	 *	@param tc The timecode object.
	 */
	public void setTimecodeObject( AudioTimecode tc )
	{
		this.timecode = tc;
	}
	
	/**
	 * 	Add an {@link AudioStream} to this mixer. It must conform
	 * 	to the same format as this mixer. If not, an {@link IllegalArgumentException}
	 * 	will be thrown.
	 * 
	 * 	@param as The {@link AudioStream} to add to this mixer.
	 * 	@param defaultGain The default gain of this stream.
	 */
	public void addStream( AudioStream as, float defaultGain )
	{
		if( as.format.equals( this.getFormat() ) )
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
			if( stream.getClass().getName().equals( 
				FixedSizeSampleAudioProcessor.class.getName() ) )
			{
				// Get the underlying stream.
				stream = ((AudioProcessor)as).getUnderlyingStream();
			}
			
			// Add the stream wrapped in a fixed size audio processor.
			this.gain.add( defaultGain );
			synchronized( streams )
			{
				this.streams.add( new FixedSizeSampleAudioProcessor( stream, bufferSize )
				{
					@Override
					public SampleChunk process(SampleChunk sample)
					{
						return sample;
					}				
				});			
			}
		}
		else	throw new IllegalArgumentException( "Format of added stream is "+
					"incompatible with the mixer." );
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#nextSampleChunk()
	 */
	@Override
	synchronized public SampleChunk nextSampleChunk() 
	{
		// If there are no streams attached to this mixer, then
		// we return null - end of mixer stream.
		if( streams.size() == 0 && !alwaysRun )
			return null;
		
		// Set the time the mixer started
		if( startMillis == -1 )
			startMillis = System.currentTimeMillis();
		
		// Get the next sample chunk from each stream.
		final SampleBuffer sb = this.currentSample.getSampleBuffer();
		SampleBuffer[] chunks = null; 
		synchronized( streams )
		{
			List<SampleBuffer> chunkList = new ArrayList<SampleBuffer>();
			for( int stream = 0; stream < streams.size(); stream++ )
			{
				final SampleChunk sc = streams.get(stream).nextSampleChunk();
				if( sc != null )
					chunkList.add( sc.getSampleBuffer() );
				else	
				{
					// Got to the end of the stream, so we'll remove it
					streams.remove( stream );
					gain.remove( stream );
				}
			}			
			chunks = chunkList.toArray( new SampleBuffer[0] ); 
		
			// Now create the new sample chunk by averaging the samples
			// at each point in each stream
			for( int i = 0; i < sb.size(); i++ )
			{
				float Z = 0;
				for( int stream = 0; stream < chunks.length; stream++ )
					if( chunks[stream] != null )
						Z += chunks[stream].get(i) * gain.get(stream);
					
				// Set the value in the new sample buffer
				sb.set( i, Z );
			}
		}
		
		// Fire the mix event
		for( MixEventListener mel : mixEventListeners )
			mel.mix( chunks, sb );

		// Create a SampleChunk for our mix stream
		SampleChunk sc = sb.getSampleChunk();
		timecode.setTimecodeInMilliseconds( System.currentTimeMillis() - startMillis );
		sc.setStartTimecode( timecode );
		
		return sc;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.AudioStream#reset()
	 */
	@Override
	public void reset()
	{
		// No implementation
	}
	
	/**
	 * 	Set the size of the buffer that the mixer will mix. Note that this
	 * 	must be done before any streams are added to the mixer.
	 * 
	 *	@param bufferSize The buffer size in samples per channel.
	 */
	public void setBufferSize( int bufferSize )
	{
		this.bufferSize = bufferSize;
		currentSample.setSamples( new byte[bufferSize*format.getNumChannels()] );
	}
	
	/**
	 * 	Whether to run the mixer when there are no audio streams to mix.
	 *	@param alwaysRun TRUE to make the mixer always run.
	 */
	public void setAlwaysRun( boolean alwaysRun )
	{
		this.alwaysRun = alwaysRun;
	}

	/**
	 * 	Add a mix event listener to this AudioMixer.
	 *	@param mel The {@link MixEventListener} to add
	 */
	public void addMixEventListener( MixEventListener mel )
	{
		mixEventListeners.add( mel );
	}
	
	/**
	 * 	Remove the given {@link MixEventListener} from this mixer.
	 *	@param mel The {@link MixEventListener} to remove
	 */
	public void removeMixEventListener( MixEventListener mel )
	{
		mixEventListeners.remove( mel );
	}
}
