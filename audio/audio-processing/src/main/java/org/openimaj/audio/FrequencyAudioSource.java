package org.openimaj.audio;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

public class FrequencyAudioSource extends AudioProcessor implements Runnable{
	
	public static interface Listener{

		public void consumeFrequency(float[] fftReal, float[] fftImag, int low, int high);
		
	}

	private FourierTransform fftProc;
	private List<IndependentPair<Listener, Pair<Integer>>> listeners;
	private float[] fftReal;
	private float[] fftImag;
	
	
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
		// Draw the spectra
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

	public void addFrequencyListener(Listener l) {
		Pair<Integer> range = null;
		this.listeners.add(IndependentPair.pair(l,range));
	}
	public void addFrequencyListener(Listener l, Pair<Integer> requestFrequencyRange) {
		this.listeners.add(IndependentPair.pair(l,requestFrequencyRange));
	}
}
