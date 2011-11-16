package org.openimaj.demos.video;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideo;

import Jama.Matrix;

public class VideoWithinVideo implements VideoDisplayListener<MBFImage> {
	public File videoFile;
	public XuggleVideo video;
	public VideoCapture capture;
	public VideoDisplay<MBFImage> display;
	public Polygon targetArea;
	public MBFImageRenderer renderer;
	public List<IndependentPair<Point2d, Point2d>> pointList;
	public Point2dImpl topLeftS = new Point2dImpl(),topLeftB = new Point2dImpl();
	public Point2dImpl topRightS = new Point2dImpl(),topRightB = new Point2dImpl();
	public Point2dImpl bottomLeftS = new Point2dImpl(),bottomLeftB = new Point2dImpl();
	public Point2dImpl bottomRightS = new Point2dImpl(),bottomRightB = new Point2dImpl();
	public Matrix captureToVideo;
	public Rectangle videoRect;
	private MBFImage nextCaptureFrame;
	
	
	
	public VideoWithinVideo(String videoPath) throws IOException{
		this.videoFile = new File(videoPath);
		this.video = new XuggleVideo(videoFile,true);
		this.capture = new VideoCapture(320,240);
		nextCaptureFrame = capture.getNextFrame().clone();
		
		this.videoRect = new Rectangle(0,0,video.getWidth(),video.getHeight());
		this.captureToVideo = TransformUtilities.makeTransform(
				new Rectangle(0,0,capture.getWidth(),capture.getHeight()), 
				videoRect
		);
		
		display = VideoDisplay.createVideoDisplay(video);
		CaptureVideoSIFT s = new CaptureVideoSIFT(this);
		display.addVideoListener(this);
		
//		targetArea = new Polygon(
//				new Point2dImpl(100,100),
//				new Point2dImpl(200,150),
//				new Point2dImpl(200,230),
//				new Point2dImpl(0,200)
//		);
//		
		
		// Prepare the homography matrix
		pointList = new ArrayList<IndependentPair<Point2d, Point2d>>();
		pointList.add(IndependentPair.pair((Point2d)topLeftB, (Point2d)topLeftS));
		pointList.add(IndependentPair.pair((Point2d)topRightB, (Point2d)topRightS));
		pointList.add(IndependentPair.pair((Point2d)bottomRightB, (Point2d)bottomRightS));
		pointList.add(IndependentPair.pair((Point2d)bottomLeftB, (Point2d)bottomLeftS));
		
		
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {}

	@Override
	public void beforeUpdate(MBFImage frame) {
		DisplayUtilities.displayName(frame, "video");
		if(renderer == null){
			this.renderer = frame.createRenderer();
			
		}
//		this.renderer.drawShapeFilled(targetArea, RGBColour.RED);
		updatePolygon();
		ProjectionProcessor<Float[], MBFImage> proc = new MBFProjectionProcessor();
		proc.setMatrix(captureToVideo);
		
			proc.processImage(nextCaptureFrame);
			if(this.targetArea != null){
				Matrix transform = TransformUtilities.homographyMatrix(pointList);
				proc.setMatrix(transform);
				proc.processImage(frame.clone());
			}
		synchronized(this){
			proc.performProjection(0, 0,frame);
		}
	}
	
	public void updatePolygon() {
		if(this.targetArea!=null){
			Point2dImpl stl = (Point2dImpl) targetArea.vertices.get(0);
			Point2dImpl str = (Point2dImpl) targetArea.vertices.get(1);
			Point2dImpl sbr = (Point2dImpl) targetArea.vertices.get(2);
			Point2dImpl sbl = (Point2dImpl) targetArea.vertices.get(3);
			this.topLeftS.x = stl.x; this.topLeftS.y = stl.y; // top left small rectangle
			this.topRightS.x = str.x; this.topRightS.y = str.y; // top right small rectangle
			this.bottomRightS.x = sbr.x; this.bottomRightS.y = sbr.y; // bottom right small rectangle
			this.bottomLeftS.x = sbl.x; this.bottomLeftS.y = sbl.y; // bottom right small rectangle
		}
		
		this.topLeftB.x = videoRect.x; this.topLeftB.y = videoRect.y; // top left big rectangle 
		this.topRightB.x = videoRect.x+videoRect.width; this.topRightB.y = videoRect.y; // top right big rectangle
		this.bottomRightB.x = videoRect.x+videoRect.width; this.bottomRightB.y = videoRect.y+videoRect.height;  // bottom right big rectangle
		this.bottomLeftB.x = videoRect.x; this.bottomLeftB.y = videoRect.y+videoRect.height;  // bottom right big rectangle
	}

	public static void main(String[] args) throws IOException {
		VideoWithinVideo vwv = new VideoWithinVideo("/Users/ss/Dropbox/Public/keyboardcat.flv");
	}

	public synchronized void copyToCaptureFrame(MBFImage frameWrite) {
		this.nextCaptureFrame.internalCopy(frameWrite);
	}
}






