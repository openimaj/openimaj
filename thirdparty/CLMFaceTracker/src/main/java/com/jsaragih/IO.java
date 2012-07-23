package com.jsaragih;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
		int r = s.nextInt();
		int c = s.nextInt();
		s.nextInt(); // types are ignored

		Matrix M = new Matrix(r, c);
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
		int r = s.nextInt();
		int c = s.nextInt();
		s.nextInt(); // types are ignored

		FImage M = new FImage(c, r);
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
		int r = s.nextInt();
		int c = s.nextInt();
		s.nextInt(); // types are ignored

		int[][] M = new int[r][c];
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
		Scanner s = new Scanner(in);

		while (true) {
			String str = s.next();
			if ("n_connections:".equals(str))
				break;
		}

		int n = s.nextInt();
		int[][] con = new int[2][n];

		while (true) {
			String c = s.next();
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
		Scanner s = new Scanner(in);

		while (true) {
			String str = s.next();
			if ("n_tri:".equals(str))
				break;
		}

		int n = s.nextInt();
		int[][] tri = new int[n][3];

		while (true) {
			String c = s.next();
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
		int r = s.nextInt();
		int c = s.nextInt();
		s.nextInt(); // types are ignored

		FImage M = new FImage(c, r);
		final float[][] Mv = M.pixels;

		for (int rr = 0; rr < r; rr++)
			for (int cc = 0; cc < c; cc++)
				Mv[rr][cc] = s.next("[^ ]").codePointAt(0); // Integer.parseInt(s.next());//s.nextByte();

		return M;
	}
}
