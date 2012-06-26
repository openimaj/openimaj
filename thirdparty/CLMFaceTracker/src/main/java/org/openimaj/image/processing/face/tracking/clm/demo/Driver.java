package org.openimaj.image.processing.face.tracking.clm.demo;

import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.tracking.clm.IO;
import org.openimaj.image.processing.face.tracking.clm.Tracker;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

public class Driver extends KeyAdapter implements VideoDisplayListener<MBFImage> {
	Tracker model = Tracker.Load(Tracker.class.getResourceAsStream("face2.tracker"));
	int [][] tri = IO.LoadTri(Tracker.class.getResourceAsStream("face.tri"));
	int [][] con = IO.LoadCon(Tracker.class.getResourceAsStream("face.con"));
	
	boolean fcheck = false; 
	float scale = 0.5f; 
	int fpd = -1; 
	boolean show = true;

	//set other tracking parameters
	int [] wSize1 = {7};
	int [] wSize2 = {11, 9, 7};

	int nIter = 5; 
	double clamp=3;
	double fTol=0.01;
	
	int fnum = 0;
	double fps = 0;
	long t1, t0 = System.currentTimeMillis();
	
	boolean failed = true; 
	
	public Driver() throws IOException {
		VideoCapture vc = new VideoCapture(640, 480);
		vc.setFPS(60);

		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(vc);
		vd.addVideoListener(this);
		
		SwingUtilities.getRoot(vd.getScreen()).addKeyListener(this);
	}
	
	public Driver(Container c) throws IOException {
		VideoCapture vc = new VideoCapture(640, 480);

		ImageComponent ic = new ImageComponent(true);
		c.add(ic);
		
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(vc, ic);
		vd.addVideoListener(this);
		
		SwingUtilities.getRoot(vd.getScreen()).addKeyListener(this);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == 'd')
			model.FrameReset();
	}
	
	@Override
	public void beforeUpdate(MBFImage frame) {
		//grab image, resize and flip
		frame.flipX();
		
		//			MBFImage frame = ImageUtilities.readMBF(new File("/Users/jsh2/Desktop/face.png"));
		FImage im = frame.flatten();//Transforms.calculateIntensityNTSC(frame);
		//			im.multiplyInplace(255F);

		if(scale != 1)
			if (scale == 0.5f)
				im = ResizeProcessor.halfSize(im);
			else
				im = ResizeProcessor.resample(im, (int)(scale*im.width), (int)(scale*im.height));

		//flip image?

		//track this image
		int[] wSize; 
		if (failed)
			wSize = wSize2; 
		else 
			wSize = wSize1;

		if ( model.Track(im, wSize, fpd, nIter, clamp, fTol, fcheck) == 0 ) {
			int idx = model._clm.GetViewIdx();
			failed = false;

			//frame.fill(RGBColour.BLACK);
			Draw(frame, model._shape, con, tri, model._clm._visi[idx], scale); 
		} else {
			model.FrameReset();
			failed = true;
		}     
		//draw framerate on display image 
		if(fnum >= 9){      
			t1 = System.currentTimeMillis();
			fps = 10 / ((double)(t1-t0)/1000.0); 
			t0 = t1;
			fnum = 0;
		}
		else {
			fnum += 1;
		}
		
		if(show) {
			String text = String.format("%d frames/sec", (int)Math.round(fps)); 
			frame.drawText(text, 10, 20, HersheyFont.ROMAN_SIMPLEX, 20, RGBColour.GREEN);
		}
	}
	
	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {}
	
	void Draw(MBFImage image, Matrix shape, int[][] con, int[][] tri, Matrix visi, float scale)
	{
		int n = shape.getRowDimension() / 2; 
		Point2dImpl p1,p2; 
		Float[] c;


		//draw triangulation
//		c = RGBColour.BLACK;
//		for (int i = 0; i < tri.length; i++) {
//			if (visi.get(tri[i][0], 0) == 0 ||
//					visi.get(tri[i][1],0) == 0 ||
//					visi.get(tri[i][2],0) == 0)
//				continue;
//
//			p1 = new Point2dImpl((float)shape.get(tri[i][0],0), (float)shape.get(tri[i][0]+n,0));
//			p2 = new Point2dImpl((float)shape.get(tri[i][1],0), (float)shape.get(tri[i][1]+n,0));
//			image.drawLine(p1, p2, c);
//
//			p1 = new Point2dImpl((float)shape.get(tri[i][0],0), (float)shape.get(tri[i][0]+n,0));
//			p2 = new Point2dImpl((float)shape.get(tri[i][2],0), (float)shape.get(tri[i][2]+n,0));
//			image.drawLine(p1, p2, c);
//
//			p1 = new Point2dImpl((float)shape.get(tri[i][2],0), (float)shape.get(tri[i][2]+n,0));
//			p2 = new Point2dImpl((float)shape.get(tri[i][1],0), (float)shape.get(tri[i][1]+n,0));
//			image.drawLine(p1, p2, c);
//		}
		//draw connections
		c = RGBColour.WHITE;
		for (int i = 0; i < con[0].length; i++) {
			if(visi.get(con[0][i], 0) == 0 ||
					visi.get(con[1][i], 0) == 0)
				continue;

			p1 = new Point2dImpl((float)shape.get(con[0][i],0)/scale, (float)shape.get(con[0][i]+n,0)/scale);
			p2 = new Point2dImpl((float)shape.get(con[1][i],0)/scale, (float)shape.get(con[1][i]+n,0)/scale);

			image.drawLine(p1, p2, 2, c);
		}
//		//draw points
//		for (int i = 0; i < n; i++) {    
//			if(visi.get(i, 0) == 0)
//				continue;
//			p1 = new Point2dImpl((float)shape.get(i,0), (float)shape.get(i+n,0));
//			c = RGBColour.RED; 
//			image.drawPoint(p1, c, 2);
//		}
	}
	
	public static void main(String[] args) throws Exception {
		new Driver();
	}
}

