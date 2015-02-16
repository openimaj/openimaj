/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 *
 */
package org.openimaj.image.text.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.ConnectedComponent.ConnectMode;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.PixelSet;
import org.openimaj.image.processing.convolution.CompassOperators.Compass0;
import org.openimaj.image.processing.convolution.CompassOperators.Compass135;
import org.openimaj.image.processing.convolution.CompassOperators.Compass45;
import org.openimaj.image.processing.convolution.CompassOperators.Compass90;
import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processing.morphology.Close;
import org.openimaj.image.processing.morphology.Dilate;
import org.openimaj.image.processing.morphology.StructuringElement;
import org.openimaj.image.processing.morphology.Thin;
import org.openimaj.image.processing.threshold.OtsuThreshold;
import org.openimaj.image.processing.transform.SkewCorrector;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;
import org.openimaj.image.processor.connectedcomponent.render.OrientatedBoundingBoxRenderer;
import org.openimaj.image.text.ocr.OCRProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.IndependentPair;

/**
 * A processor that attempts to extract text from an image. It uses a 3-stage
 * process: 1) find possible text regions; 2) filter then extract those regions;
 * 3) OCR.
 * <p>
 * In the first stage it builds a feature map which is an image where the pixel
 * intensity is the likelihood of a pixel being within a text region. It does
 * this by a series of convolutions and morphological operations that find
 * regions that have short edges in multiple directions.
 * <p>
 * In the second stage, the regions are turned into blobs and those blobs that
 * are too small or inappropriately shaped are removed. The regions are then
 * extracted from the original image as subimages containing text. The extracted
 * subimages can have an expansion multipler applied to the box to ensure that
 * enough surrounding information is contained within the extracted subimage for
 * the OCR to work. Use {@link #setBoundingBoxPaddingPc(float)} with a multipler
 * to expand the bounding boxes with; i.e. 1.05 will expand the bounding box by
 * 5%.
 * <p>
 * The third stage simply uses an {@link OCRProcessor} to process the subimages
 * and extract textual strings. Use the {@link #setOCRProcessor(OCRProcessor)}
 * to set the {@link OCRProcessor} to use to extract text. Note that by default
 * no processor is set. If the processor is executed without an
 * {@link OCRProcessor} being set, the OCR stage will not occur. This part of
 * the implementation has moved into {@link TextExtractor} super class.
 * <p>
 * The output of the processor can be retrieved using {@link #getTextRegions()}
 * which returns a map where the key is a bounding box of every detected text
 * region and the value is a pair of subimage to extracted text.
 * <p>
 * From: [paper 01626635.pdf] Xiaoqing Liu and Jagath Samarabandu; An Edge-based
 * Text Region Extraction Algorithm for Indoor Mobile Robot Navigation,
 * Proceedings of the IEEE International Conference on Mechatronics & Automation
 * Niagara Falls, Canada, July 2005
 *
 * @see "http://ieeexplore.ieee.org/xpl/freeabs_all.jsp?arnumber=1626635"
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 29 Jul 2011
 *
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Xiaoqing Liu", "Samarabandu, J." },
		title = "An edge-based text region extraction algorithm for indoor mobile robot navigation",
		year = "2005",
		booktitle = "Mechatronics and Automation, 2005 IEEE International Conference",
		pages = { " 701 ", " 706 Vol. 2" },
		month = "July-1 Aug.",
		number = "",
		volume = "2",
		customData = {
				"keywords",
				"edge-based text region extraction; feature extraction; scene text; text localization; vision-based mobile robot navigation; character recognition; edge detection; feature extraction; mobile robots; navigation; path planning; robot vision;",
				"doi", "10.1109/ICMA.2005.1626635", "ISSN", "" })
public class LiuSamarabanduTextExtractorBasic extends TextExtractor<FImage>
{
	/** Whether to debug the text extractor - displaying images as it goes */
	public static final boolean DEBUG = false;

	/** Percentage of size to add around the bounding box of a text region */
	private float boundingBoxPaddingPc = 1.1f;

	/** The output of the processor. Use #getTextRegions() */
	private Map<Rectangle, FImage> textRegions = null;

	/**
	 * Helper method that convolves the given image with the given convolution
	 * operator. The method also performs an abs() and a normalise(). We can
	 * take the absolute value as we're only interested in whether edges are,
	 * for example, vertical and not in what direction they are.
	 *
	 * @param img
	 *            The image to convolve
	 * @param c
	 *            The convolution operator
	 * @return A convolved image.
	 */
	private FImage processImage(FImage img, FConvolution c)
	{
		return img.process(c)
				.abs()
				.normalise();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void processImage(FImage image)
	{
		// Find which regions might be text
		final FImage fmap = textRegionDetection(image);

		// Process the feature map
		processFeatureMap(fmap, image);

		// The output process image is the feature map
		image.internalAssign(fmap);
	}

	/**
	 * Process a feature map. This function will side affect the field
	 * <code>textRegions</code> in this class. Use {@link #getTextRegions()} to
	 * retrieve the text regions extracted from this method.
	 *
	 * @param fmap
	 *            The feature map to process
	 * @param image
	 *            The original image.
	 */
	public void processFeatureMap(FImage fmap, FImage image)
	{
		// Extract the text regions from the image
		final Map<Rectangle, FImage> t = textRegionLocalisation(fmap, image);
		this.textRegions = t;
	}

	/**
	 * Calculate the feature map that give the approximate localisation of
	 * candidate text regions.
	 *
	 * @param image
	 *            The image to process.
	 * @return The feature map
	 */
	public FImage textRegionDetection(FImage image)
	{
		// 1) Directional Filtering
		final HashMap<Integer, FImage> e = new HashMap<Integer, FImage>();
		e.put(0, processImage(image, new Compass0()));
		e.put(45, processImage(image, new Compass45()));
		e.put(90, processImage(image, new Compass90()));
		e.put(135, processImage(image, new Compass135()));

		// 2) Edge Selection
		// 2a) Get strong edges
		final FImage e90strong = e.get(90).process(new OtsuThreshold());

		if (DEBUG)
			DisplayUtilities.display(e90strong, "Strong Edges");

		// 2b) Get weak edges
		// 2b)i) Dilate:
		// Use a horizontal 1x3 structuring element
		final StructuringElement se = new StructuringElement();
		se.positive.add(new Pixel(0, 0));
		se.positive.add(new Pixel(-1, 0));
		se.positive.add(new Pixel(1, 0));

		if (DEBUG)
			System.out.println("Dilating with a 1x3 structuring element");

		final FImage dilated = e90strong.process(new Dilate(se));

		// 2b)ii) Close:
		// Use a vertical mx1 structuring element
		int m = (int) (dilated.getHeight() / 25d);

		if (DEBUG)
			System.out.println("Closing with a " + m + "x1 structuring element.");

		final StructuringElement se2 = new StructuringElement();
		for (int i = 0; i < m; i++)
			se2.positive.add(new Pixel(0, i - m / 2));
		final FImage closed = dilated.process(new Close(se2));

		// 2b)iii) E90w = |E90 x (closed-dilated)|z
		// |.|z is the Otsu threshold operator
		FImage e90weak = closed.subtract(dilated).abs();
		e90weak.multiplyInplace(e.get(90));
		e90weak = e90weak.process(new OtsuThreshold());

		if (DEBUG)
			DisplayUtilities.display(e90weak, "Weak Edges");

		final FImage e90edges = e90strong.add(e90weak).normalise().process(
				new OtsuThreshold());

		if (DEBUG)
			DisplayUtilities.display(e90edges, "Edges");

		// 3a) Thin edges
		final FImage e90thin = e90edges.process(new Thin(StructuringElement.BOX));

		if (DEBUG)
			DisplayUtilities.display(e90thin, "Thinned");

		final ConnectedComponentLabeler ccl = new ConnectedComponentLabeler(
				ConnectMode.CONNECT_4);
		final List<ConnectedComponent> cc = ccl.findComponents(e90thin);

		// 4a) Label edges
		final FImage e90labelled = new FImage(e90thin.getWidth(), e90thin.getHeight());
		final ConnectedComponentProcessor ccp = new ConnectedComponentProcessor()
		{
			@Override
			public void process(ConnectedComponent cc)
			{
				final int a = cc.calculateArea();
				for (final Pixel p : cc.pixels)
					e90labelled.setPixel((int) p.getX(), (int) p.getY(), (float) a);
			}
		};
		ConnectedComponent.process(cc, ccp);

		if (DEBUG) {
			DisplayUtilities.display(e90labelled.clone().normalise(), "Labelled Edges");
			System.out.println("Max edge length: " + e90labelled.max());
		}

		// Threshold the labelled edges to get only short ones
		final FImage e90short = e90labelled.clone().clip(0f, 1f).subtract(
				e90labelled.threshold(e90labelled.max() / 4 * 3));

		if (DEBUG)
			DisplayUtilities.display(e90short.clone().normalise(), "Thresholded Lengths");

		// 5) Feature Map Generation
		// Suppress false regions and enhance candidate regions
		// 5a) candidate = Dilation( e90short )[mxm]
		final StructuringElement se3 = new StructuringElement();
		for (int i = 0; i < m; i++)
			for (int j = 0; j < m; j++)
				se3.positive.add(new Pixel(i - m / 2, j - m / 2));
		final FImage e90candidate = e90short.process(new Dilate(se3));

		if (DEBUG)
			DisplayUtilities.display(e90candidate, "Candidate Regions");

		// 5b) refined = candidate * sum( e0,e45,e90,e135 );
		final FImage is = e.get(0).clone().
				addInplace(e.get(45)).
				addInplace(e.get(90)).
				addInplace(e.get(135));

		// We normalise the refined so that we can more simply
		// normalise the pixel values in the feature map generation
		// by the window size as we know each pixel is (0,1)
		final FImage refined = e90candidate.multiply(is).normalise();

		if (DEBUG)
			DisplayUtilities.display(refined, "Refined");

		// 5c) fmap(i,j) = N{W(i,j).sum[-c<m<c]( sum[-c<n<c]( refined(i+m,j+n) )
		// ) }
		final int c = 5; // window size -- value not specified in paper
		final FImage fmap = new FImage(image.getWidth(), image.getHeight());

		// Unlike the paper, we use the actual values of the edges
		// in each of the direction images to calculate the weight
		// for each pixel in the pixel map. So, instead of counting
		// (which would require thresholding the direction images),
		// we simply add the values of the maximum edges in each
		// of the direction images; it means if there are 4 directions
		// strongly represented in the window the weight will be near
		// 4 (before normalisation); if there is only 1, the weight will
		// be near 1. Anyway, we have to store the maximum for each direction
		// image within the window which is what this hashmap is for,
		// and what the updateMaxPixDir() private method is used for.
		final HashMap<Integer, Float> maxPixDir = new HashMap<Integer, Float>();

		// For every pixel in the feature map
		for (int j = c; j < image.getHeight() - c; j++)
		{
			for (int i = c; i < image.getWidth() - c; i++)
			{
				float pixelValue = 0;
				final float N = c * c;
				maxPixDir.clear();

				// Look in the window
				for (m = -c; m < c; m++)
				{
					for (int n = -c; n < c; n++)
					{
						pixelValue += refined.getPixel(i + m, j + n);

						updateMaxPixDir(maxPixDir, e, 0, i + m, j + n);
						updateMaxPixDir(maxPixDir, e, 45, i + m, j + n);
						updateMaxPixDir(maxPixDir, e, 90, i + m, j + n);
						updateMaxPixDir(maxPixDir, e, 135, i + m, j + n);
					}
				}

				float w = maxPixDir.get(0) +
						maxPixDir.get(45) +
						maxPixDir.get(90) +
						maxPixDir.get(135);
				w /= 4; // normalise the weight so it's between 0 and 1

				pixelValue *= w;
				pixelValue /= N;

				fmap.setPixel(i, j, pixelValue);
			}
		}

		if (DEBUG)
			DisplayUtilities.display(fmap.clone().normalise(), "Feature Map");

		return fmap;
	}

	/**
	 * Helper method to store the maximum pixel value for a given direction at a
	 * given x and y coordinate.
	 *
	 * @param maxPixDir
	 *            The hashmap in which the maxes are stored
	 * @param e
	 *            The hashmap of direction images
	 * @param dir
	 *            The direction to dereference
	 * @param x
	 *            the x-coord
	 * @param y
	 *            the y-coord
	 */
	private void updateMaxPixDir(HashMap<Integer, Float> maxPixDir, HashMap<Integer, FImage> e, int dir, int x, int y)
	{
		Float xx = null;
		if ((xx = maxPixDir.get(dir)) == null)
			maxPixDir.put(dir, e.get(dir).getPixel(x, y));
		else
			maxPixDir.put(dir, Math.max(xx, e.get(dir).getPixel(x, y)));
	}

	/**
	 * Extract the regions that probably contain text (as given by the feature
	 * map)
	 *
	 * @param fmap
	 *            The feature map calculated from
	 *            {@link #textRegionDetection(FImage)}
	 * @param image
	 *            The original image
	 * @return A map of boundingbox->images that area localised text regions
	 */
	public Map<Rectangle, FImage> textRegionLocalisation(FImage fmap, FImage image)
	{
		// We'll store the localised text regions in this list
		final HashMap<Rectangle, FImage> textAreas = new HashMap<Rectangle, FImage>();

		// Threshold the image to find high probability regions
		final FImage thresh = fmap.clone().normalise().process(new OtsuThreshold());

		// Dilate with a 7x7 structuring element
		final StructuringElement se = new StructuringElement();
		final int ses = 9;
		for (int i = 0; i < ses; i++)
			for (int j = 0; j < ses; j++)
				se.positive.add(new Pixel(i, j));

		final FImage dilated = thresh.process(new Dilate(se));

		if (DEBUG)
			DisplayUtilities.display(dilated, "Candidate text-blobs");

		// We use the connected-component labeller to extract the components.
		final ConnectedComponentLabeler ccl = new
				ConnectedComponentLabeler(ConnectMode.CONNECT_4);
		final List<ConnectedComponent> ccs = ccl.findComponents(dilated);

		System.out.println("Got " + ccs.size() + " connected components.");

		// Now we can filter by iterating over the components
		// Heuristics:
		// Area(region) >= (1/20).max
		int maxArea = 0;
		for (final PixelSet cc : ccs)
			maxArea = Math.max(maxArea, cc.calculateArea());

		// - Remove regions that are too small
		for (final Iterator<ConnectedComponent> cci = ccs.iterator(); cci.hasNext();)
			if (cci.next().calculateArea() < maxArea / 20d)
				cci.remove();

		// Ratio(w/h) = w/h >= 0.2
		// - Remove regions that aren't square enough.
		for (final Iterator<ConnectedComponent> cci = ccs.iterator(); cci.hasNext();)
		{
			final PixelSet cc = cci.next();
			final Rectangle r = cc.calculateRegularBoundingBox();
			if (r.width / r.height < 0.2)
				cci.remove();
		}

		if (DEBUG) {
			final MBFImage bb = new MBFImage(image.getWidth(), image.getHeight(), 3);
			bb.createRenderer().drawImage(image, 0, 0);
			final OrientatedBoundingBoxRenderer<Float[]> obbr = new
					OrientatedBoundingBoxRenderer<Float[]>(bb, new Float[] { 1f, 1f, 0f });
					ConnectedComponent.process(ccs, obbr);
					DisplayUtilities.display(bb);
					System.out.println("Continuing with " + ccs.size() + " connected components.");
		}

		// Extract the text regions from the original image
		for (final PixelSet cc : ccs)
		{
			if (cc.getPixels().size() < 20)
				continue;

			// Extract from the image the text region
			final Rectangle r = cc.calculateRegularBoundingBox();
			r.scaleCentroid(boundingBoxPaddingPc);
			FImage textArea = image.extractROI(r);

			// Threshold of the image make it easier to extract MSERs
			final OtsuThreshold o = new OtsuThreshold();
			o.processImage(textArea);

			if (DEBUG)
				DisplayUtilities.display(textArea, "text area - before distortion");

			// // We distort the image to attempt to make the lettering
			// straighter
			// // and more readable for the OCR
			// // We work out the bounding box of the text to distort
			// Polygon p = cc.calculateOrientatedBoundingBox();
			//
			// // and the mapping to distort the text to a regular rectangle
			// List<IndependentPair<Point2d,Point2d>> pointPairs =
			// calculateHomography( p );
			//
			// // Calculate the homography matrix to distort the image.
			// Matrix homoMatrix = TransformUtilities.homographyMatrix(
			// pointPairs );
			//
			// // Transform the image
			// textArea = textArea.transform( homoMatrix );

			final SkewCorrector sc = new SkewCorrector();
			sc.setAccuracy(4);
			textArea = textArea.process(sc);

			// Store the detected text and the distorted image
			textAreas.put(r, textArea);

			if (DEBUG)
				DisplayUtilities.display(textArea, "text area - after distortion");
		}

		return textAreas;
	}

	/**
	 * Calculates the point pairing for a given distorted polygon into
	 * orthogonal space.
	 *
	 * @param p
	 *            The polygon with 4 points
	 * @return A list of point pairs
	 */
	public List<IndependentPair<Point2d, Point2d>> calculateHomography(Polygon p)
	{
		// Our output list
		final List<IndependentPair<Point2d, Point2d>> pointPairs = new
				ArrayList<IndependentPair<Point2d, Point2d>>();

		// Get the vertices
		//
		// p1----------p4
		// | |
		// p2----------p3
		//
		final List<Point2d> v = p.getVertices();
		final Point2d p1 = v.get(0);
		final Point2d p2 = v.get(1);
		final Point2d p3 = v.get(2);
		final Point2d p4 = v.get(3);

		// Mapped vertices
		final Point2d p1p = new Point2dImpl(p2.getX(), p1.getY()); // vertical
																	// above p2
		final Point2d p2p = v.get(1); // don't move
		final Point2d p3p = new Point2dImpl(p3.getX(), p2.getY()); // horizontal
																	// from p2
		final Point2d p4p = new Point2dImpl(p3p.getX(), p1.getY()); // vertical
																	// above p3

		pointPairs.add(new IndependentPair<Point2d, Point2d>(p1, p1p));
		pointPairs.add(new IndependentPair<Point2d, Point2d>(p2, p2p));
		pointPairs.add(new IndependentPair<Point2d, Point2d>(p3, p3p));
		pointPairs.add(new IndependentPair<Point2d, Point2d>(p4, p4p));

		return pointPairs;
	}

	/**
	 * Get the expansion value of the bounding boxes that are generated for the
	 * text regions.
	 *
	 * @return the new bounding box expansion multiplier.
	 */
	public float getBoundingBoxPaddingPc()
	{
		return boundingBoxPaddingPc;
	}

	/**
	 * Set the expansion value for the subimage extraction.
	 *
	 * @param boundingBoxPaddingPc
	 *            the new multiplier
	 */
	public void setBoundingBoxPaddingPc(float boundingBoxPaddingPc)
	{
		this.boundingBoxPaddingPc = boundingBoxPaddingPc;
	}

	/**
	 * Returns a map of bounding box to image and textual string.
	 *
	 * @return A map of image bounding box to subimage and text string.
	 */
	@Override
	public Map<Rectangle, FImage> getTextRegions()
	{
		return this.textRegions;
	}
}
