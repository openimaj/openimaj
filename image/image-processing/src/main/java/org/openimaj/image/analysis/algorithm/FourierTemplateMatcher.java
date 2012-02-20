package org.openimaj.image.analysis.algorithm;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processing.algorithm.FourierCorrelation;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Basic template matching for {@link FImage}s. Template matching is
 * performed in the frequency domain.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class FourierTemplateMatcher implements ImageAnalyser<FImage> {
	private FourierCorrelation correlation;
	private Rectangle searchBounds;
	private FImage responseMap;
	private int templateWidth;
	private int templateHeight;
		
	/**
	 * Default constructor with the template to match. When matching is
	 * performed by {@link #analyseImage(FImage)}, the whole image
	 * will be searched.
	 * 
	 * @param template The template
	 */
	public FourierTemplateMatcher(FImage template) {
		this.correlation = new FourierCorrelation(template);
		this.templateWidth = template.width;
		this.templateHeight = template.height;
	}
	
	/**
	 * Construct with the template to match and the bounds rectangle in which
	 * to search. The search bounds rectangle is defined with respect
	 * to the centre of the template.
	 * 
	 * @param template The template
	 * @param bounds The bounding box for search.
	 */
	public FourierTemplateMatcher(FImage template, Rectangle bounds) {
		this(template);
		this.searchBounds = bounds;
	}
	
	/**
	 * @return the search bound rectangle
	 */
	public Rectangle getSearchBounds() {
		return searchBounds;
	}

	/**
	 * Set the search bounds rectangle. The search bounds rectangle 
	 * is defined with respect to the centre of the template.
	 * Setting to <code>null</code> results in the entire image
	 * being searched.
	 * 
	 * @param searchBounds the search bounds to set
	 */
	public void setSearchBounds(Rectangle searchBounds) {
		this.searchBounds = searchBounds;
	}

	/**
	 * Perform template matching. If a bounds rectangle
	 * is has not been set or is null, then the whole
	 * image will be searched. Otherwise the area of the image
	 * which lies in the previously set search bounds will be
	 * searched.
	 * 
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		FImage subImage = image.clone();
		
		if (this.searchBounds != null) {
			final int halfWidth = templateWidth / 2;
			final int halfHeight = templateHeight / 2;
			
			int x = (int) Math.max(searchBounds.x - halfWidth, 0);
			int width = (int) searchBounds.width + templateWidth;
			if (searchBounds.x - halfWidth < 0) {
				width += (searchBounds.x - halfWidth);
			}
			if (x + width > image.width)
				width = image.width;
			
			int y = (int) Math.max(searchBounds.y - halfHeight, 0);
			int height = (int) searchBounds.height + templateHeight;
			if (searchBounds.y - halfHeight < 0) {
				height += (searchBounds.y - halfHeight);
			}
			if (y + height > image.height)
				height = image.height;
			
			subImage = image.extractROI(
					x,
					y,
					width,
					height
			);
		}
		
		responseMap = subImage.processInline(correlation);
	}
	
	/**
	 * Get the top-N "best" responses found by the template matcher.
	 * 
	 * @param numResponses The number of responses
	 * @return the best responses found
	 */
	public FValuePixel[] getBestResponses(int numResponses) {
		return TemplateMatcher.getBestResponses(numResponses, responseMap, getXOffset(), getYOffset(), FValuePixel.ReverseValueComparator.INSTANCE);
	}
	
	/**
	 * @return The x-offset of the top-left of the response map
	 * 		returned by {@link #getResponseMap()} to the original image
	 * 		analysed by {@link #analyseImage(FImage)}.
	 */
	public int getXOffset() {
		final int halfWidth = templateWidth / 2;
		
		if (this.searchBounds == null)
			return halfWidth;
		else 
			return (int) Math.max(searchBounds.x - halfWidth, halfWidth);
	}
	
	/**
	 * @return The y-offset of the top-left of the response map
	 * 		returned by {@link #getResponseMap()} to the original image
	 * 		analysed by {@link #analyseImage(FImage)}.
	 */
	public int getYOffset() {
		final int halfHeight = templateHeight / 2;
		
		if (this.searchBounds == null)
			return halfHeight;
		else 
			return (int) Math.max(searchBounds.y - halfHeight, halfHeight);
	}
	
	/**
	 * @return The responseMap generated from the last call to {@link #analyseImage(FImage)}
	 */
	public FImage getResponseMap() {
		return responseMap;
	}
	
	/**
	 * Testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		FImage image = ImageUtilities.readF(new File("/Users/jsh2/Desktop/image.png"));
		FImage template = image.extractROI(100, 100, 100, 100);
		image.fill(0f);
		image.drawImage(template, 100, 100);
		
		FourierTemplateMatcher matcher = new FourierTemplateMatcher(template);
		matcher.setSearchBounds(new Rectangle(100,100,200,200));
		image.analyseWith(matcher);
		DisplayUtilities.display(matcher.responseMap.normalise());
		
		MBFImage cimg = image.toRGB();
		for (FValuePixel p : matcher.getBestResponses(10)) {
			System.out.println(p);
			cimg.drawPoint(p, RGBColour.RED, 1);
		}
		
		cimg.drawShape(matcher.getSearchBounds(), RGBColour.BLUE);
		cimg.drawShape(new Rectangle(100,100,100,100), RGBColour.GREEN);
		
		DisplayUtilities.display(cimg);
	}
}
