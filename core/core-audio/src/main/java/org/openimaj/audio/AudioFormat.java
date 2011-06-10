/**
 * 
 */
package org.openimaj.audio;


/**
 *	This class encapsulates the information that determines the format
 *	of audio data.
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 8 Jun 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioFormat
{
	/** The number of channels for each sample */
	private int nChannels = 0;
	
	/** The number of bits in each sample */
	private int nBits = 0;
	
	/** The sample rate in KHz */
	private double sampleRateKHz = 0;
	
	/** Whether the data is signed */
	private boolean isSigned = true;
	
	/** Whether the data is big-endian */
	private boolean isBigEndian = false;
	
	/**
	 * 	Construct a new audio format object with the
	 * 	given number of bits and sample rate.
	 * 
	 *	@param nBits The number of bits in each sample
	 *	@param sampleRate The sample rate in kilohertz
	 *	@param nChannels The number of channels
	 */
	public AudioFormat( int nBits, double sampleRate, int nChannels )
	{
		this.nBits = nBits;
		this.sampleRateKHz = sampleRate;
		this.setNumChannels( nChannels );
	}

	/**
	 * 	Get the number of bits in each sample. 
	 *	@return The number of bits in each sample.
	 */
	public int getNBits()
	{
		return nBits;
	}

	/**
	 * 	Get the rate at which the audio should be replayed
	 *	@return The audio sample rate in kilohertz.
	 */
	public double getSampleRateKHz()
	{
		return sampleRateKHz;
	}

	/**
	 * 	Set the number of channels in this format.
	 *	@param nChannels the number of channels
	 */
	public void setNumChannels( int nChannels )
	{
		this.nChannels = nChannels;
	}

	/**
	 * 	Get the number of channels in this format.
	 *	@return the number of channels
	 */
	public int getNumChannels()
	{
		return nChannels;
	}

	/**
	 * 	Set whether the data is signed or not.
	 *	@param isSigned Whether the data is signed.
	 */
	public void setSigned( boolean isSigned )
	{
		this.isSigned = isSigned;
	}

	/**
	 * 	Returns whether the data is signed or unsigned.
	 *	@return TRUE if the data is signed; FALSE otherwise;
	 */
	public boolean isSigned()
	{
		return isSigned;
	}

	/**
	 * 	Set whether the data is big-endian or not.
	 *	@param isBigEndian Whether the data is big-endian
	 */
	public void setBigEndian( boolean isBigEndian )
	{
		this.isBigEndian = isBigEndian;
	}

	/**
	 * 	Returns whether the data is big or little endian.
	 *	@return TRUE if the data is big-endian; FALSE if little-endian
	 */
	public boolean isBigEndian()
	{
		return isBigEndian;
	}
	
	/**
	 *	@inheritDoc
	 * 	@see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[Audio: "+getSampleRateKHz()+"KHz, "+getNBits()+"bit, "+
			getNumChannels()+" channel"+(getNumChannels()>1?"s":"")+
			", "+(isSigned?"signed":"unsigned")+", "+
			(isBigEndian?"big-endian":"little-endian")+"]";
	}
	
	/**
	 *	@inheritDoc
	 * 	@see java.lang.Object#clone()
	 */
	@Override
	public AudioFormat clone()
	{
		AudioFormat af = new AudioFormat( 
				getNBits(), getSampleRateKHz(), getNumChannels() );
		af.setBigEndian( isBigEndian );
		af.setSigned( isSigned );
		return af;
	}
}
