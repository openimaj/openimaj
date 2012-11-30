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

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

import org.openimaj.image.FImage;

import Jama.Matrix;

/**
 * IO Utilities
 * 
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class IO {
	static {
		Tracker.init();
	}

	/**
	 * Types of object
	 */
	public enum Types {
		/** PDM */
		PDM,
		/** PAW */
		PAW,
		/** PATCH */
		PATCH,
		/** MPATCH */
		MPATCH,
		/** CLM */
		CLM,
		/** FDET */
		FDET,
		/** FCHECK */
		FCHECK,
		/** MFCHECK */
		MFCHECK,
		/** TRACKER */
		TRACKER
	};

	/**
	 * Read a matrix
	 * 
	 * @param s
	 * @return the matrix
	 */
	public static Matrix readMat(Scanner s) {
		s.useLocale(Locale.UK);

		final int r = s.nextInt();
		final int c = s.nextInt();
		s.nextInt(); // types are ignored

		final Matrix M = new Matrix(r, c);
		final double[][] Mv = M.getArray();

		for (int rr = 0; rr < r; rr++)
			for (int cc = 0; cc < c; cc++)
				Mv[rr][cc] = s.nextDouble();

		return M;
	}

	static void writeMat(BufferedWriter s, Matrix M) throws IOException {
		final int r = M.getRowDimension();
		final int c = M.getColumnDimension();

		s.write(r + " " + c + " 0"); // type always 0 for java version as its
										// ignored

		final double[][] Mv = M.getArray();
		for (int rr = 0; rr < r; rr++)
			for (int cc = 0; cc < c; cc++)
				s.write(Mv[rr][cc] + " ");
	}

	static void writeImg(BufferedWriter s, FImage img) throws IOException {
		final int r = img.height;
		final int c = img.width;

		s.write(r + " " + c + " 0"); // type always 0 for java version as its
										// ignored

		final float[][] Mv = img.pixels;
		for (int rr = 0; rr < r; rr++)
			for (int cc = 0; cc < c; cc++)
				s.write(Mv[rr][cc] + " ");
	}

	static FImage readImg(Scanner s) {
		s.useLocale(Locale.UK);

		final int r = s.nextInt();
		final int c = s.nextInt();
		s.nextInt(); // types are ignored

		final FImage M = new FImage(c, r);
		final float[][] Mv = M.pixels;

		for (int rr = 0; rr < r; rr++)
			for (int cc = 0; cc < c; cc++)
				Mv[rr][cc] = s.nextFloat();

		return M;
	}

	static void writeIntArray(BufferedWriter s, int[][] arr) throws IOException {
		final int r = arr.length;
		final int c = arr[0].length;

		s.write(r + " " + c + " 0"); // type always 0 for java version as its
										// ignored

		for (int rr = 0; rr < r; rr++)
			for (int cc = 0; cc < c; cc++)
				s.write(arr[rr][cc] + " ");
	}

	static int[][] readIntArray(Scanner s) {
		s.useLocale(Locale.UK);

		final int r = s.nextInt();
		final int c = s.nextInt();
		s.nextInt(); // types are ignored

		final int[][] M = new int[r][c];
		for (int rr = 0; rr < r; rr++)
			for (int cc = 0; cc < c; cc++)
				M[rr][cc] = s.nextInt();

		return M;
	}

	static int[][] loadCon(final String fname) throws FileNotFoundException {
		return loadCon(new FileInputStream(fname));
	}

	/**
	 * Load connections
	 * 
	 * @param in
	 * @return the connections
	 */
	public static int[][] loadCon(final InputStream in) {
		final Scanner s = new Scanner(in);
		s.useLocale(Locale.UK);

		while (true) {
			final String str = s.next();
			if ("n_connections:".equals(str))
				break;
		}

		final int n = s.nextInt();
		final int[][] con = new int[2][n];

		while (true) {
			final String c = s.next();
			if (c.equals("{"))
				break;
		}

		for (int i = 0; i < n; i++) {
			con[0][i] = s.nextInt();
			con[1][i] = s.nextInt();
		}
		s.close();

		return con;
	}

	static int[][] loadTri(final String fname) throws FileNotFoundException {
		return loadTri(new FileInputStream(fname));
	}

	/**
	 * Load triangles
	 * 
	 * @param in
	 * @return triangles
	 */
	public static int[][] loadTri(final InputStream in) {
		final Scanner s = new Scanner(in);
		s.useLocale(Locale.UK);

		while (true) {
			final String str = s.next();
			if ("n_tri:".equals(str))
				break;
		}

		final int n = s.nextInt();
		final int[][] tri = new int[n][3];

		while (true) {
			final String c = s.next();
			if (c.equals("{"))
				break;
		}

		for (int i = 0; i < n; i++) {
			tri[i][0] = s.nextInt();
			tri[i][1] = s.nextInt();
			tri[i][2] = s.nextInt();
		}
		s.close();

		return tri;
	}

	static FImage readImgByte(Scanner s) {
		s.useLocale(Locale.UK);

		final int r = s.nextInt();
		final int c = s.nextInt();
		s.nextInt(); // types are ignored

		final FImage M = new FImage(c, r);
		final float[][] Mv = M.pixels;

		for (int rr = 0; rr < r; rr++)
			for (int cc = 0; cc < c; cc++)
				Mv[rr][cc] = s.next("[^ ]").codePointAt(0); // Integer.parseInt(s.next());//s.nextByte();

		return M;
	}
}
