package org.openimaj.demos.sandbox.tld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.junit.Test;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;

public class TestTLDForwardBackwardMedianFlowTracker {
	
	String root = "/Users/ss/Development/matlab/OpenTLD/";
	String imagesRoot = "_input";
	String trackedpoints = "_trackedpoints";
	String bboxes = "_bboxes";
	private String trackedPointName = "trackedpoints_%05d";
	private String imageName = "%05d.png";
	private String bboxName = "bbox_%05d";
	
	public static void main(String[] args) throws Exception {
		TestTLDForwardBackwardMedianFlowTracker t = new TestTLDForwardBackwardMedianFlowTracker();
		t.testTLDFB();
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testTLDFB() throws Exception {
		FeatureList[] features = loadTrackedPoints(0);
		FImage[] images = loadImages(0);
		Rectangle[] bbs = loadBoundingBox(0);
		
		drawTracked(images,features,bbs);
		
//		TLDFrontBackMedianFlowTracker fbTracker = new TLDFrontBackMedianFlowTracker(images[0], images[1], features[0], features[1], features[2], bbs[0]);
	}
	private void drawTracked(FImage[] images, FeatureList[] features, Rectangle[] bbs) {
		MBFImage draw = new MBFImage(images[0].width*2,images[1].height,3);
		MBFImage img1 = new MBFImage(new FImage[]{images[0].clone(),images[0].clone(),images[0].clone()});
		MBFImage img2 = new MBFImage(new FImage[]{images[1].clone(),images[1].clone(),images[1].clone()});
		
		img1.drawShape(bbs[0], 3, RGBColour.YELLOW);
		img2.drawShape(bbs[1], 3, RGBColour.YELLOW);
		
		draw.drawImage(img1, 0, 0);
		draw.drawImage(img2, img1.getWidth(), 0);
		
		
		
		DisplayUtilities.display(draw);
	}
	private Rectangle[] loadBoundingBox(int i) throws FileNotFoundException {
		File base = new File(root,bboxes);
		File location = new File(base,String.format(bboxName , i));
		Scanner s = new Scanner(location);
		
		Rectangle[] ret = new Rectangle[2];
		ret[0] = readRect(s);
		ret[1] = readRect(s);
		return ret ;
	}
	private Rectangle readRect(Scanner s) {
		float x1,x2,y1,y2;
		x1 = s.nextFloat();
		y1 = s.nextFloat();
		x2 = s.nextFloat();
		y2 = s.nextFloat();
		return new Rectangle(x1,y1,x2-x1,y2-y1);
	}
	private FImage[] loadImages(int i) throws IOException {
		FImage[] images = new FImage[2];
		File imageBase = new File(root,imagesRoot);
		// because matlab is fucking retarded
		images[0] = ImageUtilities.readF(new File(imageBase,String.format(imageName , i+1)));
		images[1] = ImageUtilities.readF(new File(imageBase,String.format(imageName , i+2)));
		return images;
	}
	private FeatureList[] loadTrackedPoints(int i) throws FileNotFoundException {
		File base = new File(root,trackedpoints);
		File location = new File(base,String.format(trackedPointName , i));
		Scanner s = new Scanner(location);
		
		s.nextLine(); // The header
		int nPoints = s.nextInt();
		FeatureList[] features = new FeatureList[3];
		features[0] = new FeatureList(nPoints);
		features[1] = new FeatureList(nPoints);
		features[2] = new FeatureList(nPoints);
		for (int j = 0; j < nPoints; j++) {
			features[0].features[j] = new Feature();
			features[0].features[j].x = s.nextFloat();
			features[0].features[j].y = s.nextFloat();
			
			features[1].features[j] = new Feature();
			features[1].features[j].x = s.nextFloat();
			features[1].features[j].y = s.nextFloat();
			
			features[2].features[j] = new FBFeature();
			features[2].features[j].x = s.nextFloat();
			features[2].features[j].y = s.nextFloat();
			
			features[0].features[j].val = (int)s.nextInt();
			features[1].features[j].val = features[0].features[j].val;
			features[2].features[j].val = features[0].features[j].val;
			
			((FBFeature)(features[2].features[j])).forwardBackDistance = s.nextFloat();
			((FBFeature)(features[2].features[j])).normalisedCrossCorrelation = s.nextFloat();
		}
		return features;
	}
}
