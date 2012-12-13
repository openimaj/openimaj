/**
 *
 */
package org.openimaj.audio.features;

import java.io.PrintWriter;
import java.util.List;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.filters.MelFilterBank;
import org.openimaj.audio.filters.TriangularFilter;

import Jama.Matrix;

/**
 *
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Dec 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MFCC
{
	private final int nCoeffs = 20;
	private int nFilters = 40;
	private double lowFreqHz = 20;
	private double highFreqHz = 16000;
	private final MelFilterBank filterBank;
	private final List<TriangularFilter> filters;

	/**
	 *
	 */
	public MFCC()
    {
		this( 40, 20, 16000 );
    }

	/**
	 *	@param nFilters
	 *	@param lowHz
	 *	@param highHz
	 */
	public MFCC( final int nFilters, final double lowHz, final double highHz )
	{
		this.lowFreqHz = lowHz;
		this.highFreqHz = highHz;
		this.nFilters = nFilters;
		this.filterBank = new MelFilterBank( this.nFilters, this.lowFreqHz, this.highFreqHz );
		this.filters = this.filterBank.getFilters();
	}

	/**
	 *	@param sc
	 *	@return the MFCCs
	 */
	public double[][] calculateMFCC( final SampleChunk sc )
	{
		return null;
	}

	/**
	 *	@param samples
	 * 	@param af
	 *	@return the MFCCs
	 */
	public double[][] calculateMFCC( final double[][] samples, final AudioFormat af )
	{
		final double[][] mfccs = new double[samples.length][this.nFilters];
		for( int i = 0; i < samples.length; i++ )
			mfccs[i] = this.calculateMFCC( samples[i], af );
		return mfccs;
	}

	/**
	 *	@param samples
	 * 	@param af
	 *	@return the MFCCs
	 */
	public double[] calculateMFCC( final double[] samples, final AudioFormat af )
	{
		// The power spectrum is expected to include the wrap around
		final Matrix mfccFilterWeights = new Matrix( this.nFilters, samples.length/2+1 );

		// Calculate the MFCC filter weights for all the filters at all
		// the frequency bins in the spectrum
		for( int filter = 0; filter < mfccFilterWeights.getRowDimension(); filter++ )
		{
			for( int specBin = 0; specBin < mfccFilterWeights.getColumnDimension(); specBin++ )
			{
				final double f = (af.getSampleRateKHz()*1000/samples.length)*specBin;
				final double w = this.filters.get(filter).getWeightAt( f );

				mfccFilterWeights.set( filter, specBin, w );
			}
		}

		// TODO: this looks fault atm
		// Calculate a simple DCT Matrix
		final Matrix dct = new Matrix( this.nFilters, this.nCoeffs );
		final double norm = 1d/Math.sqrt(this.nFilters/2);
		final double cosNorm = Math.PI/2d/this.nFilters;
		for( int filter = 0; filter < dct.getRowDimension(); filter++ )
		{
			for( int coeff = 0; coeff < dct.getColumnDimension(); coeff++ )
			{
				final double c = norm * Math.cos(coeff*cosNorm) * 2*filter;
				dct.set( filter, coeff, c );
				System.out.println( ""+filter+","+coeff+" = "+dct.get( filter, coeff ) );
			}
		}

		System.out.println( "DCT: "+dct.get(0,0) );
		System.out.println( dct.getRowDimension()+"x"+dct.getColumnDimension() );
		dct.print( new PrintWriter( System.err ), 1, 1 );

		final double[] mfccs = new double[this.nFilters];
		return mfccs;
	}
}
