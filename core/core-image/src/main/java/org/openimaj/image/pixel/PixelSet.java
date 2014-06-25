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
package org.openimaj.image.pixel;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.io.ReadWriteable;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

/**
 * A set of (not-necessarily connected) pixels within an image. This class
 * provides a number of utility functions for working with and analysing the
 * pixels.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class PixelSet implements Cloneable, ReadWriteable, Iterable<Pixel> {

	/** The set of pixels within this connected component */
	public Set<Pixel> pixels = new HashSet<Pixel>();

	/**
	 * Default constructor; makes an empty PixelSet
	 */
	public PixelSet() {
		// intentionally left blank
	}

	/**
	 * Construct a rectangular {@link PixelSet}. Pixels are created for the area
	 * within the rectangle but these will not have any specific value as they
	 * are not associated to any particular image.
	 * 
	 * @param x
	 *            The top-left x-coordinate of the rectangle
	 * @param y
	 *            The top-left y-coordinate of the rectangle
	 * @param w
	 *            The width of the rectangle
	 * @param h
	 *            The height of the rectangle
	 */
	public PixelSet(int x, int y, int w, int h) {
		for (int j = y; j < h + y; j++)
			for (int i = x; i < w + x; i++)
				pixels.add(new Pixel(i, j));
	}

	/**
	 * Constructs a PixelSet from a mask image. Pixels are created for areas
	 * within the mask image that are non-zero. Note that this may result in a
	 * connected component definition that is unconnected and some methods may
	 * not return expected results.
	 * 
	 * @param img
	 *            The mask image to construct a connected component from.
	 */
	public PixelSet(int[][] img) {
		for (int j = 0; j < img.length; j++) {
			for (int i = 0; i < img[j].length; i++) {
				if (img[j][i] > 0)
					pixels.add(new Pixel(i, j));
			}
		}
	}

	/**
	 * Constructs a PixelSet from a mask image. Pixels are created for areas
	 * within the mask image that are above the given threshold. Note that this
	 * may result in a connected component definition that is unconnected and
	 * some methods may not return expected results.
	 * 
	 * @param mask
	 *            The mask image to construct a connected component from.
	 * @param thresh
	 *            the threshold value.
	 */
	public PixelSet(FImage mask, float thresh) {
		for (int j = 0; j < mask.height; j++) {
			for (int i = 0; i < mask.width; i++) {
				if (mask.pixels[j][i] >= thresh)
					pixels.add(new Pixel(i, j));
			}
		}
	}

	/**
	 * Construct a PixelSet from the given set of {@link Pixel}s. The pixels are
	 * shallow copied into the connected component. If the pixels do not form a
	 * connected component then some methods in this class may not return
	 * expected results.
	 * 
	 * @param pixels
	 *            A {@link Set} of {@link Pixel}s.
	 */
	public PixelSet(Set<Pixel> pixels) {
		this.pixels.addAll(pixels);
	}

	protected void fromShape(Shape shape) {
		final int minx = (int) Math.round(shape.minX());
		final int maxx = (int) Math.round(shape.maxX());
		final int miny = (int) Math.round(shape.minY());
		final int maxy = (int) Math.round(shape.maxY());

		for (int y = miny; y <= maxy; y++) {
			for (int x = minx; x <= maxx; x++) {
				final Pixel p = new Pixel(x, y);
				if (shape.isInside(p))
					addPixel(p);
			}
		}
	}

	/**
	 * Add a pixel into this connected component. If the pixel is not adjacent
	 * to another pixel within this connected component, then some methods may
	 * return unexpected results. Side-affects this object.
	 * 
	 * @param x
	 *            The x-coordinate of the pixel to add
	 * @param y
	 *            The y-coordinate of the pixel to add
	 */
	public void addPixel(int x, int y) {
		addPixel(new Pixel(x, y));
	}

	/**
	 * Add a pixel into this connected component. If the pixel is not adjacent
	 * to another pixel within this connected component, then some methods may
	 * return unexpected results. Side-affects this object.
	 * 
	 * @param p
	 *            The pixel to add
	 */
	public void addPixel(Pixel p) {
		pixels.add(p);
	}

	/**
	 * Returns the set of {@link Pixel}s that are within this component.
	 * 
	 * @return the set of {@link Pixel}s that are within this component.
	 */
	public Set<Pixel> getPixels() {
		return pixels;
	}

	/**
	 * Shallow copies the pixels from the given {@link ConnectedComponent} into
	 * this object. If the pixels that are added are not adjacent to other
	 * pixels within the component some methods may return unexpected results.
	 * Side-affects this object.
	 * 
	 * @param c
	 *            The {@link ConnectedComponent} to copy pixels from.
	 */
	public void merge(ConnectedComponent c) {
		pixels.addAll(c.pixels);
	}

	/**
	 * Returns whether the given pixel is within this connected component. This
	 * is synonymous to
	 * <code>{@link #getPixels() getPixels}().contains(p)</code>.
	 * 
	 * @param p
	 *            The pixel to find.
	 * @return TRUE if the pixel is contained within this component; FALSE
	 *         otherwise
	 */
	public boolean find(Pixel p) {
		return pixels.contains(p);
	}

	/**
	 * Returns whether the given coordinates are within this connected
	 * component. This is synonymous with
	 * <code>{@link #find(Pixel) find}( new Pixel(x,y) )</code>.
	 * 
	 * @param x
	 *            The x-coordinate of the pixel to find
	 * @param y
	 *            The y-coordinate of the pixel to find
	 * @return TRUE if the pixel is contained within this component; FALSE
	 *         otherwise
	 */
	public boolean find(int x, int y) {
		return find(new Pixel(x, y));
	}

	/**
	 * Calculate the area of this connected component in pixels. This is
	 * synonymous with <code>{@link #getPixels() getPixels}.size()</code>
	 * 
	 * @return the number of pixels covered by this connected component.
	 */
	public int calculateArea() {
		return pixels.size();
	}

	/**
	 * Calculate the pq moment, μ<sub>pq</sub> around the given centroid. From
	 * Equation 6.44 in Sonka, Hlavac and Boyle. If instead of giving the
	 * centroid (xc, yc), you give (0,0), then this will return the standard
	 * moments m<sub>pq</sub> about the origin.
	 * 
	 * @param p
	 *            The P moment to calculate
	 * @param q
	 *            The Q moment to calculate
	 * @param xc
	 *            x-coordinate of centroid
	 * @param yc
	 *            y-coordinate of centroid
	 * @return The pq moment, M<sub>pq</sub>.
	 */
	public double calculateMoment(int p, int q, double xc, double yc) {
		if (p == 0 && q == 0)
			return calculateArea();

		double mpq = 0;
		for (final Pixel pix : pixels) {
			mpq += Math.pow(pix.x - xc, p) * Math.pow(pix.y - yc, q);
		}
		return mpq;
	}

	/**
	 * Calculate the pq central moment, μ<sub>pq</sub> for this region. From
	 * Equation 6.44 in Sonka, Hlavac and Boyle.
	 * 
	 * @param p
	 *            The P moment to calculate
	 * @param q
	 *            The Q moment to calculate
	 * @return The pq moment, M<sub>pq</sub>.
	 */
	public double calculateMoment(int p, int q) {
		if (p == 0 && q == 0)
			return calculateArea();

		final double[] centroid = calculateCentroid();

		double mpq = 0;
		for (final Pixel pix : pixels) {
			mpq += Math.pow(pix.x - centroid[0], p) * Math.pow(pix.y - centroid[1], q);
		}
		return mpq;
	}

	/**
	 * Calculate the normalized, unscaled, central moments η<sub>pq</sub>. From
	 * Equation 6.47 in Sonka, Hlavac and Boyle [1st Ed.]. Normalised central
	 * moments are invariant to scale and translation.
	 * 
	 * @param p
	 *            The P moment to calculate
	 * @param q
	 *            The Q moment to calculate
	 * @return The normalised, unscaled central moment, M<sub>pq</sub>.
	 */
	public double calculateMomentNormalised(int p, int q) {
		final double gamma = ((p + q) / 2) + 1;
		return calculateMoment(p, q) / Math.pow(pixels.size(), gamma);
	}

	/**
	 * Calculates the principle direction of the connected component. This is
	 * given by
	 * <code>0.5 * atan( (M<sub>20</sub>-M<sub>02</sub>) / 2 * M<sub>11</sub> )</code>
	 * so results in an angle between -PI and +PI.
	 * 
	 * @return The principle direction (-PI/2 to +PI/2 radians) of the connected
	 *         component.
	 */
	public double calculateDirection() {
		final double[] centroid = calculateCentroid();
		final double u11 = calculateMoment(1, 1, centroid[0], centroid[1]);
		final double u20 = calculateMoment(2, 0, centroid[0], centroid[1]);
		final double u02 = calculateMoment(0, 2, centroid[0], centroid[1]);

		final double theta = 0.5 * Math.atan2((2 * u11), (u20 - u02));

		return theta;
	}

	/**
	 * Calculate the centroid of the connected component. This is the average of
	 * all the pixel coordinates in the component. The result is returned in a
	 * double array where the the first index is the x-coordinate and second is
	 * the y-coordinate.
	 * 
	 * @return The centroid point as a double array (x then y).
	 */
	public double[] calculateCentroid() {
		final double[] centroid = new double[2];
		final double m00 = calculateMoment(0, 0, 0, 0);
		centroid[0] = calculateMoment(1, 0, 0, 0) / m00;
		centroid[1] = calculateMoment(0, 1, 0, 0) / m00;

		return centroid;
	}

	/**
	 * Calculates the centroid pixel of the connected component. That is, the
	 * centroid value is rounded to the nearest pixel.
	 * 
	 * @return A {@link Pixel} at the centroid.
	 */
	public Pixel calculateCentroidPixel() {
		final double[] centroid = calculateCentroid();
		return new Pixel((int) Math.round(centroid[0]), (int) Math.round(centroid[1]));
	}

	/**
	 * Calculate the height and width of a box surrounding the component by
	 * averaging the distances of pixels above and below the centroid. The
	 * result is a double array where the first index is the height and the
	 * second is the width.
	 * 
	 * @param centroid
	 *            The centroid of the component.
	 * @return average height and width as a double array.
	 */
	public double[] calculateAverageHeightWidth(double[] centroid) {
		double height, width, accumPosH = 0, accumNegH = 0, accumPosW = 0, accumNegW = 0;
		int nPosH = 0, nNegH = 0, nPosW = 0, nNegW = 0;

		for (final Pixel p : pixels) {
			final double x = p.getX() - centroid[0];
			final double y = p.getY() - centroid[1];

			if (x >= 0) {
				accumPosW += x;
				nPosW++;
			} else {
				accumNegW += x;
				nNegW++;
			}

			if (y >= 0) {
				accumPosH += y;
				nPosH++;
			} else {
				accumNegH += y;
				nNegH++;
			}
		}
		height = 2 * ((accumPosH / nPosH) + Math.abs(accumNegH / nNegH));
		width = 2 * ((accumPosW / nPosW) + Math.abs(accumNegW / nNegW));

		return new double[] { height, width };
	}

	/**
	 * Calculate the height and width of a box surrounding the component by
	 * averaging the distances of pixels above and below the centroid. The
	 * result is a double array where the first index is the height and the
	 * second is the width.
	 * 
	 * @return average height and width as a double array.
	 */
	public double[] calculateAverageHeightWidth() {
		return calculateAverageHeightWidth(calculateCentroid());
	}

	/**
	 * Compute the aspect ratio of the regular bounding box.
	 * 
	 * @return the aspect ratio of the regular bounding box.
	 */
	public double calculateRegularBoundingBoxAspectRatio() {
		final Rectangle bb = calculateRegularBoundingBox();

		return bb.width / bb.height;
	}

	/**
	 * Calculate the regular bounding box of the region by calculating the
	 * maximum and minimum x and y coordinates of the pixels contained within
	 * the region. The result is an integer array containing the (x,y)
	 * coordinate of the top-left of the bounding box, and the width and height
	 * of the bounding box.
	 * 
	 * @return an {@link Rectangle} describing the bounds
	 */
	public Rectangle calculateRegularBoundingBox() {
		int xmin = Integer.MAX_VALUE, xmax = 0, ymin = Integer.MAX_VALUE, ymax = 0;

		for (final Pixel p : pixels) {
			if (p.x < xmin)
				xmin = p.x;
			if (p.x > xmax)
				xmax = p.x;
			if (p.y < ymin)
				ymin = p.y;
			if (p.y > ymax)
				ymax = p.y;
		}

		return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	/**
	 * Translates the region's pixels by x and y. This method side-affects the
	 * pixels in this object.
	 * 
	 * @param x
	 *            The offset in the horizontal direction
	 * @param y
	 *            The offset in the vertical direction.
	 */
	public void translate(int x, int y) {
		// Note: changing the position changes the hashcode, so you need to
		// rehash the set!
		final Set<Pixel> newPixels = new HashSet<Pixel>();

		for (final Pixel p : pixels) {
			p.x += x;
			p.y += y;
			newPixels.add(p);
		}

		pixels = newPixels;
	}

	/**
	 * Gets the top-left most pixel within the connected component.
	 * 
	 * @return the top-left most pixel within the connected component.
	 */
	public Pixel topLeftMostPixel() {
		int top = Integer.MAX_VALUE;
		Pixel pix = null;
		for (final Pixel p : pixels) {
			if (p.y < top) {
				top = p.y;
				pix = p;
			}
		}

		for (final Pixel p : pixels) {
			if (p.y == top) {
				if (p.x < pix.x)
					pix = p;
			}
		}

		return pix;
	}

	/**
	 * Gets the bottom-right most pixel in the connected component.
	 * 
	 * @return the bottom-right most pixel in the connected component.
	 */
	public Pixel bottomRightMostPixel() {
		int bottom = Integer.MIN_VALUE;
		Pixel pix = null;
		for (final Pixel p : pixels) {
			if (p.y > bottom) {
				bottom = p.y;
				pix = p;
			}
		}

		for (final Pixel p : pixels) {
			if (p.y == bottom) {
				if (p.x > pix.x)
					pix = p;
			}
		}

		return pix;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConnectedComponent(" + "area=" + calculateArea() + ")";
	}

	/**
	 * Extract a 1xarea image with all the pixels from the region in it. Useful
	 * for analysing the colour for example
	 * 
	 * @param input
	 *            input image to extract samples from
	 * @return new image with pixels set from samples
	 */
	public MBFImage extractPixels1d(MBFImage input) {
		final MBFImage out = new MBFImage(pixels.size(), 1, input.numBands());

		int j = 0;
		for (final Pixel p : pixels) {
			for (int i = 0; i < input.numBands(); i++) {
				out.setPixel(j, 0, input.getPixel(p.x, p.y));
			}
			j++;
		}

		return out;
	}

	/**
	 * Extract a 1 x area image with all the pixels from the region in it.
	 * Useful for analysing the colour for example
	 * 
	 * @param input
	 *            image to extract pixel values from
	 * @return new image filled with pixel values
	 */
	public FImage extractPixels1d(FImage input) {
		final FImage out = new FImage(pixels.size(), 1);

		int j = 0;
		for (final Pixel p : pixels) {
			out.pixels[0][j] = input.pixels[p.y][p.x];
			j++;
		}

		return out;
	}

	/**
	 * Returns an image containing just the connected component cropped from the
	 * original image. Although that the result image is necessarily
	 * rectangular, the pixels which are not part of the connected component
	 * will be transparent.
	 * 
	 * @param input
	 *            The input image from which to take the pixels
	 * @param blackout
	 *            Whether to blackout pixels that are not part of the region or
	 *            whether to mark them as transparent
	 * @return An image with the component's pixels cropped
	 */
	public MBFImage crop(MBFImage input, boolean blackout) {
		final Rectangle bb = this.calculateRegularBoundingBox();

		final MBFImage output = new MBFImage((int) bb.width, (int) bb.height, input.numBands());

		for (int y = 0; y < (int) bb.height; y++) {
			for (int x = 0; x < (int) bb.width; x++) {
				for (int b = 0; b < input.numBands(); b++) {
					if (!blackout || this.pixels.contains(new Pixel(x + (int) bb.x, y + (int) bb.y)))
						output.getBand(b).setPixel(x, y, input.getBand(b).getPixel(x + (int) bb.x, y + (int) bb.y));
					else
						output.getBand(b).setPixel(x, y, 0f);
				}
			}
		}

		return output;
	}

	/**
	 * This is a convenience method that simply calls
	 * {@link #crop(MBFImage, boolean)}
	 * 
	 * @param input
	 *            The input image from which to take the pixels
	 * @param blackout
	 *            Whether to blackout pixels that are not part of the region or
	 *            whether to mark them as transparent
	 * @return An image with the component's pixels cropped
	 */
	public MBFImage extractPixels2d(MBFImage input, boolean blackout) {
		return this.crop(input, blackout);
	}

	/**
	 * Returns an image where the connected component is masked in the image.
	 * The image is the same size as the image that is passed in.
	 * 
	 * @param input
	 *            The input image from which to take the size.
	 * @return An {@link FImage} containing a binary mask; pixels within the
	 *         connected component will have value 1, outside with have value 0
	 */
	public FImage calculateBinaryMask(Image<?, ?> input) {
		final FImage n = new FImage(input.getWidth(), input.getHeight());

		for (final Pixel p : pixels)
			n.pixels[p.y][p.x] = 1;

		return n;
	}

	/**
	 * Returns an ASCII representation of the connected component as a mask;
	 * where the output is "1" for a pixel within the mask and "0" for a pixel
	 * outside of the mask.
	 * 
	 * @return An image string.
	 */
	public String toStringImage() {
		final Rectangle bb = this.calculateRegularBoundingBox();

		String s = "";
		for (int j = (int) bb.y - 1; j <= bb.y + bb.height + 1; j++) {
			for (int i = (int) bb.x - 1; i <= bb.x + bb.width + 1; i++) {
				if (pixels.contains(new Pixel(i, j)))
					s += "1";
				else
					s += "0";
			}
			s += "\n";
		}
		return s;
	}

	/**
	 * Repositions the connected component so that its bounding box has its
	 * origin at (0,0). Side-affects this connected component.
	 */
	public void reposition() {
		final Rectangle bb = this.calculateRegularBoundingBox();
		translate(-(int) bb.x, -(int) bb.y);
	}

	/**
	 * Returns a mask image for this connected component that will be the size
	 * of this component's bounding box. Pixels within the component will be set
	 * to value 1.0, while pixels outside of the component will retain their
	 * initial value.
	 * 
	 * @return An {@link FImage} mask image
	 */
	public FImage toFImage() {
		final Rectangle bb = this.calculateRegularBoundingBox();

		final FImage img = new FImage((int) (bb.x + bb.width + 1), (int) (bb.y + bb.height + 1));

		for (final Pixel p : pixels)
			img.pixels[p.y][p.x] = 1;

		return img;
	}

	/**
	 * Returns a mask image for this connected component that will be the size
	 * of this component's bounding box plus a border of the given amount of
	 * padding. Pixels within the component will be set to value 1.0, while
	 * pixels outside of the component will retain their initial value.
	 * 
	 * @param padding
	 *            The number of pixels padding to add around the outside of the
	 *            mask.
	 * @return An {@link FImage} mask image
	 */
	public FImage toFImage(int padding) {
		final Rectangle bb = this.calculateRegularBoundingBox();

		final FImage img = new FImage((int) (bb.x + bb.width + 1 + 2 * padding),
				(int) (bb.y + bb.height + 1 + 2 * padding));

		for (final Pixel p : pixels)
			img.pixels[p.y + padding][p.x + padding] = 1;

		return img;
	}

	/**
	 * Affine transform the shape with the given transform matrix. Side-affects
	 * this component.
	 * 
	 * @param transform
	 *            The matrix containing the transform.
	 */
	public void transform(Matrix transform) {
		final Matrix p1 = new Matrix(3, 1);

		for (final Pixel p : pixels) {
			p1.set(0, 0, p.getX());
			p1.set(1, 0, p.getY());
			p1.set(2, 0, 1);

			final Matrix p2_est = transform.times(p1);

			p.x = (int) Math.rint(p2_est.get(0, 0));
			p.y = (int) Math.rint(p2_est.get(1, 0));
		}
	}

	/**
	 * Returns a normalisation matrix for this component.
	 * 
	 * @return A normalisation matrix.
	 */
	public Matrix normMatrix() {
		final double u20 = calculateMoment(2, 0);
		final double u02 = calculateMoment(0, 2);
		final double u11 = -calculateMoment(1, 1);

		Matrix tf = new Matrix(3, 3);
		tf.set(0, 0, u20);
		tf.set(1, 1, u02);
		tf.set(0, 1, u11);
		tf.set(1, 0, u11);
		tf.set(2, 2, 1);

		tf = tf.inverse();
		tf = tf.times(1 / Math.sqrt(tf.det()));
		tf = MatrixUtils.sqrt(tf);

		tf.set(2, 2, 1);

		return tf;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		final int count = in.nextInt();

		for (int i = 0; i < count; i++) {
			final Pixel p = new Pixel();
			p.readASCII(in);
			pixels.add(p);
		}
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final int count = in.readInt();

		for (int i = 0; i < count; i++) {
			final Pixel p = new Pixel();
			p.readBinary(in);
			pixels.add(p);
		}
	}

	@Override
	public String asciiHeader() {
		return "PixelSet";
	}

	@Override
	public byte[] binaryHeader() {
		return "PS".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(pixels.size());
		for (final Pixel p : pixels) {
			p.writeASCII(out);
			out.println();
		}
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(pixels.size());
		for (final Pixel p : pixels)
			p.writeBinary(out);
	}

	@Override
	public Iterator<Pixel> iterator() {
		return this.pixels.iterator();
	}

}
