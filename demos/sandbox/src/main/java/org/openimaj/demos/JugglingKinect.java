package org.openimaj.demos;

import java.io.IOException;

import org.openimaj.hardware.kinect.KinectException;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.HoughCircles;
import org.openimaj.image.analysis.algorithm.HoughCircles.WeightedCircle;
import org.openimaj.image.processing.edges.CannyEdgeDetector2;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class JugglingKinect {
	public static void main(String[] args) throws KinectException, IOException {
//		KinectController c = new KinectController();
//		VideoDisplay<MBFImage> vid = (VideoDisplay<MBFImage>) VideoDisplay.createVideoDisplay(c.videoStream);
		VideoDisplay<MBFImage> vid = (VideoDisplay<MBFImage>) VideoDisplay.createVideoDisplay(new VideoCapture(320,240));
		vid.addVideoListener(new VideoDisplayListener<MBFImage>() {
			int frames = 0;
			private HoughCircles circles;
			@Override
			public void beforeUpdate(MBFImage frame) {
				FImage gframe = frame.flatten();
				frames ++;
//				FImage hband = trans.getBand(1).normalise();
//				frame = frame.process(new Disk(20));
				CannyEdgeDetector2 d = new CannyEdgeDetector2();
				ResizeProcessor resize = new ResizeProcessor(0.3f);
				FImage resized = gframe.process(resize);
//				FImage canny = resized.process(new FSobelMagnitude()).threshold(0.8f);
				FImage canny = resized.process(d);
				if(this.circles == null)
					this.circles = new HoughCircles(canny.width/50,canny.width/3);
				canny.analyseWith(circles);
//				if(frames % 2 == 0){
//					f = f.process(circles);
//					f.drawPoints(circles.accum, 1.f, 10);
//					f.drawShape(new Circle(10,10,10), 1f);
//				}
				MBFImage colResized = new MBFImage(resized.clone(),resized.clone(),resized.clone());
				for (WeightedCircle circ : circles.getBest(1)) {
					System.out.println(circ.weight);
					colResized.drawShape(circ, new Float[]{circ.weight,0f,0f});
				}
				
				DisplayUtilities.displayName(canny,"circles");
				DisplayUtilities.displayName(colResized,"wang");
			}
			
			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
