package org.openimaj.image.renderer;

import org.openimaj.image.MultiBandImage;
import org.openimaj.image.SingleBandImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;

/**
 * Abstract base for {@link ImageRenderer}s that work on 
 * {@link MultiBandImage}s.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> The pixel type
 * @param <I> The concrete subclass type
 * @param <S> The concrete subclass type of each band
 */
public abstract class MultiBandRenderer 
		<T extends Comparable<T>, 
		I extends MultiBandImage<T,I,S>, 
		S extends SingleBandImage<T,S>> 
	extends 
		ImageRenderer<T[], I>
{
	/**
	 * Construct with given target image.
	 * @param targetImage the target image.
	 */
	public MultiBandRenderer(I targetImage) {
		super(targetImage);
	}
	
	/**
	 * Construct with given target image and rendering hints.
	 * @param targetImage the target image.
	 * @param hints the render hints
	 */
	public MultiBandRenderer(I targetImage, RenderHints hints) {
		super(targetImage, hints);
	}
	
	/**
	 * 	Draws the given single band image onto each band at the given
	 * 	position. Side-affects this image. The single band image must be of
	 * 	the same type as the bands within this image.
	 * 
	 *  @param image A {@link SingleBandImage} to draw
	 *  @param x The x-coordinate for the top-left of the drawn image
	 *  @param y The y-coordinate for the top-left of the drawn image
	 */
	public void drawImage(S image, int x, int y) {
		for (S band : targetImage.bands) 
			band.createRenderer(hints).drawImage(image, x, y);
	}

	/**
	 * 	Draws the given single band image onto the specific band at the given
	 * 	position. Side-affects this image. The single band image must be of
	 * 	the same type as the bands within this image.
	 * 
	 *  @param image A {@link SingleBandImage} to draw
	 *  @param band The band onto which the image will be drawn
	 *  @param x The x-coordinate for the top-left of the drawn image
	 *  @param y The y-coordinate for the top-left of the drawn image
	 */
	public void drawImage(S image, int band, int x, int y) {
		targetImage.bands.get(band).createRenderer(hints).drawImage(image, x, y);
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.renderer.ImageRenderer#drawLine(int, int, double, int, int, java.lang.Object)
	 */
	@Override
	public void drawLine(int x1, int y1, double theta, int length, int thickness, T[] grey) {
		assert(grey.length == targetImage.bands.size());
		
		for (int i=0; i<grey.length; i++) {
			targetImage.bands.get(i).createRenderer(hints).drawLine(x1, y1, theta, length, thickness, grey[i]);
		}
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.renderer.ImageRenderer#drawLine(int, int, int, int, int, java.lang.Object)
	 */
	@Override
	public void drawLine(int x0, int y0, int x1, int y1, int thickness, T[] grey) {
		assert(grey.length == targetImage.bands.size());
		
		for (int i=0; i<grey.length; i++) {
			targetImage.bands.get(i).createRenderer(hints).drawLine(x0, y0, x1, y1, thickness, grey[i]);
		}
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.renderer.ImageRenderer#drawPoint(org.openimaj.math.geometry.point.Point2d, java.lang.Object, int)
	 */
	@Override
	public void drawPoint(Point2d p, T[] col, int size) {
		for (int i = 0; i < targetImage.bands.size(); i++)
			targetImage.bands.get(i).createRenderer(hints).drawPoint(p, col[i], size);
	}

	/**
	 *  {@inheritDoc}
	 *  @see org.openimaj.image.renderer.ImageRenderer#drawPolygon(org.openimaj.math.geometry.shape.Polygon, int, java.lang.Object)
	 */
	@Override
	public void drawPolygon(Polygon p, int thickness, T[] grey) {
		assert(grey.length == targetImage.bands.size());
		
		for (int i=0; i<grey.length; i++) {
			targetImage.bands.get(i).createRenderer(hints).drawPolygon(p, thickness, grey[i]);
		}
	}
}
