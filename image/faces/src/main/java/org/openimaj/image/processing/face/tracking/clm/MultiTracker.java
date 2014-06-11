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
package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.FourierTemplateMatcher;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

import com.jsaragih.CLM;
import com.jsaragih.FDet;
import com.jsaragih.IO;
import com.jsaragih.MFCheck;

/**
 * A CLM Tracker that is able to deal with multiple tracks within the same
 * video. To instantiate use {@link #load(InputStream)} to get a
 * {@link TrackerVars} object which can be used to construct the Tracker.
 * <p>
 * <code><pre>MultiTracker t = new MultiTracker( MultiTracker.load( new File("face.tracker.file") ) );</pre></code>
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
public class MultiTracker {
	/**
	 * Encapsulates the variables for a single tracked face. This includes the
	 * model, the shape parameters, the last-matched template and the bounding
	 * rectangle.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 4 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static class TrackedFace extends DetectedFace {
		/** The constrained local model */
		public CLM clm;

		/** The current shape */
		public Matrix shape;

		/** The reference shape */
		public Matrix referenceShape;

		/** The template image */
		public FImage templateImage;

		/** The last matched bounds: _rect */
		public Rectangle lastMatchBounds;

		/** The redetected bounds: R */
		public Rectangle redetectedBounds;

		protected boolean gen = true;

		/**
		 * @param r
		 *            The rectangle in which the initial face was found
		 * @param tv
		 *            The initial tracker vars to use
		 */
		public TrackedFace(final Rectangle r, final TrackerVars tv) {
			this.redetectedBounds = r;
			this.clm = tv.clm.copy();
			this.shape = tv.shape.copy();
			this.referenceShape = tv.referenceShape.copy();
		}

		@Override
		public Rectangle getBounds()
		{
			return this.lastMatchBounds;
		}

		@Override
		public String toString() {
			return "Face["
					+ (this.redetectedBounds == null ? "null" : this.redetectedBounds
							.toString()) + "]";
		}
	}

	/**
	 * This class is used to store the tracker variables when they are loaded
	 * from a file. These variables can then be copied to make specific
	 * trackers.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 * @created 5 Jul 2012
	 * @version $Author$, $Revision$, $Date$
	 */
	public static class TrackerVars {
		/** The constrained local model */
		public CLM clm;

		/** The current shape */
		public Matrix shape;

		/** The reference shape */
		public Matrix referenceShape;

		/** The Face detector */
		public FDet faceDetector;

		/** The failure checker */
		public MFCheck failureCheck;

		/** Initialisation similarity */
		double[] similarity;
	}

	/** Scaling of template for template matching */
	private static final double TSCALE = 0.3;

	/** */
	public List<TrackedFace> trackedFaces = new ArrayList<TrackedFace>();

	/** The initial tracker */
	private TrackerVars initialTracker = null;

	/** < Frame number since last detection */
	private long framesSinceLastDetection;

	/** The frame currently being processed */
	private FImage currentFrame;

	private FImage small_;

	/**
	 * Create a tracker using the given model, face detector, failure checker,
	 * reference shape and similarity measures. These values will be copied into
	 * all trackers.
	 *
	 * @param clm
	 *            The local model
	 * @param fdet
	 *            The face detector
	 * @param fcheck
	 *            The failure checker
	 * @param rshape
	 *            The reference shape
	 * @param simil
	 *            The similarity measures
	 */
	public MultiTracker(final CLM clm, final FDet fdet, final MFCheck fcheck, final Matrix rshape,
			final double[] simil)
	{
		this.initialTracker = new TrackerVars();
		this.initialTracker.clm = clm;
		this.initialTracker.clm._pdm.identity(clm._plocal, clm._pglobl);
		this.initialTracker.faceDetector = fdet;
		this.initialTracker.failureCheck = fcheck;
		this.initialTracker.referenceShape = rshape.copy();
		this.initialTracker.similarity = simil;
		this.initialTracker.shape = new Matrix(2 * clm._pdm.nPoints(), 1);
		this.framesSinceLastDetection = -1;
	}

	/**
	 * Create a tracker with the given variables.
	 *
	 * @param tv
	 *            The tracker variables to use for all face trackers.
	 */
	public MultiTracker(final TrackerVars tv) {
		this.initialTracker = tv;
		this.framesSinceLastDetection = -1;
	}

	/**
	 * Constructor for making a tracker when loading data.
	 */
	protected MultiTracker() {
	}

	/**
	 * Reset frame number (will perform detection in next image)
	 */
	public void frameReset() {
		this.framesSinceLastDetection = -1;
		this.trackedFaces.clear();
	}

	/**
	 * Track faces from a previous frame to the given frame.
	 *
	 * @param im
	 *            The video frame
	 * @param wSize
	 *            The window size
	 * @param fpd
	 *            The number of frames between forced redetecs
	 * @param nIter
	 *            The number of iterations for model fitting
	 * @param clamp
	 *            The number s.d.'s in which a model must fit
	 * @param fTol
	 *            The tolerance for model fitting
	 * @param fcheck
	 *            Whether to automatically check for failed tracking
	 * @param searchAreaSize
	 *            The size of the template match search area
	 * @return 0 for success, -1 for failure.
	 */
	public int track(final FImage im, final int[] wSize, final int fpd, final int nIter,
			final double clamp, final double fTol, final boolean fcheck,
			final float searchAreaSize)
	{
		this.currentFrame = im;

		if ((this.framesSinceLastDetection < 0)
				|| (fpd >= 0 && fpd < this.framesSinceLastDetection))
		{
			this.framesSinceLastDetection = 0;
			final List<Rectangle> RL = this.initialTracker.faceDetector
					.detect(this.currentFrame);

			// Convert the detected rectangles into face trackers
			// trackedFaces.clear();
			// for (final Rectangle r : RL)
			// trackedFaces.add(new TrackedFace(r, initialTracker));
			if (this.trackedFaces.size() == 0) {
				for (final Rectangle r : RL)
					this.trackedFaces.add(new TrackedFace(r, this.initialTracker));
			} else {
				this.trackRedetect(this.currentFrame, searchAreaSize);

				final int sz = this.trackedFaces.size();
				for (final Rectangle r : RL) {
					boolean found = false;
					for (int i = 0; i < sz; i++) {
						if (r.percentageOverlap(this.trackedFaces.get(i).redetectedBounds) > 0.5) {
							found = true;
							break;
						}
					}

					if (!found)
						this.trackedFaces.add(new TrackedFace(r, this.initialTracker));
				}
			}
		} else {
			// Updates the tracked faces
			this.trackRedetect(this.currentFrame, searchAreaSize);
		}

		// Didn't find any faces in this frame? Try again next frame.
		if (this.trackedFaces.size() == 0)
			return -1;

		boolean resize = true;

		for (final Iterator<TrackedFace> iterator = this.trackedFaces.iterator(); iterator.hasNext();) {
			final TrackedFace f = iterator.next();

			if ((f.redetectedBounds.width == 0)
					|| (f.redetectedBounds.height == 0))
			{
				iterator.remove();
				this.framesSinceLastDetection = -1;
				continue;
				// return -1;
			}

			if (f.gen) {
				this.initShape(f.redetectedBounds, f.shape, f.referenceShape);
				f.clm._pdm.calcParams(f.shape, f.clm._plocal, f.clm._pglobl);
			} else {
				final double tx = f.redetectedBounds.x - f.lastMatchBounds.x;
				final double ty = f.redetectedBounds.y - f.lastMatchBounds.y;

				f.clm._pglobl.getArray()[4][0] += tx;
				f.clm._pglobl.getArray()[5][0] += ty;

				resize = false;
			}

			f.clm.fit(this.currentFrame, wSize, nIter, clamp, fTol);
			f.clm._pdm.calcShape2D(f.shape, f.clm._plocal, f.clm._pglobl);

			if (fcheck) {
				if (!this.initialTracker.failureCheck.check(f.clm.getViewIdx(),
						this.currentFrame, f.shape))
				{
					iterator.remove();
					continue;
					// return -1;
				}
			}

			f.lastMatchBounds = this.updateTemplate(f, this.currentFrame, f.shape,
					resize);

			if ((f.lastMatchBounds.width == 0)
					|| (f.lastMatchBounds.height == 0))
			{
				iterator.remove();
				this.framesSinceLastDetection = -1;
				continue;
				// return -1;
			}
		}

		// Didn't find any faces in this frame? Try again next frame.
		if (this.trackedFaces.size() == 0)
			return -1;

		this.framesSinceLastDetection++;

		return 0;
	}

	/**
	 * Initialise the shape within the given rectangle based on the given
	 * reference shape.
	 *
	 * @param r
	 *            The rectangle
	 * @param shape
	 *            The shape to initialise
	 * @param _rshape
	 *            The reference shape
	 */
	public void initShape(final Rectangle r, final Matrix shape,
			final Matrix _rshape)
	{
		assert ((shape.getRowDimension() == _rshape.getRowDimension()) && (shape
				.getColumnDimension() == _rshape.getColumnDimension()));

		final int n = _rshape.getRowDimension() / 2;

		final double a = r.width * Math.cos(this.initialTracker.similarity[1])
				* this.initialTracker.similarity[0] + 1;
		final double b = r.width * Math.sin(this.initialTracker.similarity[1])
				* this.initialTracker.similarity[0];

		final double tx = r.x + (int) (r.width / 2) + r.width
				* this.initialTracker.similarity[2];
		final double ty = r.y + (int) (r.height / 2) + r.height
				* this.initialTracker.similarity[3];

		final double[][] s = _rshape.getArray();
		final double[][] d = shape.getArray();

		for (int i = 0; i < n; i++) {
			d[i][0] = a * s[i][0] - b * s[i + n][0] + tx;
			d[i + n][0] = b * s[i][0] + a * s[i + n][0] + ty;
		}
	}

	/**
	 * Redetect the faces in the new frame.
	 *
	 * @param im
	 *            The new frame.
	 * @param searchAreaSize
	 *            The search area size
	 */
	private void trackRedetect(final FImage im, final float searchAreaSize) {
		final int ww = im.width;
		final int hh = im.height;

		// Resize the frame so processing is quicker.
		this.small_ = ResizeProcessor.resample(im, (int) (MultiTracker.TSCALE * ww),
				(int) (MultiTracker.TSCALE * hh));

		for (final TrackedFace f : this.trackedFaces) {
			f.gen = false;

			// Get the new search area nearby to the last match
			Rectangle searchAreaBounds = f.lastMatchBounds.clone();
			searchAreaBounds.scale((float) MultiTracker.TSCALE);
			searchAreaBounds.scaleCentroid(searchAreaSize);
			searchAreaBounds = searchAreaBounds.overlapping(this.small_.getBounds());

			// Get the search image
			final FImage searchArea = this.small_.extractROI(searchAreaBounds);

			// Template match the template over the reduced size image.
			final FourierTemplateMatcher matcher = new FourierTemplateMatcher(
					f.templateImage,
					FourierTemplateMatcher.Mode.NORM_CORRELATION_COEFFICIENT);
			matcher.analyseImage(searchArea);

			// Get the response map
			final float[][] ncc_ = matcher.getResponseMap().pixels;

			// DisplayUtilities.displayName( matcher.getResponseMap(),
			// "responseMap" );
			// DisplayUtilities.displayName( f.templateImage, "template" );

			f.redetectedBounds = f.templateImage.getBounds();

			// Find the maximum template match in the image
			final int h = searchArea.height - f.templateImage.height + 1;
			final int w = searchArea.width - f.templateImage.width + 1;
			float vb = -2;
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					final float v = ncc_[y][x];
					if (v > vb) {
						vb = v;
						f.redetectedBounds.x = x + searchAreaBounds.x;
						f.redetectedBounds.y = y + searchAreaBounds.y;
					}
				}
			}

			// Rescale the rectangle to full-size image coordinates.
			f.redetectedBounds.scale((float) (1d / MultiTracker.TSCALE));
		}
	}

	protected Rectangle updateTemplate(final TrackedFace f, final FImage im, final Matrix s,
			final boolean resize)
	{
		final int n = s.getRowDimension() / 2;

		final double[][] sv = s.getArray();
		double xmax = sv[0][0], ymax = sv[n][0], xmin = sv[0][0], ymin = sv[n][0];

		for (int i = 0; i < n; i++) {
			final double vx = sv[i][0];
			final double vy = sv[i + n][0];

			xmax = Math.max(xmax, vx);
			ymax = Math.max(ymax, vy);

			xmin = Math.min(xmin, vx);
			ymin = Math.min(ymin, vy);
		}

		if ((xmin < 0) || (ymin < 0) || (xmax >= im.width)
				|| (ymax >= im.height) || Double.isNaN(xmin)
				|| Double.isInfinite(xmin) || Double.isNaN(xmax)
				|| Double.isInfinite(xmax) || Double.isNaN(ymin)
				|| Double.isInfinite(ymin) || Double.isNaN(ymax)
				|| Double.isInfinite(ymax))
		{
			return new Rectangle(0, 0, 0, 0);
		} else {
			xmin *= MultiTracker.TSCALE;
			ymin *= MultiTracker.TSCALE;
			xmax *= MultiTracker.TSCALE;
			ymax *= MultiTracker.TSCALE;

			final Rectangle R = new Rectangle((float) Math.floor(xmin),
					(float) Math.floor(ymin), (float) Math.ceil(xmax - xmin),
					(float) Math.ceil(ymax - ymin));

			final int ww = im.width;
			final int hh = im.height;

			if (resize)
				this.small_ = ResizeProcessor.resample(im, (int) (MultiTracker.TSCALE * ww),
						(int) (MultiTracker.TSCALE * hh));

			f.templateImage = this.small_.extractROI(R);

			R.x *= 1.0 / MultiTracker.TSCALE;
			R.y *= 1.0 / MultiTracker.TSCALE;
			R.width *= 1.0 / MultiTracker.TSCALE;
			R.height *= 1.0 / MultiTracker.TSCALE;

			return R;
		}
	}

	/**
	 * Load a tracker from a file.
	 *
	 * @param fname
	 *            File name to read from
	 * @return A tracker variable class
	 * @throws FileNotFoundException
	 */
	public static TrackerVars load(final String fname)
			throws FileNotFoundException
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			final Scanner sc = new Scanner(br);
			return MultiTracker.read(sc, true);
		} finally {
			try {
				br.close();
			} catch (final IOException e) {
			}
		}
	}

	/**
	 * Load a tracker from an input stream.
	 *
	 * @param in
	 *            The input stream
	 * @return a tracker
	 */
	public static TrackerVars load(final InputStream in) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			final Scanner sc = new Scanner(br);
			return MultiTracker.read(sc, true);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (final IOException e) {
			}
		}
	}

	/**
	 *
	 * @param s
	 * @param readType
	 * @return
	 */
	private static TrackerVars read(final Scanner s, final boolean readType) {
		if (readType) {
			final int type = s.nextInt();
			assert (type == IO.Types.TRACKER.ordinal());
		}
		final TrackerVars trackerVars = new TrackerVars();
		trackerVars.clm = CLM.read(s, true);
		trackerVars.faceDetector = FDet.read(s, true);
		trackerVars.failureCheck = MFCheck.read(s, true);
		trackerVars.referenceShape = IO.readMat(s);
		trackerVars.similarity = new double[] { s.nextDouble(), s.nextDouble(),
				s.nextDouble(), s.nextDouble() };
		trackerVars.shape = new Matrix(2 * trackerVars.clm._pdm.nPoints(), 1);
		trackerVars.clm._pdm.identity(trackerVars.clm._plocal,
				trackerVars.clm._pglobl);

		return trackerVars;
	}

	/**
	 * Returns the initial variables used for each face tracker.
	 *
	 * @return The initial variables
	 */
	public TrackerVars getInitialVars() {
		return this.initialTracker;
	}
}
