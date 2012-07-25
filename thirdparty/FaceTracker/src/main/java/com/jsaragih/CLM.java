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
import java.util.Arrays;
import java.util.Scanner;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

/**
 * Constrained Local Model
 * 
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
public class CLM {
	static { Tracker.init(); }
	
	class SimTData {
		// data for similarity xform
		double a;
		double b;
		double tx;
		double ty;
	}

	/** 3D Shape model */
	public PDM _pdm;

	/** local parameters */
	public Matrix _plocal;

	/** global parameters */
	public Matrix _pglobl;

	/** Reference shape */
	public Matrix _refs;

	/** Centers/view (Euler) */
	public Matrix[] _cent;

	/** Visibility for each view */
	public Matrix[] _visi;

	/** Patches/point/view */
	public MPatch[][] _patch;

	private Matrix cshape_, bshape_, oshape_, ms_, u_, g_, J_, H_;
	private FImage[] prob_;
	private FImage[] pmem_;
	private FImage[] wmem_;

	void calcSimT(Matrix src, Matrix dst, SimTData data) {
		assert ((src.getRowDimension() == dst.getRowDimension())
				&& (src.getColumnDimension() == dst.getColumnDimension()) && (src
				.getColumnDimension() == 1));

		int n = src.getRowDimension() / 2;

		Matrix H = new Matrix(4, 4);
		Matrix g = new Matrix(4, 1);

		final double[][] Hv = H.getArray();
		final double[][] gv = g.getArray();

		for (int i = 0; i < n; i++) {
			double ptr1x = src.get(i, 0);
			double ptr1y = src.get(i + n, 0);
			double ptr2x = dst.get(i, 0);
			double ptr2y = dst.get(i + n, 0);

			Hv[0][0] += (ptr1x * ptr1x) + (ptr1y * ptr1y);
			Hv[0][2] += ptr1x;
			Hv[0][3] += ptr1y;

			gv[0][0] += ptr1x * ptr2x + ptr1y * ptr2y;
			gv[1][0] += ptr1x * ptr2y - ptr1y * ptr2x;
			gv[2][0] += ptr2x;
			gv[3][0] += ptr2y;
		}

		Hv[1][1] = Hv[0][0];
		Hv[3][0] = Hv[0][3];
		Hv[1][2] = Hv[2][1] = -Hv[3][0];
		Hv[1][3] = Hv[3][1] = Hv[2][0] = Hv[0][2];
		Hv[2][2] = Hv[3][3] = n;

		Matrix p = H.solve(g);

		data.a = p.get(0, 0);
		data.b = p.get(1, 0);
		data.tx = p.get(2, 0);
		data.ty = p.get(3, 0);
	}

	void invSimT(SimTData in, SimTData out) {
		Matrix M = new Matrix(
				new double[][] { { in.a, -in.b }, { in.b, in.a } });
		Matrix N = M.inverse();
		out.a = N.get(0, 0);
		out.b = N.get(1, 0);

		out.tx = -1.0 * (N.get(0, 0) * in.tx + N.get(0, 1) * in.ty);
		out.ty = -1.0 * (N.get(1, 0) * in.tx + N.get(1, 1) * in.ty);
	}

	void simT(Matrix s, SimTData data) {
		assert (s.getColumnDimension() == 1);

		int n = s.getRowDimension() / 2;

		for (int i = 0; i < n; i++) {
			double x = s.get(i, 0);
			double y = s.get(i + n, 0);

			s.set(i, 0, data.a * x - data.b * y + data.tx);
			s.set(i + n, 0, data.b * x + data.a * y + data.ty);
		}
	}

	/**
	 * Construct CLM
	 * 
	 * @param s
	 * @param r
	 * @param c
	 * @param v
	 * @param p
	 */
	public CLM(PDM s, Matrix r, Matrix[] c, Matrix[] v, MPatch[][] p) {
		int n = p.length;

		assert (((int) c.length == n) && ((int) v.length == n));
		assert ((r.getRowDimension() == 2 * s.nPoints()) && (r
				.getColumnDimension() == 1));

		for (int i = 0; i < n; i++) {
			assert ((int) p[i].length == s.nPoints());
			assert ((c[i].getRowDimension() == 3) && (c[i].getColumnDimension() == 1));
			assert ((v[i].getRowDimension() == s.nPoints()) && (v[i]
					.getColumnDimension() == 1));
		}

		_pdm = s;
		_refs = r.copy();
		_cent = new Matrix[n];
		_visi = new Matrix[n];
		_patch = new MPatch[n][];

		for (int i = 0; i < n; i++) {
			_cent[i] = c[i].copy();
			_visi[i] = v[i].copy();
			_patch[i] = new MPatch[p[i].length];

			for (int j = 0; j < p[i].length; j++)
				_patch[i][j] = p[i][j];
		}

		_plocal = new Matrix(_pdm.nModes(), 1);
		_pglobl = new Matrix(6, 1);
		cshape_ = new Matrix(2 * _pdm.nPoints(), 1);
		bshape_ = new Matrix(2 * _pdm.nPoints(), 1);
		oshape_ = new Matrix(2 * _pdm.nPoints(), 1);
		ms_ = new Matrix(2 * _pdm.nPoints(), 1);
		u_ = new Matrix(6 + _pdm.nModes(), 1);
		g_ = new Matrix(6 + _pdm.nModes(), 1);
		J_ = new Matrix(2 * _pdm.nPoints(), 6 + _pdm.nModes());
		H_ = new Matrix(6 + _pdm.nModes(), 6 + _pdm.nModes());

		prob_ = new FImage[_pdm.nPoints()];
		pmem_ = new FImage[_pdm.nPoints()];
		wmem_ = new FImage[_pdm.nPoints()];
	}

	CLM() {
	}

	static CLM load(final String fname) throws FileNotFoundException {
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
		s.write(IO.Types.CLM.ordinal() + " " + _patch.length + " ");
		_pdm.write(s);
		IO.writeMat(s, _refs);

		for (int i = 0; i < _cent.length; i++)
			IO.writeMat(s, _cent[i]);

		for (int i = 0; i < _visi.length; i++)
			IO.writeMat(s, _visi[i]);

		for (int i = 0; i < _patch.length; i++) {
			for (int j = 0; j < _pdm.nPoints(); j++)
				_patch[i][j].write(s);
		}
	}

	/**
	 * Read a CLM
	 * @param s
	 * @param readType
	 * @return the CLM
	 */
	public static CLM read(Scanner s, boolean readType) {
		if (readType) {
			int type = s.nextInt();
			assert (type == IO.Types.CLM.ordinal());
		}

		CLM clm = new CLM();

		int n = s.nextInt();
		clm._pdm = PDM.read(s, true);
		clm._cent = new Matrix[n];
		clm._visi = new Matrix[n];
		clm._patch = new MPatch[n][];
		clm._refs = IO.readMat(s);

		for (int i = 0; i < clm._cent.length; i++)
			clm._cent[i] = IO.readMat(s);

		for (int i = 0; i < clm._visi.length; i++)
			clm._visi[i] = IO.readMat(s);

		for (int i = 0; i < clm._patch.length; i++) {
			clm._patch[i] = new MPatch[clm._pdm.nPoints()];

			for (int j = 0; j < clm._pdm.nPoints(); j++) {
				clm._patch[i][j] = MPatch.read(s, true);
			}
		}

		clm._plocal = new Matrix(clm._pdm.nModes(), 1);
		clm._pglobl = new Matrix(6, 1);
		clm.cshape_ = new Matrix(2 * clm._pdm.nPoints(), 1);
		clm.bshape_ = new Matrix(2 * clm._pdm.nPoints(), 1);
		clm.oshape_ = new Matrix(2 * clm._pdm.nPoints(), 1);
		clm.ms_ = new Matrix(2 * clm._pdm.nPoints(), 1);
		clm.u_ = new Matrix(6 + clm._pdm.nModes(), 1);
		clm.g_ = new Matrix(6 + clm._pdm.nModes(), 1);
		clm.J_ = new Matrix(2 * clm._pdm.nPoints(), 6 + clm._pdm.nModes());
		clm.H_ = new Matrix(6 + clm._pdm.nModes(), 6 + clm._pdm.nModes());
		clm.prob_ = new FImage[clm._pdm.nPoints()];
		clm.pmem_ = new FImage[clm._pdm.nPoints()];
		clm.wmem_ = new FImage[clm._pdm.nPoints()];

		return clm;
	}

	/**
	 * Makes a copy of this CLM.
	 * 
	 * @return A copy of this CLM.
	 */
	public CLM copy() {
		CLM c = new CLM();
		c._pdm = _pdm.copy();
		c._cent = new Matrix[_cent.length];
		c._visi = new Matrix[_visi.length];
		c._patch = new MPatch[_patch.length][];

		for (int i = 0; i < _cent.length; i++)
			c._cent[i] = _cent[i].copy();

		for (int i = 0; i < _visi.length; i++)
			c._visi[i] = _visi[i].copy();

		for (int i = 0; i < _patch.length; i++) {
			c._patch[i] = new MPatch[_pdm.nPoints()];
			for (int j = 0; j < _pdm.nPoints(); j++)
				c._patch[i][j] = _patch[i][j].copy();
		}

		c._refs = _refs.copy();
		c._plocal = _plocal.copy();
		c._pglobl = _pglobl.copy();
		c.cshape_ = cshape_.copy();
		c.bshape_ = bshape_.copy();
		c.oshape_ = oshape_.copy();
		c.ms_ = ms_.copy();
		c.u_ = u_.copy();
		c.g_ = g_.copy();
		c.J_ = J_.copy();
		c.H_ = H_.copy();
		c.prob_ = Arrays
				.copyOf(prob_, prob_.length, (new FImage[0]).getClass());
		c.pmem_ = Arrays
				.copyOf(pmem_, pmem_.length, (new FImage[0]).getClass());
		c.wmem_ = Arrays
				.copyOf(wmem_, wmem_.length, (new FImage[0]).getClass());

		return c;
	}

	final int nViews() {
		return _patch.length;
	}

	/**
	 * @return View index
	 */
	public int getViewIdx() {
		int idx = 0;

		if (this.nViews() == 1) {
			return 0;
		} else {
			int i;
			double v1, v2, v3, d, dbest = -1.0;
			for (i = 0; i < this.nViews(); i++) {
				v1 = _pglobl.get(1, 0) - _cent[i].get(0, 0);
				v2 = _pglobl.get(2, 0) - _cent[i].get(1, 0);
				v3 = _pglobl.get(3, 0) - _cent[i].get(2, 0);

				d = v1 * v1 + v2 * v2 + v3 * v3;

				if (dbest < 0 || d < dbest) {
					dbest = d;
					idx = i;
				}
			}
			return idx;
		}
	}

	/**
	 * Fit the model to the image
	 * @param im
	 * @param wSize
	 * @param nIter
	 * @param clamp
	 * @param fTol
	 */
	public void fit(FImage im, int[] wSize, int nIter, double clamp, double fTol) {
		int i, idx, n = _pdm.nPoints();

		SimTData d1 = new SimTData();
		SimTData d2 = new SimTData();

		for (int witer = 0; witer < wSize.length; witer++) {
			_pdm.calcShape2D(cshape_, _plocal, _pglobl);

			calcSimT(_refs, cshape_, d1);
			invSimT(d1, d2);

			idx = getViewIdx();

			for (i = 0; i < n; i++) {
				if (_visi[idx].getRowDimension() == n) {
					if (_visi[idx].get(i, 0) == 0)
						continue;
				}

				int w = wSize[witer] + _patch[idx][i]._w - 1;
				int h = wSize[witer] + _patch[idx][i]._h - 1;

				Matrix sim = new Matrix(new double[][] {
						{ d1.a, -d1.b, cshape_.get(i, 0) },
						{ d1.b, d1.a, cshape_.get(i + n, 0) } });

				if (wmem_[i] == null || (w > wmem_[i].width)
						|| (h > wmem_[i].height))
					wmem_[i] = new FImage(w, h);

				// gah, we need to get a subimage backed by the original;
				// luckily its from the origin
				FImage wimg = subImage(wmem_[i], w, h);

				FImage wimg_o = wimg; // why? is this supposed to clone?
				Matrix sim_o = sim;
				FImage im_o = im;

				cvGetQuadrangleSubPix(im_o, wimg_o, sim_o);

				if (pmem_[i] == null || wSize[witer] > pmem_[i].height)
					pmem_[i] = new FImage(wSize[witer], wSize[witer]);

				prob_[i] = subImage(pmem_[i], wSize[witer], wSize[witer]);

				_patch[idx][i].response(wimg, prob_[i]);
			}

			simT(cshape_, d2);
			_pdm.applySimT(d2, _pglobl);
			bshape_.setMatrix(0, cshape_.getRowDimension() - 1, 0,
					cshape_.getColumnDimension() - 1, cshape_);

			this.optimize(idx, wSize[witer], nIter, fTol, clamp, true);
			this.optimize(idx, wSize[witer], nIter, fTol, clamp, false);

			_pdm.applySimT(d1, _pglobl);
		}
	}

	/**
	 * Construct a view on an FImage from the origin to a new height/width
	 * (which must be the same or smaller than in the input image)
	 * 
	 * @param fImage
	 * @param i
	 * @param j
	 * @return
	 */
	private FImage subImage(FImage fImage, int w, int h) {
		FImage img = new FImage(fImage.pixels);
		img.width = w;
		img.height = h;
		return img;
	}

	private void cvGetQuadrangleSubPix(FImage src, FImage dest, Matrix tx) {
		// FIXME: move this somewhere appropriate
		final float[][] dpix = dest.pixels;

		final double A11 = tx.get(0, 0);
		final double A12 = tx.get(0, 1);
		final double A21 = tx.get(1, 0);
		final double A22 = tx.get(1, 1);
		final double b1 = tx.get(0, 2);
		final double b2 = tx.get(1, 2);

		for (int y = 0; y < dest.width; y++) {
			for (int x = 0; x < dest.height; x++) {
				double xp = x - (dest.width - 1) * 0.5;
				double yp = y - (dest.height - 1) * 0.5;

				float xpp = (float) (A11 * xp + A12 * yp + b1);
				float ypp = (float) (A21 * xp + A22 * yp + b2);

				dpix[y][x] = src.getPixelInterpNative(xpp, ypp, 0);
			}
		}
	}

	void optimize(int idx, int wSize, int nIter, double fTol, double clamp,
			boolean rigid) {
		int m = _pdm.nModes();
		int n = _pdm.nPoints();

		double sigma = (wSize * wSize) / 36.0;

		Matrix u, g, J, H;
		if (rigid) {
			// FIXME - in the original this creates "views" rather than
			// copies
			u = u_.getMatrix(0, 6 - 1, 0, 1 - 1);
			g = g_.getMatrix(0, 6 - 1, 0, 1 - 1);
			J = J_.getMatrix(0, 2 * n - 1, 0, 6 - 1);
			H = H_.getMatrix(0, 6 - 1, 0, 6 - 1);
		} else {
			u = u_;
			g = g_;
			J = J_;
			H = H_;
		}

		for (int iter = 0; iter < nIter; iter++) {
			_pdm.calcShape2D(cshape_, _plocal, _pglobl);

			if (iter > 0) {
				if (l2norm(cshape_, oshape_) < fTol)
					break;
			}

			oshape_.setMatrix(0, oshape_.getRowDimension() - 1, 0,
					oshape_.getColumnDimension() - 1, cshape_);

			if (rigid) {
				_pdm.calcRigidJacob(_plocal, _pglobl, J);
			} else {
				_pdm.calcJacob(_plocal, _pglobl, J);
			}

			for (int i = 0; i < n; i++) {
				if (_visi[idx].getRowDimension() == n) {
					if (_visi[idx].get(i, 0) == 0) {
						MatrixUtils.setRow(J, i, 0);

						MatrixUtils.setRow(J, i + n, 0);

						ms_.set(i, 0, 0);
						ms_.set(i + n, 0, 0);

						continue;
					}
				}

				double dx = cshape_.get(i, 0) - bshape_.get(i, 0) + (wSize - 1)
						/ 2;
				double dy = cshape_.get(i + n, 0) - bshape_.get(i + n, 0)
						+ (wSize - 1) / 2;

				double mx = 0.0, my = 0.0, sum = 0.0;
				for (int ii = 0; ii < wSize; ii++) {
					double vx = (dy - ii) * (dy - ii);

					for (int jj = 0; jj < wSize; jj++) {
						double vy = (dx - jj) * (dx - jj);

						double v = prob_[i].pixels[ii][jj];
						v *= Math.exp(-0.5 * (vx + vy) / sigma);
						sum += v;
						mx += v * jj;
						my += v * ii;
					}
				}

				ms_.set(i, 0, mx / sum - dx);
				ms_.set(i + n, 0, my / sum - dy);
			}

			g = J.transpose().times(ms_);
			H = J.transpose().times(J);

			if (!rigid) {
				for (int i = 0; i < m; i++) {
					double var = 0.5 * sigma / _pdm._E.get(0, i);

					H.getArray()[6 + i][6 + i] += var;
					g.getArray()[6 + i][0] -= var * _plocal.get(i, 0);
				}
			}

			MatrixUtils.fill(u_, 0);
			u = H.solve(g);

			if (rigid)
				u_.setMatrix(0, 6 - 1, 0, 1 - 1, u);
			else
				u_.setMatrix(0, u.getRowDimension() - 1, 0,
						u.getColumnDimension() - 1, u);

			_pdm.calcReferenceUpdate(u_, _plocal, _pglobl);

			if (!rigid)
				_pdm.clamp(_plocal, clamp);
		}

		// FIXME do we need to deal with rigid setting underlying _u correctly?
		// this attempts do do so, but might not be the best way!
		if (rigid) {
			u_.setMatrix(0, 6 - 1, 0, 1 - 1, u);
			g_.setMatrix(0, 6 - 1, 0, 1 - 1, g);
			J_.setMatrix(0, 2 * n - 1, 0, 6 - 1, J);
			H_.setMatrix(0, 6 - 1, 0, 6 - 1, H);
		} else {
			u_.setMatrix(0, u.getRowDimension() - 1, 0,
					u.getColumnDimension() - 1, u);
			g_.setMatrix(0, g.getRowDimension() - 1, 0,
					g.getColumnDimension() - 1, g);
			J_.setMatrix(0, J.getRowDimension() - 1, 0,
					J.getColumnDimension() - 1, J);
			H_.setMatrix(0, H.getRowDimension() - 1, 0,
					H.getColumnDimension() - 1, H);
		}
	}

	private double l2norm(Matrix m1, Matrix m2) {
		final double[][] m1v = m1.getArray();
		final double[][] m2v = m2.getArray();
		final int rows = m1.getRowDimension();
		final int cols = m1.getColumnDimension();

		double sum = 0;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				double diff = m1v[r][c] - m2v[r][c];

				sum += diff * diff;
			}
		}

		return Math.sqrt(sum);
	}
}
