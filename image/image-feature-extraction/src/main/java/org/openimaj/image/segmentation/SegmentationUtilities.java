package org.openimaj.image.segmentation;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.connectedcomponent.render.BlobRenderer;

/**
 * Some utility functions for dealing with segmented output 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class SegmentationUtilities {
	private SegmentationUtilities() {}
	
	/**
	 * Render the components to the image with randomly assigned colours.
	 * 
	 * @param image Image to draw to
	 * @param components the components
	 * @return the image
	 */
	public static MBFImage renderSegments(MBFImage image, List<ConnectedComponent> components) {
		for (ConnectedComponent cc : components) {
			BlobRenderer<Float[]> br = new BlobRenderer<Float[]>(image, RGBColour.randomColour());
			br.process(cc);
		}
		
		return image;
	}
	
	/**
	 * Render the components to an image with randomly assigned colours.
	 * 
	 * @param width Width of image.
	 * @param height Height of image.
	 * @param components the components.
	 * @return the rendered image.
	 */
	public static MBFImage renderSegments(int width, int height, List<ConnectedComponent> components) {
		return renderSegments(new MBFImage(width, height, ColourSpace.RGB), components);
	}
}
