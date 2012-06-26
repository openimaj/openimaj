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
 * A {@link ValueAnimator} that can wrap another
 * {@link ValueAnimator} to produce looping behavior by
 * resetting when the animator has finished.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> Type produce by animator
 */
public class LoopingValueAnimator<T> implements ValueAnimator<T> {
	ValueAnimator<T> animator;
	
	/**
	 * Construct around the given {@link ValueAnimator} to
	 * provide looping behavior.
	 * @param animator animator to loop
	 */
	public LoopingValueAnimator(ValueAnimator<T> animator) {
		this.animator = animator;
	}
	
	@Override
	public T nextValue() {
		if (animator.hasFinished())
			animator.reset();

		return animator.nextValue();
	}

	@Override
	public boolean hasFinished() {
		return false;
	}

	@Override
	public void reset() {
		animator.reset();
	}
	
	/**
	 * Construct a new {@link LoopingValueAnimator} from a {@link ValueAnimator}.
	 * 
	 * @param <T> Type produce by animator
	 * @param animator animator to loop
	 * @return new {@link LoopingValueAnimator}
	 */
	public static <T> LoopingValueAnimator<T> loop(ValueAnimator<T> animator) {
		return new LoopingValueAnimator<T>(animator);
	}
}
