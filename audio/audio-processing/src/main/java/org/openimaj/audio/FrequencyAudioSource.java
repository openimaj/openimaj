package org.openimaj.audio;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.audio.processor.AudioProcessor;

public class FrequencyAudioSource extends AudioProcessor implements Runnable{
	
	public static interface Listener{

		public void consumeFrequency(float[] fft, int sampleChunkSize, double binSize);
		
	}

	private FourierTransform fftProc;
	private List<Listener> listeners;
	
	
	public FrequencyAudioSource(AudioStream stream) {
		super(stream);
		fftProc = new FourierTransform();
		this.listeners = new ArrayList<Listener>();
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
		for(Listener l : listeners){
			l.consumeFrequency(fft,sample.getNumberOfSamples(),binSize);
		}
	}

	@Override
	public void run() {
		while(true){
			try
	        {
		        Thread.sleep( 500 );
		        SampleChunk s = null;
		        while( (s = nextSampleChunk()) != null )
		        	process( s );
	        }
	        catch( InterruptedException e )
	        {
		        e.printStackTrace();
	        } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addFrequencyListener(Listener l) {
		this.listeners.add(l);
	}
}
