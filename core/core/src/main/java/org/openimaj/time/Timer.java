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
package org.openimaj.time;

/**
 * Timer instances let you track time from start to end of some 
 * process. The time granularity is in milliseconds.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class Timer {
	private long start = 0;
	private long end = 0;
	
	/**
	 * Create a timer. The timer will not start unit {@link #start()} is called.
	 */
	public Timer() {
	}

	/**
	 * @return instantiate and start a new {@link Timer} instance.
	 */
	public static Timer timer() {
		Timer timer = new Timer();
		
		timer.start();
		
		return timer;
	}

	/**
	 * Get the duration of the timer in milliseconds. If the timer has
	 * been stopped then this is how long the timer ran for. If the
	 * timer is still running, then this is the time between it starting
	 * and now.
	 * 
	 * @return the duration of the timer in milliseconds
	 */
	public long duration() {
		long e = end == 0 ? System.currentTimeMillis() : end;
		
		return e - start;
	}

	/**
	 * Start the timer
	 */
	public void start() {
		this.start = System.currentTimeMillis();
	}

	/**
	 * Stop the timer
	 */
	public void stop() {
		this.end = System.currentTimeMillis();
	}
}