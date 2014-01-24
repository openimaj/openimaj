package org.openimaj.image.contour;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.contour.SuzukiContourProcessor.Border;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.image.processor.KernelProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ContourAestheticode {
	
	static class Aestheticode{
		public Aestheticode(Border root) {
			this.root = root;
		}
		Border root;
	}
	
	public static void main(String[] args) throws IOException {
		String code = "/org/openimaj/image/contour/aestheticode/aestheticode.jpg";
		MBFImage img = ImageUtilities.readMBF(ContourAestheticode.class.getResource(code));
		
		final OtsuThreshold thresh = new OtsuThreshold();
		
		ResizeProcessor resize = new ResizeProcessor(0.3f);
		FImage threshImg = img.flatten().process(resize ).process(thresh);
		Border root = SuzukiContourProcessor.findContours(threshImg);
		MBFImage contourImage = MBFImage.createRGB(threshImg);
		MBFImage detectedImage = MBFImage.createRGB(threshImg);
		ContourRenderer.drawContours(contourImage, root);
		DisplayUtilities.display(threshImg);
		DisplayUtilities.display(contourImage);
		DisplayUtilities.display(img.process(resize));
		
		List<Aestheticode> detected = detectCodes(root,threshImg.getBounds().calculateArea());
		
		for (Aestheticode aestheticode : detected) {
			ContourRenderer.drawContours(detectedImage, aestheticode.root);
		}
		DisplayUtilities.display(detectedImage);
	}

	private static List<Aestheticode> detectCodes(Border root, double area) {
		List<Aestheticode> found = new ArrayList<Aestheticode>();
		detectCode(root,found,area);
		return found;
	}

	private static int detectCode(Border root, List<Aestheticode> found, double area) {
		int depth = -1;
		
		Rectangle bb = root.calculateRegularBoundingBox();
		if(bb.x > 277 && bb.y > 270 && bb.x < 430 && bb.y < 410){
			System.out.println("MAYBE?");
			for (Border child : root.children) {
				for (Point2d aestheticode : child.getVertices()) {
					System.out.println(aestheticode);
				}
			}
			found.add(new Aestheticode(root));
			
		}
//		double arProp = bb.calculateArea()/area;
//		boolean alleq = arProp > 0.01 ;
//		
		for (Border child : root.children) {
			int cDepth = detectCode(child, found,area);
//			if(depth == -1) depth = cDepth;
//			else if(depth != cDepth) alleq = false; 
		}
		return 0;
//		
//		if(depth == -1){
//			return 0;
//		}
//		
//		if(alleq && depth == 2){
//			found.add(new Aestheticode(root));
//		}
//		
//		return depth + 1;
	}
	
	
}
