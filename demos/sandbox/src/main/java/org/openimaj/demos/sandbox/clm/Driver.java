package org.openimaj.demos.sandbox.clm;

import java.io.File;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

public class Driver {
	public static void main(String[] args) throws Exception {

		boolean fcheck = false; 
		double scale = 1; 
		int fpd = -1; 
		boolean show = true;

		//set other tracking parameters
		int [] wSize1 = {7};
		int [] wSize2 = {11, 9, 7};

		int nIter = 5; 
		double clamp=3;
		double fTol=0.01;

		Tracker model = Tracker.Load("/Users/jsh2/Desktop/FaceTracker/model/face2.tracker");
		int [][] tri = IO.LoadTri("/Users/jsh2/Desktop/FaceTracker/model/face.tri");
		int [][] con = IO.LoadCon("/Users/jsh2/Desktop/FaceTracker/model/face.con");

		//initialize camera and display window
		VideoCapture vc = new VideoCapture(320, 240);

		boolean failed = true; 
		while (true) 
		{
			//grab image, resize and flip
			MBFImage frame = vc.getNextFrame();
//			MBFImage frame = ImageUtilities.readMBF(new File("/Users/jsh2/Desktop/face.png"));
			FImage im = frame.flatten();//Transforms.calculateIntensityNTSC(frame);
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

				System.out.println("tracked");
				Draw(frame, model._shape, con, tri, model._clm._visi[idx]); 
			} else {
				if(show) {
					//			    	  cv::Mat R(im,cvRect(0,0,150,50));
					//			    	  R = cv::Scalar(0,0,255);
				}
				System.out.println("failed");
				model.FrameReset();
				failed = true;
			}     
			//draw framerate on display image 
			//			    if(fnum >= 9){      
			//			      t1 = cvGetTickCount();
			//			      fps = 10.0/((double(t1-t0)/cvGetTickFrequency())/1e+6); 
			//			      t0 = t1; fnum = 0;
			//			    }else fnum += 1;
			//			    if(show) {
			//			      sprintf(sss,"%d frames/sec",(int)round(fps)); text = sss;
			//			      cv::putText(im,text,cv::Point(10,20),
			//					  CV_FONT_HERSHEY_SIMPLEX,0.5,CV_RGB(255,255,255));
			//			    }

			//show image and check for user input
			DisplayUtilities.displayName(frame, "Face Tracker"); 
			//			    int c = cvWaitKey(10);
			//			    if(c == 27)break; else if(char(c) == 'd')model.FrameReset();
		}
	}

	static void Draw(MBFImage image, Matrix shape, int[][] con, int[][] tri, Matrix visi)
	{
		int n = shape.getRowDimension() / 2; 
		Point2dImpl p1,p2; 
		Float[] c;
		

		//draw triangulation
		c = RGBColour.WHITE;
		for (int i = 0; i < tri.length; i++) {
			if (visi.get(tri[i][0], 0) == 0 ||
					visi.get(tri[i][1],0) == 0 ||
					visi.get(tri[i][2],0) == 0)
				continue;
			
			p1 = new Point2dImpl((float)shape.get(tri[i][0],0), (float)shape.get(tri[i][0]+n,0));
			p2 = new Point2dImpl((float)shape.get(tri[i][1],0), (float)shape.get(tri[i][1]+n,0));
			image.drawLine(p1, p2, c);
			
			p1 = new Point2dImpl((float)shape.get(tri[i][0],0), (float)shape.get(tri[i][0]+n,0));
			p2 = new Point2dImpl((float)shape.get(tri[i][2],0), (float)shape.get(tri[i][2]+n,0));
			image.drawLine(p1, p2, c);
			
			p1 = new Point2dImpl((float)shape.get(tri[i][2],0), (float)shape.get(tri[i][2]+n,0));
			p2 = new Point2dImpl((float)shape.get(tri[i][1],0), (float)shape.get(tri[i][1]+n,0));
			image.drawLine(p1, p2, c);
		}
		//draw connections
		c = RGBColour.BLUE;
		for (int i = 0; i < con[0].length; i++) {
			if(visi.get(con[0][i], 0) == 0 ||
					visi.get(con[1][i], 0) == 0)
				continue;
			
			p1 = new Point2dImpl((float)shape.get(con[0][i],0), (float)shape.get(con[0][i]+n,0));
			p2 = new Point2dImpl((float)shape.get(con[1][i],0), (float)shape.get(con[1][i]+n,0));
			
			image.drawLine(p1, p2, c);
		}
		//draw points
		for (int i = 0; i < n; i++) {    
			if(visi.get(i, 0) == 0)
				continue;
			p1 = new Point2dImpl((float)shape.get(i,0), (float)shape.get(i+n,0));
			c = RGBColour.RED; 
			image.drawPoint(p1, c, 2);
		}
	}
}
