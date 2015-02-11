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
package org.openimaj.demos.audio;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioMixer;
import org.openimaj.audio.AudioMixer.MixEventListener;
import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.analysis.BeatDetector;
import org.openimaj.audio.analysis.EffectiveSoundPressure;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.audio.timecode.MeasuresBeatsTicksTimecode;
import org.openimaj.audio.util.MusicUtils;
import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.time.Sequencer;
import org.openimaj.video.xuggle.XuggleAudio;

/**
 *	A demonstration of the {@link AudioMixer} function in OpenIMAJ.
 *	Also demonstrates the {@link EffectiveSoundPressure} processor
 *	(for calculating the loudness of each channel), the {@link BeatDetector}
 *	processor (for displaying beats) and the {@link Sequencer} for
 *	sequencing actions in time (in this case triggering the audio loops).
 *	<p>
 *	Note: if this demo makes a horrible noise as the number of channels being
 *	mixed increases, then it's probably because the sample buffer isn't long
 *	enough for your system. On line 70 it's set to 720 samples. You may increase
 *	this if it doesn't work.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 26 Nov 2011
 */
@Demo(
	title = "Audio Mixing and Beat Detection",
	author = "David Dupplaw", 
	description = "Demonstrates some of the OpenIMAJ audio functionality for " +
			"audio processing. Includes the mixing of audio streams, the " +
			"sequencing of events (in this case audio events), the calculation " +
			"of sound levels and the detection of beats within music.", 
	keywords = { "audio", "sound", "vu", "loudness", "beat detection", "mixing",
			"sequencing", "events", "pressure", "dB" },
	icon = "/org/openimaj/demos/icons/audio/vumeter.png",
	screenshot = "/org/openimaj/demos/screens/audio/mixing.png"
)
public class AudioMixerDemo 
{
	/**
	 * 	Construct the demo for the audio mixer
	 */
	public AudioMixerDemo() 
	{
		// The image will contain the VU meters
		this.img = new MBFImage( 300, 400, 3 );
		DisplayUtilities.displayName( this.img, "VU Meters" );
		
		// Create a new audio mixer than mixes audio streams
		final AudioFormat mixerFormat = new AudioFormat( 16, 44.1, 2 );
		final AudioMixer am = new AudioMixer( mixerFormat );
		am.addMixEventListener( new VURenderer( mixerFormat ) );
		am.setMixEvents( true );
		
		// Due to the way we sequence the loops, the size of this buffer will
		// cause a lag in the sequenced loops, so the smaller it is the better.
		// However, the smaller it is, the less time we'll have to mix and process
		// the sample chunks and display the VU meters (because in this demo it's
		// all done in one thread).
		am.setBufferSize( 720 );
		
		// Create a new audio player (this will be the timekeeper for the sequencer)
		final AudioPlayer ap = new AudioPlayer( am /*, "M44 [plughw:0,0]" */ );
		ap.setTimecodeObject( new MeasuresBeatsTicksTimecode( 140 ) );
		
		// Create a new sequencer that will set up the different streams
		final Sequencer seq = new Sequencer( ap, MusicUtils.millisPerBeat( 140 )/4 );

		// We instantiate a XuggleAudio here as it seems to take a long time
		// to start this class up for the first time and the synchronisation 
		// goes out unless we preload the class and everything it needs here.
		new XuggleAudio( AudioMixer.class.getResource("/org/openimaj/demos/audio/140bpm-2205.mp3") );

		// Set up the various events in the sequencer
		// First we set up all the actions (which can be reused)
		final Sequencer.SequencedAction drums = this.getAction(
				"/org/openimaj/demos/audio/140bpm_formware_psytech.mp3", am );
		final Sequencer.SequencedAction bass = this.getAction(
						"/org/openimaj/demos/audio/140bpm-Arp.mp3", am );
		final Sequencer.SequencedAction tb303 = this.getAction(
				"/org/openimaj/demos/audio/140bpm-303.mp3", am );
		final Sequencer.SequencedAction tb2205 = this.getAction(
				"/org/openimaj/demos/audio/140bpm-2205.mp3", am );

		// Add the drums events
		for( int i = 1; i < 29; i += 4 )
			seq.addEvent( new Sequencer.SequencerEvent( 
					new MeasuresBeatsTicksTimecode( 140,i,0,0 ), drums ) );
		seq.addEvent( new Sequencer.SequencerEvent( 
				new MeasuresBeatsTicksTimecode( 140,5,0,0 ), bass ) );
		seq.addEvent( new Sequencer.SequencerEvent( 
				new MeasuresBeatsTicksTimecode( 140,13,0,0 ), tb303 ) );
		seq.addEvent( new Sequencer.SequencerEvent( 
				new MeasuresBeatsTicksTimecode( 140,17,0,0 ), tb303 ) );
		seq.addEvent( new Sequencer.SequencerEvent( 
				new MeasuresBeatsTicksTimecode( 140,21,0,0 ), bass ) );
		seq.addEvent( new Sequencer.SequencerEvent( 
				new MeasuresBeatsTicksTimecode( 140,21,0,0 ), tb303 ) );
		for( int i = 21; i < 33; i+=2 )
			seq.addEvent( new Sequencer.SequencerEvent( 
				new MeasuresBeatsTicksTimecode( 140,i,0,0 ), tb2205 ) );

		// Run the sequencer (and the audio player which is the time keeper)
		seq.run();
	}

	/**
	 * 	Returns a sequenced action that will play a given sound file on
	 * 	the given mixer.
	 * 
	 *	@param soundFile The sound file to play
	 *	@param am The mixer to play it on
	 *	@return A {@link SequencedAction}
	 */
	private Sequencer.SequencedAction getAction( final String soundFile, 
			final AudioMixer am )
	{
		return new Sequencer.SequencedAction()
		{
			@Override
			public boolean performAction()
			{
				final XuggleAudio xa5 = new XuggleAudio(
					AudioMixer.class.getResource( soundFile ) );
				am.addStream( xa5, 0.3f );
				return true;
			}
		};
	}

	/** The image we'll draw into */
	private MBFImage img = null;
	
	/**
	 * 	Class that will render the channels in VU meters when
	 * 	a mix event occurs.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *	
	 *	@created 29 Nov 2011
	 */
	private class VURenderer implements MixEventListener
	{
		/** Processor to get RMS */
		private final EffectiveSoundPressure rms = new EffectiveSoundPressure();
		
		/** The beat detector processor */
		private BeatDetector beatDetector = null;
		
		/** 0db in our RMS range */
		private final double ref = 82;
		
		/** db value where we go red on the VU */
		private final double redAbove = 0.8;
		
		/** Size of each VU block */
		private final int blockPadding = 4;
		private final int blockHeight = 6;
		private final int blockSize = this.blockHeight + this.blockPadding;
		
		/** Width of the VU Display */
		private final int blockWidth = 30;
		
		/** Space between each VU */
		private final int padding = 10;
		
		/** Gap between channel VUs */
		private final int intraChannelGap = 2;
		
		/** Size of total VU meter */
		private final int vuSize = 300;
		
		/** Where we start drawing the VU meters */
		private final int yOffset = 350;
		
		/** Where we start drawing the VU Meters */
		private final int xOffset = 50;
		
		private final Float[] colourAbove = RGBColour.RED;
		private final Float[] colourBelow = RGBColour.GREEN;

		/** A hysteresis for the beat detector lights */
		private final int[] beatDetectorLightCount = new int[10];
		
		/**
		 * 	Instantiate the VU renderer.
		 *	@param af The audio format of the mixer
		 */
		public VURenderer( final AudioFormat af )
		{
			this.beatDetector = new BeatDetector( af );
		}
		
		/**
		 *	{@inheritDoc}
		 * 	@see org.openimaj.audio.AudioMixer.MixEventListener#mix(org.openimaj.audio.samples.SampleBuffer[], org.openimaj.audio.samples.SampleBuffer)
		 */
		@Override
		public void mix( final SampleBuffer[] channels, final SampleBuffer mix )
		{
			// Really, the drawing should be done in another thread and the
			// samples which are being drawn buffered. However, that would
			// somewhat complicate matters, so we're just going to try and
			// draw the VU meters between each mix of the mixer. That means
			// the VUs will get a bit flickery and it's possible that if this
			// process takes too long the mixer will start to stutter.
			
			AudioMixerDemo.this.img.zero();
			
			for( int i = 0; i < channels.length; i++ )
			{
				try
				{
					final int redAboveY = (int)(this.yOffset - this.redAbove*this.vuSize);
					final int x = this.xOffset + i * (this.blockWidth+this.padding);
					final int nc = channels[i].getFormat().getNumChannels();
					for( int c = 0; c < nc; c++ )
					{
						// Convert the samples into a dB value
						this.rms.process( channels[i].getSampleChunk(c) );
						double d = 6/Math.log(2)*
							Math.log(this.rms.getEffectiveSoundPressure())-this.ref;
						d = Math.exp(Math.log(1.055)*d);
	
						// Draw the VU Meters
						for( int y = this.yOffset; y > this.yOffset-(d*this.vuSize); y -= this.blockSize )
							AudioMixerDemo.this.img.drawShapeFilled( 
								new Rectangle( x+(c*this.blockWidth/nc), y-this.blockHeight, 
									this.blockWidth/nc-this.intraChannelGap, this.blockHeight ), 
								y < redAboveY? this.colourAbove : this.colourBelow );
					}
						
					// Do beat detection and show the beats as a yellow
					// light underneath the VUs
					if( this.beatDetectorLightCount[i] <= 0 )
					{
						this.beatDetector.process( channels[i].getSampleChunk() );
						if( this.beatDetector.beatDetected() )
							this.beatDetectorLightCount[i] = 20;
					}
					
					// The light count is a hysteresis for the lights so that
					// they stay on for longer than the pulse detected as a beat.
					if( this.beatDetectorLightCount[i]-- >= 0 )
						AudioMixerDemo.this.img.drawShapeFilled( 
							new Rectangle( x, this.yOffset+4, this.blockWidth, 15 ),
							RGBColour.YELLOW );
					else
						AudioMixerDemo.this.img.drawShapeFilled( 
								new Rectangle( x, this.yOffset+4, this.blockWidth, 15 ),
								new Float[]{0.2f,0.2f,0f} );
					
					DisplayUtilities.displayName( AudioMixerDemo.this.img, "VU Meters" );
				}
				catch( final Exception e )
				{
					e.printStackTrace();
				}
			}
		}		
	}
	
	/**
	 * 	Default main
	 *  @param args Command-line arguments
	 */
	public static void main(final String[] args) 
	{
		new AudioMixerDemo();
	}
}
