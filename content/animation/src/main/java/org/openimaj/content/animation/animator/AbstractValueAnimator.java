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
package org.openimaj.content.animation.animator;

/**
 * Base class for objects capable of "animating" a value;
 * that is providing a new value everytime {@link #nextValue()} is
 * called, subject to some constraints.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> Type of value produced 
 */
public abstract class AbstractValueAnimator<T> implements ValueAnimator<T> {
	private T currentValue;
	
	/**
	 * Number of times {@link #nextValue()} has been called since construction or last reset
	 */
	private int currentCount = 0;
	
	private int startWait = 0;
	private int stopWait = 0;
	private int completedAt = -1;
	
	/**
	 * Construct with initial value
	 * @param initial initial value
	 * @param startWait amount of time in ticks to wait before starting animation.
	 * @param stopWait amount of time in ticks to wait after finishing animation.
	 */
	public AbstractValueAnimator(T initial, int startWait, int stopWait) {
		currentValue = initial;
		this.startWait = startWait;
		this.stopWait = stopWait;
	}
	
	/**
	 * Get the next value. If the animator has finished,
	 * the continuation is checked to see if there are
	 * more animators to run. If not, then the last value 
	 * is returned. 
	 * 
	 * @return the next value.
	 */
	@Override
	public T nextValue() {
		if (!(currentCount < startWait || hasFinished() || completedAt > 0)) {
			currentValue = makeNextValue();
		}
		
		currentCount++;
		
		return currentValue;
	}
	
	protected abstract T makeNextValue();
	
	protected abstract void resetToInitial();
	
	@Override
	public final void reset() {
		resetToInitial();
		currentCount = 0;
		completedAt = -1;
	}
	
	protected abstract boolean complete();
	
	@Override
	public final boolean hasFinished() {
		boolean comp = complete();
		
		if (!comp)
			return false;
		
		if (completedAt < 0)
			completedAt = currentCount;
		
		if (currentCount - completedAt < stopWait) 
			return false;
		
		return true;
	}
}
