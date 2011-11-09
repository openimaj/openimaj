package org.openimaj.math.matrix;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;

import Jama.Matrix;

/**
 * A similarity matrix is a square matrix with an associated index.
 * It can be used to store all the similarities across a set
 * of objects.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class SimilarityMatrix extends ReadWriteableMatrix implements ReadWriteable {
	private static final long serialVersionUID = 1L;
	
	protected String[] index;

	/**
	 * Construct an empty similarity matrix. Only for IOUtils use.
	 */
	protected SimilarityMatrix() {
		super();
	}
	
	/**
	 * Construct a similarity matrix with the given size
	 * and allocate the index accordingly.
	 * @param size the size of the matrix
	 */
	public SimilarityMatrix(int size) {
		super(size, size);
		index = new String[size];
	}
	
	/**
	 * Construct a similarity matrix with the given index
	 * and set the matrix size based on the index length.
	 * @param index the index.
	 */
	public SimilarityMatrix(String [] index) {
		super(index.length, index.length);
		this.index = index;
	}
	
	/**
	 * Construct a similarity matrix based on the given index
	 * and matrix. The matrix must be square and its dimensions
	 * must be the same as the index length.
	 * 
	 * @param index the index
	 * @param data the matrix
	 */
	public SimilarityMatrix(String [] index, Matrix data) {
		super(data);
		
		if (data.getColumnDimension() != data.getRowDimension())
			throw new IllegalArgumentException("matrix must be square");
		
		if (index.length != data.getRowDimension())
			throw new IllegalArgumentException("index must have same length as matrix sides");
		
		this.index = index;
	}
	
	/**
	 * Get the offset in the index for a given value 
	 * @param value the value
	 */
	public int indexOf(String value) {
		return Arrays.binarySearch(index, value);
	}
	
	/**
	 * Set the value of the index at a given offset
	 * @param i the offset
	 * @param value the value
	 */
	public void setIndexValue(int i, String value) {
		index[i] = value;
	}
	
	/**
	 * Get a value from the index
	 * @param i the offset into the index
	 * @return the value
	 */
	public String getIndexValue(int i) {
		return index[i];
	}
	
	/**
	 * Get the index
	 * @return the index
	 */
	public String [] getIndex() {
		return index;
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		super.readASCII(in);
		
		index = new String[this.getRowDimension()];
		
		for (int i=0; i<index.length; i++)
			index[i] = in.nextLine();
	}

	@Override
	public String asciiHeader() {
		return this.getClass().getName() + " ";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		
		index = new String[this.getRowDimension()];
		
		for (int i=0; i<index.length; i++)
			index[i] = in.readUTF();
	}

	@Override
	public byte[] binaryHeader() {
		return "SimMat".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		super.writeASCII(out);
		
		for (String s : index)
			out.println(s);
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		
		for (String s : index)
			out.writeUTF(s);
	}
}
