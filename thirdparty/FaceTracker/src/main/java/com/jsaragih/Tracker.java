/**
 * FaceTracker Licence
 * -------------------
 * (Academic, non-commercial, not-for-profit licence)
 *
 * Copyright (c) 2010 Jason Mora Saragih
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * The software is provided under the terms of this licence stricly for
 *       academic, non-commercial, not-for-profit purposes.
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions (licence) and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions (licence) and the following disclaimer
 *       in the documentation and/or other materials provided with the
 *       distribution.
 *     * The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *     * As this software depends on other libraries, the user must adhere to and
 *       keep in place any licencing terms of those libraries.
 *     * Any publications arising from the use of this software, including but
 *       not limited to academic journal and conference publications, technical
 *       reports and manuals, must cite the following work:
 *
 *       J. M. Saragih, S. Lucey, and J. F. Cohn. Face Alignment through Subspace
 *       Constrained Mean-Shifts. International Journal of Computer Vision
 *       (ICCV), September, 2009.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jsaragih;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.FourierTemplateMatcher;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

/**
 * The initial ported version of the CLMTracker that can only track a single
 * face in an image. It's had only a few small changes to allow it to work with
 * the list which the face detector class now returns. Unless you're sure you
 * only want to track a single face, you should probably use the new
 * OpenIMAJ org.openimaj.image.processing.face.tracking.clm.MultiTracker class 
 * from the faces sub-project instead.
 * 
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
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
		}
	)
public class Tracker {
	private static boolean init = false;
	static { Tracker.init(); }
	
	static synchronized void init() {
		if (!init) {
			System.err.println("This software uses the OpenIMAJ port of FaceTracker.");
			System.err.println("FaceTracker has a different license to the rest of OpenIMAJ:");
			System.err.println();
			System.err.println("FaceTracker Licence");
			System.err.println("-------------------");
			System.err.println("(Academic, non-commercial, not-for-profit licence)");
			System.err.println();
			System.err.println("Copyright (c) 2010 Jason Mora Saragih");
			System.err.println("All rights reserved.");
			System.err.println("");
			System.err.println("Redistribution and use in source and binary forms, with or without ");
			System.err.println("modification, are permitted provided that the following conditions are met:");
			System.err.println();
			System.err.println("    * The software is provided under the terms of this licence stricly for");
			System.err.println("      academic, non-commercial, not-for-profit purposes.");
			System.err.println("    * Redistributions of source code must retain the above copyright notice, ");
			System.err.println("      this list of conditions (licence) and the following disclaimer.");
			System.err.println("    * Redistributions in binary form must reproduce the above copyright ");
			System.err.println("      notice, this list of conditions (licence) and the following disclaimer ");
			System.err.println("      in the documentation and/or other materials provided with the ");
			System.err.println("      distribution.");
			System.err.println("    * The name of the author may not be used to endorse or promote products ");
			System.err.println("      derived from this software without specific prior written permission.");
			System.err.println("    * As this software depends on other libraries, the user must adhere to and ");
			System.err.println("      keep in place any licencing terms of those libraries.");
			System.err.println("    * Any publications arising from the use of this software, including but");
			System.err.println("      not limited to academic journal and conference publications, technical");
			System.err.println("      reports and manuals, must cite the following work:");
			System.err.println();
			System.err.println("      J. M. Saragih, S. Lucey, and J. F. Cohn. Face Alignment through Subspace ");
			System.err.println("      Constrained Mean-Shifts. International Journal of Computer Vision ");
			System.err.println("      (ICCV), September, 2009.");
			System.err.println();
			System.err.println("THIS SOFTWARE IS PROVIDED BY THE AUTHOR \"AS IS\" AND ANY EXPRESS OR IMPLIED ");
			System.err.println("WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF ");
			System.err.println("MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO ");
			System.err.println("EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, ");
			System.err.println("INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, ");
			System.err.println("BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, ");
			System.err.println("DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY ");
			System.err.println("OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING ");
			System.err.println("NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, ");
			System.err.println("EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.");
			init = true;
		}
	}

	private static final double TSCALE = 0.3;

	/** Constrained Local Model */
	public CLM _clm;

	/** Face Detector */
	FDet _fdet;

	/** Frame number since last detection */
	long _frame;

	/** Failure checker */
	MFCheck _fcheck;

	/** Current shape */
	public Matrix _shape;

	/** Reference shape */
	public Matrix _rshape;

	/** Detected rectangle */
	Rectangle _rect;

	/** Initialization similarity */
	double[] _simil;

	FImage gray_, temp_;

	private FImage small_;

	Tracker(CLM clm, FDet fdet, MFCheck fcheck, Matrix rshape, double[] simil) {
		_clm = clm;
		_fdet = fdet;
		_fcheck = fcheck;

		_rshape = rshape.copy();
		_simil = simil;

		_shape = new Matrix(2 * _clm._pdm.nPoints(), 1);
		_rect.x = 0;
		_rect.y = 0;
		_rect.width = 0;
		_rect.height = 0;
		_frame = -1;
		_clm._pdm.identity(_clm._plocal, _clm._pglobl);
	}

	Tracker() {
	}

	/** Reset frame number (will perform detection in next image) */
	public void frameReset() {
		_frame = -1;
	}

	static Tracker load(final String fname) throws FileNotFoundException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			Scanner sc = new Scanner(br);
			return read(sc, true);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * @param in
	 * @return a tracker
	 */
	public static Tracker load(final InputStream in) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			Scanner sc = new Scanner(br);
			return read(sc, true);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
	}

	void save(final String fname) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fname));

			write(bw);
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
			}
		}
	}

	void write(BufferedWriter s) throws IOException {
		s.write(IO.Types.TRACKER.ordinal() + " ");

		_clm.write(s);
		_fdet.write(s);
		_fcheck.write(s);
		IO.writeMat(s, _rshape);

		s.write(_simil[0] + " " + _simil[1] + " " + _simil[2] + " " + _simil[3]
		                                                                     + " ");
	}

	static Tracker read(Scanner s, boolean readType) {
		if (readType) {
			int type = s.nextInt();
			assert (type == IO.Types.TRACKER.ordinal());
		}
		Tracker tracker = new Tracker();
		tracker._clm = CLM.read(s, true);
		tracker._fdet = FDet.read(s, true);
		tracker._fcheck = MFCheck.read(s, true);
		tracker._rshape = IO.readMat(s);

		tracker._simil = new double[] { s.nextDouble(), s.nextDouble(),
				s.nextDouble(), s.nextDouble() };
		tracker._shape = new Matrix(2 * tracker._clm._pdm.nPoints(), 1);

		tracker._rect = new Rectangle();
		tracker._rect.x = 0;
		tracker._rect.y = 0;
		tracker._rect.width = 0;
		tracker._rect.height = 0;
		tracker._frame = -1;
		tracker._clm._pdm.identity(tracker._clm._plocal, tracker._clm._pglobl);

		return tracker;
	}

	/**
	 * @param im
	 * @param wSize
	 * @param fpd
	 * @param nIter
	 * @param clamp
	 * @param fTol
	 * @param fcheck
	 * @return 0 for success, -1 for redetect
	 */
	public int track(FImage im, int[] wSize, final int fpd, final int nIter,
			final double clamp, final double fTol, final boolean fcheck) {
		gray_ = im;

		boolean gen, rsize = true;
		Rectangle R = new Rectangle(0, 0, 0, 0);

		if ((_frame < 0) || (fpd >= 0 && fpd < _frame)) {
			_frame = 0;
			List<Rectangle> RL = _fdet.detect(gray_);

			// Get largest
			double max = 0;
			for (Rectangle r : RL)
				if (r.calculateArea() > max) {
					max = r.calculateArea();
					R = r;
				}

			gen = true;
		} else {
			R = redetect(gray_);
			gen = false;
		}

		if ((R.width == 0) || (R.height == 0)) {
			_frame = -1;
			return -1;
		}

		_frame++;

		if (gen) {
			initShape(R, _shape);
			_clm._pdm.calcParams(_shape, _clm._plocal, _clm._pglobl);
		} else {
			double tx = R.x - _rect.x;
			double ty = R.y - _rect.y;

			_clm._pglobl.getArray()[4][0] += tx;
			_clm._pglobl.getArray()[5][0] += ty;

			rsize = false;
		}

		_clm.fit(gray_, wSize, nIter, clamp, fTol);

		_clm._pdm.calcShape2D(_shape, _clm._plocal, _clm._pglobl);

		if (fcheck) {
			if (!_fcheck.check(_clm.getViewIdx(), gray_, _shape))
				return -1;
		}

		_rect = updateTemplate(gray_, _shape, rsize);

		if ((_rect.width == 0) || (_rect.height == 0))
			return -1;

		return 0;
	}

	void initShape(Rectangle r, Matrix shape) {
		assert ((shape.getRowDimension() == _rshape.getRowDimension()) && (shape
				.getColumnDimension() == _rshape.getColumnDimension()));

		int n = _rshape.getRowDimension() / 2;

		double a = r.width * Math.cos(_simil[1]) * _simil[0] + 1;
		double b = r.width * Math.sin(_simil[1]) * _simil[0];

		double tx = r.x + (int) (r.width / 2) + r.width * _simil[2];
		double ty = r.y + (int) (r.height / 2) + r.height * _simil[3];

		double[][] s = _rshape.getArray();
		double[][] d = shape.getArray();

		for (int i = 0; i < n; i++) {
			d[i][0] = a * s[i][0] - b * s[i + n][0] + tx;
			d[i + n][0] = b * s[i][0] + a * s[i + n][0] + ty;
		}
	}

	Rectangle redetect(FImage im) {
		final int ww = im.width;
		final int hh = im.height;

		int w = (int) (TSCALE * ww - temp_.width + 1);
		int h = (int) (TSCALE * hh - temp_.height + 1);

		small_ = ResizeProcessor.resample(im, (int) (TSCALE * ww),
				(int) (TSCALE * hh));

		h = small_.height - temp_.height + 1;
		w = small_.width - temp_.width + 1;

		FourierTemplateMatcher matcher = new FourierTemplateMatcher(temp_,
				FourierTemplateMatcher.Mode.NORM_CORRELATION_COEFFICIENT);
		matcher.analyseImage(small_);
		float[][] ncc_ = matcher.getResponseMap().pixels;

		Rectangle R = temp_.getBounds();
		float v, vb = -2;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				v = ncc_[y][x];

				if (v > vb) {
					vb = v;
					R.x = x;
					R.y = y;
				}
			}
		}

		R.x *= 1.0 / TSCALE;
		R.y *= 1.0 / TSCALE;

		R.width *= 1.0 / TSCALE;
		R.height *= 1.0 / TSCALE;

		return R;
	}

	Rectangle updateTemplate(FImage im, Matrix s, boolean rsize) {
		final int n = s.getRowDimension() / 2;

		double[][] sv = s.getArray(); // ,y = s.begin<double>()+n;
		double xmax = sv[0][0], ymax = sv[n][0], xmin = sv[0][0], ymin = sv[n][0];

		for (int i = 0; i < n; i++) {
			double vx = sv[i][0];
			double vy = sv[i + n][0];

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
				|| Double.isInfinite(ymax)) {
			return new Rectangle(0, 0, 0, 0);
		} else {
			xmin *= TSCALE;
			ymin *= TSCALE;
			xmax *= TSCALE;
			ymax *= TSCALE;

			Rectangle R = new Rectangle((float) Math.floor(xmin),
					(float) Math.floor(ymin), (float) Math.ceil(xmax - xmin),
					(float) Math.ceil(ymax - ymin));

			final int ww = im.width;
			final int hh = im.height;

			if (rsize) {
				small_ = ResizeProcessor.resample(im, (int) (TSCALE * ww),
						(int) (TSCALE * hh));
			}

			temp_ = small_.extractROI(R);

			R.x *= 1.0 / TSCALE;
			R.y *= 1.0 / TSCALE;
			R.width *= 1.0 / TSCALE;
			R.height *= 1.0 / TSCALE;

			return R;
		}
	}
}
