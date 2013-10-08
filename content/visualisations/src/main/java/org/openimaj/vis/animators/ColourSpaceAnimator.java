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
package org.openimaj.vis.animators;

import org.openimaj.content.animation.animator.LinearTimeBasedFloatValueAnimator;
import org.openimaj.content.animation.animator.ValueAnimator;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

import cern.colt.Arrays;

/**
 *	An animator that moves through a colour space from one colour to another.
 *	Start and end colours are provided in RGB. The colour space through which the
 *	animator moves can be defined - the default is RGB. Works only with 3-dimensional
 *	colour spaces.
 *
 * 	@author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 	@created 26 Jun 2013
 */
public class ColourSpaceAnimator implements ValueAnimator<Float[]>
{
	private final LinearTimeBasedFloatValueAnimator c1;
	private final LinearTimeBasedFloatValueAnimator c2;
	private final LinearTimeBasedFloatValueAnimator c3;

	/** 1x1 Buffer required for the colour space transform functions */
	private final MBFImage buffer;

	/** The colour space being used */
	private ColourSpace colourspace = ColourSpace.RGB;

	/**
	 *	Construct the animator using the start and end colours and a duration (milliseconds)
	 *	@param start Start colour (RGB)
	 *	@param end End colour (RGB)
	 *	@param duration The duration (milliseconds)
	 */
	public ColourSpaceAnimator( final Float[] start, final Float[] end, final long duration )
	{
		final MBFImage startImg = new MBFImage( 1, 1, 3 );
		startImg.setPixel( 0, 0, start );
		final MBFImage endImg = new MBFImage( 1, 1, 3 );
		this.buffer = new MBFImage( 1, 1, 3 );
		endImg.setPixel( 0, 0, end );

		System.out.println( "In buffer: "+Arrays.toString( end ) );
		System.out.println( "In buffer: "+Arrays.toString( endImg.getPixel( 0,0 ) ) );

		final Float[] labstart = this.colourspace.convertFromRGB( startImg ).getPixel( 0, 0 );
		final Float[] labend = this.colourspace.convertFromRGB( endImg ).getPixel( 0, 0 );

		this.c1 = new LinearTimeBasedFloatValueAnimator( labstart[0], labend[0], duration );
		this.c2 = new LinearTimeBasedFloatValueAnimator( labstart[1], labend[1], duration );
		this.c3 = new LinearTimeBasedFloatValueAnimator( labstart[2], labend[2], duration );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.content.animation.animator.ValueAnimator#nextValue()
	 */
	@Override
	public Float[] nextValue()
	{
		this.buffer.setPixel( 0, 0, new Float[]
		{ this.c1.nextValue(), this.c2.nextValue(), this.c3.nextValue() } );
		final Float[] retPix = this.colourspace.convertToRGB( this.buffer ).getPixel( 0, 0 );
		return retPix;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.content.animation.animator.ValueAnimator#hasFinished()
	 */
	@Override
	public boolean hasFinished()
	{
		return this.c1.hasFinished() && this.c2.hasFinished() && this.c3.hasFinished();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.content.animation.animator.ValueAnimator#reset()
	 */
	@Override
	public void reset()
	{
		this.c1.reset();
		this.c2.reset();
		this.c3.reset();
	}

	/**
	 *	@return the colourspace
	 */
	public ColourSpace getColourspace()
	{
		return this.colourspace;
	}

	/**
	 *	@param colourspace the colourspace to set
	 */
	public void setColourspace( final ColourSpace colourspace )
	{
		this.colourspace = colourspace;
	}
}
