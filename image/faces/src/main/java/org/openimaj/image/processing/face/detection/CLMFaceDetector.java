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
package org.openimaj.image.processing.face.detection;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

import com.jsaragih.CLM;
import com.jsaragih.FDet;
import com.jsaragih.IO;
import com.jsaragih.MFCheck;
import com.jsaragih.Tracker;

/**
 * Face detector based on a constrained local model. Fits a 3D face model to
 * each detection.
 * 
 * @see CLM
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Jason M. Saragih", "Simon Lucey", "Jeffrey F. Cohn" },
		title = "Face alignment through subspace constrained mean-shifts",
		year = "2009",
		booktitle = "IEEE 12th International Conference on Computer Vision, ICCV 2009, Kyoto, Japan, September 27 - October 4, 2009",
		pages = { "1034", "1041" },
		publisher = "IEEE",
		customData = {
				"doi", "http://dx.doi.org/10.1109/ICCV.2009.5459377",
				"researchr", "http://researchr.org/publication/SaragihLC09",
				"cites", "0",
				"citedby", "0"
		})
public class CLMFaceDetector implements FaceDetector<CLMDetectedFace, FImage> {
	/**
	 * Configuration for the face detector
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static class Configuration {
		/** The constrained local model */
		public CLM clm;

		/** The reference shape */
		public Matrix referenceShape;

		/** The current shape */
		public Matrix shape;

		/** The Face detector */
		public FDet faceDetector;

		/** The failure checker */
		public MFCheck failureCheck;

		/** Initialisation similarity */
		double[] similarity;

		/** The face mesh */
		public int[][] triangles = null;

		/** The face connections */
		public int[][] connections = null;

		/** Whether to use the face check (using pixels as a face classifier) */
		public boolean fcheck = false;

		/** Search window sizes */
		public int[] windowSize = { 11, 9, 7 };

		/** Number of iterations to use for model fitting */
		public int nIter = 5;

		/**
		 * Number of standard deviations from the mean face to allow in the
		 * model
		 */
		public double clamp = 3;

		/** Model fitting optimisation tolerance */
		public double fTol = 0.01;

		private void read(final InputStream in) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(in));
				final Scanner sc = new Scanner(br);
				read(sc, true);
			} finally {
				try {
					br.close();
				} catch (final IOException e) {
				}
			}
		}

		private void read(Scanner s, boolean readType) {
			if (readType) {
				final int type = s.nextInt();
				assert (type == IO.Types.TRACKER.ordinal());
			}

			clm = CLM.read(s, true);
			faceDetector = FDet.read(s, true);
			failureCheck = MFCheck.read(s, true);
			referenceShape = IO.readMat(s);
			similarity = new double[] { s.nextDouble(), s.nextDouble(), s.nextDouble(), s.nextDouble() };
			shape = new Matrix(2 * clm._pdm.nPoints(), 1);
			clm._pdm.identity(clm._plocal, clm._pglobl);
		}

		/**
		 * Construct with the default model parameters
		 */
		public Configuration() {
			read(Tracker.class.getResourceAsStream("face2.tracker"));
			triangles = IO.loadTri(Tracker.class.getResourceAsStream("face.tri"));
			connections = IO.loadCon(Tracker.class.getResourceAsStream("face.con"));
		}
	}

	private Configuration config;

	/**
	 * Default constructor
	 */
	public CLMFaceDetector() {
		config = new Configuration();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		config = IOUtils.read(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		IOUtils.write(config, out);
	}

	@Override
	public List<CLMDetectedFace> detectFaces(FImage image) {
		final List<Rectangle> detRects = config.faceDetector.detect(image);

		return detectFaces(image, detRects);
	}

	/**
	 * Detect faces in the image using the given rectangles as the seeds from
	 * which to start fitting the model.
	 * 
	 * @param image
	 *            the image
	 * @param detRects
	 *            the seed rectangles
	 * @return the detected faces
	 */
	public List<CLMDetectedFace> detectFaces(FImage image, List<Rectangle> detRects) {
		final List<CLMDetectedFace> faces = new ArrayList<CLMDetectedFace>();

		for (final Rectangle f : detRects) {
			if ((f.width == 0) || (f.height == 0)) {
				continue;
			}

			initShape(f, config.shape, config.referenceShape);
			config.clm._pdm.calcParams(config.shape, config.clm._plocal, config.clm._pglobl);

			config.clm.fit(image, config.windowSize, config.nIter, config.clamp, config.fTol);
			config.clm._pdm.calcShape2D(config.shape, config.clm._plocal, config.clm._pglobl);

			if (config.fcheck) {
				if (!config.failureCheck.check(config.clm.getViewIdx(), image, config.shape)) {
					continue;
				}
			}

			faces.add(new CLMDetectedFace(f, config.shape.copy(), config.clm._pglobl.copy(), config.clm._plocal.copy(),
					config.clm._visi[config.clm.getViewIdx()].copy(), image));
		}

		return faces;
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
	private void initShape(final Rectangle r, final Matrix shape, final Matrix _rshape) {
		assert ((shape.getRowDimension() == _rshape.getRowDimension()) && (shape
				.getColumnDimension() == _rshape.getColumnDimension()));

		final int n = _rshape.getRowDimension() / 2;

		final double a = r.width * Math.cos(config.similarity[1]) * config.similarity[0] + 1;
		final double b = r.width * Math.sin(config.similarity[1]) * config.similarity[0];

		final double tx = r.x + (int) (r.width / 2) + r.width * config.similarity[2];
		final double ty = r.y + (int) (r.height / 2) + r.height * config.similarity[3];

		final double[][] s = _rshape.getArray();
		final double[][] d = shape.getArray();

		for (int i = 0; i < n; i++) {
			d[i][0] = a * s[i][0] - b * s[i + n][0] + tx;
			d[i + n][0] = b * s[i][0] + a * s[i + n][0] + ty;
		}
	}

	/**
	 * Get the internal configuration of the detector.
	 * 
	 * @return the internal configuration of the detector.
	 */
	public Configuration getConfiguration() {
		return config;
	}
}
