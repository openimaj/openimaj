package org.openimaj.utils.threads;

public abstract class WatchedRunner implements Runnable{
	
	private long timeBeforeSkip;
	private boolean taskCompleted;
	private Thread runningThread;

	public WatchedRunner(long timeBeforeSkip) {
		this.timeBeforeSkip = timeBeforeSkip;
	}

	public abstract void doTask();
	
	@Override
	public void run() {
		this.taskCompleted = false;
		doTask();
		this.taskCompleted = true;
		synchronized (this) {
			this.notify();
		}
	}
	
	public void go(){
		runningThread = new Thread(this);
		
		try {
			synchronized (this) {
				runningThread.start();
				this.wait(this.timeBeforeSkip);
			}
		} catch (InterruptedException e) {
		}
		if(!this.taskCompleted) runningThread.interrupt();
	}

	public boolean taskCompleted() {
		return taskCompleted;
	}

}
