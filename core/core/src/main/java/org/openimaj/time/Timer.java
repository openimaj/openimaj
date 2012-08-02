package org.openimaj.time;

/**
 * Timer instances let you track time from start to end of some process
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Timer{
	private Timer(){};
	/**
	 * @return instantiate and call {@link #start()} on a new {@link Timer} instance
	 */
	public static Timer timer(){
		Timer timing = new Timer();
		timing.start();
		return timing;
	}
	private long start;
	private long end;
	/**
	 * @return end - start
	 */
	public long duration(){
		return end - start;
	}
	
	/**
	 * start the timer
	 */
	public void start(){
		this.start = System.currentTimeMillis();
	}
	/**
	 * end the timer
	 */
	public void end(){
		this.end = System.currentTimeMillis();
	}
}