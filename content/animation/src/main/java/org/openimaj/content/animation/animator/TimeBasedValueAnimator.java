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
package org.openimaj.content.animation.animator;

/**
 *	An animator that will animate a value over a given time period. The animator
 *	is constructed using a specific time period and on the first call of the
 *	makeNextValue() will start a timer. 
 *	<p>
 *	Subclasses must implement a method that
 *	can return a value based on an absolute percentage value. This class will 
 *	convert the time each nextValue() is called into a percentage value between
 *	the start and end times and use that to calculate the absolute value of
 *	the animator.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 14 Aug 2012
 *	@version $Author$, $Revision$, $Date$
 * 	@param <T> The type of value produced 
 */
public abstract class TimeBasedValueAnimator<T> implements ValueAnimator<T>
{
	/** The time the animation started */
	private long startTime = 0;
	
	/** The length of the animation */
	private long animationLength = 0;
	
	/** The start value of the animation */
	protected T startValue = null;
	
	/** The end/target value of the animation */
	protected T endValue = null;
	
	/** The current percentage */
	private double currentPC = 0;
	
	/**
	 *	@param initial The start value for the animator
	 * 	@param end The end value for the animator
	 * 	@param millis The length of time the animation should run
	 */
	public TimeBasedValueAnimator( final T initial, final T end, final long millis )
	{
		this.startValue = initial;
		this.endValue = end;
		this.animationLength = millis;
	}

	/**
	 * 	Given a percentage value (0 < pc <=1), the method should return a value
	 * 	for the animation.
	 * 
	 *	@param pc the percentage value
	 *	@return The animator value
	 */
	protected abstract T calculateValue( double pc );

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.content.animation.animator.ValueAnimator#nextValue()
	 */
	@Override
	public T nextValue()
	{		
		final long currentTime = System.currentTimeMillis(); 
		if( this.startTime == 0 )
			this.startTime = currentTime;
		
		this.currentPC = (currentTime - this.startTime) / (double)this.animationLength;

		if( this.isComplete() ) return this.endValue;

		return this.calculateValue( this.currentPC );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.content.animation.animator.ValueAnimator#reset()
	 */
	@Override
	public void reset()
	{
		this.startTime = 0;
		this.currentPC = 0;
	}

	/**
	 * 	Returns whether the animator has completed
	 *	@return TRUE if the animator has completed.
	 */
	public boolean isComplete()
	{
		return this.currentPC >= 1.0;
	}

	/**
	 * 	A implementation sugar for {@link #isComplete()}
	 * 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.content.animation.animator.ValueAnimator#hasFinished()
	 */
	@Override
	public boolean hasFinished()
	{
		return this.isComplete();
	}
}
