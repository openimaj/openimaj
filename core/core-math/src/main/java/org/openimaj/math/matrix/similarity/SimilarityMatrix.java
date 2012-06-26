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
package org.openimaj.math.matrix.similarity;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.openimaj.io.ReadWriteable;
import org.openimaj.math.matrix.ReadWriteableMatrix;
import org.openimaj.math.matrix.similarity.processor.SimilarityMatrixProcessor;

import Jama.Matrix;

/**
 * A similarity matrix is a square matrix with an associated index.
 * It can be used to store all the similarities across a set
 * of objects.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
	 * Construct a similarity matrix based on the given index
	 * and matrix data. The matrix data must be square and its dimensions
	 * must be the same as the index length.
	 * 
	 * @param index the index
	 * @param data the matrix data
	 */
	public SimilarityMatrix(String [] index, double[][] data) {
		super(data);

		if (index.length != this.getRowDimension())
			throw new IllegalArgumentException("index must have same length as matrix sides");

		this.index = index;
	}

	/**
	 * Get the offset in the index for a given value 
	 * @param value the value
	 * @return the index
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

	/**
	 * Convert the similarity matrix to an unweighted, undirected
	 * graph representation. A threshold is used to determine
	 * if edges should be created. If the value at [r][c] is bigger
	 * than the threshold, then an edge will be created between the
	 * vertices represented by index[r] and index[c].
	 * 
	 * @param threshold the threshold
	 * @return the graph
	 */
	public UndirectedGraph<String, DefaultEdge> toUndirectedUnweightedGraph(double threshold) {
		UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

		final int rows = this.getRowDimension();
		final int cols = this.getColumnDimension();
		final double[][] data = this.getArray(); 

		for (String s : index) {
			graph.addVertex(s);
		}

		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				if (r != c && data[r][c] > threshold)
					graph.addEdge(index[r], index[c]);
			}
		}

		return graph;
	}

	@Override   
	public SimilarityMatrix copy() {
		double[][] C = this.getArrayCopy();
		String[] i = Arrays.copyOf(index, index.length);
		
		return new SimilarityMatrix(i, C);
	}
	
	@Override   
	public SimilarityMatrix clone() {
		return copy();
	}

	/**
	 * Process a copy of this similarity matrix with the
	 * given processor and return the copy.
	 * 
	 * @param proc the processor
	 * @return a processed copy of this matrix
	 */
	public SimilarityMatrix process(SimilarityMatrixProcessor proc) {
		SimilarityMatrix mat = this.clone();
		proc.process(mat);
		return mat;
	}

	/**
	 * Process this matrix with the given processor.
	 * @param proc the processor
	 * @return this.
	 */
	public SimilarityMatrix processInplace(SimilarityMatrixProcessor proc) {
		proc.process(this);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		int maxIndexLength = 0;
		for (String s : index) 
			if (s.length() > maxIndexLength) 
				maxIndexLength = s.length();

		final int maxIndexCountLength = (index.length + "").length();
		final String indexFormatString = "%"+(maxIndexCountLength+2)+"s %" + maxIndexLength + "s ";  

		final int rows = this.getRowDimension();
		final int cols = this.getColumnDimension();
		final double[][] data = this.getArray(); 

		sb.append(String.format("%"+(maxIndexLength+maxIndexCountLength+3)+"s", ""));
		for (int r=0; r<rows; r++) {
			sb.append(String.format("%9s", String.format("(%d)", r)));
		}
		sb.append("\n");

		for (int r=0; r<rows; r++) {
			sb.append(String.format(indexFormatString, String.format("(%d)", r), index[r]));

			for (int c=0; c<cols; c++) {
				sb.append(String.format("%8.3f ", data[r][c]));
			}
			sb.append("\n");
		}

		return sb.toString();
	}
}
