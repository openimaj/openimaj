package org.openimaj.image.processing.face.tracking.clm;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.openimaj.image.FImage;

import Jama.Matrix;

public class IO {
	public enum Types {PDM,PAW,PATCH,MPATCH,CLM,FDET,FCHECK,MFCHECK,TRACKER};

	public static Matrix ReadMat(Scanner s)
	{
		int r = s.nextInt();
		int c = s.nextInt();
		int t = s.nextInt(); //types are ignored
		
		Matrix M = new Matrix(r, c);
		final double[][] Mv = M.getArray();
		
		for (int rr=0; rr<r; rr++)
			for (int cc=0; cc<c; cc++) 
				Mv[rr][cc] = s.nextDouble();
		
		return M;
	}
	
	//===========================================================================
	static void WriteMat(BufferedWriter s, Matrix M) throws IOException
	{
		final int r = M.getRowDimension();
		final int c = M.getColumnDimension();
		
		s.write(r + " " + c + " 0"); //type always 0 for java version as its ignored
		
		final double[][] Mv = M.getArray();
		for (int rr=0; rr<r; rr++)
			for (int cc=0; cc<c; cc++) 
				s.write(Mv[rr][cc] + " ");
	}

	static void WriteImg(BufferedWriter s, FImage img) throws IOException {
		final int r = img.height;
		final int c = img.width;
		
		s.write(r + " " + c + " 0"); //type always 0 for java version as its ignored
		
		final float[][] Mv = img.pixels;
		for (int rr=0; rr<r; rr++)
			for (int cc=0; cc<c; cc++) 
				s.write(Mv[rr][cc] + " ");
	}

	static FImage ReadImg(Scanner s) {
		int r = s.nextInt();
		int c = s.nextInt();
		int t = s.nextInt(); //types are ignored
		
		FImage M = new FImage(c, r);
		final float[][] Mv = M.pixels;
		
		for (int rr=0; rr<r; rr++)
			for (int cc=0; cc<c; cc++) 
				Mv[rr][cc] = s.nextFloat();
		
		return M;
	}

	public static void WriteIntArray(BufferedWriter s, int[][] arr) throws IOException {
		final int r = arr.length;
		final int c = arr[0].length;
		
		s.write(r + " " + c + " 0"); //type always 0 for java version as its ignored
		
		for (int rr=0; rr<r; rr++)
			for (int cc=0; cc<c; cc++) 
				s.write(arr[rr][cc] + " ");
	}

	static int[][] ReadIntArray(Scanner s) {
		int r = s.nextInt();
		int c = s.nextInt();
		int t = s.nextInt(); //types are ignored
		
		int [][] M = new int[r][c];
		for (int rr=0; rr<r; rr++)
			for (int cc=0; cc<c; cc++) 
				M[rr][cc] = s.nextInt();
		
		return M;
	}

	static int[][] LoadCon(final String fname) throws FileNotFoundException
	{
		return LoadCon(new FileInputStream(fname));
	}
	
	public static int[][] LoadCon(final InputStream in) 
	{
		Scanner s = new Scanner(in);
		
		while (true) {
			String str = s.next();
			if("n_connections:".equals(str)) break;
		}
		
		int n = s.nextInt(); 
		int[][] con = new int[2][n];
		
		while (true) {
			String c = s.next();
			if(c.equals("{"))
				break;
		}
		
		for(int i = 0; i < n; i++) {
			con[0][i] = s.nextInt();
			con[1][i] = s.nextInt();
		}
		s.close();
		
		return con;
	}
	//=============================================================================
	static int[][] LoadTri(final String fname) throws FileNotFoundException
	{
		return LoadTri(new FileInputStream(fname));
	}
	
	public static int[][] LoadTri(final InputStream in)
	{
		Scanner s = new Scanner(in);
		
		while (true) {
			String str = s.next();
			if("n_tri:".equals(str)) break;
		}
		
		int n = s.nextInt(); 
		int[][] tri = new int[n][3];
		
		while (true) {
			String c = s.next();
			if(c.equals("{"))
				break;
		}
		
		for(int i = 0; i < n; i++) {
			tri[i][0] = s.nextInt();
			tri[i][1] = s.nextInt();
			tri[i][2] = s.nextInt();
		}
		s.close();
		
		return tri;
	}
	//===========================================================================

	public static FImage ReadImgByte(Scanner s) {
		int r = s.nextInt();
		int c = s.nextInt();
		int t = s.nextInt(); //types are ignored
		
		FImage M = new FImage(c, r);
		final float[][] Mv = M.pixels;
		
		for (int rr=0; rr<r; rr++)
			for (int cc=0; cc<c; cc++)
				Mv[rr][cc] = s.next("[^ ]").codePointAt(0); //Integer.parseInt(s.next());//s.nextByte();
		
		return M;
	}
}
