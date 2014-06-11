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
package org.openimaj.image.feature.local.interest.experiment;

import gnu.trove.list.array.TDoubleArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.feature.local.ScaleSpaceLocation;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.EllipticInterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.feature.local.interest.InterestPointVisualiser;
import org.openimaj.knn.CoordinateKDTree;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.PayloadCoordinate;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * An interest point repeatability as originally implemented <a
 * href="http://www.robots.ox.ac.uk/~vgg/research/affine/evaluation.html"
 * >here</a>.
 * <p>
 * We find some interest points in two images, and the known homography to go
 * from image 1 to image 2
 * <p>
 * We apply this exhaustively to a pairwise matching of each feature to each
 * other feature and compare the distances of the transformed features from the
 * second image to the features in the first image. If the pair distance is
 * below a give threshold they are placed on top of each other and their overlap
 * measured.
 * <p>
 * Repeatability is measured at a given overlap threshold, if two feature point
 * ellipses overlap over a certain percentage of their overall size then those
 * features are counted as repeatable. The repeatability of a given IPD for a
 * given pair of images is the proportion of repeatable features for a given
 * maximum distance and a given overlap percentage.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of {@link InterestPointData}
 */
public class IPDRepeatability<T extends InterestPointData> {

	static Logger logger = Logger.getLogger(IPDRepeatability.class);
	static {
		BasicConfigurator.configure();
		logger.setLevel(Level.INFO);
	}

	/**
	 * A pair of matching features with a score
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 * @param <B>
	 * @param <T>
	 */
	public static class ScoredPair<B extends Comparable<B>, T extends Pair<B>>
			implements Comparable<ScoredPair<B, T>>
	{
		private T pair;
		private double score;

		ScoredPair(T pair, double score) {
			this.pair = pair;
			this.score = score;
		}

		@Override
		public int compareTo(ScoredPair<B, T> that) {
			int diff = Double.compare(this.score, that.score);
			if (diff != 0)
				return -diff;
			else {
				diff = this.pair.firstObject().compareTo(
						that.pair.firstObject());
				if (diff == 0) {
					return this.pair.secondObject().compareTo(
							that.pair.secondObject());
				}
				return diff;
			}
		}
	}

	private Matrix homography;
	private List<Ellipse> validImage2Points;
	private List<Ellipse> validImage1Points;
	private List<ScoredPair<Integer, Pair<Integer>>> prunedOverlapping;
	private double maximumDistanceMultiple = 4;
	private int imageWidth;
	private int imageHeight;

	public IPDRepeatability() {
	}

	/**
	 * Check the repeatability against two imags, two sets of points and a
	 * homography between the two images.
	 * 
	 * @param image1
	 * @param image2
	 * @param image1Points
	 * @param image2Points
	 * @param homography
	 */
	public IPDRepeatability(Image<?, ?> image1, Image<?, ?> image2,
			List<Ellipse> image1Points, List<Ellipse> image2Points,
			Matrix homography)
	{
		setup(image1, image2, image1Points, image2Points, homography);
	}

	/**
	 * Check the repeatability between two images from files, an interest point
	 * detector used to find the feature points in the images and a homography
	 * from a file. The homography file has the format:
	 * 
	 * number number number number number number number number number
	 * 
	 * @param image1f
	 * @param image2f
	 * @param ipd
	 * @param homographyf
	 * @throws IOException
	 */
	public IPDRepeatability(File image1f, File image2f,
			InterestPointDetector<T> ipd, File homographyf) throws IOException
	{
		final MBFImage image1 = ImageUtilities.readMBF(image1f);
		final MBFImage image2 = ImageUtilities.readMBF(image2f);

		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image1));
		final List<T> image1Points = ipd.getInterestPoints(20);
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image2));
		final List<T> image2Points = ipd.getInterestPoints(20);

		final List<Ellipse> image1Ellipse = new ArrayList<Ellipse>();
		final List<Ellipse> image2Ellipse = new ArrayList<Ellipse>();
		for (final InterestPointData d : image1Points)
			image1Ellipse.add(d.getEllipse());
		for (final InterestPointData d : image2Points)
			image2Ellipse.add(d.getEllipse());

		final Matrix homography = readHomography(homographyf);
		setup(image1, image2, image1Ellipse, image2Ellipse, homography);
	}

	/**
	 * Two images, features extracted using ipd, homography found in stream. See
	 * {@link IPDRepeatability}
	 * 
	 * @param image1
	 * @param image2
	 * @param ipd
	 * @param homographyf
	 * @throws IOException
	 */
	public IPDRepeatability(MBFImage image1, MBFImage image2,
			InterestPointDetector<T> ipd, InputStream homographyf)
			throws IOException
	{

		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image1));
		final List<T> image1Points = ipd.getInterestPoints(20);
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image2));
		final List<T> image2Points = ipd.getInterestPoints(20);

		final List<Ellipse> image1Ellipse = new ArrayList<Ellipse>();
		final List<Ellipse> image2Ellipse = new ArrayList<Ellipse>();
		for (final InterestPointData d : image1Points)
			image1Ellipse.add(d.getEllipse());
		for (final InterestPointData d : image2Points)
			image2Ellipse.add(d.getEllipse());

		final Matrix homography = readHomography(homographyf);
		setup(image1, image2, image1Ellipse, image2Ellipse, homography);
	}

	/**
	 * Two images, features extracted using ipd, homography matrix between the
	 * two images
	 * 
	 * @param image1
	 * @param image2
	 * @param ipd
	 * @param homography
	 * @throws IOException
	 */
	public IPDRepeatability(MBFImage image1, MBFImage image2,
			InterestPointDetector<T> ipd, Matrix homography) throws IOException
	{

		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image1));
		final List<T> image1Points = ipd.getInterestPoints(20);
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image2));
		final List<T> image2Points = ipd.getInterestPoints(20);

		final List<Ellipse> image1Ellipse = new ArrayList<Ellipse>();
		final List<Ellipse> image2Ellipse = new ArrayList<Ellipse>();
		for (final InterestPointData d : image1Points)
			image1Ellipse.add(d.getEllipse());
		for (final InterestPointData d : image2Points)
			image2Ellipse.add(d.getEllipse());

		setup(image1, image2, image1Ellipse, image2Ellipse, homography);
	}

	public IPDRepeatability(List<Ellipse> firstImagePoints,
			List<Ellipse> secondImagePoints, Matrix transform)
	{
		this.validImage1Points = firstImagePoints;
		this.validImage2Points = secondImagePoints;
		this.homography = transform;
	}

	private void setup(Image<?, ?> image1, Image<?, ?> image2,
			List<Ellipse> image1Points, List<Ellipse> image2Points,
			Matrix homography)
	{
		this.homography = homography;

		this.validImage2Points = IPDRepeatability.validPoints(image2Points,
				image1, homography);
		this.validImage1Points = IPDRepeatability.validPoints(image1Points,
				image2, homography.inverse());
		this.imageWidth = image1.getWidth();
		this.imageHeight = image1.getHeight();
		if (logger.getLevel() == Level.DEBUG) {
			final MBFImage combined = new MBFImage(image1.getWidth()
					+ image2.getWidth(), Math.max(image1.getHeight(),
					image2.getHeight()), ColourSpace.RGB);

			final InterestPointVisualiser<Float[], MBFImage> ipdv1 = new InterestPointVisualiser<Float[], MBFImage>(
					(MBFImage) image1, this.validImage1Points);
			combined.drawImage(
					ipdv1.drawPatches(RGBColour.RED, RGBColour.BLUE), 0, 0);
			final InterestPointVisualiser<Float[], MBFImage> ipdv2 = new InterestPointVisualiser<Float[], MBFImage>(
					(MBFImage) image2, this.validImage2Points);
			combined.drawImage(
					ipdv2.drawPatches(RGBColour.RED, RGBColour.BLUE),
					image1.getWidth(), 0);

			// DisplayUtilities.displayName(combined, "valid points");

		}
	}

	/**
	 * The percentage of valid points found to be repeatable. Repeatability of a
	 * given ellipse is defined by what percentage overlap there is between it
	 * and a nearby detected ellipse (after an affine transform). The
	 * repeatability of all the points in an image is defined by the proprotion
	 * of points which could catch and did match with their ellipses overlapping
	 * by more than or equal to the percentageOverlap (1 == complete overlap, 0
	 * == no overlap)
	 * 
	 * @param percentageOverlap
	 *            the percentage overlap two ellipses must be over to be
	 *            considered a "repeatable" point
	 * @return the percentage of ellipses which are repeatable
	 */
	public double repeatability(double percentageOverlap) {
		prepare();
		final double potentialMatches = Math.min(this.validImage2Points.size(),
				this.validImage1Points.size());
		if (potentialMatches == 0) {
			return 0;
		}
		double countMatches = 0;

		for (final ScoredPair<Integer, Pair<Integer>> d : this.prunedOverlapping) {
			if (d.score >= percentageOverlap)
				countMatches++;
		}
		return countMatches / potentialMatches;
	}

	/**
	 * Find pairs of interest points whose ellipses overlap sufficiently and
	 * calculate how much they overlap. This function must be told what maximum
	 * distance factor is which two interest points are considered to match and
	 * therefore their overlap measured.
	 * 
	 * @return map of an interest point pair to a percentage overlap (0 >
	 *         overlap =<1.0
	 */
	public List<ScoredPair<Integer, Pair<Integer>>> calculateOverlappingEllipses() {
		final int smallerSetSize = Math.min(this.validImage1Points.size(),
				this.validImage2Points.size());
		final PriorityQueue<ScoredPair<Integer, Pair<Integer>>> overlapping = new PriorityQueue<ScoredPair<Integer, Pair<Integer>>>(
				this.validImage1Points.size() * this.validImage2Points.size());
		int oldQueueSize = 0;
		// Map<Integer,Matrix> matrixHash1 = new HashMap<Integer,Matrix>();
		// Map<Integer,Matrix> matrixHash2 = new HashMap<Integer,Matrix>();

		final CoordinateKDTree<PayloadCoordinate<ScaleSpaceLocation, IndependentPair<Integer, Ellipse>>> tree = new CoordinateKDTree<PayloadCoordinate<ScaleSpaceLocation, IndependentPair<Integer, Ellipse>>>();

		int j = 0;
		for (Ellipse ellipse2 : this.validImage2Points) {
			ellipse2 = ellipse2.transformAffine(this.homography.inverse());
			final ScaleSpaceLocation ep = new ScaleSpaceLocation(ellipse2.calculateCentroid()
					.getX(), ellipse2.calculateCentroid().getY(), (float) getRadius(
					ellipse2, this.maximumDistanceMultiple));
			tree.insert(PayloadCoordinate.payload(ep,
					new IndependentPair<Integer, Ellipse>(j, ellipse2)));
			j++;
		}

		int i = 0;
		logger.debug("Checking all ellipses against each other");
		for (final Ellipse ellipse1 : this.validImage1Points) {

			final float radius = (float) getRadius(ellipse1,
					this.maximumDistanceMultiple);

			final List<PayloadCoordinate<ScaleSpaceLocation, IndependentPair<Integer, Ellipse>>> possible = new ArrayList<PayloadCoordinate<ScaleSpaceLocation, IndependentPair<Integer, Ellipse>>>();
			final Point2d left = ellipse1.calculateCentroid();
			left.translate(-radius, -radius);
			final Point2d right = ellipse1.calculateCentroid();
			right.translate(radius, radius);

			final float scaleRadius = (float) (radius * 0.5);

			tree.rangeSearch(possible,
					new ScaleSpaceLocation(left.getX(), left.getY(), radius
							- scaleRadius), new ScaleSpaceLocation(
							right.getX(), right.getY(), radius + scaleRadius));
			// logger.debug("Checking ellipse: " + i + " found: " +
			// possible.size() + " nearby ellipses");

			for (final PayloadCoordinate<ScaleSpaceLocation, IndependentPair<Integer, Ellipse>> payloadCoordinate : possible)
			{
				final IndependentPair<Integer, Ellipse> pl = payloadCoordinate
						.getPayload();
				j = pl.firstObject();
				final Ellipse ellipse2 = pl.secondObject();
				final Matrix e1Mat = EllipseUtilities.ellipseToCovariance(ellipse1)
						.inverse();
				final Matrix e2Mat = EllipseUtilities.ellipseToCovariance(ellipse2)
						.inverse();
				final double overlap = calculateOverlapPercentageOxford(e1Mat, e2Mat,
						ellipse1, ellipse2, maximumDistanceMultiple);
				// double overlap =
				// calculateOverlapPercentage(ellipse1,ellipse2,this.maximumDistanceMultiple);
				// if(logger.getLevel() == Level.DEBUG){
				// double oxfordOverlap =
				// calculateOverlapPercentageOxford(e1Mat, e2Mat, ellipse1,
				// ellipse2, maximumDistanceMultiple);
				// // displayEllipsesFull(ellipse1,ellipse2);
				// // displayEllipsesZoomed(ellipse1,ellipse2);
				//
				// logger.debug("different in overlap: " + Math.abs(overlap -
				// oxfordOverlap));
				//
				//
				// }
				// final float e1x = ellipse1.getCOG().getX();
				// final float e1y = ellipse1.getCOG().getY();
				// final float e2x = ellipse2.getCOG().getX();
				// final float e2y = ellipse2.getCOG().getY();
				// 130.78,y=31.68
				// if(e2x > 130 && e2x < 131 && e2y > 31 && e2y < 32 &&
				// e1x > 100 && e1x < 101 && e1y > 60 && e1y < 61){
				// System.out.println("FOUND IT!!");
				// }
				if (overlap >= 0) {
					// System.out.println(overlap + " Adding: " +
					// ellipse1.getCOG() + " -> " + ellipse2.getCOG() +
					// " with score: " + overlap);
					overlapping.add(new ScoredPair<Integer, Pair<Integer>>(
							new Pair<Integer>(i, j), overlap));
					if (oldQueueSize == overlapping.size()) {
						System.err.println("The queue didn't change in size!!");
					}
					oldQueueSize = overlapping.size();
				}
			}
			// for(Ellipse ellipse2: this.validImage2Points)
			// {

			// ellipse2 = ellipse2.transformAffine(this.homography.inverse());
			//
			// double overlap =
			// calculateOverlapPercentage(ellipse1,ellipse2,this.maximumDistanceMultiple);
			// if(overlap > 0){
			// // System.out.println(overlap + " Adding: " + ellipse1.getCOG() +
			// " -> " + ellipse2.getCOG() + " with score: " + overlap);
			// overlapping.add(new ScoredPair<Integer,Pair<Integer>>(new
			// Pair<Integer>(i,j), overlap));
			// if(oldQueueSize == overlapping.size()){
			// System.err.println("The queue didn't change in size!!");
			// }
			// oldQueueSize = overlapping.size();
			// }

			// }
			i++;
		}
		logger.debug("pruning overlapping ellipses, finding best case for each ellipse");
		final List<ScoredPair<Integer, Pair<Integer>>> prunedOverlapping = pruneOverlapping(
				overlapping, smallerSetSize);
		return prunedOverlapping;
	}

	@SuppressWarnings("unused")
	private static void displayEllipsesZoomed(Ellipse ellipse1, Ellipse ellipse2) {
		final int zoomHeight = 400;
		final int zoomWidth = 400;

		final int midzoomx = zoomWidth / 2;
		final int midzoomy = zoomHeight / 2;

		final double e1Radius = getRadius(ellipse1, 1);

		final double scale = (zoomWidth * 0.50) / e1Radius;
		final Matrix scaleMatrix = TransformUtilities.scaleMatrixAboutPoint(
				1 / scale, 1 / scale, 0, 0);
		final MBFImage zoomed = new MBFImage(zoomWidth, zoomHeight, ColourSpace.RGB);
		Matrix translateE1 = Matrix.identity(3, 3);
		translateE1 = translateE1.times(TransformUtilities
				.translateToPointMatrix(new Point2dImpl(0, 0), new Point2dImpl(
						midzoomx, midzoomy)));
		translateE1 = translateE1.times(scaleMatrix);
		translateE1 = translateE1.times(TransformUtilities
				.translateToPointMatrix(ellipse1.calculateCentroid(),
						new Point2dImpl(0, 0)));

		final Ellipse expandedTranslated1 = ellipse1.transformAffine(translateE1);
		final Ellipse expandedTranslated2 = ellipse2.transformAffine(translateE1);
		zoomed.drawShape(expandedTranslated1, RGBColour.RED);
		zoomed.drawShape(expandedTranslated2, RGBColour.BLUE);

		DisplayUtilities.displayName(zoomed, "zoomed image");
		System.out.println();
	}

	@SuppressWarnings("unused")
	private void displayEllipsesFull(Ellipse ellipse1, Ellipse ellipse2) {
		final MBFImage debugDisplay = new MBFImage(this.imageWidth, this.imageHeight,
				ColourSpace.RGB);
		debugDisplay.drawShape(ellipse1, RGBColour.RED);
		debugDisplay.drawShape(ellipse2, RGBColour.BLUE);
		debugDisplay
				.drawShape(
						ellipse2.calculateRegularBoundingBox().union(
								ellipse1.calculateRegularBoundingBox()),
						RGBColour.BLUE);
		DisplayUtilities.displayName(debugDisplay, "debug display full");
	}

	private static double getRadius(Ellipse ellipse,
			double maximumDistanceFactor)
	{
		double maxDistance = Math
				.sqrt((ellipse.getMajor() * ellipse.getMinor()));
		maxDistance *= maximumDistanceFactor;
		return maxDistance;
	}

	private List<ScoredPair<Integer, Pair<Integer>>> pruneOverlapping(
			PriorityQueue<ScoredPair<Integer, Pair<Integer>>> overlapping,
			int smallerSetSize)
	{
		// Use the priority queue to perform a greedy optimisation.
		// Once you see a pair don't allow any other pair involving either
		// element
		final Set<Integer> seenE1 = new HashSet<Integer>();
		final Set<Integer> seenE2 = new HashSet<Integer>();
		final List<ScoredPair<Integer, Pair<Integer>>> prunedOverlapping = new ArrayList<ScoredPair<Integer, Pair<Integer>>>();
		while (overlapping.size() > 0 && seenE1.size() < smallerSetSize) {
			final ScoredPair<Integer, Pair<Integer>> scoredPair = overlapping.poll();
			if (!(seenE1.contains(scoredPair.pair.firstObject()) || seenE2
					.contains(scoredPair.pair.secondObject())))
			{
				prunedOverlapping.add(scoredPair);
				seenE1.add(scoredPair.pair.firstObject());
				seenE2.add(scoredPair.pair.secondObject());
			} else {
			}
		}
		return prunedOverlapping;
	}

	/**
	 * Generates and initialises a new Repeatability instance. The percentage of
	 * valid points found to be repeatable. Repeatability of a given ellipse is
	 * defined by what percentage overlap there is between it and a nearby
	 * detected ellipse (after an affine transform). The repeatability of all
	 * the points in an image is defined by the proprotion of points which could
	 * catch and did match with their ellipses overlapping by more than or equal
	 * to the percentageOverlap (1 == complete overlap, 0 == no overlap)
	 * 
	 * @param img1
	 * @param img2
	 * @param e1
	 * @param e2
	 * @param transform
	 * 
	 * @param maximumDistanceMultiple
	 *            The distance multiple at which point two interest points are
	 *            considered to be "close"
	 * @return the percentage of ellipses which are repeatable
	 */
	public static IPDRepeatability<EllipticInterestPointData> repeatability(
			Image<?, ?> img1, Image<?, ?> img2, List<Ellipse> e1,
			List<Ellipse> e2, Matrix transform, double maximumDistanceMultiple)
	{
		final IPDRepeatability<EllipticInterestPointData> rep = new IPDRepeatability<EllipticInterestPointData>(
				img1, img2, e1, e2, transform);
		rep.maximumDistanceMultiple = maximumDistanceMultiple;
		rep.prepare();
		return rep;
	}

	public static <T extends InterestPointData> IPDRepeatability<T> repeatability(
			MBFImage image1, MBFImage image2, List<T> interestPoints1,
			List<T> interestPoints2, Matrix transform,
			int maximumDistanceMultiple2)
	{
		final List<Ellipse> image1Ellipse = new ArrayList<Ellipse>();
		final List<Ellipse> image2Ellipse = new ArrayList<Ellipse>();
		for (final InterestPointData d : interestPoints1)
			image1Ellipse.add(d.getEllipse());
		for (final InterestPointData d : interestPoints2)
			image2Ellipse.add(d.getEllipse());

		logger.debug("Comparing: " + image1Ellipse.size() + " to "
				+ image2Ellipse.size() + " ellipses");

		final IPDRepeatability<T> rep = new IPDRepeatability<T>(image1, image2,
				image1Ellipse, image2Ellipse, transform);
		rep.maximumDistanceMultiple = 4;
		rep.prepare();
		return rep;
	}

	private void prepare() {
		if (this.prunedOverlapping == null)
			this.prunedOverlapping = calculateOverlappingEllipses();
	}

	/**
	 * Use the transform to call find the location sourceImage.getBounds() in
	 * another image. Drop points from allPoints which are not within the
	 * transformed bounds
	 * 
	 * @param allPoints
	 * @param sourceImage
	 * @param transform
	 * @return ellipses
	 */
	public static List<Ellipse> validPoints(List<Ellipse> allPoints,
			Image<?, ?> sourceImage, Matrix transform)
	{
		final List<Ellipse> valid = new ArrayList<Ellipse>();
		final Rectangle validArea = sourceImage.getBounds();
		for (final Ellipse data : allPoints) {
			if (data.calculateCentroid().getX() == 294.079f
					&& data.calculateCentroid().getY() == 563.356f)
			{
				System.out.println();
			}
			// data.getCOG().transform(transform.inverse())
			;
			if (validArea.isInside(data.transformAffine(transform.inverse())
					.calculateRegularBoundingBox()))
			{
				valid.add(data);
			} else {
			}
		}
		return valid;
	}

	public static List<ScoredPair<Integer, Pair<Integer>>> calculateOverlappingEllipses(
			List<Ellipse> firstImagePoints, List<Ellipse> secondImagePoints,
			Matrix transform, double maximumDistanceMultiple)
	{
		final IPDRepeatability<EllipticInterestPointData> rep = new IPDRepeatability<EllipticInterestPointData>(
				firstImagePoints, secondImagePoints, transform);
		rep.maximumDistanceMultiple = maximumDistanceMultiple;
		return rep.calculateOverlappingEllipses();
	}

	public static Matrix readHomography(File homographyf) throws IOException {
		return readHomography(new FileInputStream(homographyf));
	}

	public static Matrix readHomography(InputStream homographyf)
			throws IOException
	{
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				homographyf));
		final List<TDoubleArrayList> doubleListList = new ArrayList<TDoubleArrayList>();
		String line = null;
		int nCols = -1;
		int nRows = 0;
		while ((line = reader.readLine()) != null) {
			boolean anyAdded = false;
			final String[] parts = line.split(" ");
			final TDoubleArrayList doubleList = new TDoubleArrayList();
			int currCols = 0;
			for (final String part : parts) {
				if (part.length() != 0) {
					anyAdded = true;
					doubleList.add(Double.parseDouble(part));
					currCols++;
				}
			}
			if (nCols == -1)
				nCols = currCols;
			if (currCols != nCols)
				throw new IOException("Could not read matrix file");
			if (anyAdded) {
				doubleListList.add(doubleList);
				nRows++;
			}
		}
		final Matrix ret = new Matrix(nRows, nCols);
		int rowNumber = 0;
		for (final TDoubleArrayList doubleList : doubleListList) {
			doubleList.toArray(ret.getArray()[rowNumber++], 0, nCols);
		}
		return ret;
	}

	/**
	 * The overlap of a pair of ellipses (doesn't give the same results as the
	 * oxford implementation below, TODO: FIXME :)
	 * 
	 * @param e1
	 * @param e2
	 * @param maximumDistanceFactor
	 * @return the percentage overlap with a maximum distance scaled by
	 *         maximumDistanceFactor * ellipse scale
	 */
	public double calculateOverlapPercentage(Ellipse e1, Ellipse e2,
			double maximumDistanceFactor)
	{
		final double maxDistance = getRadius(e1, maximumDistanceFactor);
		// This is a scaling of the two ellipses such that they fit in a
		// normalised space
		final double scaleFactor = getScaleFactor(e1);
		// System.out.println("Maximum distance was: " + maxDistance);
		if (new Line2d(e1.calculateCentroid(), e2.calculateCentroid()).calculateLength() >= maxDistance)
			return 0;
		final Matrix scaleMatrix1 = TransformUtilities.scaleMatrixAboutPoint(
				scaleFactor, scaleFactor, e1.calculateCentroid());
		final Matrix scaleMatrix2 = TransformUtilities.scaleMatrixAboutPoint(
				scaleFactor, scaleFactor, e2.calculateCentroid());
		// Matrix scaleMatrix = TransformUtilities.scaleMatrix(1.0f, 1.0f);

		// Matrix e1Trans =
		// TransformUtilities.translateToPointMatrix(e1.getCOG(),new
		// Point2dImpl(0,0));
		// Matrix e2Trans =
		// TransformUtilities.translateToPointMatrix(e2.getCOG(),new
		// Point2dImpl(0,0));
		final Ellipse e1Corrected = e1.transformAffine(scaleMatrix1);
		final Ellipse e2Corrected = e2.transformAffine(scaleMatrix2);

		// displayEllipsesFull(e1Corrected,e2Corrected);

		final double intersection = e1Corrected.intersectionArea(e2Corrected, 50);
		// return intersection;

		final double e1Area = e1Corrected.calculateArea();
		final double e2Area = e2Corrected.calculateArea();

		return intersection
				/ ((e1Area - intersection) + (e2Area - intersection) + intersection);
	}

	/**
	 * This is how the original matlab found the difference between two
	 * ellipses. Basically, if ellipse 1 and 2 were within a certain distance
	 * the ellipses were placed on top of each other (i.e. same centroid) and
	 * the difference between them calculated. This is simulated in a much saner
	 * way in calculateOverlapPercentage
	 * 
	 * @param e1Mat
	 * @param e2Mat
	 * @param e1
	 * @param e2
	 * @param multiplier
	 * @return the overlap percentage as calculated the matlab way (uses the
	 *         covariance matricies of the ellipses)
	 */
	public double calculateOverlapPercentageOxford(Matrix e1Mat, Matrix e2Mat,
			Ellipse e1, Ellipse e2, double multiplier)
	{
		double maxDistance = Math.sqrt((e1.getMajor() * e1.getMinor()));

		// This is a scaling of the two ellipses such that they fit in a
		// normalised space
		double scaleFactor = 30 / maxDistance;
		scaleFactor = 1 / (scaleFactor * scaleFactor);
		maxDistance *= multiplier;
		if (new Line2d(e1.calculateCentroid(), e2.calculateCentroid()).calculateLength() >= maxDistance)
			return -1;
		// System.out.println(maxDistance);
		final float dx = e2.calculateCentroid().getX() - e1.calculateCentroid().getX();
		final float dy = e2.calculateCentroid().getY() - e1.calculateCentroid().getY();

		final double yy1 = e1Mat.get(1, 1) * scaleFactor;
		final double xx1 = e1Mat.get(0, 0) * scaleFactor;
		final double xy1 = e1Mat.get(0, 1) * scaleFactor;

		final double yy2 = e2Mat.get(1, 1) * scaleFactor;
		final double xx2 = e2Mat.get(0, 0) * scaleFactor;
		final double xy2 = e2Mat.get(0, 1) * scaleFactor;

		// Ellipse e1Corrected =
		// EllipseUtilities.ellipseFromCovariance(e1.getCOG().getX(),
		// e1.getCOG().getY(), new Matrix(new
		// double[][]{{xx1,xy1},{xy1,yy1}}).inverse(), 1.0f);
		// Ellipse e2Corrected =
		// EllipseUtilities.ellipseFromCovariance(e2.getCOG().getX(),
		// e2.getCOG().getY(), new Matrix(new
		// double[][]{{xx2,xy2},{xy2,yy2}}).inverse(), 1.0f);
		// displayEllipsesFull(e1Corrected,e2Corrected);

		final double e1Width = Math.sqrt(yy1 / (xx1 * yy1 - xy1 * xy1));
		final double e1Height = Math.sqrt(xx1 / (xx1 * yy1 - xy1 * xy1));

		final double e2Width = Math.sqrt(yy2 / (xx2 * yy2 - xy2 * xy2));
		final double e2Height = Math.sqrt(xx2 / (xx2 * yy2 - xy2 * xy2));
		final float maxx = (float) Math.ceil((e1Width > (dx + e2Width)) ? e1Width
				: (dx + e2Width));
		final float minx = (float) Math
				.floor((-e1Width < (dx - e2Width)) ? (-e1Width)
						: (dx - e2Width));
		final float maxy = (float) Math.ceil((e1Height > (dy + e2Height)) ? e1Height
				: (dy + e2Height));
		final float miny = (float) Math
				.floor((-e1Height < (dy - e2Height)) ? (-e1Height)
						: (dy - e2Height));

		final float mina = (maxx - minx) < (maxy - miny) ? (maxx - minx)
				: (maxy - miny);
		final float dr = (float) (mina / 50.0);

		int bua = 0;
		int bna = 0;
		// int t1 = 0;

		// Rectangle r = new Rectangle(minx,miny,maxx - minx,maxy-miny);
		// System.out.println("Oxford rectangle: " + r);
		// System.out.println("Oxford step: " + dr);
		// compute the area
		for (float rx = minx; rx <= maxx; rx += dr) {
			final float rx2 = rx - dx;
			// t1++;
			for (float ry = miny; ry <= maxy; ry += dr) {
				final float ry2 = ry - dy;
				// compute the distance from the ellipse center
				final float a = (float) (xx1 * rx * rx + 2 * xy1 * rx * ry + yy1 * ry
						* ry);
				final float b = (float) (xx2 * rx2 * rx2 + 2 * xy2 * rx2 * ry2 + yy2
						* ry2 * ry2);
				// compute the area
				if (a < 1 && b < 1)
					bna++;
				if (a < 1 || b < 1)
					bua++;
			}
		}
		return (double) bna / (double) bua;
	}

	private static double getScaleFactor(Ellipse ellipse) {
		final double maxDistance = Math
				.sqrt((ellipse.getMajor() * ellipse.getMinor()));
		// This is a scaling of the two ellipses such that they fit in a
		// normalised space
		final double scaleFactor = 30 / maxDistance;
		// scaleFactor = (scaleFactor);

		return scaleFactor;
	}

	/**
	 * Read an ellipses from the matlab interest point files
	 * 
	 * @param inputStream
	 * @return list of ellipses
	 * @throws IOException
	 */
	public static List<Ellipse> readMatlabInterestPoints(InputStream inputStream)
			throws IOException
	{
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		reader.readLine(); // 1.0
		reader.readLine(); // nPoints

		String line = "";
		final List<Ellipse> ret = new ArrayList<Ellipse>();
		while ((line = reader.readLine()) != null) {
			final String[] parts = line.split(" ");

			final float x = Float.parseFloat(parts[0]);
			final float y = Float.parseFloat(parts[1]);
			final float xx = Float.parseFloat(parts[2]);
			final float xy = Float.parseFloat(parts[3]);
			final float yy = Float.parseFloat(parts[4]);

			final Ellipse e = EllipseUtilities.ellipseFromCovariance(x, y,
					new Matrix(new double[][] { { xx, xy }, { xy, yy } })
							.inverse(), 1.0f);
			ret.add(e);
		}
		return ret;
	}

	public static void main(String args[]) throws IOException {
		testSingleEllipseFromMatlab();
	}

	/**
	 * Check the overlap of a single ellipse using covariance numbrers loaded
	 * from matlab
	 * 
	 * @throws IOException
	 */
	public static void testSingleEllipseFromMatlab() throws IOException {
		final Matrix secondMoments1_220_0 = new Matrix(new double[][] {
				{ 0.002523f, -0.000888f }, { -0.000888f, 0.000802f } });
		final Matrix secondMoments2_220_0 = new Matrix(new double[][] {
				{ 0.000788f, -0.000406f }, { -0.000406f, 0.000668f } });
		final Ellipse e1_220_0 = EllipseUtilities.ellipseFromCovariance(185.130000f,
				139.150000f, secondMoments1_220_0.inverse(), 1.0f);
		final Ellipse e2_220_0 = EllipseUtilities.ellipseFromCovariance(185.287320f,
				137.549020f, secondMoments2_220_0.inverse(), 1.0f);
		doTest(secondMoments1_220_0, secondMoments2_220_0, e1_220_0, e2_220_0,
				0.533756f);
		final Matrix secondMoments1_265_0 = new Matrix(new double[][] {
				{ 0.002523f, -0.000888f }, { -0.000888f, 0.000802f } });
		final Matrix secondMoments2_265_0 = new Matrix(new double[][] {
				{ 0.000424f, -0.000377f }, { -0.000377f, 0.000993f } });
		final Ellipse e1_265_0 = EllipseUtilities.ellipseFromCovariance(185.130000f,
				139.150000f, secondMoments1_265_0.inverse(), 1.0f);
		final Ellipse e2_265_0 = EllipseUtilities.ellipseFromCovariance(178.697624f,
				134.167477f, secondMoments2_265_0.inverse(), 1.0f);
		doTest(secondMoments1_265_0, secondMoments2_265_0, e1_265_0, e2_265_0,
				0.391304f);
		final Matrix secondMoments1_424_0 = new Matrix(new double[][] {
				{ 0.002523f, -0.000888f }, { -0.000888f, 0.000802f } });
		final Matrix secondMoments2_424_0 = new Matrix(new double[][] {
				{ 0.000315f, -0.000280f }, { -0.000280f, 0.000851f } });
		final Ellipse e1_424_0 = EllipseUtilities.ellipseFromCovariance(185.130000f,
				139.150000f, secondMoments1_424_0.inverse(), 1.0f);
		final Ellipse e2_424_0 = EllipseUtilities.ellipseFromCovariance(177.858951f,
				134.845534f, secondMoments2_424_0.inverse(), 1.0f);
		doTest(secondMoments1_424_0, secondMoments2_424_0, e1_424_0, e2_424_0,
				0.345265f);
		final Matrix secondMoments1_548_0 = new Matrix(new double[][] {
				{ 0.002523f, -0.000888f }, { -0.000888f, 0.000802f } });
		final Matrix secondMoments2_548_0 = new Matrix(new double[][] {
				{ 0.000364f, -0.000245f }, { -0.000245f, 0.000488f } });
		final Ellipse e1_548_0 = EllipseUtilities.ellipseFromCovariance(185.130000f,
				139.150000f, secondMoments1_548_0.inverse(), 1.0f);
		final Ellipse e2_548_0 = EllipseUtilities.ellipseFromCovariance(183.986308f,
				136.929409f, secondMoments2_548_0.inverse(), 1.0f);
		doTest(secondMoments1_548_0, secondMoments2_548_0, e1_548_0, e2_548_0,
				0.309550f);
		final Matrix secondMoments1_664_0 = new Matrix(new double[][] {
				{ 0.002523f, -0.000888f }, { -0.000888f, 0.000802f } });
		final Matrix secondMoments2_664_0 = new Matrix(new double[][] {
				{ 0.000205f, -0.000190f }, { -0.000190f, 0.000621f } });
		final Ellipse e1_664_0 = EllipseUtilities.ellipseFromCovariance(185.130000f,
				139.150000f, secondMoments1_664_0.inverse(), 1.0f);
		final Ellipse e2_664_0 = EllipseUtilities.ellipseFromCovariance(177.752180f,
				135.828765f, secondMoments2_664_0.inverse(), 1.0f);
		doTest(secondMoments1_664_0, secondMoments2_664_0, e1_664_0, e2_664_0,
				0.269426f);
		final Matrix secondMoments1_772_0 = new Matrix(new double[][] {
				{ 0.002523f, -0.000888f }, { -0.000888f, 0.000802f } });
		final Matrix secondMoments2_772_0 = new Matrix(new double[][] {
				{ 0.000148f, -0.000150f }, { -0.000150f, 0.000561f } });
		final Ellipse e1_772_0 = EllipseUtilities.ellipseFromCovariance(185.130000f,
				139.150000f, secondMoments1_772_0.inverse(), 1.0f);
		final Ellipse e2_772_0 = EllipseUtilities.ellipseFromCovariance(176.399164f,
				135.967058f, secondMoments2_772_0.inverse(), 1.0f);
		doTest(secondMoments1_772_0, secondMoments2_772_0, e1_772_0, e2_772_0,
				0.220970f);
		final Matrix secondMoments1_944_0 = new Matrix(new double[][] {
				{ 0.002523f, -0.000888f }, { -0.000888f, 0.000802f } });
		final Matrix secondMoments2_944_0 = new Matrix(new double[][] {
				{ 0.000099f, -0.000059f }, { -0.000059f, 0.000309f } });
		final Ellipse e1_944_0 = EllipseUtilities.ellipseFromCovariance(185.130000f,
				139.150000f, secondMoments1_944_0.inverse(), 1.0f);
		final Ellipse e2_944_0 = EllipseUtilities.ellipseFromCovariance(176.545412f,
				138.198624f, secondMoments2_944_0.inverse(), 1.0f);
		doTest(secondMoments1_944_0, secondMoments2_944_0, e1_944_0, e2_944_0,
				0.146820f);
		final Matrix secondMoments1_1199_0 = new Matrix(new double[][] {
				{ 0.002523f, -0.000888f }, { -0.000888f, 0.000802f } });
		final Matrix secondMoments2_1199_0 = new Matrix(new double[][] {
				{ 0.000071f, 0.000024f }, { 0.000024f, 0.000094f } });
		final Ellipse e1_1199_0 = EllipseUtilities.ellipseFromCovariance(185.130000f,
				139.150000f, secondMoments1_1199_0.inverse(), 1.0f);
		final Ellipse e2_1199_0 = EllipseUtilities.ellipseFromCovariance(175.623656f,
				142.291151f, secondMoments2_1199_0.inverse(), 1.0f);
		doTest(secondMoments1_1199_0, secondMoments2_1199_0, e1_1199_0,
				e2_1199_0, 0.069626f);
		final Matrix secondMoments1_1269_0 = new Matrix(new double[][] {
				{ 0.002523f, -0.000888f }, { -0.000888f, 0.000802f } });
		final Matrix secondMoments2_1269_0 = new Matrix(new double[][] {
				{ 0.000070f, 0.000034f }, { 0.000034f, 0.000058f } });
		final Ellipse e1_1269_0 = EllipseUtilities.ellipseFromCovariance(185.130000f,
				139.150000f, secondMoments1_1269_0.inverse(), 1.0f);
		final Ellipse e2_1269_0 = EllipseUtilities.ellipseFromCovariance(171.919962f,
				142.703652f, secondMoments2_1269_0.inverse(), 1.0f);
		doTest(secondMoments1_1269_0, secondMoments2_1269_0, e1_1269_0,
				e2_1269_0, 0.048306f);
		final Matrix secondMoments1_3_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_3_1 = new Matrix(new double[][] {
				{ 0.000724f, 0.000618f }, { 0.000618f, 0.001520f } });
		final Ellipse e1_3_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_3_1.inverse(), 1.0f);
		final Ellipse e2_3_1 = EllipseUtilities.ellipseFromCovariance(280.304662f,
				140.134832f, secondMoments2_3_1.inverse(), 1.0f);
		doTest(secondMoments1_3_1, secondMoments2_3_1, e1_3_1, e2_3_1,
				0.523566f);
		final Matrix secondMoments1_4_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_4_1 = new Matrix(new double[][] {
				{ 0.001578f, 0.000486f }, { 0.000486f, 0.000631f } });
		final Ellipse e1_4_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_4_1.inverse(), 1.0f);
		final Ellipse e2_4_1 = EllipseUtilities.ellipseFromCovariance(272.361295f,
				144.968486f, secondMoments2_4_1.inverse(), 1.0f);
		doTest(secondMoments1_4_1, secondMoments2_4_1, e1_4_1, e2_4_1,
				0.751560f);
		final Matrix secondMoments1_154_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_154_1 = new Matrix(new double[][] {
				{ 0.000593f, 0.000661f }, { 0.000661f, 0.001310f } });
		final Ellipse e1_154_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_154_1.inverse(), 1.0f);
		final Ellipse e2_154_1 = EllipseUtilities.ellipseFromCovariance(280.763026f,
				139.853156f, secondMoments2_154_1.inverse(), 1.0f);
		doTest(secondMoments1_154_1, secondMoments2_154_1, e1_154_1, e2_154_1,
				0.417829f);
		final Matrix secondMoments1_156_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_156_1 = new Matrix(new double[][] {
				{ 0.000644f, 0.000344f }, { 0.000344f, 0.000676f } });
		final Ellipse e1_156_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_156_1.inverse(), 1.0f);
		final Ellipse e2_156_1 = EllipseUtilities.ellipseFromCovariance(275.113701f,
				140.124526f, secondMoments2_156_1.inverse(), 1.0f);
		doTest(secondMoments1_156_1, secondMoments2_156_1, e1_156_1, e2_156_1,
				0.502418f);
		final Matrix secondMoments1_157_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_157_1 = new Matrix(new double[][] {
				{ 0.001396f, 0.000382f }, { 0.000382f, 0.000352f } });
		final Ellipse e1_157_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_157_1.inverse(), 1.0f);
		final Ellipse e2_157_1 = EllipseUtilities.ellipseFromCovariance(271.708659f,
				149.102793f, secondMoments2_157_1.inverse(), 1.0f);
		doTest(secondMoments1_157_1, secondMoments2_157_1, e1_157_1, e2_157_1,
				0.530884f);
		final Matrix secondMoments1_261_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_261_1 = new Matrix(new double[][] {
				{ 0.000609f, 0.000352f }, { 0.000352f, 0.000554f } });
		final Ellipse e1_261_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_261_1.inverse(), 1.0f);
		final Ellipse e2_261_1 = EllipseUtilities.ellipseFromCovariance(275.617119f,
				141.717819f, secondMoments2_261_1.inverse(), 1.0f);
		doTest(secondMoments1_261_1, secondMoments2_261_1, e1_261_1, e2_261_1,
				0.416771f);
		final Matrix secondMoments1_513_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_513_1 = new Matrix(new double[][] {
				{ 0.000741f, 0.000225f }, { 0.000225f, 0.000221f } });
		final Ellipse e1_513_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_513_1.inverse(), 1.0f);
		final Ellipse e2_513_1 = EllipseUtilities.ellipseFromCovariance(272.495410f,
				149.946936f, secondMoments2_513_1.inverse(), 1.0f);
		doTest(secondMoments1_513_1, secondMoments2_513_1, e1_513_1, e2_513_1,
				0.302778f);
		final Matrix secondMoments1_545_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_545_1 = new Matrix(new double[][] {
				{ 0.000629f, 0.000270f }, { 0.000270f, 0.000285f } });
		final Ellipse e1_545_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_545_1.inverse(), 1.0f);
		final Ellipse e2_545_1 = EllipseUtilities.ellipseFromCovariance(273.264727f,
				150.116431f, secondMoments2_545_1.inverse(), 1.0f);
		doTest(secondMoments1_545_1, secondMoments2_545_1, e1_545_1, e2_545_1,
				0.295045f);
		final Matrix secondMoments1_769_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_769_1 = new Matrix(new double[][] {
				{ 0.000386f, 0.000222f }, { 0.000222f, 0.000248f } });
		final Ellipse e1_769_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_769_1.inverse(), 1.0f);
		final Ellipse e2_769_1 = EllipseUtilities.ellipseFromCovariance(275.391351f,
				152.353229f, secondMoments2_769_1.inverse(), 1.0f);
		doTest(secondMoments1_769_1, secondMoments2_769_1, e1_769_1, e2_769_1,
				0.194111f);
		final Matrix secondMoments1_995_1 = new Matrix(new double[][] {
				{ 0.001639f, 0.000229f }, { 0.000229f, 0.000785f } });
		final Matrix secondMoments2_995_1 = new Matrix(new double[][] {
				{ 0.000217f, 0.000108f }, { 0.000108f, 0.000131f } });
		final Ellipse e1_995_1 = EllipseUtilities.ellipseFromCovariance(273.460000f,
				145.200000f, secondMoments1_995_1.inverse(), 1.0f);
		final Ellipse e2_995_1 = EllipseUtilities.ellipseFromCovariance(284.495607f,
				151.936791f, secondMoments2_995_1.inverse(), 1.0f);
		doTest(secondMoments1_995_1, secondMoments2_995_1, e1_995_1, e2_995_1,
				0.114420f);
		final Matrix secondMoments1_1015_2 = new Matrix(new double[][] {
				{ 0.001145f, -0.000044f }, { -0.000044f, 0.001080f } });
		final Matrix secondMoments2_1015_2 = new Matrix(new double[][] {
				{ 0.000157f, 0.000009f }, { 0.000009f, 0.000102f } });
		final Ellipse e1_1015_2 = EllipseUtilities.ellipseFromCovariance(332.750000f,
				191.180000f, secondMoments1_1015_2.inverse(), 1.0f);
		final Ellipse e2_1015_2 = EllipseUtilities.ellipseFromCovariance(334.840142f,
				197.358808f, secondMoments2_1015_2.inverse(), 1.0f);
		doTest(secondMoments1_1015_2, secondMoments2_1015_2, e1_1015_2,
				e2_1015_2, 0.113011f);
		final Matrix secondMoments1_1125_2 = new Matrix(new double[][] {
				{ 0.001145f, -0.000044f }, { -0.000044f, 0.001080f } });
		final Matrix secondMoments2_1125_2 = new Matrix(new double[][] {
				{ 0.000117f, -0.000017f }, { -0.000017f, 0.000065f } });
		final Ellipse e1_1125_2 = EllipseUtilities.ellipseFromCovariance(332.750000f,
				191.180000f, secondMoments1_1125_2.inverse(), 1.0f);
		final Ellipse e2_1125_2 = EllipseUtilities.ellipseFromCovariance(336.496971f,
				184.812171f, secondMoments2_1125_2.inverse(), 1.0f);
		doTest(secondMoments1_1125_2, secondMoments2_1125_2, e1_1125_2,
				e2_1125_2, 0.076743f);
		final Matrix secondMoments1_1200_2 = new Matrix(new double[][] {
				{ 0.001145f, -0.000044f }, { -0.000044f, 0.001080f } });
		final Matrix secondMoments2_1200_2 = new Matrix(new double[][] {
				{ 0.000090f, -0.000005f }, { -0.000005f, 0.000053f } });
		final Ellipse e1_1200_2 = EllipseUtilities.ellipseFromCovariance(332.750000f,
				191.180000f, secondMoments1_1200_2.inverse(), 1.0f);
		final Ellipse e2_1200_2 = EllipseUtilities.ellipseFromCovariance(336.907334f,
				186.759097f, secondMoments2_1200_2.inverse(), 1.0f);
		doTest(secondMoments1_1200_2, secondMoments2_1200_2, e1_1200_2,
				e2_1200_2, 0.061274f);
		final Matrix secondMoments1_1227_2 = new Matrix(new double[][] {
				{ 0.001145f, -0.000044f }, { -0.000044f, 0.001080f } });
		final Matrix secondMoments2_1227_2 = new Matrix(new double[][] {
				{ 0.000069f, -0.000007f }, { -0.000007f, 0.000045f } });
		final Ellipse e1_1227_2 = EllipseUtilities.ellipseFromCovariance(332.750000f,
				191.180000f, secondMoments1_1227_2.inverse(), 1.0f);
		final Ellipse e2_1227_2 = EllipseUtilities.ellipseFromCovariance(334.020375f,
				183.709935f, secondMoments2_1227_2.inverse(), 1.0f);
		doTest(secondMoments1_1227_2, secondMoments2_1227_2, e1_1227_2,
				e2_1227_2, 0.048780f);
		final Matrix secondMoments1_56_3 = new Matrix(new double[][] {
				{ 0.002428f, 0.000424f }, { 0.000424f, 0.000583f } });
		final Matrix secondMoments2_56_3 = new Matrix(new double[][] {
				{ 0.001432f, 0.000331f }, { 0.000331f, 0.000360f } });
		final Ellipse e1_56_3 = EllipseUtilities.ellipseFromCovariance(435.600000f,
				191.180000f, secondMoments1_56_3.inverse(), 1.0f);
		final Ellipse e2_56_3 = EllipseUtilities.ellipseFromCovariance(435.054090f,
				192.225532f, secondMoments2_56_3.inverse(), 1.0f);
		doTest(secondMoments1_56_3, secondMoments2_56_3, e1_56_3, e2_56_3,
				0.572848f);
		final Matrix secondMoments1_158_3 = new Matrix(new double[][] {
				{ 0.002428f, 0.000424f }, { 0.000424f, 0.000583f } });
		final Matrix secondMoments2_158_3 = new Matrix(new double[][] {
				{ 0.001311f, 0.000220f }, { 0.000220f, 0.000257f } });
		final Ellipse e1_158_3 = EllipseUtilities.ellipseFromCovariance(435.600000f,
				191.180000f, secondMoments1_158_3.inverse(), 1.0f);
		final Ellipse e2_158_3 = EllipseUtilities.ellipseFromCovariance(435.869415f,
				192.424474f, secondMoments2_158_3.inverse(), 1.0f);
		doTest(secondMoments1_158_3, secondMoments2_158_3, e1_158_3, e2_158_3,
				0.483477f);
		final Matrix secondMoments1_428_4 = new Matrix(new double[][] {
				{ 0.001743f, -0.000279f }, { -0.000279f, 0.000753f } });
		final Matrix secondMoments2_428_4 = new Matrix(new double[][] {
				{ 0.000641f, -0.000135f }, { -0.000135f, 0.000210f } });
		final Ellipse e1_428_4 = EllipseUtilities.ellipseFromCovariance(492.470000f,
				228.690000f, secondMoments1_428_4.inverse(), 1.0f);
		final Ellipse e2_428_4 = EllipseUtilities.ellipseFromCovariance(491.933851f,
				234.396223f, secondMoments2_428_4.inverse(), 1.0f);
		doTest(secondMoments1_428_4, secondMoments2_428_4, e1_428_4, e2_428_4,
				0.305591f);
		final Matrix secondMoments1_516_4 = new Matrix(new double[][] {
				{ 0.001743f, -0.000279f }, { -0.000279f, 0.000753f } });
		final Matrix secondMoments2_516_4 = new Matrix(new double[][] {
				{ 0.000892f, 0.000166f }, { 0.000166f, 0.000125f } });
		final Ellipse e1_516_4 = EllipseUtilities.ellipseFromCovariance(492.470000f,
				228.690000f, secondMoments1_516_4.inverse(), 1.0f);
		final Ellipse e2_516_4 = EllipseUtilities.ellipseFromCovariance(492.855834f,
				226.034456f, secondMoments2_516_4.inverse(), 1.0f);
		doTest(secondMoments1_516_4, secondMoments2_516_4, e1_516_4, e2_516_4,
				0.259701f);
		final Matrix secondMoments1_627_4 = new Matrix(new double[][] {
				{ 0.001743f, -0.000279f }, { -0.000279f, 0.000753f } });
		final Matrix secondMoments2_627_4 = new Matrix(new double[][] {
				{ 0.000634f, 0.000133f }, { 0.000133f, 0.000112f } });
		final Ellipse e1_627_4 = EllipseUtilities.ellipseFromCovariance(492.470000f,
				228.690000f, secondMoments1_627_4.inverse(), 1.0f);
		final Ellipse e2_627_4 = EllipseUtilities.ellipseFromCovariance(491.876614f,
				226.470534f, secondMoments2_627_4.inverse(), 1.0f);
		doTest(secondMoments1_627_4, secondMoments2_627_4, e1_627_4, e2_627_4,
				0.207720f);
		final Matrix secondMoments1_669_4 = new Matrix(new double[][] {
				{ 0.001743f, -0.000279f }, { -0.000279f, 0.000753f } });
		final Matrix secondMoments2_669_4 = new Matrix(new double[][] {
				{ 0.000456f, -0.000060f }, { -0.000060f, 0.000131f } });
		final Ellipse e1_669_4 = EllipseUtilities.ellipseFromCovariance(492.470000f,
				228.690000f, secondMoments1_669_4.inverse(), 1.0f);
		final Ellipse e2_669_4 = EllipseUtilities.ellipseFromCovariance(491.162552f,
				235.045782f, secondMoments2_669_4.inverse(), 1.0f);
		doTest(secondMoments1_669_4, secondMoments2_669_4, e1_669_4, e2_669_4,
				0.213105f);
		final Matrix secondMoments1_1149_4 = new Matrix(new double[][] {
				{ 0.001743f, -0.000279f }, { -0.000279f, 0.000753f } });
		final Matrix secondMoments2_1149_4 = new Matrix(new double[][] {
				{ 0.000116f, -0.000035f }, { -0.000035f, 0.000059f } });
		final Ellipse e1_1149_4 = EllipseUtilities.ellipseFromCovariance(492.470000f,
				228.690000f, secondMoments1_1149_4.inverse(), 1.0f);
		final Ellipse e2_1149_4 = EllipseUtilities.ellipseFromCovariance(491.609239f,
				242.634937f, secondMoments2_1149_4.inverse(), 1.0f);
		doTest(secondMoments1_1149_4, secondMoments2_1149_4, e1_1149_4,
				e2_1149_4, 0.067179f);
		final Matrix secondMoments1_365_5 = new Matrix(new double[][] {
				{ 0.000958f, -0.000971f }, { -0.000971f, 0.002273f } });
		final Matrix secondMoments2_365_5 = new Matrix(new double[][] {
				{ 0.000309f, -0.000272f }, { -0.000272f, 0.000808f } });
		final Ellipse e1_365_5 = EllipseUtilities.ellipseFromCovariance(337.590000f,
				240.790000f, secondMoments1_365_5.inverse(), 1.0f);
		final Ellipse e2_365_5 = EllipseUtilities.ellipseFromCovariance(336.633738f,
				239.693549f, secondMoments2_365_5.inverse(), 1.0f);
		doTest(secondMoments1_365_5, secondMoments2_365_5, e1_365_5, e2_365_5,
				0.379938f);
		final Matrix secondMoments1_638_5 = new Matrix(new double[][] {
				{ 0.000958f, -0.000971f }, { -0.000971f, 0.002273f } });
		final Matrix secondMoments2_638_5 = new Matrix(new double[][] {
				{ 0.000217f, -0.000227f }, { -0.000227f, 0.000618f } });
		final Ellipse e1_638_5 = EllipseUtilities.ellipseFromCovariance(337.590000f,
				240.790000f, secondMoments1_638_5.inverse(), 1.0f);
		final Ellipse e2_638_5 = EllipseUtilities.ellipseFromCovariance(328.577191f,
				245.186420f, secondMoments2_638_5.inverse(), 1.0f);
		doTest(secondMoments1_638_5, secondMoments2_638_5, e1_638_5, e2_638_5,
				0.259821f);
		final Matrix secondMoments1_838_5 = new Matrix(new double[][] {
				{ 0.000958f, -0.000971f }, { -0.000971f, 0.002273f } });
		final Matrix secondMoments2_838_5 = new Matrix(new double[][] {
				{ 0.000129f, 0.000101f }, { 0.000101f, 0.000360f } });
		final Ellipse e1_838_5 = EllipseUtilities.ellipseFromCovariance(337.590000f,
				240.790000f, secondMoments1_838_5.inverse(), 1.0f);
		final Ellipse e2_838_5 = EllipseUtilities.ellipseFromCovariance(326.461413f,
				235.046223f, secondMoments2_838_5.inverse(), 1.0f);
		doTest(secondMoments1_838_5, secondMoments2_838_5, e1_838_5, e2_838_5,
				0.171267f);
		final Matrix secondMoments1_996_5 = new Matrix(new double[][] {
				{ 0.000958f, -0.000971f }, { -0.000971f, 0.002273f } });
		final Matrix secondMoments2_996_5 = new Matrix(new double[][] {
				{ 0.000094f, 0.000074f }, { 0.000074f, 0.000242f } });
		final Ellipse e1_996_5 = EllipseUtilities.ellipseFromCovariance(337.590000f,
				240.790000f, secondMoments1_996_5.inverse(), 1.0f);
		final Ellipse e2_996_5 = EllipseUtilities.ellipseFromCovariance(328.356298f,
				234.286336f, secondMoments2_996_5.inverse(), 1.0f);
		doTest(secondMoments1_996_5, secondMoments2_996_5, e1_996_5, e2_996_5,
				0.118915f);
		final Matrix secondMoments1_1060_5 = new Matrix(new double[][] {
				{ 0.000958f, -0.000971f }, { -0.000971f, 0.002273f } });
		final Matrix secondMoments2_1060_5 = new Matrix(new double[][] {
				{ 0.000105f, -0.000008f }, { -0.000008f, 0.000113f } });
		final Ellipse e1_1060_5 = EllipseUtilities.ellipseFromCovariance(337.590000f,
				240.790000f, secondMoments1_1060_5.inverse(), 1.0f);
		final Ellipse e2_1060_5 = EllipseUtilities.ellipseFromCovariance(349.731703f,
				234.442286f, secondMoments2_1060_5.inverse(), 1.0f);
		doTest(secondMoments1_1060_5, secondMoments2_1060_5, e1_1060_5,
				e2_1060_5, 0.095904f);
		final Matrix secondMoments1_668_6 = new Matrix(new double[][] {
				{ 0.001354f, 0.000523f }, { 0.000523f, 0.001113f } });
		final Matrix secondMoments2_668_6 = new Matrix(new double[][] {
				{ 0.000387f, -0.000085f }, { -0.000085f, 0.000169f } });
		final Ellipse e1_668_6 = EllipseUtilities.ellipseFromCovariance(574.750000f,
				249.260000f, secondMoments1_668_6.inverse(), 1.0f);
		final Ellipse e2_668_6 = EllipseUtilities.ellipseFromCovariance(565.259118f,
				250.988953f, secondMoments2_668_6.inverse(), 1.0f);
		doTest(secondMoments1_668_6, secondMoments2_668_6, e1_668_6, e2_668_6,
				0.218441f);
		final Matrix secondMoments1_777_6 = new Matrix(new double[][] {
				{ 0.001354f, 0.000523f }, { 0.000523f, 0.001113f } });
		final Matrix secondMoments2_777_6 = new Matrix(new double[][] {
				{ 0.000283f, -0.000032f }, { -0.000032f, 0.000145f } });
		final Ellipse e1_777_6 = EllipseUtilities.ellipseFromCovariance(574.750000f,
				249.260000f, secondMoments1_777_6.inverse(), 1.0f);
		final Ellipse e2_777_6 = EllipseUtilities.ellipseFromCovariance(567.000420f,
				251.169427f, secondMoments2_777_6.inverse(), 1.0f);
		doTest(secondMoments1_777_6, secondMoments2_777_6, e1_777_6, e2_777_6,
				0.180799f);
		final Matrix secondMoments1_874_6 = new Matrix(new double[][] {
				{ 0.001354f, 0.000523f }, { 0.000523f, 0.001113f } });
		final Matrix secondMoments2_874_6 = new Matrix(new double[][] {
				{ 0.000309f, -0.000063f }, { -0.000063f, 0.000097f } });
		final Ellipse e1_874_6 = EllipseUtilities.ellipseFromCovariance(574.750000f,
				249.260000f, secondMoments1_874_6.inverse(), 1.0f);
		final Ellipse e2_874_6 = EllipseUtilities.ellipseFromCovariance(566.196720f,
				250.693547f, secondMoments2_874_6.inverse(), 1.0f);
		doTest(secondMoments1_874_6, secondMoments2_874_6, e1_874_6, e2_874_6,
				0.146600f);
		final Matrix secondMoments1_947_6 = new Matrix(new double[][] {
				{ 0.001354f, 0.000523f }, { 0.000523f, 0.001113f } });
		final Matrix secondMoments2_947_6 = new Matrix(new double[][] {
				{ 0.000261f, -0.000054f }, { -0.000054f, 0.000075f } });
		final Ellipse e1_947_6 = EllipseUtilities.ellipseFromCovariance(574.750000f,
				249.260000f, secondMoments1_947_6.inverse(), 1.0f);
		final Ellipse e2_947_6 = EllipseUtilities.ellipseFromCovariance(567.439385f,
				247.649178f, secondMoments2_947_6.inverse(), 1.0f);
		doTest(secondMoments1_947_6, secondMoments2_947_6, e1_947_6, e2_947_6,
				0.117028f);
		final Matrix secondMoments1_1311_6 = new Matrix(new double[][] {
				{ 0.001354f, 0.000523f }, { 0.000523f, 0.001113f } });
		final Matrix secondMoments2_1311_6 = new Matrix(new double[][] {
				{ 0.000058f, -0.000009f }, { -0.000009f, 0.000016f } });
		final Ellipse e1_1311_6 = EllipseUtilities.ellipseFromCovariance(574.750000f,
				249.260000f, secondMoments1_1311_6.inverse(), 1.0f);
		final Ellipse e2_1311_6 = EllipseUtilities.ellipseFromCovariance(566.572361f,
				256.816211f, secondMoments2_1311_6.inverse(), 1.0f);
		doTest(secondMoments1_1311_6, secondMoments2_1311_6, e1_1311_6,
				e2_1311_6, 0.025298f);
		final Matrix secondMoments1_1315_6 = new Matrix(new double[][] {
				{ 0.001354f, 0.000523f }, { 0.000523f, 0.001113f } });
		final Matrix secondMoments2_1315_6 = new Matrix(new double[][] {
				{ 0.000047f, -0.000008f }, { -0.000008f, 0.000018f } });
		final Ellipse e1_1315_6 = EllipseUtilities.ellipseFromCovariance(574.750000f,
				249.260000f, secondMoments1_1315_6.inverse(), 1.0f);
		final Ellipse e2_1315_6 = EllipseUtilities.ellipseFromCovariance(562.537774f,
				255.107101f, secondMoments2_1315_6.inverse(), 1.0f);
		doTest(secondMoments1_1315_6, secondMoments2_1315_6, e1_1315_6,
				e2_1315_6, 0.024782f);
		final Matrix secondMoments1_164_7 = new Matrix(new double[][] {
				{ 0.000573f, 0.000492f }, { 0.000492f, 0.002577f } });
		final Matrix secondMoments2_164_7 = new Matrix(new double[][] {
				{ 0.000386f, 0.000474f }, { 0.000474f, 0.001509f } });
		final Ellipse e1_164_7 = EllipseUtilities.ellipseFromCovariance(383.570000f,
				254.100000f, secondMoments1_164_7.inverse(), 1.0f);
		final Ellipse e2_164_7 = EllipseUtilities.ellipseFromCovariance(384.642802f,
				253.183299f, secondMoments2_164_7.inverse(), 1.0f);
		doTest(secondMoments1_164_7, secondMoments2_164_7, e1_164_7, e2_164_7,
				0.539050f);
		final Matrix secondMoments1_9_8 = new Matrix(new double[][] {
				{ 0.001197f, 0.000842f }, { 0.000842f, 0.001625f } });
		final Matrix secondMoments2_9_8 = new Matrix(new double[][] {
				{ 0.001259f, 0.001575f }, { 0.001575f, 0.002509f } });
		final Ellipse e1_9_8 = EllipseUtilities.ellipseFromCovariance(400.510000f,
				263.780000f, secondMoments1_9_8.inverse(), 1.0f);
		final Ellipse e2_9_8 = EllipseUtilities.ellipseFromCovariance(408.481337f,
				259.534703f, secondMoments2_9_8.inverse(), 1.0f);
		doTest(secondMoments1_9_8, secondMoments2_9_8, e1_9_8, e2_9_8,
				0.580043f);
		final Matrix secondMoments1_63_8 = new Matrix(new double[][] {
				{ 0.001197f, 0.000842f }, { 0.000842f, 0.001625f } });
		final Matrix secondMoments2_63_8 = new Matrix(new double[][] {
				{ 0.000968f, 0.001248f }, { 0.001248f, 0.002056f } });
		final Ellipse e1_63_8 = EllipseUtilities.ellipseFromCovariance(400.510000f,
				263.780000f, secondMoments1_63_8.inverse(), 1.0f);
		final Ellipse e2_63_8 = EllipseUtilities.ellipseFromCovariance(408.233989f,
				260.161238f, secondMoments2_63_8.inverse(), 1.0f);
		doTest(secondMoments1_63_8, secondMoments2_63_8, e1_63_8, e2_63_8,
				0.536834f);
		final Matrix secondMoments1_65_8 = new Matrix(new double[][] {
				{ 0.001197f, 0.000842f }, { 0.000842f, 0.001625f } });
		final Matrix secondMoments2_65_8 = new Matrix(new double[][] {
				{ 0.000771f, 0.000945f }, { 0.000945f, 0.001809f } });
		final Ellipse e1_65_8 = EllipseUtilities.ellipseFromCovariance(400.510000f,
				263.780000f, secondMoments1_65_8.inverse(), 1.0f);
		final Ellipse e2_65_8 = EllipseUtilities.ellipseFromCovariance(411.983753f,
				264.164628f, secondMoments2_65_8.inverse(), 1.0f);
		doTest(secondMoments1_65_8, secondMoments2_65_8, e1_65_8, e2_65_8,
				0.511713f);
		final Matrix secondMoments1_1365_9 = new Matrix(new double[][] {
				{ 0.001053f, 0.000882f }, { 0.000882f, 0.001912f } });
		final Matrix secondMoments2_1365_9 = new Matrix(new double[][] {
				{ 0.000013f, -0.000006f }, { -0.000006f, 0.000026f } });
		final Ellipse e1_1365_9 = EllipseUtilities.ellipseFromCovariance(393.250000f,
				266.200000f, secondMoments1_1365_9.inverse(), 1.0f);
		final Ellipse e2_1365_9 = EllipseUtilities.ellipseFromCovariance(383.425219f,
				274.208974f, secondMoments2_1365_9.inverse(), 1.0f);
		doTest(secondMoments1_1365_9, secondMoments2_1365_9, e1_1365_9,
				e2_1365_9, 0.016473f);
		final Matrix secondMoments1_1388_9 = new Matrix(new double[][] {
				{ 0.001053f, 0.000882f }, { 0.000882f, 0.001912f } });
		final Matrix secondMoments2_1388_9 = new Matrix(new double[][] {
				{ 0.000011f, -0.000013f }, { -0.000013f, 0.000029f } });
		final Ellipse e1_1388_9 = EllipseUtilities.ellipseFromCovariance(393.250000f,
				266.200000f, secondMoments1_1388_9.inverse(), 1.0f);
		final Ellipse e2_1388_9 = EllipseUtilities.ellipseFromCovariance(380.052283f,
				270.743548f, secondMoments2_1388_9.inverse(), 1.0f);
		doTest(secondMoments1_1388_9, secondMoments2_1388_9, e1_1388_9,
				e2_1388_9, 0.011116f);
		final Matrix secondMoments1_9_10 = new Matrix(new double[][] {
				{ 0.001525f, 0.001640f }, { 0.001640f, 0.002574f } });
		final Matrix secondMoments2_9_10 = new Matrix(new double[][] {
				{ 0.001211f, 0.001514f }, { 0.001514f, 0.002412f } });
		final Ellipse e1_9_10 = EllipseUtilities.ellipseFromCovariance(407.770000f,
				266.200000f, secondMoments1_9_10.inverse(), 1.0f);
		final Ellipse e2_9_10 = EllipseUtilities.ellipseFromCovariance(408.481337f,
				259.534703f, secondMoments2_9_10.inverse(), 1.0f);
		doTest(secondMoments1_9_10, secondMoments2_9_10, e1_9_10, e2_9_10,
				0.577320f);
		final Matrix secondMoments1_63_10 = new Matrix(new double[][] {
				{ 0.001525f, 0.001640f }, { 0.001640f, 0.002574f } });
		final Matrix secondMoments2_63_10 = new Matrix(new double[][] {
				{ 0.000930f, 0.001200f }, { 0.001200f, 0.001976f } });
		final Ellipse e1_63_10 = EllipseUtilities.ellipseFromCovariance(407.770000f,
				266.200000f, secondMoments1_63_10.inverse(), 1.0f);
		final Ellipse e2_63_10 = EllipseUtilities.ellipseFromCovariance(408.233989f,
				260.161238f, secondMoments2_63_10.inverse(), 1.0f);
		doTest(secondMoments1_63_10, secondMoments2_63_10, e1_63_10, e2_63_10,
				0.524812f);
		final Matrix secondMoments1_65_10 = new Matrix(new double[][] {
				{ 0.001525f, 0.001640f }, { 0.001640f, 0.002574f } });
		final Matrix secondMoments2_65_10 = new Matrix(new double[][] {
				{ 0.000741f, 0.000908f }, { 0.000908f, 0.001739f } });
		final Ellipse e1_65_10 = EllipseUtilities.ellipseFromCovariance(407.770000f,
				266.200000f, secondMoments1_65_10.inverse(), 1.0f);
		final Ellipse e2_65_10 = EllipseUtilities.ellipseFromCovariance(411.983753f,
				264.164628f, secondMoments2_65_10.inverse(), 1.0f);
		doTest(secondMoments1_65_10, secondMoments2_65_10, e1_65_10, e2_65_10,
				0.615385f);
		final Matrix secondMoments1_563_11 = new Matrix(new double[][] {
				{ 0.001142f, 0.000197f }, { 0.000197f, 0.001115f } });
		final Matrix secondMoments2_563_11 = new Matrix(new double[][] {
				{ 0.000367f, 0.000160f }, { 0.000160f, 0.000364f } });
		final Ellipse e1_563_11 = EllipseUtilities.ellipseFromCovariance(451.330000f,
				267.410000f, secondMoments1_563_11.inverse(), 1.0f);
		final Ellipse e2_563_11 = EllipseUtilities.ellipseFromCovariance(442.275933f,
				258.456881f, secondMoments2_563_11.inverse(), 1.0f);
		doTest(secondMoments1_563_11, secondMoments2_563_11, e1_563_11,
				e2_563_11, 0.292264f);
		final Matrix secondMoments1_637_11 = new Matrix(new double[][] {
				{ 0.001142f, 0.000197f }, { 0.000197f, 0.001115f } });
		final Matrix secondMoments2_637_11 = new Matrix(new double[][] {
				{ 0.000270f, 0.000128f }, { 0.000128f, 0.000327f } });
		final Ellipse e1_637_11 = EllipseUtilities.ellipseFromCovariance(451.330000f,
				267.410000f, secondMoments1_637_11.inverse(), 1.0f);
		final Ellipse e2_637_11 = EllipseUtilities.ellipseFromCovariance(438.144245f,
				264.255157f, secondMoments2_637_11.inverse(), 1.0f);
		doTest(secondMoments1_637_11, secondMoments2_637_11, e1_637_11,
				e2_637_11, 0.241868f);
		final Matrix secondMoments1_739_11 = new Matrix(new double[][] {
				{ 0.001142f, 0.000197f }, { 0.000197f, 0.001115f } });
		final Matrix secondMoments2_739_11 = new Matrix(new double[][] {
				{ 0.000242f, 0.000099f }, { 0.000099f, 0.000220f } });
		final Ellipse e1_739_11 = EllipseUtilities.ellipseFromCovariance(451.330000f,
				267.410000f, secondMoments1_739_11.inverse(), 1.0f);
		final Ellipse e2_739_11 = EllipseUtilities.ellipseFromCovariance(443.506090f,
				258.974375f, secondMoments2_739_11.inverse(), 1.0f);
		doTest(secondMoments1_739_11, secondMoments2_739_11, e1_739_11,
				e2_739_11, 0.187706f);
		final Matrix secondMoments1_782_11 = new Matrix(new double[][] {
				{ 0.001142f, 0.000197f }, { 0.000197f, 0.001115f } });
		final Matrix secondMoments2_782_11 = new Matrix(new double[][] {
				{ 0.000258f, 0.000104f }, { 0.000104f, 0.000229f } });
		final Ellipse e1_782_11 = EllipseUtilities.ellipseFromCovariance(451.330000f,
				267.410000f, secondMoments1_782_11.inverse(), 1.0f);
		final Ellipse e2_782_11 = EllipseUtilities.ellipseFromCovariance(442.606689f,
				260.868482f, secondMoments2_782_11.inverse(), 1.0f);
		doTest(secondMoments1_782_11, secondMoments2_782_11, e1_782_11,
				e2_782_11, 0.198262f);
		final Matrix secondMoments1_9_12 = new Matrix(new double[][] {
				{ 0.001146f, 0.000803f }, { 0.000803f, 0.001640f } });
		final Matrix secondMoments2_9_12 = new Matrix(new double[][] {
				{ 0.001229f, 0.001538f }, { 0.001538f, 0.002449f } });
		final Ellipse e1_9_12 = EllipseUtilities.ellipseFromCovariance(400.510000f,
				268.620000f, secondMoments1_9_12.inverse(), 1.0f);
		final Ellipse e2_9_12 = EllipseUtilities.ellipseFromCovariance(408.481337f,
				259.534703f, secondMoments2_9_12.inverse(), 1.0f);
		doTest(secondMoments1_9_12, secondMoments2_9_12, e1_9_12, e2_9_12,
				0.550890f);
		final Matrix secondMoments1_63_12 = new Matrix(new double[][] {
				{ 0.001146f, 0.000803f }, { 0.000803f, 0.001640f } });
		final Matrix secondMoments2_63_12 = new Matrix(new double[][] {
				{ 0.000945f, 0.001218f }, { 0.001218f, 0.002007f } });
		final Ellipse e1_63_12 = EllipseUtilities.ellipseFromCovariance(400.510000f,
				268.620000f, secondMoments1_63_12.inverse(), 1.0f);
		final Ellipse e2_63_12 = EllipseUtilities.ellipseFromCovariance(408.233989f,
				260.161238f, secondMoments2_63_12.inverse(), 1.0f);
		doTest(secondMoments1_63_12, secondMoments2_63_12, e1_63_12, e2_63_12,
				0.504769f);
		final Matrix secondMoments1_65_12 = new Matrix(new double[][] {
				{ 0.001146f, 0.000803f }, { 0.000803f, 0.001640f } });
		final Matrix secondMoments2_65_12 = new Matrix(new double[][] {
				{ 0.000752f, 0.000922f }, { 0.000922f, 0.001766f } });
		final Ellipse e1_65_12 = EllipseUtilities.ellipseFromCovariance(400.510000f,
				268.620000f, secondMoments1_65_12.inverse(), 1.0f);
		final Ellipse e2_65_12 = EllipseUtilities.ellipseFromCovariance(411.983753f,
				264.164628f, secondMoments2_65_12.inverse(), 1.0f);
		doTest(secondMoments1_65_12, secondMoments2_65_12, e1_65_12, e2_65_12,
				0.585214f);
		final Matrix secondMoments1_15_13 = new Matrix(new double[][] {
				{ 0.000660f, 0.000147f }, { 0.000147f, 0.001903f } });
		final Matrix secondMoments2_15_13 = new Matrix(new double[][] {
				{ 0.000512f, 0.000099f }, { 0.000099f, 0.001657f } });
		final Ellipse e1_15_13 = EllipseUtilities.ellipseFromCovariance(95.590000f,
				274.670000f, secondMoments1_15_13.inverse(), 1.0f);
		final Ellipse e2_15_13 = EllipseUtilities.ellipseFromCovariance(96.256202f,
				271.637173f, secondMoments2_15_13.inverse(), 1.0f);
		doTest(secondMoments1_15_13, secondMoments2_15_13, e1_15_13, e2_15_13,
				0.793562f);
		final Matrix secondMoments1_81_13 = new Matrix(new double[][] {
				{ 0.000660f, 0.000147f }, { 0.000147f, 0.001903f } });
		final Matrix secondMoments2_81_13 = new Matrix(new double[][] {
				{ 0.000537f, 0.000107f }, { 0.000107f, 0.001190f } });
		final Ellipse e1_81_13 = EllipseUtilities.ellipseFromCovariance(95.590000f,
				274.670000f, secondMoments1_81_13.inverse(), 1.0f);
		final Ellipse e2_81_13 = EllipseUtilities.ellipseFromCovariance(95.777830f,
				271.375829f, secondMoments2_81_13.inverse(), 1.0f);
		doTest(secondMoments1_81_13, secondMoments2_81_13, e1_81_13, e2_81_13,
				0.713031f);
		final Matrix secondMoments1_176_13 = new Matrix(new double[][] {
				{ 0.000660f, 0.000147f }, { 0.000147f, 0.001903f } });
		final Matrix secondMoments2_176_13 = new Matrix(new double[][] {
				{ 0.000372f, 0.000085f }, { 0.000085f, 0.001114f } });
		final Ellipse e1_176_13 = EllipseUtilities.ellipseFromCovariance(95.590000f,
				274.670000f, secondMoments1_176_13.inverse(), 1.0f);
		final Ellipse e2_176_13 = EllipseUtilities.ellipseFromCovariance(96.782894f,
				272.080449f, secondMoments2_176_13.inverse(), 1.0f);
		doTest(secondMoments1_176_13, secondMoments2_176_13, e1_176_13,
				e2_176_13, 0.572466f);
		final Matrix secondMoments1_502_13 = new Matrix(new double[][] {
				{ 0.000660f, 0.000147f }, { 0.000147f, 0.001903f } });
		final Matrix secondMoments2_502_13 = new Matrix(new double[][] {
				{ 0.000576f, -0.000056f }, { -0.000056f, 0.000220f } });
		final Ellipse e1_502_13 = EllipseUtilities.ellipseFromCovariance(95.590000f,
				274.670000f, secondMoments1_502_13.inverse(), 1.0f);
		final Ellipse e2_502_13 = EllipseUtilities.ellipseFromCovariance(91.532437f,
				286.344582f, secondMoments2_502_13.inverse(), 1.0f);
		doTest(secondMoments1_502_13, secondMoments2_502_13, e1_502_13,
				e2_502_13, 0.307848f);
		final Matrix secondMoments1_583_13 = new Matrix(new double[][] {
				{ 0.000660f, 0.000147f }, { 0.000147f, 0.001903f } });
		final Matrix secondMoments2_583_13 = new Matrix(new double[][] {
				{ 0.000097f, -0.000031f }, { -0.000031f, 0.001458f } });
		final Ellipse e1_583_13 = EllipseUtilities.ellipseFromCovariance(95.590000f,
				274.670000f, secondMoments1_583_13.inverse(), 1.0f);
		final Ellipse e2_583_13 = EllipseUtilities.ellipseFromCovariance(94.860438f,
				282.858744f, secondMoments2_583_13.inverse(), 1.0f);
		doTest(secondMoments1_583_13, secondMoments2_583_13, e1_583_13,
				e2_583_13, 0.304291f);
		final Matrix secondMoments1_640_13 = new Matrix(new double[][] {
				{ 0.000660f, 0.000147f }, { 0.000147f, 0.001903f } });
		final Matrix secondMoments2_640_13 = new Matrix(new double[][] {
				{ 0.000138f, -0.000002f }, { -0.000002f, 0.000620f } });
		final Ellipse e1_640_13 = EllipseUtilities.ellipseFromCovariance(95.590000f,
				274.670000f, secondMoments1_640_13.inverse(), 1.0f);
		final Ellipse e2_640_13 = EllipseUtilities.ellipseFromCovariance(95.102784f,
				281.836333f, secondMoments2_640_13.inverse(), 1.0f);
		doTest(secondMoments1_640_13, secondMoments2_640_13, e1_640_13,
				e2_640_13, 0.263184f);
		final Matrix secondMoments1_846_13 = new Matrix(new double[][] {
				{ 0.000660f, 0.000147f }, { 0.000147f, 0.001903f } });
		final Matrix secondMoments2_846_13 = new Matrix(new double[][] {
				{ 0.000100f, -0.000003f }, { -0.000003f, 0.000445f } });
		final Ellipse e1_846_13 = EllipseUtilities.ellipseFromCovariance(95.590000f,
				274.670000f, secondMoments1_846_13.inverse(), 1.0f);
		final Ellipse e2_846_13 = EllipseUtilities.ellipseFromCovariance(92.764510f,
				288.550082f, secondMoments2_846_13.inverse(), 1.0f);
		doTest(secondMoments1_846_13, secondMoments2_846_13, e1_846_13,
				e2_846_13, 0.189570f);
		final Matrix secondMoments1_883_13 = new Matrix(new double[][] {
				{ 0.000660f, 0.000147f }, { 0.000147f, 0.001903f } });
		final Matrix secondMoments2_883_13 = new Matrix(new double[][] {
				{ 0.000602f, -0.000000f }, { -0.000000f, 0.000063f } });
		final Ellipse e1_883_13 = EllipseUtilities.ellipseFromCovariance(95.590000f,
				274.670000f, secondMoments1_883_13.inverse(), 1.0f);
		final Ellipse e2_883_13 = EllipseUtilities.ellipseFromCovariance(93.459892f,
				267.111785f, secondMoments2_883_13.inverse(), 1.0f);
		doTest(secondMoments1_883_13, secondMoments2_883_13, e1_883_13,
				e2_883_13, 0.176420f);
		final Matrix secondMoments1_1312_13 = new Matrix(new double[][] {
				{ 0.000660f, 0.000147f }, { 0.000147f, 0.001903f } });
		final Matrix secondMoments2_1312_13 = new Matrix(new double[][] {
				{ 0.000066f, -0.000000f }, { -0.000000f, 0.000021f } });
		final Ellipse e1_1312_13 = EllipseUtilities.ellipseFromCovariance(95.590000f,
				274.670000f, secondMoments1_1312_13.inverse(), 1.0f);
		final Ellipse e2_1312_13 = EllipseUtilities.ellipseFromCovariance(97.906473f,
				268.440672f, secondMoments2_1312_13.inverse(), 1.0f);
		doTest(secondMoments1_1312_13, secondMoments2_1312_13, e1_1312_13,
				e2_1312_13, 0.033122f);
		final Matrix secondMoments1_7_14 = new Matrix(new double[][] {
				{ 0.000629f, 0.000651f }, { 0.000651f, 0.002635f } });
		final Matrix secondMoments2_7_14 = new Matrix(new double[][] {
				{ 0.000333f, 0.000425f }, { 0.000425f, 0.001766f } });
		final Ellipse e1_7_14 = EllipseUtilities.ellipseFromCovariance(735.680000f,
				289.190000f, secondMoments1_7_14.inverse(), 1.0f);
		final Ellipse e2_7_14 = EllipseUtilities.ellipseFromCovariance(735.215675f,
				289.470365f, secondMoments2_7_14.inverse(), 1.0f);
		doTest(secondMoments1_7_14, secondMoments2_7_14, e1_7_14, e2_7_14,
				0.574898f);
		final Matrix secondMoments1_160_14 = new Matrix(new double[][] {
				{ 0.000629f, 0.000651f }, { 0.000651f, 0.002635f } });
		final Matrix secondMoments2_160_14 = new Matrix(new double[][] {
				{ 0.000240f, 0.000357f }, { 0.000357f, 0.001342f } });
		final Ellipse e1_160_14 = EllipseUtilities.ellipseFromCovariance(735.680000f,
				289.190000f, secondMoments1_160_14.inverse(), 1.0f);
		final Ellipse e2_160_14 = EllipseUtilities.ellipseFromCovariance(737.219458f,
				288.986366f, secondMoments2_160_14.inverse(), 1.0f);
		doTest(secondMoments1_160_14, secondMoments2_160_14, e1_160_14,
				e2_160_14, 0.398695f);
		final Matrix secondMoments1_554_14 = new Matrix(new double[][] {
				{ 0.000629f, 0.000651f }, { 0.000651f, 0.002635f } });
		final Matrix secondMoments2_554_14 = new Matrix(new double[][] {
				{ 0.000145f, 0.000172f }, { 0.000172f, 0.000649f } });
		final Ellipse e1_554_14 = EllipseUtilities.ellipseFromCovariance(735.680000f,
				289.190000f, secondMoments1_554_14.inverse(), 1.0f);
		final Ellipse e2_554_14 = EllipseUtilities.ellipseFromCovariance(737.726233f,
				289.165221f, secondMoments2_554_14.inverse(), 1.0f);
		doTest(secondMoments1_554_14, secondMoments2_554_14, e1_554_14,
				e2_554_14, 0.228170f);
		final Matrix secondMoments1_776_14 = new Matrix(new double[][] {
				{ 0.000629f, 0.000651f }, { 0.000651f, 0.002635f } });
		final Matrix secondMoments2_776_14 = new Matrix(new double[][] {
				{ 0.000121f, 0.000071f }, { 0.000071f, 0.000273f } });
		final Ellipse e1_776_14 = EllipseUtilities.ellipseFromCovariance(735.680000f,
				289.190000f, secondMoments1_776_14.inverse(), 1.0f);
		final Ellipse e2_776_14 = EllipseUtilities.ellipseFromCovariance(737.146318f,
				287.771841f, secondMoments2_776_14.inverse(), 1.0f);
		doTest(secondMoments1_776_14, secondMoments2_776_14, e1_776_14,
				e2_776_14, 0.150452f);
		final Matrix secondMoments1_68_15 = new Matrix(new double[][] {
				{ 0.000621f, 0.000238f }, { 0.000238f, 0.002079f } });
		final Matrix secondMoments2_68_15 = new Matrix(new double[][] {
				{ 0.000271f, 0.000265f }, { 0.000265f, 0.001672f } });
		final Ellipse e1_68_15 = EllipseUtilities.ellipseFromCovariance(486.420000f,
				306.130000f, secondMoments1_68_15.inverse(), 1.0f);
		final Ellipse e2_68_15 = EllipseUtilities.ellipseFromCovariance(485.435623f,
				306.183645f, secondMoments2_68_15.inverse(), 1.0f);
		doTest(secondMoments1_68_15, secondMoments2_68_15, e1_68_15, e2_68_15,
				0.555658f);
		final Matrix secondMoments1_167_15 = new Matrix(new double[][] {
				{ 0.000621f, 0.000238f }, { 0.000238f, 0.002079f } });
		final Matrix secondMoments2_167_15 = new Matrix(new double[][] {
				{ 0.000224f, 0.000211f }, { 0.000211f, 0.001501f } });
		final Ellipse e1_167_15 = EllipseUtilities.ellipseFromCovariance(486.420000f,
				306.130000f, secondMoments1_167_15.inverse(), 1.0f);
		final Ellipse e2_167_15 = EllipseUtilities.ellipseFromCovariance(484.110847f,
				305.836654f, secondMoments2_167_15.inverse(), 1.0f);
		doTest(secondMoments1_167_15, secondMoments2_167_15, e1_167_15,
				e2_167_15, 0.486276f);
		final Matrix secondMoments1_281_15 = new Matrix(new double[][] {
				{ 0.000621f, 0.000238f }, { 0.000238f, 0.002079f } });
		final Matrix secondMoments2_281_15 = new Matrix(new double[][] {
				{ 0.000176f, 0.000168f }, { 0.000168f, 0.001212f } });
		final Ellipse e1_281_15 = EllipseUtilities.ellipseFromCovariance(486.420000f,
				306.130000f, secondMoments1_281_15.inverse(), 1.0f);
		final Ellipse e2_281_15 = EllipseUtilities.ellipseFromCovariance(485.021331f,
				306.075132f, secondMoments2_281_15.inverse(), 1.0f);
		doTest(secondMoments1_281_15, secondMoments2_281_15, e1_281_15,
				e2_281_15, 0.387940f);
		final Matrix secondMoments1_284_15 = new Matrix(new double[][] {
				{ 0.000621f, 0.000238f }, { 0.000238f, 0.002079f } });
		final Matrix secondMoments2_284_15 = new Matrix(new double[][] {
				{ 0.000414f, -0.000290f }, { -0.000290f, 0.000689f } });
		final Ellipse e1_284_15 = EllipseUtilities.ellipseFromCovariance(486.420000f,
				306.130000f, secondMoments1_284_15.inverse(), 1.0f);
		final Ellipse e2_284_15 = EllipseUtilities.ellipseFromCovariance(490.325903f,
				314.914392f, secondMoments2_284_15.inverse(), 1.0f);
		doTest(secondMoments1_284_15, secondMoments2_284_15, e1_284_15,
				e2_284_15, 0.391179f);
		final Matrix secondMoments1_437_15 = new Matrix(new double[][] {
				{ 0.000621f, 0.000238f }, { 0.000238f, 0.002079f } });
		final Matrix secondMoments2_437_15 = new Matrix(new double[][] {
				{ 0.000150f, 0.000150f }, { 0.000150f, 0.001084f } });
		final Ellipse e1_437_15 = EllipseUtilities.ellipseFromCovariance(486.420000f,
				306.130000f, secondMoments1_437_15.inverse(), 1.0f);
		final Ellipse e2_437_15 = EllipseUtilities.ellipseFromCovariance(482.683481f,
				305.834707f, secondMoments2_437_15.inverse(), 1.0f);
		doTest(secondMoments1_437_15, secondMoments2_437_15, e1_437_15,
				e2_437_15, 0.337182f);
		final Matrix secondMoments1_570_15 = new Matrix(new double[][] {
				{ 0.000621f, 0.000238f }, { 0.000238f, 0.002079f } });
		final Matrix secondMoments2_570_15 = new Matrix(new double[][] {
				{ 0.000159f, 0.000021f }, { 0.000021f, 0.000537f } });
		final Ellipse e1_570_15 = EllipseUtilities.ellipseFromCovariance(486.420000f,
				306.130000f, secondMoments1_570_15.inverse(), 1.0f);
		final Ellipse e2_570_15 = EllipseUtilities.ellipseFromCovariance(482.175137f,
				304.678265f, secondMoments2_570_15.inverse(), 1.0f);
		doTest(secondMoments1_570_15, secondMoments2_570_15, e1_570_15,
				e2_570_15, 0.262092f);
		final Matrix secondMoments1_10_16 = new Matrix(new double[][] {
				{ 0.001599f, 0.000493f }, { 0.000493f, 0.000924f } });
		final Matrix secondMoments2_10_16 = new Matrix(new double[][] {
				{ 0.000633f, 0.000295f }, { 0.000295f, 0.000950f } });
		final Ellipse e1_10_16 = EllipseUtilities.ellipseFromCovariance(464.640000f,
				315.810000f, secondMoments1_10_16.inverse(), 1.0f);
		final Ellipse e2_10_16 = EllipseUtilities.ellipseFromCovariance(461.035908f,
				312.042275f, secondMoments2_10_16.inverse(), 1.0f);
		doTest(secondMoments1_10_16, secondMoments2_10_16, e1_10_16, e2_10_16,
				0.592315f);
		final Matrix secondMoments1_71_16 = new Matrix(new double[][] {
				{ 0.001599f, 0.000493f }, { 0.000493f, 0.000924f } });
		final Matrix secondMoments2_71_16 = new Matrix(new double[][] {
				{ 0.000646f, 0.000223f }, { 0.000223f, 0.000656f } });
		final Ellipse e1_71_16 = EllipseUtilities.ellipseFromCovariance(464.640000f,
				315.810000f, secondMoments1_71_16.inverse(), 1.0f);
		final Ellipse e2_71_16 = EllipseUtilities.ellipseFromCovariance(466.645900f,
				316.588559f, secondMoments2_71_16.inverse(), 1.0f);
		doTest(secondMoments1_71_16, secondMoments2_71_16, e1_71_16, e2_71_16,
				0.550661f);
		final Matrix secondMoments1_73_16 = new Matrix(new double[][] {
				{ 0.001599f, 0.000493f }, { 0.000493f, 0.000924f } });
		final Matrix secondMoments2_73_16 = new Matrix(new double[][] {
				{ 0.001083f, -0.000252f }, { -0.000252f, 0.000435f } });
		final Ellipse e1_73_16 = EllipseUtilities.ellipseFromCovariance(464.640000f,
				315.810000f, secondMoments1_73_16.inverse(), 1.0f);
		final Ellipse e2_73_16 = EllipseUtilities.ellipseFromCovariance(455.662170f,
				321.327603f, secondMoments2_73_16.inverse(), 1.0f);
		doTest(secondMoments1_73_16, secondMoments2_73_16, e1_73_16, e2_73_16,
				0.430947f);
		final Matrix secondMoments1_386_16 = new Matrix(new double[][] {
				{ 0.001599f, 0.000493f }, { 0.000493f, 0.000924f } });
		final Matrix secondMoments2_386_16 = new Matrix(new double[][] {
				{ 0.000263f, 0.000155f }, { 0.000155f, 0.000596f } });
		final Ellipse e1_386_16 = EllipseUtilities.ellipseFromCovariance(464.640000f,
				315.810000f, secondMoments1_386_16.inverse(), 1.0f);
		final Ellipse e2_386_16 = EllipseUtilities.ellipseFromCovariance(462.827240f,
				312.515353f, secondMoments2_386_16.inverse(), 1.0f);
		doTest(secondMoments1_386_16, secondMoments2_386_16, e1_386_16,
				e2_386_16, 0.328313f);
		final Matrix secondMoments1_440_16 = new Matrix(new double[][] {
				{ 0.001599f, 0.000493f }, { 0.000493f, 0.000924f } });
		final Matrix secondMoments2_440_16 = new Matrix(new double[][] {
				{ 0.000720f, 0.000033f }, { 0.000033f, 0.000174f } });
		final Ellipse e1_440_16 = EllipseUtilities.ellipseFromCovariance(464.640000f,
				315.810000f, secondMoments1_440_16.inverse(), 1.0f);
		final Ellipse e2_440_16 = EllipseUtilities.ellipseFromCovariance(456.316914f,
				311.144240f, secondMoments2_440_16.inverse(), 1.0f);
		doTest(secondMoments1_440_16, secondMoments2_440_16, e1_440_16,
				e2_440_16, 0.316909f);
		final Matrix secondMoments1_501_16 = new Matrix(new double[][] {
				{ 0.001599f, 0.000493f }, { 0.000493f, 0.000924f } });
		final Matrix secondMoments2_501_16 = new Matrix(new double[][] {
				{ 0.000710f, 0.000061f }, { 0.000061f, 0.000133f } });
		final Ellipse e1_501_16 = EllipseUtilities.ellipseFromCovariance(464.640000f,
				315.810000f, secondMoments1_501_16.inverse(), 1.0f);
		final Ellipse e2_501_16 = EllipseUtilities.ellipseFromCovariance(456.266983f,
				310.324293f, secondMoments2_501_16.inverse(), 1.0f);
		doTest(secondMoments1_501_16, secondMoments2_501_16, e1_501_16,
				e2_501_16, 0.271487f);
		final Matrix secondMoments1_573_16 = new Matrix(new double[][] {
				{ 0.001599f, 0.000493f }, { 0.000493f, 0.000924f } });
		final Matrix secondMoments2_573_16 = new Matrix(new double[][] {
				{ 0.000592f, 0.000037f }, { 0.000037f, 0.000142f } });
		final Ellipse e1_573_16 = EllipseUtilities.ellipseFromCovariance(464.640000f,
				315.810000f, secondMoments1_573_16.inverse(), 1.0f);
		final Ellipse e2_573_16 = EllipseUtilities.ellipseFromCovariance(456.400677f,
				309.131000f, secondMoments2_573_16.inverse(), 1.0f);
		doTest(secondMoments1_573_16, secondMoments2_573_16, e1_573_16,
				e2_573_16, 0.259374f);
		final Matrix secondMoments1_997_16 = new Matrix(new double[][] {
				{ 0.001599f, 0.000493f }, { 0.000493f, 0.000924f } });
		final Matrix secondMoments2_997_16 = new Matrix(new double[][] {
				{ 0.000183f, 0.000012f }, { 0.000012f, 0.000070f } });
		final Ellipse e1_997_16 = EllipseUtilities.ellipseFromCovariance(464.640000f,
				315.810000f, secondMoments1_997_16.inverse(), 1.0f);
		final Ellipse e2_997_16 = EllipseUtilities.ellipseFromCovariance(458.808243f,
				308.421958f, secondMoments2_997_16.inverse(), 1.0f);
		doTest(secondMoments1_997_16, secondMoments2_997_16, e1_997_16,
				e2_997_16, 0.102489f);
		final Matrix secondMoments1_39_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_39_17 = new Matrix(new double[][] {
				{ 0.000746f, -0.000533f }, { -0.000533f, 0.001187f } });
		final Ellipse e1_39_17 = EllipseUtilities.ellipseFromCovariance(314.600000f,
				318.230000f, secondMoments1_39_17.inverse(), 1.0f);
		final Ellipse e2_39_17 = EllipseUtilities.ellipseFromCovariance(313.953694f,
				317.900074f, secondMoments2_39_17.inverse(), 1.0f);
		doTest(secondMoments1_39_17, secondMoments2_39_17, e1_39_17, e2_39_17,
				0.697906f);
		final Matrix secondMoments1_173_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_173_17 = new Matrix(new double[][] {
				{ 0.000538f, -0.000403f }, { -0.000403f, 0.000919f } });
		final Ellipse e1_173_17 = EllipseUtilities.ellipseFromCovariance(314.600000f,
				318.230000f, secondMoments1_173_17.inverse(), 1.0f);
		final Ellipse e2_173_17 = EllipseUtilities.ellipseFromCovariance(313.225518f,
				318.446818f, secondMoments2_173_17.inverse(), 1.0f);
		doTest(secondMoments1_173_17, secondMoments2_173_17, e1_173_17,
				e2_173_17, 0.519941f);
		final Matrix secondMoments1_293_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_293_17 = new Matrix(new double[][] {
				{ 0.000436f, -0.000319f }, { -0.000319f, 0.000764f } });
		final Ellipse e1_293_17 = EllipseUtilities.ellipseFromCovariance(314.600000f,
				318.230000f, secondMoments1_293_17.inverse(), 1.0f);
		final Ellipse e2_293_17 = EllipseUtilities.ellipseFromCovariance(312.596572f,
				318.602102f, secondMoments2_293_17.inverse(), 1.0f);
		doTest(secondMoments1_293_17, secondMoments2_293_17, e1_293_17,
				e2_293_17, 0.431624f);
		final Matrix secondMoments1_295_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_295_17 = new Matrix(new double[][] {
				{ 0.000331f, 0.000212f }, { 0.000212f, 0.000842f } });
		final Ellipse e1_295_17 = EllipseUtilities.ellipseFromCovariance(314.600000f,
				318.230000f, secondMoments1_295_17.inverse(), 1.0f);
		final Ellipse e2_295_17 = EllipseUtilities.ellipseFromCovariance(301.943402f,
				319.295289f, secondMoments2_295_17.inverse(), 1.0f);
		doTest(secondMoments1_295_17, secondMoments2_295_17, e1_295_17,
				e2_295_17, 0.373724f);
		final Matrix secondMoments1_580_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_580_17 = new Matrix(new double[][] {
				{ 0.000206f, 0.000105f }, { 0.000105f, 0.000543f } });
		final Ellipse e1_580_17 = EllipseUtilities.ellipseFromCovariance(314.600000f,
				318.230000f, secondMoments1_580_17.inverse(), 1.0f);
		final Ellipse e2_580_17 = EllipseUtilities.ellipseFromCovariance(302.970663f,
				318.730723f, secondMoments2_580_17.inverse(), 1.0f);
		doTest(secondMoments1_580_17, secondMoments2_580_17, e1_580_17,
				e2_580_17, 0.283702f);
		final Matrix secondMoments1_682_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_682_17 = new Matrix(new double[][] {
				{ 0.000189f, -0.000086f }, { -0.000086f, 0.000376f } });
		final Ellipse e1_682_17 = EllipseUtilities.ellipseFromCovariance(314.600000f,
				318.230000f, secondMoments1_682_17.inverse(), 1.0f);
		final Ellipse e2_682_17 = EllipseUtilities.ellipseFromCovariance(309.486674f,
				319.420383f, secondMoments2_682_17.inverse(), 1.0f);
		doTest(secondMoments1_682_17, secondMoments2_682_17, e1_682_17,
				e2_682_17, 0.228460f);
		final Matrix secondMoments1_789_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_789_17 = new Matrix(new double[][] {
				{ 0.000149f, -0.000033f }, { -0.000033f, 0.000329f } });
		final Ellipse e1_789_17 = EllipseUtilities.ellipseFromCovariance(314.600000f,
				318.230000f, secondMoments1_789_17.inverse(), 1.0f);
		final Ellipse e2_789_17 = EllipseUtilities.ellipseFromCovariance(307.819206f,
				318.726516f, secondMoments2_789_17.inverse(), 1.0f);
		doTest(secondMoments1_789_17, secondMoments2_789_17, e1_789_17,
				e2_789_17, 0.196657f);
		final Matrix secondMoments1_1030_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_1030_17 = new Matrix(new double[][] {
				{ 0.000092f, -0.000018f }, { -0.000018f, 0.000159f } });
		final Ellipse e1_1030_17 = EllipseUtilities.ellipseFromCovariance(
				314.600000f, 318.230000f, secondMoments1_1030_17.inverse(),
				1.0f);
		final Ellipse e2_1030_17 = EllipseUtilities.ellipseFromCovariance(
				307.429140f, 320.516190f, secondMoments2_1030_17.inverse(),
				1.0f);
		doTest(secondMoments1_1030_17, secondMoments2_1030_17, e1_1030_17,
				e2_1030_17, 0.109040f);
		final Matrix secondMoments1_1156_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_1156_17 = new Matrix(new double[][] {
				{ 0.000078f, -0.000019f }, { -0.000019f, 0.000097f } });
		final Ellipse e1_1156_17 = EllipseUtilities.ellipseFromCovariance(
				314.600000f, 318.230000f, secondMoments1_1156_17.inverse(),
				1.0f);
		final Ellipse e2_1156_17 = EllipseUtilities.ellipseFromCovariance(
				307.771879f, 324.116857f, secondMoments2_1156_17.inverse(),
				1.0f);
		doTest(secondMoments1_1156_17, secondMoments2_1156_17, e1_1156_17,
				e2_1156_17, 0.077324f);
		final Matrix secondMoments1_1207_17 = new Matrix(new double[][] {
				{ 0.001362f, -0.000774f }, { -0.000774f, 0.001346f } });
		final Matrix secondMoments2_1207_17 = new Matrix(new double[][] {
				{ 0.000069f, -0.000007f }, { -0.000007f, 0.000070f } });
		final Ellipse e1_1207_17 = EllipseUtilities.ellipseFromCovariance(
				314.600000f, 318.230000f, secondMoments1_1207_17.inverse(),
				1.0f);
		final Ellipse e2_1207_17 = EllipseUtilities.ellipseFromCovariance(
				305.410180f, 323.894917f, secondMoments2_1207_17.inverse(),
				1.0f);
		doTest(secondMoments1_1207_17, secondMoments2_1207_17, e1_1207_17,
				e2_1207_17, 0.060606f);
		final Matrix secondMoments1_89_18 = new Matrix(new double[][] {
				{ 0.001609f, -0.000768f }, { -0.000768f, 0.001134f } });
		final Matrix secondMoments2_89_18 = new Matrix(new double[][] {
				{ 0.000844f, -0.000444f }, { -0.000444f, 0.000933f } });
		final Ellipse e1_89_18 = EllipseUtilities.ellipseFromCovariance(122.210000f,
				323.070000f, secondMoments1_89_18.inverse(), 1.0f);
		final Ellipse e2_89_18 = EllipseUtilities.ellipseFromCovariance(121.248810f,
				322.865319f, secondMoments2_89_18.inverse(), 1.0f);
		doTest(secondMoments1_89_18, secondMoments2_89_18, e1_89_18, e2_89_18,
				0.691817f);
		final Matrix secondMoments1_90_18 = new Matrix(new double[][] {
				{ 0.001609f, -0.000768f }, { -0.000768f, 0.001134f } });
		final Matrix secondMoments2_90_18 = new Matrix(new double[][] {
				{ 0.000938f, 0.000083f }, { 0.000083f, 0.000707f } });
		final Ellipse e1_90_18 = EllipseUtilities.ellipseFromCovariance(122.210000f,
				323.070000f, secondMoments1_90_18.inverse(), 1.0f);
		final Ellipse e2_90_18 = EllipseUtilities.ellipseFromCovariance(125.111521f,
				334.004283f, secondMoments2_90_18.inverse(), 1.0f);
		doTest(secondMoments1_90_18, secondMoments2_90_18, e1_90_18, e2_90_18,
				0.528642f);
		final Matrix secondMoments1_180_18 = new Matrix(new double[][] {
				{ 0.001609f, -0.000768f }, { -0.000768f, 0.001134f } });
		final Matrix secondMoments2_180_18 = new Matrix(new double[][] {
				{ 0.000814f, -0.000393f }, { -0.000393f, 0.000748f } });
		final Ellipse e1_180_18 = EllipseUtilities.ellipseFromCovariance(122.210000f,
				323.070000f, secondMoments1_180_18.inverse(), 1.0f);
		final Ellipse e2_180_18 = EllipseUtilities.ellipseFromCovariance(121.634598f,
				323.117841f, secondMoments2_180_18.inverse(), 1.0f);
		doTest(secondMoments1_180_18, secondMoments2_180_18, e1_180_18,
				e2_180_18, 0.605201f);
		final Matrix secondMoments1_181_18 = new Matrix(new double[][] {
				{ 0.001609f, -0.000768f }, { -0.000768f, 0.001134f } });
		final Matrix secondMoments2_181_18 = new Matrix(new double[][] {
				{ 0.000579f, 0.000129f }, { 0.000129f, 0.000783f } });
		final Ellipse e1_181_18 = EllipseUtilities.ellipseFromCovariance(122.210000f,
				323.070000f, secondMoments1_181_18.inverse(), 1.0f);
		final Ellipse e2_181_18 = EllipseUtilities.ellipseFromCovariance(124.290424f,
				334.918246f, secondMoments2_181_18.inverse(), 1.0f);
		doTest(secondMoments1_181_18, secondMoments2_181_18, e1_181_18,
				e2_181_18, 0.439394f);
		final Matrix secondMoments1_451_18 = new Matrix(new double[][] {
				{ 0.001609f, -0.000768f }, { -0.000768f, 0.001134f } });
		final Matrix secondMoments2_451_18 = new Matrix(new double[][] {
				{ 0.000434f, -0.000197f }, { -0.000197f, 0.000569f } });
		final Ellipse e1_451_18 = EllipseUtilities.ellipseFromCovariance(122.210000f,
				323.070000f, secondMoments1_451_18.inverse(), 1.0f);
		final Ellipse e2_451_18 = EllipseUtilities.ellipseFromCovariance(120.562590f,
				323.776546f, secondMoments2_451_18.inverse(), 1.0f);
		doTest(secondMoments1_451_18, secondMoments2_451_18, e1_451_18,
				e2_451_18, 0.411040f);
		final Matrix secondMoments1_452_18 = new Matrix(new double[][] {
				{ 0.001609f, -0.000768f }, { -0.000768f, 0.001134f } });
		final Matrix secondMoments2_452_18 = new Matrix(new double[][] {
				{ 0.000400f, 0.000070f }, { 0.000070f, 0.000535f } });
		final Ellipse e1_452_18 = EllipseUtilities.ellipseFromCovariance(122.210000f,
				323.070000f, secondMoments1_452_18.inverse(), 1.0f);
		final Ellipse e2_452_18 = EllipseUtilities.ellipseFromCovariance(123.132287f,
				334.051363f, secondMoments2_452_18.inverse(), 1.0f);
		doTest(secondMoments1_452_18, secondMoments2_452_18, e1_452_18,
				e2_452_18, 0.370512f);
		final Matrix secondMoments1_689_18 = new Matrix(new double[][] {
				{ 0.001609f, -0.000768f }, { -0.000768f, 0.001134f } });
		final Matrix secondMoments2_689_18 = new Matrix(new double[][] {
				{ 0.000281f, 0.000029f }, { 0.000029f, 0.000357f } });
		final Ellipse e1_689_18 = EllipseUtilities.ellipseFromCovariance(122.210000f,
				323.070000f, secondMoments1_689_18.inverse(), 1.0f);
		final Ellipse e2_689_18 = EllipseUtilities.ellipseFromCovariance(122.579885f,
				333.188132f, secondMoments2_689_18.inverse(), 1.0f);
		doTest(secondMoments1_689_18, secondMoments2_689_18, e1_689_18,
				e2_689_18, 0.282375f);

	}

	private static void doTest(Matrix secondMoments1, Matrix secondMoments2,
			Ellipse e1, Ellipse e2, float f)
	{
		System.out.println(e1 + " vs " + e2);
		final IPDRepeatability<EllipticInterestPointData> dummy = new IPDRepeatability<EllipticInterestPointData>();
		final double overlap = dummy.calculateOverlapPercentageOxford(secondMoments1,
				secondMoments2, e1, e2, 4.0f);
		System.out
				.format("\tGot overlap: %4.2f, expecting %4.2f\n", overlap, f);
		if (Math.abs(f - overlap) > 0.02) {
			System.err.println("THERE WAS AN ERROR WITH THE ABOVE ELLIPSE!!");
		}
	}
}
