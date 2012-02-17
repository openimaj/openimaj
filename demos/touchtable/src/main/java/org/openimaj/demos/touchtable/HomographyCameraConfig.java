package org.openimaj.demos.touchtable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.math.geometry.point.Point2d;

import Jama.Matrix;

public class HomographyCameraConfig implements CameraConfig {
	
	Matrix cameraMatrix;
	float[] distortion;
	Matrix homography;
	
	public HomographyCameraConfig(){
		this.cameraMatrix = new Matrix(3,3);
		this.homography = new Matrix(3,3);
		this.distortion = new float[8];
	}
	
	public HomographyCameraConfig(float f, float g){
		this();
		this.cameraMatrix.set(0, 2, f);
		this.cameraMatrix.set(1, 2, g);
		this.cameraMatrix.set(0, 0, 1.0f);
		this.cameraMatrix.set(1, 1, 1.0f);
	}
	
	public HomographyCameraConfig(float fx, float fy,float cx, float cy, float k1, float k2, float p1, float p2){
		this();
		this.cameraMatrix.set(0, 2, cx);
		this.cameraMatrix.set(1, 2, cy);
		this.cameraMatrix.set(0, 0, fx);
		this.cameraMatrix.set(1, 1, fy);
		this.distortion[0] = k1;
		this.distortion[1] = k2;
		this.distortion[2] = p1;
		this.distortion[3] = p2;
		this.distortion[4] = 0;
		this.distortion[5] = 0;
		this.distortion[6] = 0;
		this.distortion[7] = 0;
	}
	
	@Override
	public Touch transformTouch(Touch point){
		int distortion_iterations = 5; // From OpenCV
		double x, y, x0, y0;
		
		double cx = cameraMatrix.get(0,2);
		double cy = cameraMatrix.get(1,2);
		
		double fx = cameraMatrix.get(0,0) / 10;
		double fy = cameraMatrix.get(1,1) / 10;
		
		double ifx = 1/fx;
		double ify = 1/fy;
		
		float k1 = distortion[0] ;
		float k2 = distortion[1];
		float p1 = distortion[2];
		float p2 = distortion[3];
		float k3 = distortion[4];
		float k4 = distortion[5];
		float k5 = distortion[6];
		float k6 = distortion[7];
		
		x = point.getX();
		y = point.getY();
		
		x0 = x = (x - cx)*ifx;
		y0 = y = (y - cy)*ify;
		
	
		// compensate distortion iteratively
        for( int j = 0; j < distortion_iterations; j++ )
        {
            double r2 = x*x + y*y;
            double icdist = (1 + ((k6*r2 + k5)*r2 + k4)*r2)/(1 + ((k3*r2 + k2)*r2 + k1)*r2);
            double deltaX = 2*p1*x*y + p2*(r2 + 2*x*x);
            double deltaY = p1*(r2 + 2*y*y) + 2*p2*x*y;
            x = (x0 - deltaX)*icdist;
            y = (y0 - deltaY)*icdist;
        }
        
		point.setX((float) x);
		point.setY((float) y);
		point.translate((float)cx, (float)cy);
		
		Point2d newc = point.getCOG().transform(homography);
		
        return new Touch(newc.getX(),newc.getY(),point.getRadius(), point.touchID, point.motionVector);

	}
	
	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		
		cameraMatrix.set(0,0,in.nextFloat());
		cameraMatrix.set(1,1,in.nextFloat());
		cameraMatrix.set(0,2,in.nextFloat());
		cameraMatrix.set(1,2,in.nextFloat());
		
		homography.set(0,0,in.nextFloat());homography.set(0,1,in.nextFloat());homography.set(0,2,in.nextFloat());
		homography.set(1,0,in.nextFloat());homography.set(1,1,in.nextFloat());homography.set(1,2,in.nextFloat());
		homography.set(2,0,in.nextFloat());homography.set(2,1,in.nextFloat());homography.set(2,2,in.nextFloat());
		
		for (int i = 0; i < distortion.length; i++) {
			distortion[i] = in.nextFloat();
		}
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.format("%f %f %f %f\n",cameraMatrix.get(0,0),cameraMatrix.get(1,1),cameraMatrix.get(0,2),cameraMatrix.get(1,2));
		out.format("%f %f %f %f %f %f %f %f %f\n",
				homography.get(0,0),homography.get(0,1),homography.get(0,2),
				homography.get(1,0),homography.get(1,1),homography.get(1,2),
				homography.get(2,0),homography.get(2,1),homography.get(2,2)
		);
		
		out.format("%f %f %f %f %f %f %f %f\n",
			distortion[0],distortion[1],distortion[2],distortion[3],
			distortion[4],distortion[5],distortion[6],distortion[7]
		);
	}

}
