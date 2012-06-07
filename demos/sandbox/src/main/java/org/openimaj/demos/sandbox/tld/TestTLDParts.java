package org.openimaj.demos.sandbox.tld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

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

public class TestTLDParts {
	
	String root = "/Users/ss/Development/matlab/OpenTLD/";
	String imagesRoot = "_input";
	String trackedpoints = "_trackedpoints";
	String bboxes = "_bboxes";
	private String trackedPointName = "trackedpoints_%05d";
	private String imageName = "%05d.png";
	private String bboxName = "bbox_%05d";
	
	public static void main(String[] args) throws Exception {
		TestTLDParts t = new TestTLDParts();
//		t.testTLDFB();
		t.testTLDFern();
	}
	
	/**
	 * @throws Exception
	 */
	public void testTLDFB() throws Exception {
		FBFeatureSet[] features = loadTrackedPoints(0);
		FImage[] images = loadImages(0);
		Rectangle[] bbs = loadBoundingBox(0);
		
		drawTracked(images,features,bbs);
		
		TLDOptions opts = new TLDOptions();
		TLDFrontBackMedianFlowTracker fbTracker = new TLDFrontBackMedianFlowTracker(images[0], images[1], features, bbs[0],opts);
		Rectangle predictedBox = fbTracker.predictedBox();
		System.out.println(bbs[1]);
		System.out.println(predictedBox);
		
	}
	
	/**
	 * thest the TLD fern detector
	 * @throws Exception
	 */
	public void testTLDFern() throws Exception {
		TLDOptions opts = new TLDOptions();
		FImage[] images = loadImages(0);
		Rectangle[] bbs = loadBoundingBox(0);
		
		TLDInit init = new TLDInit(opts);
		init.initWithFirstFrame(images[0], bbs[0]);
		
//		det.update();
//		det.evaluate();
	}
	
	private void drawTracked(FImage[] images, FBFeatureSet[] features, Rectangle[] bbs) {
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
		x1 = s.nextFloat()-1;
		y1 = s.nextFloat()-1;
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
	private FBFeatureSet[] loadTrackedPoints(int i) throws FileNotFoundException {
		File base = new File(root,trackedpoints);
		File location = new File(base,String.format(trackedPointName , i));
		Scanner s = new Scanner(location);
		
		s.nextLine(); // The header
		int nPoints = s.nextInt();
		FBFeatureSet[] features = new FBFeatureSet[nPoints];
		for (int j = 0; j < nPoints; j++) {
			features[j] = new FBFeatureSet();
			features[j].start = new Feature();
			features[j].start.x = s.nextFloat();
			features[j].start.y = s.nextFloat();
			
			features[j].middle = new Feature();
			features[j].middle.x = s.nextFloat();
			features[j].middle.y = s.nextFloat();
			
			features[j].end = new Feature();
			features[j].end.x = s.nextFloat();
			features[j].end.y = s.nextFloat();
			
			features[j].start.val = (int)s.nextInt();
			features[j].middle.val = features[j].start.val;
			features[j].end.val = features[j].start.val;
			if(features[j].start.val!=-1){
				System.out.println("YEP!");
			}
			
			features[j].forwardBackDistance = s.nextFloat();
			features[j].normalisedCrossCorrelation = s.nextFloat();
		}
		return features;
	}
}
