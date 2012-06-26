package org.openimaj.demos.sandbox.tldcpp.detector;

import org.openimaj.image.FImage;

/**
 * a 2D double array such that each element is the sum of each
 * value to the top left of the element,
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class IntegralImage {
	double[][] data; 
	
	IntegralImage() {
	}
	void calcIntImg(FImage img, boolean squared)
	{
		data = new double[img.height][img.width];
		float[][] input = img.pixels;
		double[][] output = data;
		for(int j = 0;j < img.height;j++){
			for(int i = 0;i < img.width;i++){
				double A = (i > 0) ? output[j][i - 1] : 0;
				double B = (j > 0) ? output[j - 1][i] : 0;
				double C = (j > 0 && i > 0) ? output[j - 1][ i - 1] : 0;
				double value = input[j][i];
				if(squared) {
					value = value*value;
				}
				output[j][i] = A + B - C + value;
			}
		}

	}
}
