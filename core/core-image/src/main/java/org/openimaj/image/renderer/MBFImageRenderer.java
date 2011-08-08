package org.openimaj.image.renderer;

import java.util.Arrays;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 * {@link ImageRenderer} for {@link MBFImage} images.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class MBFImageRenderer extends MultiBandRenderer<Float, MBFImage, FImage> {
	
	/**
	 * Construct with given target image.
	 * @param targetImage the target image.
	 */
	public MBFImageRenderer(MBFImage targetImage) {
		super(targetImage);
	}
	
	/**
	 * Construct with given target image and rendering hints.
	 * @param targetImage the target image.
	 * @param hints the render hints
	 */
	public MBFImageRenderer(MBFImage targetImage, RenderHints hints) {
		super(targetImage, hints);
	}

	@Override
	public Float[] defaultBackgroundColour() {
		return new Float[this.targetImage.numBands()];
	}
	
	@Override
	public Float[] defaultForegroundColour() {
		Float [] c = new Float[this.targetImage.numBands()];
		Arrays.fill(c, 1f);
		return c;
	}
	
	/**
	 * Draw the provided image at the given coordinates.
	 * Parts of the image outside the bounds of this image
	 * will be ignored
	 * 
	 * @param image Image to draw. 
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	@Override
	public void drawImage(MBFImage image, int x, int y) {
		if(this.targetImage.bands.size() == 3 && this.targetImage.bands.size() == image.bands.size()) {
			super.drawImage(image, x, y);
			return;
		}
		
		int stopx = Math.min(targetImage.getWidth(), x + image.getWidth());
		int stopy = Math.min(targetImage.getHeight(), y + image.getHeight());
		int startx = Math.max(0, x);
		int starty = Math.max(0, y);
	
		/**
		 * If either image is 4 channel then we deal with the alpha channel correctly.
		 * Basically you add together the pixel values such that the pixel on top dominates (i.e. the image being added)
		 */
		float thisA=1.0f,thatA=1.0f,thisR,thisG,thisB,thatR,thatG,thatB,a,r,g,b;
		Float[] toSet = new Float[this.targetImage.bands.size()];
		for (int yy=starty; yy<stopy; yy++)
		{
			for (int xx=startx; xx<stopx; xx++)
			{
				Float[] thisPixel = this.targetImage.getPixel(xx, yy);
				Float[] thatPixel = image.getPixel(xx-x,yy-y);
				
				if(thisPixel.length == 4)
				{
					thisA = thisPixel[3];
					
				}
				thisR = thisPixel[0];
				thisG = thisPixel[1];
				thisB = thisPixel[2];
				if(thatPixel.length == 4)
				{
					thatA = thatPixel[3];
				}
				thatR = thatPixel[0];
				thatG = thatPixel[1];
				thatB = thatPixel[2];
				
				
				a = thatA + thisA * (1 - thatA); a = a > 1.0f ? 1.0f : a;
				r = thatR * thatA + (thisR*thisA)*(1-thatA); r = r > 1.0f ? 1.0f : r;
				g = thatG * thatA + (thisG*thisA)*(1-thatA); g = g > 1.0f ? 1.0f : g;
				b = thatB * thatA + (thisB*thisA)*(1-thatA); b = b > 1.0f ? 1.0f : b;
				
				if(toSet.length == 4)
				{
					toSet[0] = a;
					toSet[1] = r;
					toSet[2] = g;
					toSet[3] = b;
				}
				else{
					toSet[0] = r;
					toSet[1] = g;
					toSet[2] = b;
				}
				targetImage.setPixel(xx, yy, toSet);
			}
		}
	}
}
