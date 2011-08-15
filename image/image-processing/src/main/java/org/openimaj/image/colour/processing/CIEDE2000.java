package org.openimaj.image.colour.processing;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Implementation of the CIE 2000 colour difference equation, 
 * and a processor to calculate a colour disparity map between
 * two images.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class CIEDE2000 implements ImageProcessor<MBFImage> {
	private FImage disparityMap;
	
	public static double calculateDeltaE(double [] lab1, double[] lab2) {
		return calculateDeltaE(lab1[0],lab1[1],lab1[2],lab2[0],lab2[1],lab2[2]);
	}
	
	public static float calculateDeltaE(float [] lab1, float[] lab2) {
		return (float) calculateDeltaE(lab1[0],lab1[1],lab1[2],lab2[0],lab2[1],lab2[2]);
	}
	
	public static float calculateDeltaE(Float [] lab1, Float[] lab2) {
		return (float) calculateDeltaE(lab1[0],lab1[1],lab1[2],lab2[0],lab2[1],lab2[2]);
	}
	
	public static double calculateDeltaE(double L1, double a1, double b1, double L2, double a2, double b2) {
		double Lmean = (L1 + L2) / 2;
		double C1 =  Math.sqrt(a1*a1 + b1*b1);
		double C2 =  Math.sqrt(a2*a2 + b2*b2);
		double Cmean = (C1 + C2) / 2;
		double G =  (( 1 - Math.sqrt( Math.pow(Cmean, 7) / (Math.pow(Cmean, 7) + Math.pow(25, 7)) ) ) / 2);
		double a1sharp = a1 * (1 + G);
		double a2sharp = a2 * (1 + G);
		double C1sharp =  Math.sqrt(a1sharp*a1sharp + b1*b1);
		double C2sharp =  Math.sqrt(a2sharp*a2sharp + b2*b2);
		double CmeanSharp = (C1sharp + C2sharp) / 2;
		double h1sharp =  ((Math.atan2(b1, a1sharp) >= 0) ? Math.atan2(b1, a1sharp) : Math.atan2(b1, a1sharp) + 2*Math.PI);
		double h2sharp =  ((Math.atan2(b2, a2sharp) >= 0) ? Math.atan2(b2, a2sharp) : Math.atan2(b2, a2sharp) + 2*Math.PI);
		double HmeanSharp =  ((Math.abs(h1sharp - h2sharp) > Math.PI) ? (h1sharp + h2sharp + 2*Math.PI) / 2 : (h1sharp + h2sharp) / 2);
		double T =  (1 - 0.17 * Math.cos(HmeanSharp - 30 * Math.PI / 180) + 0.24 * Math.cos(2*HmeanSharp) + 0.32 * Math.cos(3 * HmeanSharp + 6*Math.PI/180) - 0.2 * Math.cos(4 * HmeanSharp - 63 * Math.PI / 180));
		double deltahsharp =  ((Math.abs(h2sharp - h1sharp) <= Math.PI) ? h2sharp - h1sharp : (h2sharp < h1sharp) ? h2sharp - h1sharp + 2*Math.PI : h2sharp - h1sharp - 2*Math.PI);
		double deltaL = L2 - L1;
		double deltaCsharp = C2sharp - C1sharp;
		double deltaHsharp =  (2 * Math.sqrt(C1sharp*C2sharp) * Math.sin(deltahsharp / 2));
		double SL =  (1 + ( ( 0.015*(Lmean - 50)*(Lmean - 50) ) / ( Math.sqrt( 20 + (Lmean - 50)*(Lmean - 50) ) ) ));
		double SC =  (1 + 0.045 * CmeanSharp);
		double SH =  (1 + 0.015 * CmeanSharp * T);
		double deltaTheta =  ((30 * Math.PI / 180) * Math.exp(-( ((HmeanSharp - 275*Math.PI/180) / 25 ) * ((HmeanSharp - 275*Math.PI/180) / 25 ))));
		double RC =  (2 * Math.sqrt(Math.pow(CmeanSharp, 7) / (Math.pow(CmeanSharp, 7) * Math.pow(25, 7))));
		double RT =  (-RC * Math.sin(2 * deltaTheta));
		double KL = 1;
		double KC = 1;
		double KH = 1;
		
		double deltaE = Math.sqrt(
				((deltaL/(KL*SL)) * (deltaL/(KL*SL))) +
				((deltaCsharp/(KC*SC)) * (deltaCsharp/(KC*SC))) +
				((deltaHsharp/(KH*SH)) * (deltaHsharp/(KH*SH))) +
				(RT * (deltaCsharp/(KC*SC)) * (deltaHsharp/(KH*SH)))
				);
				
		return deltaE;
	}

	public static FImage makeDisparityMap(MBFImage lab1, MBFImage lab2) {
		if (lab1.colourSpace != ColourSpace.CIE_Lab) {
			lab1 = ColourSpace.convert(lab1, ColourSpace.CIE_Lab);
		}
		
		if (lab2.colourSpace != ColourSpace.CIE_Lab) {
			lab2 = ColourSpace.convert(lab2, ColourSpace.CIE_Lab);
		}
		
		FImage disparity = new FImage(lab1.getWidth(), lab1.getHeight());
		for (int y=0; y<disparity.height; y++) {
			for (int x=0; x<disparity.width; x++) {
				disparity.pixels[y][x] = calculateDeltaE(lab1.getPixel(x, y), lab2.getPixel(x, y));
			}
		}
		
		return disparity;
	}
	
	@Override
	public void processImage(MBFImage image, Image<?, ?>... otherimages) {
		this.disparityMap = makeDisparityMap(image, (MBFImage)otherimages[0]);
	}
	
	public FImage getDisparityMap() {
		return disparityMap;
	}
}
