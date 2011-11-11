package org.openimaj.demos.video;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
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
	private File videoFile;
	private XuggleVideo video;
	private VideoCapture capture;
	private VideoDisplay<MBFImage> display;
	private Polygon targetArea;
	private MBFImageRenderer renderer;
	private List<IndependentPair<Point2d, Point2d>> pointList;
	private Point2dImpl topLeftS = new Point2dImpl(),topLeftB = new Point2dImpl();
	private Point2dImpl topRightS = new Point2dImpl(),topRightB = new Point2dImpl();
	private Point2dImpl bottomLeftS = new Point2dImpl(),bottomLeftB = new Point2dImpl();
	private Point2dImpl bottomRightS = new Point2dImpl(),bottomRightB = new Point2dImpl();
	
	
	
	public VideoWithinVideo(String videoPath) throws IOException{
		this.videoFile = new File(videoPath);
		this.video = new XuggleVideo(videoFile);
		
		this.capture = new VideoCapture(640,480);
		
		display = VideoDisplay.createVideoDisplay(capture);
		display.addVideoListener(this);
		
		targetArea = new Polygon(
				new Point2dImpl(100,100),
				new Point2dImpl(200,200),
				new Point2dImpl(200,400),
				new Point2dImpl(80,300)
		);
		
		
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
		if(renderer == null){
			this.renderer = frame.createRenderer();
		}
		
		Point2dImpl stl = (Point2dImpl) targetArea.vertices.get(0);
		Point2dImpl str = (Point2dImpl) targetArea.vertices.get(1);
		Point2dImpl sbr = (Point2dImpl) targetArea.vertices.get(2);
		Point2dImpl sbl = (Point2dImpl) targetArea.vertices.get(3);
		
//		this.renderer.drawShapeFilled(targetArea, RGBColour.RED);
		MBFImage nextVideoFrame = video.getNextFrame();
		Rectangle big = nextVideoFrame.getBounds();
		this.topLeftB.x = big.x; this.topLeftB.y = big.y; // top left big rectangle 
		this.topLeftS.x = stl.x; this.topLeftS.y = stl.y; // top left small rectangle
		this.topRightB.x = big.x+big.width; this.topRightB.y = big.y; // top right big rectangle
		this.topRightS.x = str.x; this.topRightS.y = str.y; // top right small rectangle
		this.bottomRightB.x = big.x+big.width; this.bottomRightB.y = big.y+big.height;  // bottom right big rectangle
		this.bottomRightS.x = sbr.x; this.bottomRightS.y = sbr.y; // bottom right small rectangle
		this.bottomLeftB.x = big.x; this.bottomLeftB.y = big.y+big.height;  // bottom right big rectangle
		this.bottomLeftS.x = sbl.x; this.bottomLeftS.y = sbl.y; // bottom right small rectangle
		
		Matrix transform = TransformUtilities.homographyMatrix(pointList);
		
		ProjectionProcessor<Float[], MBFImage> proc = new ProjectionProcessor<Float[], MBFImage>();
//		proc.processImage(frame);
		proc.setMatrix(transform);
		proc.processImage(nextVideoFrame);
		proc.performProjection(0, 0,frame);
	}
	
	public static void main(String[] args) throws IOException {
		VideoWithinVideo vwv = new VideoWithinVideo("/Users/ss/Dropbox/Public/keyboardcat.flv");
	}
}






