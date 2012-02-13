package org.openimaj.audio;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

/**
 * {@link AudioProcessor} that provides frequency information.
 * 
 * @author David Dupplaw <dpd@ecs.soton.ac.uk>
 *
 */
public class FrequencyAudioSource extends AudioProcessor implements Runnable {
	
	/**
	 * Interface for classes that listen to the frequency information
	 * extracted by the {@link FrequencyAudioSource}.
	 * 
	 * @author David Dupplaw <dpd@ecs.soton.ac.uk>
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

	private FourierTransform fftProc;
	private List<IndependentPair<Listener, Pair<Integer>>> listeners;
	private float[] fftReal;
	private float[] fftImag;
	
	
	/**
	 * Construct on top of given stream
	 * @param stream the stream
	 */
	public FrequencyAudioSource(AudioStream stream) {
		super(stream);
		fftProc = new FourierTransform();
		this.listeners = new ArrayList<IndependentPair<Listener, Pair<Integer>>>();
		new Thread(this).start();
	}
	
	@Override
	public SampleChunk process(SampleChunk sample) throws Exception {
		fftProc.process(sample);
		float[] fft = fftProc.getLastFFT();
		fireFrequencyEvent(fft,sample);
		return sample;
	}

	private void fireFrequencyEvent(float[] fft,SampleChunk sample) {
		double binSize = (sample.getFormat().getSampleRateKHz()*1000) / (fft.length/2);
		if(fftReal == null || fft.length/4 != fftReal.length){
			fftReal = new float[fft.length/4];
			fftImag = new float[fft.length/4];
		}
		// Extract the spectra
		for( int i = 0; i < fft.length/4; i++ )
		{
			float re = fft[i*2];
			float im = fft[i*2+1];
			fftReal[i] = re;
			fftImag[i] = im;
		}
		for(IndependentPair<Listener,Pair<Integer>> l : listeners){
			Pair<Integer> range = l.secondObject();
			int low = (int) (range.firstObject()/binSize);
			int high = (int) (range.secondObject()/binSize);
			l.firstObject().consumeFrequency(fftReal,fftImag,low,high);
		}
	}

	@Override
	public void run() {
//		while(true){
			try
	        {
		        Thread.sleep( 500 );
		        SampleChunk s = null;
		        while( (s = nextSampleChunk()) != null ) {
		        	process( s );
		        }
	        }
	        catch( InterruptedException e )
	        {
	        	e.printStackTrace();
	        } 
	        catch (Exception e) 
	        {
	        	e.printStackTrace();
	        }
	        
//		}
	}

	/**
	 * Add a listener
	 * @param l the listener
	 */
	public void addFrequencyListener(Listener l) {
		Pair<Integer> range = null;
		this.listeners.add(IndependentPair.pair(l,range));
	}
	
	/**
	 * Add a listener
	 * @param l the listener
	 * @param requestFrequencyRange the range
	 */
	public void addFrequencyListener(Listener l, Pair<Integer> requestFrequencyRange) {
		this.listeners.add(IndependentPair.pair(l,requestFrequencyRange));
	}
}
