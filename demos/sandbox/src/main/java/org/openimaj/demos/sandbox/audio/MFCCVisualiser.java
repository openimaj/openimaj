/**
 * 
 */
package org.openimaj.demos.sandbox.audio;

import java.io.File;
import java.util.Arrays;

import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.analysis.MFCC;
import org.openimaj.audio.conversion.MultichannelToMonoProcessor;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.vis.general.BarVisualisation;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 26 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MFCCVisualiser
{
	/**
	 *	@param args
	 * @throws InterruptedException 
	 */
	public static void main( String[] args ) throws InterruptedException
	{
		// Setup a thread grabbing audio
//		JavaSoundAudioGrabber a = new JavaSoundAudioGrabber();
//		a.setFormat( new AudioFormat( 16, 96.1, 1 ) );
//		a.setMaxBufferSize( 1024 );
//		new Thread( a ).start();
		XuggleAudio xa = new XuggleAudio( new File("heads1.mpeg") );
		MultichannelToMonoProcessor a = new MultichannelToMonoProcessor( xa );

		// Setup a visualisation
		BarVisualisation bv = new BarVisualisation( 1500, 500 );
		bv.setDrawValues( false );
		bv.showWindow( "MFCCs" );
		
		// Setup an MFCC processor
		MFCC mfcc = new MFCC();
		
		// Read sample chunks
		SampleChunk sc = null;
		while( (sc = a.nextSampleChunk()) != null )
		{
			bv.getVisualisationImage().zero();
			float[][] mfccs = mfcc.calculateMFCC( sc );
			
			System.out.println( Arrays.deepToString( mfccs ) );
			bv.setData( ArrayUtils.floatToDouble( mfccs[0] ) );

			Thread.sleep( 20000 );
		}
	}
}
