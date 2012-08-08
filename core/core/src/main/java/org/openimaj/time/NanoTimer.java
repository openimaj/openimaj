package org.openimaj.time;

/**
 * Timer instances let you track time from start to end of some 
 * process. The time granularity is in nanoseconds.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class NanoTimer {
	private long start = 0;
	private long end = 0;
	
	/**
	 * Create a timer. The timer will not start unit {@link #start()} is called.
	 */
	public NanoTimer() {
	}

	/**
	 * @return instantiate and start a new {@link NanoTimer} instance.
	 */
	public static NanoTimer timer() {
		NanoTimer timer = new NanoTimer();
		
		timer.start();
		
		return timer;
	}

	/**
	 * Get the duration of the timer in nanoseconds. If the timer has
	 * been stopped then this is how long the timer ran for. If the
	 * timer is still running, then this is the time between it starting
	 * and now.
	 * 
	 * @return the duration of the timer in nanoseconds
	 */
	public long duration() {
		long e = end == 0 ? System.nanoTime() : end;
		
		return e - start;
	}

	/**
	 * Start the timer
	 */
	public void start() {
		this.start = System.nanoTime();
	}

	/**
	 * Stop the timer
	 */
	public void stop() {
		this.end = System.nanoTime();
	}
}