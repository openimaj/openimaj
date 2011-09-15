package org.openimaj.demos;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.video.xuggle.XuggleVideo;

public class MultiTouchSurface implements Runnable {
	private XuggleVideo stream;
	private Thread monitorThread;

	MultiTouchSurface(){
		String sourceURL = "http://152.78.64.19:8080/foo";
		stream = new XuggleVideo(sourceURL);
	}

	private void monitor() {
		this.monitorThread = new Thread(this);
		monitorThread.start();
	}
	
	public static void main(String args[]){
		MultiTouchSurface surface = new MultiTouchSurface();
		surface.monitor();
	}

	@Override
	public void run() {
		for(MBFImage image : this.stream){
			displayFrame(image);
//			trackInputs(image);
//			handleInputs(image);
		}
	}

	private void displayFrame(MBFImage image) {
		DisplayUtilities.displayName(image, "inputFrame");
	}
}
