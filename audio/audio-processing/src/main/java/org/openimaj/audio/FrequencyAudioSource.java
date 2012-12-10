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
package org.openimaj.audio;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.analysis.FourierTransform;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

/**
 * {@link AudioProcessor} that provides frequency information.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class FrequencyAudioSource extends AudioProcessor implements Runnable {
	
	/**
	 * Interface for classes that listen to the frequency information
	 * extracted by the {@link FrequencyAudioSource}.
	 * 
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *
	 */
	public static interface Listener {

		/**
		 * Called when new frequency data is available.
		 * 
		 * @param fftReal real fft values
		 * @param fftImag imaginary fft values
		 * @param low
		 * @param high
		 */
		public void consumeFrequency(float[] fftReal, float[] fftImag, int low, int high);
		
	}

	private final FourierTransform fftProc;
	private final List<IndependentPair<Listener, Pair<Integer>>> listeners;
	private float[] fftReal;
	private float[] fftImag;
	
	
	/**
	 * Construct on top of given stream
	 * @param stream the stream
	 */
	public FrequencyAudioSource(final AudioStream stream) {
		super(stream);
		this.fftProc = new FourierTransform();
		this.listeners = new ArrayList<IndependentPair<Listener, Pair<Integer>>>();
		new Thread(this).start();
	}
	
	@Override
	public SampleChunk process(final SampleChunk sample) throws Exception {
		this.fftProc.process(sample);
		final float[] fft = this.fftProc.getLastFFT()[0];
		this.fireFrequencyEvent(fft,sample);
		return sample;
	}

	private void fireFrequencyEvent(final float[] fft,final SampleChunk sample) {
		final double binSize = (sample.getFormat().getSampleRateKHz()*1000) / (fft.length/2);
		if(this.fftReal == null || fft.length/4 != this.fftReal.length){
			this.fftReal = new float[fft.length/4];
			this.fftImag = new float[fft.length/4];
		}
		// Extract the spectra
		for( int i = 0; i < fft.length/4; i++ )
		{
			final float re = fft[i*2];
			final float im = fft[i*2+1];
			this.fftReal[i] = re;
			this.fftImag[i] = im;
		}
		for(final IndependentPair<Listener,Pair<Integer>> l : this.listeners){
			final Pair<Integer> range = l.secondObject();
			final int low = (int) (range.firstObject()/binSize);
			final int high = (int) (range.secondObject()/binSize);
			l.firstObject().consumeFrequency(this.fftReal,this.fftImag,low,high);
		}
	}

	@Override
	public void run() {
//		while(true){
			try
	        {
		        Thread.sleep( 500 );
		        SampleChunk s = null;
		        while( (s = this.nextSampleChunk()) != null ) {
		        	this.process( s );
		        }
	        }
	        catch( final InterruptedException e )
	        {
	        	e.printStackTrace();
	        } 
	        catch (final Exception e) 
	        {
	        	e.printStackTrace();
	        }
	        
//		}
	}

	/**
	 * Add a listener
	 * @param l the listener
	 */
	public void addFrequencyListener(final Listener l) {
		final Pair<Integer> range = null;
		this.listeners.add(IndependentPair.pair(l,range));
	}
	
	/**
	 * Add a listener
	 * @param l the listener
	 * @param requestFrequencyRange the range
	 */
	public void addFrequencyListener(final Listener l, final Pair<Integer> requestFrequencyRange) {
		this.listeners.add(IndependentPair.pair(l,requestFrequencyRange));
	}
}
