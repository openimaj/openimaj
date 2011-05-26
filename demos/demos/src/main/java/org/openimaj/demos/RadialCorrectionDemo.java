package org.openimaj.demos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.RadialDistortionModel;
import org.openimaj.util.pair.IndependentPair;

public class RadialCorrectionDemo {
	public static void main(String args[]) throws IOException{
		List<IndependentPair<Point2d,Point2d>> pairs = new ArrayList<IndependentPair<Point2d,Point2d>>();
		Point2dImpl middle = new Point2dImpl(192,144);
		Point2d[] training = null;
		training = new Point2d[]{
				new Point2dImpl(15,91),
				new Point2dImpl(75,46),
				new Point2dImpl(152,2)
			};
		appendPointsToPairs(training.clone(),middle,pairs);
		
		training = new Point2d[]{
				new Point2dImpl(347,18),
				new Point2dImpl(267,148),
				new Point2dImpl(358,280)
			};
		appendPointsToPairs(training.clone(),middle,pairs);
		
		RadialDistortionModel model = new RadialDistortionModel(8);
		model.setMiddle(middle);
		model.estimate(pairs);
		
		MBFImage image = ImageUtilities.readMBF(RadialCorrectionDemo.class.getResourceAsStream("/org/openimaj/image/data/fisheye.jpeg"));
		MBFImage corrected = image.clone();
		corrected.fill(RGBColour.BLACK);
		
		for(int y = 0; y < corrected.getHeight(); y++){
			for(int x = 0; x < corrected.getWidth(); x++){
				Point2d point = new Point2dImpl(x,y);
				Point2d pred = model.predict(point);
//				System.out.print(point + "->" + pred + ", ");
				corrected.setPixel(x, y,image.getPixelInterp((int)(pred.getX()), (int)(pred.getY())));
			}
//			System.out.println();
		}
		
		MBFImage compare = new MBFImage(image.getWidth() + corrected.getWidth(),image.getHeight(),ColourSpace.RGB);
		compare.drawImage(image, 0, 0);
		compare.drawImage(corrected, image.getWidth(), 0);
		
		DisplayUtilities.display(compare);
	}

	private static void appendPointsToPairs(Point2d[] training,Point2dImpl middle, List<IndependentPair<Point2d, Point2d>> pairs) {
//		for(int i = 0 ; i < training.length; i++){
//			training[i].setX(training[i].getX() - middle.x );
//			training[i].setY(training[i].getY() - middle.y );
//		}
		
		Line2d line = new Line2d(training[0],training[training.length-1]);
		
		for(int i = 0; i < training.length -1 ; i++){
			IndependentPair<Point2d, Point2d> pair = RadialDistortionModel.getRadialIndependantPair(line, training[i],middle);
			
			pairs.add(new IndependentPair<Point2d,Point2d>(pair.secondObject(),pair.firstObject()));
//			pairs.add(pair);
		}
	}
}
