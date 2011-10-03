package org.openimaj.demos;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.video.xuggle.XuggleVideo;

public class MultiTouchSurface implements Runnable {
	private XuggleVideo stream;
	private Thread monitorThread;
	private OtsuThreshold thresholder;

	MultiTouchSurface(){
		String sourceURL = "http://152.78.64.19:8080/foo";
		stream = new XuggleVideo(sourceURL);
	}

	private void monitor() {
		this.monitorThread = new Thread(this);
		this.thresholder = new OtsuThreshold();
		monitorThread.start();
	}
	
	public static void main(String args[]){
		MultiTouchSurface surface = new MultiTouchSurface();
		surface.monitor();
	}

	@Override
	public void run() {
		for(MBFImage image : this.stream){
			FImage gimage = Transforms.calculateIntensityNTSC(image);
//			gimage.processInline(this.thresholder);
			gimage.threshold(0.7f);
			displayFrame(gimage);
//			trackInputs(image);
//			handleInputs(image);
		}
	}

	private void displayFrame(FImage image) {
		DisplayUtilities.displayName(image, "inputFrame");
	}
}
