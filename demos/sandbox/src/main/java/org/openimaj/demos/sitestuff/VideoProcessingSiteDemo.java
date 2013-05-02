package org.openimaj.demos.sitestuff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.demos.sandbox.image.gif.GifSequenceWriter;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class VideoProcessingSiteDemo {
	public static void main(String[] args) throws VideoCaptureException {
		VideoCapture cap = new VideoCapture(640, 480);
		final List<Image<?,?>> frames = new ArrayList<Image<?,?>>();
		VideoDisplay.createOffscreenVideoDisplay(cap).addVideoListener(new VideoDisplayListener<MBFImage>() {
			MBFImage last;
			int nWritten = 0;
			@Override
			public void beforeUpdate(MBFImage frame) {
				if(frame==null) return;
				MBFImage combined = new MBFImage(frame.getWidth()*2,frame.getHeight(),ColourSpace.RGB);
				combined.drawImage(frame, 0, 0);
				if(last != null){
					combined.drawImage(
						frame.subtract(last).abs(), frame.getWidth(),0
					);
				}
				last = frame.clone();
				combined.processInplace(new ResizeProcessor(400, 300));
				DisplayUtilities.displayName(combined, "combined");
				frames.add(combined);
				if(frames.size()%60 == 0){
					try {
						File gifOut = new File("/Users/ss/Desktop/videoProc/"+ "out_" + nWritten + ".gif");
						GifSequenceWriter.writeGif(frames,200, true, gifOut);
						frames.clear();
						System.out.println("GIF written: " + gifOut);
						nWritten++;
					} catch (Exception e) {
					}
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {

			}
		});
	}
}
