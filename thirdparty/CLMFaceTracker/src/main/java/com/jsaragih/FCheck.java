package com.jsaragih;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.image.FImage;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

/**
 * Check whether a patch looks like a face by applying an SVM classifier
 * 
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FCheck {
	/** < Piecewise affine warp */
	PAW _paw;
	
	/** < SVM bias */
	double _b;
	
	/** < SVM gain */
	Matrix _w;

	private Matrix vec_;
	private FImage crop_;

	/**
	 * Construct
	 * 
	 * @param b
	 * @param w
	 * @param paw
	 */
	FCheck(double b, Matrix w, PAW paw) {
		assert ((w.getRowDimension() == paw._nPix));

		_b = b;
		_w = w.copy();
		_paw = paw;

		crop_ = new FImage(_paw._mask.width, _paw._mask.height);
		vec_ = new Matrix(_paw._nPix, 1);
	}

	protected FCheck() {
	}

	static FCheck load(final String fname) throws FileNotFoundException {
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
		s.write(IO.Types.FCHECK.ordinal() + " " + _b + " ");
		IO.writeMat(s, _w);
		_paw.write(s);
	}

	static FCheck read(Scanner s, boolean readType) {
		if (readType) {
			int type = s.nextInt();
			assert (type == IO.Types.FCHECK.ordinal());
		}

		FCheck fcheck = new FCheck();

		fcheck._b = s.nextDouble();
		fcheck._w = IO.readMat(s);
		fcheck._paw = PAW.read(s, true);
		fcheck.crop_ = new FImage(fcheck._paw._mask.width,
				fcheck._paw._mask.height);
		fcheck.vec_ = new Matrix(fcheck._paw._nPix, 1);

		return fcheck;
	}

	boolean check(FImage im, Matrix s) {
		assert ((s.getRowDimension() / 2 == _paw.nPoints()) && (s
				.getColumnDimension() == 1));

		_paw.crop(im, crop_, s);

		if ((vec_.getRowDimension() != _paw._nPix)
				|| (vec_.getColumnDimension() != 1))
			vec_ = new Matrix(_paw._nPix, 1);

		final int w = crop_.width;
		final int h = crop_.height;

		final double[][] vp = vec_.getArray();
		final float[][] cp = crop_.pixels;
		final float[][] mp = _paw._mask.pixels;

		for (int i = 0, k = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				if (mp[i][j] != 0) {
					vp[k][0] = cp[i][j];
					k++;
				}
			}
		}

		double mean = MatrixUtils.sum(vec_) / vec_.getRowDimension();
		MatrixUtils.minus(vec_, mean);

		double var = 0;
		for (int i = 0; i < _paw._nPix; i++)
			var += vec_.get(i, 0) * vec_.get(i, 0);

		if (var < 1.0e-10)
			MatrixUtils.fill(vec_, 0);
		else
			vec_ = vec_.times(1 / Math.sqrt(var)); // FIXME inline

		double wdv = 0;
		for (int i = 0; i < _paw._nPix; i++)
			wdv += _w.get(i, 0) * vec_.get(i, 0);

		if ((wdv + _b) > 0)
			return true;

		return false;
	}
}
