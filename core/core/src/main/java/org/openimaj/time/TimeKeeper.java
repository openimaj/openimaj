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
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
 *	@created 28 Nov 2011
 */
public interface TimeKeeper<T extends Timecode> extends Runnable
{
	/**
	 * 	Use this method to start the time keeper running.
	 * 
	 *	@inheritDoc
	 * 	@see java.lang.Runnable#run()
	 */
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
