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
package org.openimaj.audio.util;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.openimaj.audio.AudioDevice;
import org.openimaj.audio.AudioFormat;

/**
 *	Various static methods for dealing with audio information and data.	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 *	@created 26 Nov 2011
 */
public class AudioUtils
{
	/** When looking for devices, these are the sample rates we'll try */
	static final double[] freqsToTry = new double[] 
	                {11.025, 22.05, 44.1, 48, 96.1, 192};
	
	/** When looking for devices, these are the bits per sample we'll try */
	static final int[] bitsToTry = new int[] {8,16,24,32};
	
	/** When looking for devices, these are the number of channels we'll try */
	static final int[] chansToTry = new int[] {1,2,4,5,7,8};
	
	/**
	 * 	Returns a list of devices that are available on this system.
	 *	@return The list of devices available on this system.
	 */
	static public List<AudioDevice> getDevices()
	{
		final List<AudioDevice> l = new ArrayList<AudioDevice>();
		final Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		 
		for(int i = 0; i < mixerInfo.length; i++)
		{
			l.add( new AudioDevice( mixerInfo[i].getName(),
					mixerInfo[i].getDescription() ) );
		}
		
		return l;
	}
	
	/**
	 * 	Returns a Java sound line for the given device name. Use 
	 * 	{@link AudioDevice#deviceName} as input to this method. Use
	 * 	{@link AudioUtils#getDevices()} to get an {@link AudioDevice} object.
	 * 
	 *	@param deviceName The device name.
	 *  @param af The format 
	 *	@return A Java sound line.
	 * 	@throws LineUnavailableException 
	 */
	static public SourceDataLine getJavaOutputLine( final String deviceName, 
			final AudioFormat af ) 
		throws LineUnavailableException
	{
		final Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		for( final Mixer.Info info: mixerInfo )
		{
			if( info.getName().equals( deviceName ) )
			{
				final Mixer m = AudioSystem.getMixer(info);
				if( m.getSourceLineInfo().length > 0 )
					return (SourceDataLine)AudioSystem.getLine( 
							m.getSourceLineInfo()[0] );
			}
		}
		
		return null;
	}
	
	/**
	 * 	Gets a Java output line (SourceDataLine) that can play something with
	 * 	the given audio format.
	 * 
	 *	@param af The audio format.
	 *	@return A SourceDataLine
	 *	@throws LineUnavailableException
	 */
	static public SourceDataLine getAnyJavaOutputLine( final AudioFormat af ) 
		throws LineUnavailableException
	{
		// Convert the OpenIMAJ audio format to a Java Sound audio format object
		final javax.sound.sampled.AudioFormat audioFormat = af.getJavaAudioFormat();
		
		// Create info to create an output data line
		final DataLine.Info info = new DataLine.Info(	
				SourceDataLine.class, audioFormat );
		
		// Get the output line to write to using the given
		// sample format we just created.
		return (SourceDataLine) AudioSystem.getLine( info );
	}
	
	/**
	 * 	Converts a frequency to an approximation of a Mel frequency.
	 * 	This formula gives a close approximation for frequencies less than
	 * 	1000Hz.
	 *  
	 *	@param freq The frequency to convert
	 *	@return The Mel frequency
	 */
	static public double frequencyToMelFrequency( final double freq )
	{
		return (1127d * Math.log( 1 + freq/700d ) );
//		return (2595d * Math.log10(1 + freq/700d) );
	}
	
	/**
	 * 	Converts a Mel frequency back into an approximation of a frequency.
	 * 
	 *	@param melFreq The Mel frequency to convert
	 *	@return The frequency
	 */
	static public double melFrequencyToFrequency( final double melFreq )
	{
		return (700d * Math.exp( melFreq/1127d ) -700d );
//		 return (700d * (Math.pow(10, melFreq/2595d) - 1) );
	}
	
	/**
	 * 	Converts a frequency to a Bark frequency
	 * 
	 *	@param freq The frequency to convert
	 *	@return The Bark frequency
	 */
	static public double frequencyToBarkFrequency( final double freq )
	{
		return 6*Math.log( (freq/600d) + Math.sqrt(Math.pow(freq/600d,2) + 1 ) );
	}

	/**
	 * 
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		try
		{
			System.out.println( AudioUtils.getDevices() );
			System.out.println( AudioUtils.getJavaOutputLine( "Line 1/2 (M-Audio Delta 44)", 
					new AudioFormat( 16, 44.1, 2 ) ) );
		}
		catch( final LineUnavailableException e )
		{
			e.printStackTrace();
		}
	}
}
