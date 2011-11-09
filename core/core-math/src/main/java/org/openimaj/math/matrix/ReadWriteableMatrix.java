package org.openimaj.math.matrix;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;

import Jama.Matrix;

/**
 * A wrapper around a JAMA Matrix that is read-writeable by
 * OpenIMAJ IOUtils.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ReadWriteableMatrix extends Matrix implements ReadWriteable {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new matrix of zero size. Only for IOUtils use.
	 */
	protected ReadWriteableMatrix() {
		super(0, 0);
	}
	
	/**
	 * Construct a matrix using the provided 2-D double array.
	 * The array is assigned internally and is not copied.
	 * 
	 * @param data the data
 	 */
	public ReadWriteableMatrix(double[][] data) {
		super(data);
	}

	/**
	 * Construct a new matrix of the given size
	 * @param rows Number of rows
	 * @param cols Number of columns
	 */
	public ReadWriteableMatrix(int rows, int cols) {
		super(rows, cols);
	}
	
	/**
	 * Construct a matrix using the provided matrix.
	 * The matrix data is assigned internally and is not copied.
	 * 
	 * @param data the data
 	 */
	public ReadWriteableMatrix(Matrix data) {
		this(data.getArray());
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		final int rows = in.nextInt();
		final int cols = in.nextInt();
		
		double[][] data = new double[rows][cols];
		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = in.nextDouble();
		
		setData(rows, cols, data);		
	}

	@Override
	public String asciiHeader() {
		return this.getClass().getName() + " ";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final int rows = in.readInt();
		final int cols = in.readInt();
		
		double[][] data = new double[rows][cols];
		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				data[r][c] = in.readDouble();
		
		setData(rows, cols, data);
	}
	
	protected void setData(int m, int n, double[][] data) {
		Class<Matrix> clz = Matrix.class;
		try {
			Field mField = clz.getDeclaredField("m");
			mField.setAccessible(true);
			mField.setInt(this, m);
			
			Field nField = clz.getDeclaredField("n");
			nField.setAccessible(true);
			nField.setInt(this, n);
			
			Field AField = clz.getDeclaredField("A");
			AField.setAccessible(true);
			AField.set(this, data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "RWMAT".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		final int rows = this.getRowDimension();
		final int cols = this.getColumnDimension();
		final double[][] data = this.getArray(); 
		
		out.print(rows + " " + cols);
		out.println();
		
		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				out.print(data[r][c] + " ");
			}
			out.println();
		}
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		final int rows = this.getRowDimension();
		final int cols = this.getColumnDimension();
		final double[][] data = this.getArray(); 
		
		out.writeInt(rows);
		out.writeInt(cols);
		
		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				out.writeDouble(data[r][c]);
	}
}
