package org.openimaj.demos.touchtable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.util.pair.IndependentPair;


public class TouchTableScreen extends JFrame implements Runnable {

	/**
	 * A touchtable full screen jframe
	 */
	private static final long serialVersionUID = -966931575089952536L;
	private MBFImage image;
	Mode mode;
	public CameraConfig cameraConfig;
	private Rectangle inputArea;
	private Rectangle visibleArea;
	
	interface Mode{
		public class DRAWING implements Mode {

			protected TouchTableScreen touchScreen;
			private List<Touch> points;
			

			public DRAWING(TouchTableScreen touchScreen) {
				this.touchScreen = touchScreen;
				points = new ArrayList<Touch>();
			}

			@Override
			public void acceptTouch(List<Touch> filtered) {
				this.setDrawingPoints(filtered);
			}

			private synchronized void setDrawingPoints(List<Touch> filtered) {
				this.points.addAll(filtered);
			}

			@Override
			public void drawToImage(MBFImage image) {
				List<Touch> toDraw = this.getDrawingPoints();
//				if(this.touchScreen.cameraConfig instanceof TriangleCameraConfig){
//					((TriangleCameraConfig)this.touchScreen.cameraConfig).drawTriangles(image);
//					
//				}
				for (Touch touch : toDraw) {
//					Point2d trans = point2d.transform(this.touchScreen.cameraConfig.homography);
					
					Circle trans = this.touchScreen.cameraConfig.transformTouch(touch);
					if(trans != null)
						image.drawShapeFilled(trans, RGBColour.BLUE);
				}
			}

			protected synchronized List<Touch> getDrawingPoints() {
				List<Touch> toRet = this.points;
				this.points = new ArrayList<Touch>();
				return toRet;
			}
		}
		
		public class DRAWING_TRACKED extends DRAWING {
			Map<Long, Float[]> colours = new HashMap<Long, Float[]>();
			ReallyBasicTouchTracker tracker = new ReallyBasicTouchTracker(100);
			
			public DRAWING_TRACKED(TouchTableScreen touchScreen) {
				super(touchScreen);
			}
			
			@Override
			public void drawToImage(MBFImage image) {
				List<Touch> toDraw = this.getDrawingPoints();
				
				List<Touch> tracked = new ArrayList<Touch>();
				
				for (Touch touch : toDraw) {
					Touch trans = this.touchScreen.cameraConfig.transformTouch(touch);
					
					if(trans != null)
						tracked.add(trans);
				}
				
				tracked = tracker.trackPoints(tracked);
				
				for (Touch touch : tracked) {
					Float[] col = colours.get(touch.touchID);
					
					if (col == null)
						colours.put(touch.touchID, col = RGBColour.randomColour());
				
					image.drawShapeFilled(touch, col);
					image.drawLine((int)touch.getX(), (int)touch.getY(), (int)(touch.getX()+touch.motionVector.x), (int)(touch.getY()+touch.motionVector.y), col);
				}
			}
		}
		
		class CALIBRATION_TRIANGLES implements Mode {
			
			private static final int GRIDY = 4;
			private static final int GRIDX = 5;

			private ArrayList<Point2d> touchArray;
			
			int gridxy = (GRIDX+1)* (GRIDY+1); // a 4x4 grid of points
			private TouchTableScreen touchScreen;
			
			public CALIBRATION_TRIANGLES(TouchTableScreen touchTableScreen) {
				this.touchScreen = touchTableScreen;
				this.touchArray = new ArrayList<Point2d>();
			}

			@Override
			public void acceptTouch(List<Touch> filtered) {
				Point2d pixelToAdd = filtered.get(0).getCOG();
				Point2d lastPointAdded = null;
				if(this.touchArray.size() != 0) lastPointAdded = this.touchArray.get(this.touchArray.size() - 1);
				if(
					lastPointAdded == null || 
					Line2d.distance(pixelToAdd, lastPointAdded) > TouchTableDemo.SMALLEST_POINT_DIAMETER
				) {
					this.touchArray.add(pixelToAdd);
				}
				
				if(this.touchArray.size() == this.gridxy){
					calibrate();
				}
			}

			@Override
			public void drawToImage(MBFImage image) {
				image.fill(RGBColour.WHITE);
				int nPoints = touchArray.size();
				float gridX = nPoints % (GRIDX+1);
				float gridY = nPoints / (GRIDX+1);				
				
				Point2dImpl currentpoint = new Point2dImpl(
						(image.getWidth() * (gridX / GRIDX)),
						((image.getHeight()) * (gridY / GRIDY))
				);
				drawTarget(image,currentpoint);
			}
			
			private void drawTarget(MBFImage image, Point2d point){
				image.drawShapeFilled(new Rectangle(point.getX()-5,point.getY()-5,10,10),RGBColour.RED);
			}
			
			private void calibrate() {
				this.touchScreen.cameraConfig = new TriangleCameraConfig(
					this.touchArray,GRIDX,GRIDY,this.touchScreen.visibleArea
				); 
				touchScreen.mode = new Mode.DRAWING(touchScreen);
			}
			
		}
		class CALIBRATION_HOMOGRAPHY implements Mode{
			
			private static Point2d TOP_LEFT = null;
			private static Point2d TOP_RIGHT = null;
			private static Point2d BOTTOM_LEFT = null;
			private static Point2d BOTTOM_RIGHT = null;
			private ArrayList<Point2d> touchArray;
			private TouchTableScreen touchScreen;

			public CALIBRATION_HOMOGRAPHY(TouchTableScreen touchTableScreen){
				this.touchArray = new ArrayList<Point2d>();
				TOP_LEFT = new Point2dImpl(30f,30f);
				TOP_RIGHT = new Point2dImpl(touchTableScreen.image.getWidth()-30f,30f);
				BOTTOM_LEFT = new Point2dImpl(30f,touchTableScreen.image.getHeight()-30f);
				BOTTOM_RIGHT = new Point2dImpl(touchTableScreen.image.getWidth()-30f,touchTableScreen.image.getHeight()-30f);
				this.touchScreen = touchTableScreen;
			}
			
			@Override
			public void drawToImage(MBFImage image) {
				image.fill(RGBColour.WHITE);
				switch (this.touchArray.size()) {
				case 0:
					drawTarget(image,TOP_LEFT);
					break;
				case 1:
					drawTarget(image,TOP_RIGHT);
					break;
				case 2:
					drawTarget(image,BOTTOM_LEFT);
					break;
				case 3:
					drawTarget(image,BOTTOM_RIGHT);
					break;
				default:
					break;
				}
			}

			private void drawTarget(MBFImage image, Point2d point){
				image.drawPoint(point, RGBColour.RED, 10);
			}
			@Override
			public void acceptTouch(List<Touch> filtered) {
				Point2d pixelToAdd = filtered.get(0).getCOG();
				Point2d lastPointAdded = null;
				if(this.touchArray.size() != 0) lastPointAdded = this.touchArray.get(this.touchArray.size() - 1);
				if(
					lastPointAdded == null || 
					Line2d.distance(pixelToAdd, lastPointAdded) > TouchTableDemo.SMALLEST_POINT_DIAMETER
				) {
					this.touchArray.add(pixelToAdd);
				}
				
				if(this.touchArray.size() == 4){
					calibrate();
				}
			}
			private void calibrate() {
				HomographyModel m = new HomographyModel(10f);
				List<IndependentPair<Point2d, Point2d>> matches = new ArrayList<IndependentPair<Point2d, Point2d>>();
			
				matches.add(IndependentPair.pair(TOP_LEFT, this.touchArray.get(0)));
				matches.add(IndependentPair.pair(TOP_RIGHT, this.touchArray.get(1)));
				matches.add(IndependentPair.pair(BOTTOM_LEFT, this.touchArray.get(2)));
				matches.add(IndependentPair.pair(BOTTOM_RIGHT, this.touchArray.get(3)));
				HomographyCameraConfig cameraConfig = new HomographyCameraConfig(
						4.9736307741305950e+002f, 4.9705029823649602e+002f, 
						touchScreen.inputArea.width/2, touchScreen.inputArea.height/2,
						5.8322574816106650e-002f,-1.7482068549377444e-001f,
						-3.1083477039117124e-003f, -4.3781939644044129e-003f
				); 
				m.estimate(matches);
				cameraConfig.homography = m.getTransform().inverse();
				touchScreen.cameraConfig = cameraConfig;
				touchScreen.mode = new Mode.DRAWING(touchScreen);
			}
		};

		public void drawToImage(MBFImage image );

		public void acceptTouch(List<Touch> filtered);
	}
	
	public TouchTableScreen(Rectangle extractionArea, Rectangle visibleArea){
		this.setUndecorated(true);
		this.inputArea = extractionArea;
		this.visibleArea= visibleArea;
	}
	
	public void init(){
		int width = this.getWidth();
		int height = this.getHeight();
		
		
		image = new MBFImage(width,height,ColourSpace.RGB);
		this.mode = new Mode.CALIBRATION_TRIANGLES(this);
		
		
		
		Thread t = new Thread(this);
		t.start();
	}
	public void touchEvent(List<Touch> filtered) {
		this.mode.acceptTouch(filtered);
	}
	@Override
	public void run() {
		while(true){
			MBFImage extracted = this.image.extractROI(this.visibleArea);
			this.mode.drawToImage(extracted);
			this.image.drawImage(extracted, 0, 0);
			DisplayUtilities.display(this.image, this);
		}
	}
	public void setCameraConfig(CameraConfig newCC) {
		this.cameraConfig = newCC;
		this.mode = new Mode.DRAWING(this);
	}

}
