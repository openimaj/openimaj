package org.openimaj.demos.sandbox.asm;

import org.openimaj.image.FImage;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

public class NormalLandmark extends Point2dImpl {
	protected Point2d normalVector;
	protected float [] samples;
	
	public NormalLandmark(Point2d pt, Point2d normal, FImage image, int numSamples) {
		
	}
	
	public static float [] extractSamples(Line2d line, FImage image, int numSamples) {
		float[] samples = new float[numSamples];
		
		Point2d p1 = line.getBeginPoint();
		Point2d p2 = line.getEndPoint();
		float x = p1.getX();
		float y = p1.getY();
		float dxStep = (p2.getX() - x) / (numSamples-1);
		float dyStep = (p2.getY() - y) / (numSamples-1);
		
		for (int i=0; i<numSamples; i++) {
			samples[i] = image.getPixelInterpNative(x, y, 0);
			
			System.out.println(x + " " + y);
			
			x += dxStep;
			y += dyStep;
		}
		
		return samples;
	}
}
