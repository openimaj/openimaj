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
package org.openimaj.time;

/**
 *	A time keeper knows what time it is. You can ask the time keeper what
 *	time it is using the {@link #getTime()} method.
 *	<p>
 *	Subclasses must override the method to return appropriate time-based
 *	objects.  Note that the time keeper is passive - it does not let anyone
 *	know when certain times occur - it merely provides times when asked.
 *	<p>
 *	Initially the timekeeper should not be started but should only start when
 *	{@link #run()} is called. If it's important that the time keeper is started
 *	when constructed, you should call {@link #run()} in the constructor.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	
 * 	@param <T> The type of {@link Timecode}  
 *	@created 28 Nov 2011
 */
public interface TimeKeeper<T extends Timecode> extends Runnable
{
	/**
	 * 	Use this method to start the time keeper running.
	 * 
	 *	{@inheritDoc}
	 * 	@see java.lang.Runnable#run()
	 */
	@Override
	public void run();
	
	/**
	 * 	Use this method to stop the time keeper from running.
	 */
	void stop();
	
	/**
	 * 	Returns the current time.
	 *	@return the current time object.
	 */
	public T getTime();
}
