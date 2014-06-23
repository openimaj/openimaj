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
import java.util.Scanner;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.transform.RemapProcessor;

import Jama.Matrix;

/**
 * Piecewise affine warp
 * 
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class PAW {
	static {
		Tracker.init();
	}

	/** Number of pixels */
	int _nPix;

	/** Minimum x-coord for src */
	double _xmin;

	/** Minimum y-coord for src */
	double _ymin;

	/** Source points */
	Matrix _src;

	/** destination points */
	Matrix _dst;

	/** Triangulation */
	int[][] _tri;

	/** Triangle for each valid pixel */
	int[][] _tridx;

	/** Valid region mask */
	FImage _mask;

	/** affine coeffs for all triangles */
	Matrix _coeff;

	/** matrix of (c,x,y) coeffs for alpha */
	Matrix _alpha;

	/** matrix of (c,x,y) coeffs for alpha */
	Matrix _beta;

	/** x-destination of warped points */
	FImage _mapx;

	/** y-destination of warped points */
	FImage _mapy;

	boolean sameSide(double x0, double y0, double x1, double y1, double x2,
			double y2, double x3, double y3)
	{
		final double x = (x3 - x2) * (y0 - y2) - (x0 - x2) * (y3 - y2);
		final double y = (x3 - x2) * (y1 - y2) - (x1 - x2) * (y3 - y2);

		if (x * y >= 0)
			return true;
		return false;
	}

	int isWithinTri(double x, double y, int[][] tri, Matrix shape) {
		final int n = tri.length;
		final int p = shape.getRowDimension() / 2;

		for (int t = 0; t < n; t++) {
			final int i = tri[t][0];
			final int j = tri[t][1];
			final int k = tri[t][2];

			final double s11 = shape.get(i, 0);
			final double s21 = shape.get(j, 0);
			final double s31 = shape.get(k, 0);
			final double s12 = shape.get(i + p, 0);
			final double s22 = shape.get(j + p, 0);
			final double s32 = shape.get(k + p, 0);

			if (sameSide(x, y, s11, s12, s21, s22, s31, s32)
					&& sameSide(x, y, s21, s22, s11, s12, s31, s32)
					&& sameSide(x, y, s31, s32, s11, s12, s21, s22))
				return t;
		}
		return -1;
	}

	static PAW load(final String fname) throws FileNotFoundException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			final Scanner sc = new Scanner(br);
			return read(sc, true);
		} finally {
			try {
				br.close();
			} catch (final IOException e) {
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
			} catch (final IOException e) {
			}
		}
	}

	void write(BufferedWriter s) throws IOException {
		s.write(IO.Types.PAW.ordinal() + " " + _nPix + " " + _xmin + " "
				+ _ymin + " ");

		IO.writeMat(s, _src);
		IO.writeIntArray(s, _tri);
		IO.writeIntArray(s, _tridx);
		IO.writeImg(s, _mask);
		IO.writeMat(s, _alpha);
		IO.writeMat(s, _beta);
	}

	static PAW read(Scanner s, boolean readType) {
		if (readType) {
			final int type = s.nextInt();
			assert (type == IO.Types.PAW.ordinal());
		}

		final PAW paw = new PAW();
		paw._nPix = s.nextInt();
		paw._xmin = s.nextDouble();
		paw._ymin = s.nextDouble();

		paw._src = IO.readMat(s);
		paw._tri = IO.readIntArray(s);
		paw._tridx = IO.readIntArray(s);
		paw._mask = IO.readImgByte(s);
		paw._alpha = IO.readMat(s);
		paw._beta = IO.readMat(s);

		paw._mapx = new FImage(paw._mask.width, paw._mask.height);
		paw._mapy = new FImage(paw._mask.width, paw._mask.height);

		paw._coeff = new Matrix(paw.nTri(), 6);
		paw._dst = paw._src;

		return paw;
	}

	int nPoints() {
		return _src.getRowDimension() / 2;
	}

	int nTri() {
		return _tri.length;
	}

	int width() {
		return _mask.width;
	}

	int height() {
		return _mask.height;
	}

	PAW(Matrix src, int[][] tri) {
		assert (src.getColumnDimension() == 1);
		assert (tri[0].length == 3);

		_src = src.copy();
		_tri = tri.clone();

		final int n = nPoints();

		_alpha = new Matrix(nTri(), 3);
		_beta = new Matrix(nTri(), 3);

		for (int i = 0; i < nTri(); i++) {
			final int j = _tri[i][0];
			final int k = _tri[i][1];
			final int l = _tri[i][2];

			final double c1 = _src.get(l + n, 0) - _src.get(j + n, 0);
			final double c2 = _src.get(l, 0) - _src.get(j, 0);
			final double c4 = _src.get(k + n, 0) - _src.get(j + n, 0);
			final double c3 = _src.get(k, 0) - _src.get(j, 0);
			final double c5 = c3 * c1 - c2 * c4;

			_alpha.set(i, 0, (_src.get(j + n, 0) * c2 - _src.get(j, 0) * c1)
					/ c5);
			_alpha.set(i, 1, c1 / c5);
			_alpha.set(i, 2, -c2 / c5);

			_beta.set(i, 0, (_src.get(j, 0) * c4 - _src.get(j + n, 0) * c3)
					/ c5);
			_beta.set(i, 1, -c4 / c5);
			_beta.set(i, 2, c3 / c5);
		}

		double xmax, ymax, xmin, ymin;
		xmax = xmin = _src.get(0, 0);
		ymax = ymin = _src.get(n, 0);

		for (int i = 0; i < n; i++) {
			final double vx = _src.get(i, 0);
			final double vy = _src.get(i + n, 0);

			xmax = Math.max(xmax, vx);
			ymax = Math.max(ymax, vy);
			xmin = Math.min(xmin, vx);
			ymin = Math.min(ymin, vy);
		}

		final int w = (int) (xmax - xmin + 1.0);
		final int h = (int) (ymax - ymin + 1.0);
		_mask = new FImage(w, h);
		_tridx = new int[h][w];

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				if ((_tridx[i][j] = isWithinTri(j + xmin, i + ymin, tri, _src)) == -1) {
					_mask.pixels[i][j] = 0;
				} else {
					_mask.pixels[i][j] = 0;
				}
			}
		}

		_mapx = new FImage(_mask.width, _mask.height);
		_mapy = new FImage(_mask.width, _mask.height);
		_coeff = new Matrix(nTri(), 6);

		_dst = _src;
		_xmin = xmin;
		_ymin = ymin;
	}

	PAW() {
	}

	void crop(FImage src, FImage dst, Matrix s) {
		assert ((s.getRowDimension() == _src.getRowDimension()) && (s
				.getColumnDimension() == 1));

		_dst = s;

		calcCoeff();

		warpRegion(_mapx, _mapy);

		RemapProcessor.remap(src, dst, _mapx, _mapy);
	}

	void calcCoeff() {
		final int p = nPoints();

		for (int l = 0; l < nTri(); l++) {
			final int i = _tri[l][0];
			final int j = _tri[l][1];
			final int k = _tri[l][2];

			final double c1 = _dst.get(i, 0);
			final double c2 = _dst.get(j, 0) - c1;
			final double c3 = _dst.get(k, 0) - c1;
			final double c4 = _dst.get(i + p, 0);
			final double c5 = _dst.get(j + p, 0) - c4;
			final double c6 = _dst.get(k + p, 0) - c4;

			final double[] coeff = _coeff.getArray()[l];
			final double[] alpha = _alpha.getArray()[l];
			final double[] beta = _beta.getArray()[l];

			coeff[0] = c1 + c2 * alpha[0] + c3 * beta[0];
			coeff[1] = c2 * alpha[1] + c3 * beta[1];
			coeff[2] = c2 * alpha[2] + c3 * beta[2];
			coeff[3] = c4 + c5 * alpha[0] + c6 * beta[0];
			coeff[4] = c5 * alpha[1] + c6 * beta[1];
			coeff[5] = c5 * alpha[2] + c6 * beta[2];
		}
	}

	void warpRegion(FImage mapx, FImage mapy) {
		if ((mapx.height != _mask.height) || (mapx.width != _mask.width))
			_mapx.internalAssign(new FImage(_mask.width, _mask.height));

		if ((mapy.height != _mask.height) || (mapy.width != _mask.width))
			_mapy.internalAssign(new FImage(_mask.width, _mask.height));

		int k = -1;
		double[] a = null, ap;

		final float[][] xp = mapx.pixels;
		final float[][] yp = mapy.pixels;
		final float[][] mp = _mask.pixels;

		for (int y = 0; y < _mask.height; y++) {
			final double yi = y + _ymin;

			for (int x = 0; x < _mask.width; x++) {
				final double xi = x + _xmin;

				if (mp[y][x] == 0) {
					xp[y][x] = -1;
					yp[y][x] = -1;
				} else {
					final int j = _tridx[y][x];

					if (j != k) {
						a = _coeff.getArray()[j];
						k = j;
					}
					ap = a;
					double xo = ap[0];
					xo += ap[1] * xi;
					xp[y][x] = (float) (xo + ap[2] * yi);

					double yo = ap[3];
					yo += ap[4] * xi;
					yp[y][x] = (float) (yo + ap[5] * yi);
				}
			}
		}
	}
}
