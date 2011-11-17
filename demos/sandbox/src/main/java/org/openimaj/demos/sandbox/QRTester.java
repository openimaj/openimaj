package org.openimaj.demos.sandbox;

import gnu.trove.TIntArrayList;

import java.io.IOException;
import java.net.MalformedURLException;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

public class QRTester {
	public static void main(String[] args) throws MalformedURLException, IOException {
//		MBFImage cimg = ImageUtilities.readMBF(new URL("http://cdn.socialnomics.net/wp-content/uploads/2011/03/QR-code-girl1.jpg"));
////		MBFImage cimg = ImageUtilities.readMBF(new URL("http://thinkd2c.files.wordpress.com/2011/05/qrcode_wwd.png"));
//		findMarkers(cimg);
//		DisplayUtilities.display(cimg);
		
		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(new VideoCapture(640, 480));
		display.addVideoListener(new VideoDisplayListener<MBFImage>() {
			
			@Override
			public void beforeUpdate(MBFImage frame) {
				findMarkers(frame);
			}
			
			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	static void findMarkers(MBFImage cimg) {
		FImage image = cimg.flatten();
		//image = image.processInline(new OtsuThreshold());
		image = image.threshold(0.2f);

		for (int y=0; y<image.height; y+=2) {
			TIntArrayList centres = processLineH(image, y);
		
			for (int x : centres.toNativeArray()) {
				cimg.drawLine(x, y-10, x, y+10, RGBColour.RED);
				cimg.drawLine(x-10, y, x+10, y, RGBColour.RED);
			}
		}
//		cimg.internalAssign(new MBFImage(image,image,image));
	}


	static TIntArrayList processLineH(FImage image, int y) {
		TIntArrayList counts = new TIntArrayList();

		int start = 0;
		while (start<image.width) {
			if (image.pixels[y][start] == 0)
				break;
			start++;
		}
		
		for (int i=start; i<image.width; i++) {
			int count = 0;
			float state = image.pixels[y][i];
			for ( ; i<image.width; i++) {
				if (image.pixels[y][i] != state) {
					i--; //step back because the outer loop increments
					break;
				}
				count++;
			}
			counts.add(count);
		}

		return findPossibleH(counts, start);
	}

	static TIntArrayList findPossibleH(TIntArrayList counts, final int start) {
		TIntArrayList centers = new TIntArrayList(); 

		//assume first count is black. Only need check patterns starting with black...
		for (int i=0, co=start; i<counts.size()-5; i+=2) {
			TIntArrayList pattern = counts.subList(i, i+5);
			
			if (isValid(pattern)) {
				int sum = 0;  
				for (int j : pattern.toNativeArray())
					sum += j;
				
				centers.add(co + (sum/2));
			}
			co += counts.get(i) + counts.get(i+1);
		}
		return centers;
	}


	private static boolean isValid(TIntArrayList pattern) {
		//1 1 3 1 1
		//B W B W B

		float [] apat = {1, 1, 3, 1, 1};
		float [] fpat = { pattern.get(0), pattern.get(1), pattern.get(2), pattern.get(3), pattern.get(4) };
		
//		System.out.print(Arrays.toString(fpat) + "\t\t");

		float ratio = 4 / (fpat[0] + fpat[1] + fpat[3] + fpat[4]);
		for (int i=0; i<5; i++)
			fpat[i] *= ratio;

		float error = 0;
		for (int i=0; i<5; i++) {
			float diff = apat[i] - fpat[i];
			error += diff*diff;
		}

//		System.out.println(error);
//		System.out.println(Arrays.toString(fpat) + "\t\t" + error);
		if (error < 0.5)
			return true;
		
		return false;
	}

}
