package org.openimaj.image.contour;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.contour.SuzukiContourProcessor.Border;
import org.openimaj.image.renderer.MBFImageRenderer;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ContourRenderer extends MBFImageRenderer {

	/**
	 * @param targetImage
	 */
	public ContourRenderer(MBFImage targetImage) {
		super(targetImage);
	}

	public static void drawContours(MBFImage imgC, Border root) {
		new ContourRenderer(imgC).drawContours(root);
	}

	public void drawContours(Border root) {
		final List<Border> toDraw = new ArrayList<Border>();
		toDraw.add(root);
		while (!toDraw.isEmpty()) {
			final Border next = toDraw.remove(toDraw.size() - 1);
			Float[] c = null;
			switch (next.type) {
			case HOLE:
				c = RGBColour.BLUE;
				break;
			case OUTER:
				c = RGBColour.RED;
				break;
			}
			this.drawShape(next, 3, c);

			toDraw.addAll(next.children);
		}
	}

}
