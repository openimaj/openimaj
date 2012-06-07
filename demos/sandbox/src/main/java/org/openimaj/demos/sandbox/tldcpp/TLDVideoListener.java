package org.openimaj.demos.sandbox.tldcpp;

import org.openimaj.demos.sandbox.tldcpp.TLDMain.Command;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.VideoDisplay.Mode;
import org.openimaj.video.tracking.klt.Feature;

public class TLDVideoListener implements VideoDisplayListener<MBFImage> {

	
	private TLDMain tldMain;
	private boolean reuseFrameOnce;
	private boolean skipProcessingOnce;
	private int currentFrame;

	public TLDVideoListener(TLDMain tldMain) {
		this.tldMain = tldMain;
		this.reuseFrameOnce = false;
		this.skipProcessingOnce = false;
		currentFrame = 0;
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		if(this.tldMain.selector.drawRect(frame)){
			return;
		}
		long tic = System.currentTimeMillis();
		currentFrame++;
		FImage grey = null;
		if(!reuseFrameOnce) {
			grey = frame.flatten();
		}

		if(!skipProcessingOnce) {
			tldMain.tld.processImage(grey);
		} else {
			skipProcessingOnce = false;
		}

    	if(tldMain.printResults != null) {
			if(tldMain.tld.currBB != null) {
				tldMain.resultsFile.printf("%d %.2f %.2f %.2f %.2f %f\n", currentFrame-1, tldMain.tld.currBB.x, tldMain.tld.currBB.y, tldMain.tld.currBB.width, tldMain.tld.currBB.height, tldMain.tld.currConf);
			} else {
				tldMain.resultsFile.printf("%d NaN NaN NaN NaN NaN\n", currentFrame-1);
			}
    	}

    	float fps = (System.currentTimeMillis() - tic)/1000;

		boolean confident = (tldMain.tld.currConf >= tldMain.threshold) ? true : false;

		if(tldMain.showOutput || tldMain.saveDir != null) {
			String learningString = "";
			if(tldMain.tld.learning) {
				learningString = "learning";
			}

			String string = String.format("#%d,Posterior %.2f; fps: %.2f, #numwindows:%d, %s", currentFrame-1, tldMain.tld.currConf, fps, tldMain.tld.detectorCascade.numWindows, learningString);
			Float[] yellow = RGBColour.YELLOW;
			Float[] blue = RGBColour.BLUE;
			Float[] black = RGBColour.BLACK;
			Float[] white = RGBColour.WHITE;
			Float[] red = RGBColour.RED;

			if(tldMain.tld.currBB != null) {
				Float[] rectangleColor = confident ? blue : yellow;
//				cvRectangle(img, tld.currBB.tl(), tld.currBB.br(), rectangleColor, 8, 8, 0);
				frame.drawShape(tldMain.tld.currBB, rectangleColor);
				if(this.tldMain.tld.medianFlowTracker.featuresTrackedFrom!=null){
					// Draw the tracked points
					Feature[] from = this.tldMain.tld.medianFlowTracker.featuresTrackedFrom.features;
					Feature[] to = this.tldMain.tld.medianFlowTracker.featuresTrackedTo.features;
					for (int i = 0; i < from.length; i++) {
						frame.drawLine(from[i], to[i], 3, red);
					}					
				}
			}

			HersheyFont font = HersheyFont.ROMAN_SIMPLEX;
//			cvRectangle(img, cvPoint(0,0), cvPoint(img.width,50), black, CV_FILLED, 8, 0);
//			cvPutText(img, string, cvPoint(25,25), &font, white);
			frame.drawText(string, 25,25, font, 12);

			if(tldMain.showForeground) {

				for(int i = 0; i < tldMain.tld.detectorCascade.detectionResult.fgList.size(); i++) {
					Rectangle r = tldMain.tld.detectorCascade.detectionResult.fgList.get(i);
//					cvRectangle(img, r.tl(),r.br(), white, 1);
				}

			}


		}

		if(reuseFrameOnce) {
			reuseFrameOnce = false;
		}

	
	}
	
	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		if(this.tldMain.command == Command.NONE) {
		}
		else if(this.tldMain.command == Command.ALTERNATING){
		}
		else if(this.tldMain.command == Command.CLEAR){
			this.tldMain.tld.release();
		}
		else if(this.tldMain.command == Command.SELECT){
			this.tldMain.disp.setMode(Mode.PAUSE);
			this.tldMain.selector.selectBoundingBox();
		}
		else if(this.tldMain.command == Command.LEARNING){
			tldMain.tld.learningEnabled = !tldMain.tld.learningEnabled;
		}
		tldMain.command = Command.NONE;
	}

}
