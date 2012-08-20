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
 *	{@link #run()} is called. TimeKeepers may run continuously, so it is important
 *	that timekeepers are run in threads. 
 *	<p><code>
 *		new Thread( timeKeeper ).start()
 *	</code></p> 
 *	For time keepers that do not run continuously
 *	this will incur an overhead, so if you know that your time keeper does not
 *	run continuously, you should control it closely; otherwise timekeepers of
 *	unknown type should be run in threads. If your timekeeper does run in a
 *	thread, then the stop() method should cause the timekeeper to exit the 
 *	run method.
 *	<p>
 *	The semantic difference between pause and stop is that pause is intended
 *	for short term stoppages in the running of the timekeeper that will result
 *	in the timekeeper being restarted either from the same position or from a
 *	newly seeked position. The stop method is expected to be called when the
 *	timekeeper is being shut down or will be required to start from the beginning
 *	again when restarted. Some timekeepers may not support mid-stream stopping
 *	and they should return false for the {@link #supportsPause()} method.
 *	Similarly, timekeepers that do not support seeking should return false
 *	for the {@link #supportsSeek()} method.
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
	 * 	Pause the running of the timekeeper.
	 */
	void pause();
	
	/**
	 * 	Use this method to stop the time keeper from running.
	 */
	void stop();
	
	/**
	 * 	Returns the current time.
	 *	@return the current time object.
	 */
	public T getTime();
	
	/**
	 * 	Seek to a given timestamp.
	 *	@param timestamp The timestamp to seek to
	 */
	public void seek( long timestamp ); 
	
	/**
	 * 	Reset the timekeeper.
	 */
	public void reset();
	
	/**
	 * 	Returns whether the timekeeper supports pausing. If the timekeeper
	 * 	supports pausing then a stop() followed by a run() will continue
	 * 	from where the timekeeper was paused. Use reset() inbetween to force
	 * 	the timekeeper to start from the beginning again.
	 *  
	 *	@return TRUE if the timekeeper supports pausing. 
	 */
	public boolean supportsPause();
	
	/**
	 * 	Returns whether the timekeeper supports seeking.
	 * 
	 *	@return TRUE if the timekeeper supports seeking.
	 */
	public boolean supportsSeek();
}
