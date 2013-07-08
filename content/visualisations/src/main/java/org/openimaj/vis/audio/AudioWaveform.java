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
package org.openimaj.vis.audio;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.samples.FloatSampleBuffer;
import org.openimaj.audio.samples.SampleBuffer;
import org.openimaj.image.MBFImage;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.vis.VisualisationImpl;

/**
 *	A visualisation for signals. It utilises the {@link SampleBuffer} class
 *	to store the samples to be displayed. It will display multi-channel signals
 *	as given by the audio format of the SampleBuffer.
 *	<p>
 *	A method for accepting sample chunks is implemented so that a "live" display
 *	of audio waveform can be displayed.
 *	<p>
 *	If you prefer to display an overview of the complete audio of a resource,
 *	use the {@link AudioOverviewVisualisation}.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 13 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class AudioWaveform extends VisualisationImpl<SampleBuffer>
{
	/** */
	private static final long serialVersionUID = 1L;

	/** Whether to have a decay on the waveform */
	private boolean decay = false;

	/** The decay amount if decay is set to true */
	private float decayAmount = 0.3f;

	/** The maximum signal value */
	private float maxValue = 100f;

	/** The scalar in the x direction */
	private float xScale = 1f;

	/** Whether to automatically determine the x scalar */
	private boolean autoFit = true;

	/** Whether to automatically determine the y scalar */
	private boolean autoScale = true;

	/** The colour to draw the waveform */
	private Float[] colour = new Float[]{1f,1f,1f,1f};

	/**
	 *	Create an audio waveform display of the given width and height
	 *	@param w The width of the image
	 *	@param h The height of the image
	 */
	public AudioWaveform( final int w, final int h )
	{
		super( w, h );
	}

	/**
	 * 	Create an audio waveform that overlays the given visualisation.
	 * 	@param v The visualisation to overlay
	 */
	public AudioWaveform( final VisualisationImpl<?> v )
	{
		super( v );
	}

	/**
	 * 	Draw the given sample chunk into an image and returns that image.
	 * 	The image is reused, so if you want to keep it you must clone
	 * 	the image afterwards.
	 *
	 * 	@param sb The sample chunk to draw
	 * 	@return The image drawn
	 */
	public MBFImage drawWaveform( final SampleBuffer sb )
	{
		synchronized( this.visImage )
		{
			// If decay is set we lower the values of the data already in place
			if( !this.clearBeforeDraw && this.decay )
				this.visImage.multiplyInplace( this.decayAmount );

			// Work out the y scalar
			float m = this.maxValue;
			if( this.autoScale )
				m = (float)Math.max(
						Math.abs( ArrayUtils.minValue( sb.asDoubleArray() ) ),
						Math.abs( ArrayUtils.maxValue( sb.asDoubleArray() ) ) );

			final int nc = sb.getFormat().getNumChannels();
			final int channelHeight = this.visImage.getHeight()/nc;
			final float scalar = this.visImage.getHeight() / (m*2*nc);
			final int h = this.getHeight();

			// Work out the xscalar
			if( this.autoFit )
				this.xScale = this.visImage.getWidth() / (sb.size()/(float)nc);

			// Plot the wave form
			for( int c = 0; c < nc; c++ )
			{
				final int yOffset = channelHeight * c + channelHeight/2;
				int lastX = 0;
				int lastY = yOffset;
				for( int i = 1; i < sb.size()/nc; i += nc )
				{
					final int x = (int)(i*this.xScale);
					final int y = (int)(sb.get(i*nc+c)/Integer.MAX_VALUE*scalar+yOffset);
					this.visImage.drawLine( lastX, lastY, x, h-y, this.colour );
					lastX = x;
					lastY = h-y;
				}
			}
		}

		return this.visImage;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.vis.VisualisationImpl#update()
	 */
	@Override
	public void update()
	{
		if( this.data != null )
			synchronized( this.data )
			{
				this.drawWaveform( this.data );
			}
	}

	/**
	 * 	If the samples are represented as a set of doubles, you can set them
	 * 	here. The assumed format will be a single channel at 44.1KHz.
	 *	@param samples The sample data.
	 */
	public void setData( final double[] samples )
	{
		final FloatSampleBuffer fsb = new FloatSampleBuffer( samples,
				new AudioFormat( -1, 44.1, 1 ) );
		super.setData( fsb );
	}

	/**
	 * 	Samples represented as a set of doubles.
	 *	@param samples The samples.
	 *	@param format The format of the samples
	 */
	public void setData( final double[] samples, final AudioFormat format )
	{
		final FloatSampleBuffer fsb = new FloatSampleBuffer( samples,
				format.clone().setNBits( -1 ) );
		super.setData( fsb );
	}

	/**
	 * 	Set the maximum value that the signal can achieve. This method also
	 * 	disables autoScale.
	 *
	 *	@param f The maximum that a signal can achieve.
	 */
	public void setMaximum( final float f )
	{
		this.maxValue = f;
		this.autoScale = false;
	}

	/**
	 * 	Get the maximum value in use
	 *	@return The maximum value
	 */
	public float getMaximum()
	{
		return this.maxValue;
	}

	/**
	 * 	Set the x-scale at which to draw the waveform. This method also disables
	 * 	auto fit.
	 *	@param f The scale at which to draw the waveform.
	 */
	public void setXScale( final float f )
	{
		this.xScale = f;
		this.autoFit = false;
	}

	/**
	 * 	Whether to auto fit the x-axis
	 *	@param tf TRUE to auto-fit the x-axis
	 */
	public void setAutoFit( final boolean tf )
	{
		this.autoFit = tf;
	}

	/**
	 * 	Whether to auto fit the y-axis
	 *	@param tf TRUE to auto-fit the y-axis
	 */
	public void setAutoScale( final boolean tf )
	{
		this.autoScale = tf;
	}

	/**
	 * 	Returns whether decay is set
	 *	@return TRUE if decay is set
	 */
	public boolean isDecay()
	{
		return this.decay;
	}

	/**
	 * 	Set whether decay is to be used
	 *	@param decay TRUE for decay, FALSE otherwise
	 */
	public void setDecay( final boolean decay )
	{
		this.decay = decay;
	}

	/**
	 * 	Get the amount of decay in use
	 *	@return The decay amount
	 */
	public float getDecayAmount()
	{
		return this.decayAmount;
	}

	/**
	 * 	Set the amount of decay to use
	 *	@param decayAmount the decay amount
	 */
	public void setDecayAmount( final float decayAmount )
	{
		this.decayAmount = decayAmount;
	}

	/**
	 * 	Set the colour to draw the signal
	 *	@param colour The colour
	 */
	public void setColour( final Float[] colour )
	{
		this.colour = colour;
	}
}
