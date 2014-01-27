package org.openimaj.image.contour;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.contour.SuzukiContourProcessor.Border;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.threshold.OtsuThreshold;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ContourAestheticode {
	
	public static void main(String[] args) throws IOException {
		String code = "/org/openimaj/image/contour/aestheticode/aestheticode.jpg";
		MBFImage img = ImageUtilities.readMBF(ContourAestheticode.class.getResource(code));
		
		final OtsuThreshold thresh = new OtsuThreshold();
		
		ResizeProcessor resize = new ResizeProcessor(0.3f);
		FImage threshImg = img.flatten().process(resize ).process(thresh);
		DisplayUtilities.display(threshImg);
		Border root = SuzukiContourProcessor.findContours(threshImg);
		MBFImage contourImage = MBFImage.createRGB(threshImg);
		MBFImage detectedImage = MBFImage.createRGB(threshImg);
		ContourRenderer.drawContours(contourImage, root);
		DisplayUtilities.display(contourImage);
		DisplayUtilities.display(img.process(resize));
		
		List<Aestheticode> detected = new FindAestheticode().apply(root);
		
		for (Aestheticode aestheticode : detected) {
			ContourRenderer.drawContours(detectedImage, aestheticode.root);
		}
		DisplayUtilities.display(detectedImage);
	}

	
	
	
}
