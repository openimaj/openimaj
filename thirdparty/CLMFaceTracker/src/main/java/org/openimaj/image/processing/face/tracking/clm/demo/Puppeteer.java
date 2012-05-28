package org.openimaj.image.processing.face.tracking.clm.demo;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.tracking.clm.IO;
import org.openimaj.image.processing.face.tracking.clm.Tracker;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.PiecewiseMeshWarp;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.shape.Triangle;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

public class Puppeteer {
	public static void main(String[] args) throws Exception {

		boolean fcheck = true; 
		double scale = 1; 
		int fpd = -1;

		//set other tracking parameters
		int [] wSize1 = {7};
		int [] wSize2 = {11, 9, 7};

		int nIter = 5; 
		double clamp=3;
		double fTol=0.01;

		final Tracker model = Tracker.Load(Tracker.class.getResourceAsStream("face2.tracker"));
		int [][] tri = IO.LoadTri(Tracker.class.getResourceAsStream("face.tri"));
		int [][] con = IO.LoadCon(Tracker.class.getResourceAsStream("face.con"));

		//initialize camera and display window
		VideoCapture vc = new VideoCapture(320, 240);
		vc.setFPS(60);

		JFrame jfr = DisplayUtilities.displayName(vc.getNextFrame(), "Face Tracker");
		jfr.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == 'd')
					model.FrameReset();
			}
		});

		
		MBFImage puppet = ImageUtilities.readMBF(new URL("http://www.oii.ox.ac.uk/images/people/large/nigel_shadbolt.jpg"));
		FImage pimg = puppet.flatten();
		if (model.Track(pimg, wSize2, fpd, nIter, clamp, fTol, false) != 0) throw new Exception("puppet not found");
		List<Triangle> puppetTris = getTriangles(model._shape, con, tri, model._clm._visi[model._clm.GetViewIdx()]);
		model.FrameReset();
		
		int fnum = 0;
		double fps = 0;
		long time1, time0 = System.currentTimeMillis();
		
		Rectangle bounds = new Rectangle();
		boolean failed = true; 
		while (true) 
		{
			//grab image, resize and flip
			MBFImage frame = vc.getNextFrame();
			//			MBFImage frame = ImageUtilities.readMBF(new File("/Users/jsh2/Desktop/face.png"));
			FImage im = frame.flatten();
			//			im.multiplyInline(255F);

			if(scale != 1)
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

				List<Pair<Shape>> mtris = new ArrayList<Pair<Shape>>(); 
				List<Triangle> tris = getTriangles(model._shape, con, tri, model._clm._visi[idx]);
				bounds.x = 1000; bounds.y = 1000; bounds.width = 0; bounds.height = 0;
				for (int i=0; i<tris.size(); i++) {
					Triangle t1 = puppetTris.get(i);
					Triangle t2 = tris.get(i);
					if (t1 != null && t2 != null) {
						mtris.add(new Pair<Shape>(t1, t2));
						
						double minx = t2.minX();
						double maxx = t2.maxX();
						if (bounds.x > minx) bounds.x = (float) minx;
						if (bounds.width < maxx) bounds.width = (float) maxx;
						
						double miny = t2.minY();
						double maxy = t2.maxY();
						if (bounds.y > miny) bounds.y = (float) miny;
						if (bounds.height < maxy) bounds.height = (float) maxy;
					}
				}
				bounds.width -= bounds.x;
				bounds.height -= bounds.y;
				
				//PiecewiseMeshWarp<Float[], MBFImage> pmw = new PiecewiseMeshWarp<Float[], MBFImage>(mtris);
				//composite(frame, puppet.process(pmw), bounds);				
			} else {
				model.FrameReset();
				failed = true;
			}     

			//draw framerate on display image 
			if(fnum >= 9){      
				time1 = System.currentTimeMillis();
				fps = 10 / ((double)(time1-time0)/1000.0); 
				time0 = time1;
				fnum = 0;
			}
			else {
				fnum += 1;
			}
			
			String text = String.format("%d frames/sec", (int)Math.round(fps)); 
			frame.drawText(text, 10, 20, HersheyFont.ROMAN_SIMPLEX, 20, RGBColour.GREEN);
			
			//show image and check for user input
			DisplayUtilities.display(frame, "Face Tracker");
		}
	}

	static void composite(MBFImage back, MBFImage fore, Rectangle bounds) {
		final float[][] rin = fore.bands.get(0).pixels;
		final float[][] gin = fore.bands.get(1).pixels;
		final float[][] bin = fore.bands.get(2).pixels;
		
		final float[][] rout = back.bands.get(0).pixels;
		final float[][] gout = back.bands.get(1).pixels;
		final float[][] bout = back.bands.get(2).pixels;
		
		int xmin = (int) Math.max(0, bounds.x);
		int ymin = (int) Math.max(0, bounds.y);
		
		int ymax = (int) Math.min(Math.min(fore.getHeight(), back.getHeight()), bounds.y + bounds.height);
		int xmax = (int) Math.min(Math.min(fore.getWidth(), back.getWidth()), bounds.x + bounds.width);
				
		for (int y=ymin; y<ymax; y++) {
			for (int x=xmin; x<xmax; x++) {
				if (rin[y][x] != 0 && gin[y][x] != 0 && bin[y][x] != 0) {
					rout[y][x] = rin[y][x];
					gout[y][x] = gin[y][x];
					bout[y][x] = bin[y][x];
				}
			}
		}
	}
	
	static List<Triangle> getTriangles(Matrix shape, int[][] con, int[][] tri, Matrix visi)
	{
		final int n = shape.getRowDimension() / 2; 
		List<Triangle> tris = new ArrayList<Triangle>();

		//draw triangulation
		for (int i = 0; i < tri.length; i++) {
			if (visi.get(tri[i][0], 0) == 0 ||
					visi.get(tri[i][1],0) == 0 ||
					visi.get(tri[i][2],0) == 0) 
			{
				tris.add(null);
			} else {
				Triangle t = new Triangle(
						new Point2dImpl((float)shape.get(tri[i][0],0), (float)shape.get(tri[i][0]+n,0)),
						new Point2dImpl((float)shape.get(tri[i][1],0), (float)shape.get(tri[i][1]+n,0)),
						new Point2dImpl((float)shape.get(tri[i][2],0), (float)shape.get(tri[i][2]+n,0))
				);
				tris.add(t);
			}
		}
		
		return tris;
	}
}
