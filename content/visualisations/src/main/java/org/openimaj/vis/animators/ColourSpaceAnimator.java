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
