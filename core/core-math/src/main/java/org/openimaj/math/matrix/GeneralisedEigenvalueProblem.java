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
package org.openimaj.math.matrix;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;

import org.netlib.lapack.LAPACK;
import org.netlib.util.intW;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Methods for solving the Generalised Eigenvalue Problem: A x = L B x.
 * <p>
 * Internally the methods in this class use LAPACK to compute the solutions.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class GeneralisedEigenvalueProblem {
    private final static int sygvd(int itype, String jobz, String uplo, DenseMatrix A, DenseMatrix B, DenseVector W) {
        int info = dsygvd(itype, jobz, uplo, A.numColumns(), A.getData(), A.numRows(), B.getData(), B.numRows(), W.getData());
        if (info == 0) {
            return 0;
        } else {
            if (info < 0) throw new IllegalArgumentException("LAPACK ERROR: DSYGVD returned " + info);
            throw new RuntimeException("LAPACK ERROR: DSYGVD returned " + info);
        }
    }
    
    private final static int dsygvd(int itype, String jobz, String uplo, int n, double[] a, int lda, double[] b, int ldb, double[] w) {
      double[] work = new double[1];
      double[] tmp = new double[1];
      intW info = new intW(-1);
      int lwork;
      int[] iwork = new int[1];
      int liwork;
      
      //call with info=-1 to determine size of working space
      LAPACK.getInstance().dsygvd(itype, jobz, uplo, n, tmp, lda, tmp, ldb, tmp, work, -1, iwork, 0, info);
      
      if (info.val != 0)
        return info.val; //an error occurred
      
      //setup working space
      lwork = (int) work[0]; 
      work = new double[lwork];
      liwork = (int) iwork[0]; 
      iwork = new int[liwork];
      //and do the work
      LAPACK.getInstance().dsygvd(itype, jobz, uplo, n, a, lda, b, ldb, w, work, lwork, iwork, liwork, info);
      
      return info.val;
    }
    
	/**
     * Compute the generalised eigenvalues, L, of the problem A x = L B x.
     * The returned eigenvalues are not ordered.
     *
     * @param A symmetric Matrix A; only the upper triangle is used.
     * @param B symmetric Matrix B; only the upper triangle is used.
     * @return the eigenvalues L.
     */
    public static DenseVector symmetricGeneralisedEigenvalues(DenseMatrix A, DenseMatrix B) {
        if (!A.isSquare() || !B.isSquare()) 
        	throw new IllegalArgumentException("Input matrices must be square");
        
        DenseVector W = new DenseVector(A.numRows());
        sygvd(1, "N", "U", A.copy(), B.copy(), W);
        
        return W;
    }
    
    /**
     * Solve the general problem A x = L B x.
     * The returned eigenvalues are not ordered.
     *
     * @param A symmetric matrix A
     * @param B symmetric matrix B
     * @return The eigenvectors x and eigenvalues L.
     */
    public static IndependentPair<DenseMatrix, DenseVector> symmetricGeneralisedEigenvectors(DenseMatrix A, DenseMatrix B) {
    	if (!A.isSquare() || !B.isSquare()) 
        	throw new IllegalArgumentException("Input matrices must be square");
    	
    	DenseMatrix vecs = A.copy();
    	DenseVector W = new DenseVector(A.numRows());
        
    	sygvd(1, "V", "U", vecs, B.copy(), W);
        
        return new IndependentPair<DenseMatrix, DenseVector>(vecs, W);
    }
    
    /**
     * Compute the generalised eigenvalues, L, of the problem A x = L B x.
     * The returned eigenvalues are not ordered.
     *
     * @param A symmetric Matrix A; only the upper triangle is used.
     * @param B symmetric Matrix B; only the upper triangle is used.
     * @return the eigenvalues L.
     */
    public static double[] symmetricGeneralisedEigenvalues(Matrix A, Matrix B) {
        if ((A.getRowDimension() != A.getColumnDimension()) || (B.getRowDimension() != B.getColumnDimension())) 
        	throw new IllegalArgumentException("Input matrices must be square");
        
        DenseVector W = new DenseVector(A.getRowDimension());
        sygvd(1, "N", "U", new DenseMatrix(A.getArray()), new DenseMatrix(B.getArray()), W);
        
        return W.getData();
    }
    
    /**
     * Solve the general problem A x = L B x. 
     * The returned eigenvalues are not ordered.
     *
     * @param A symmetric matrix A
     * @param B symmetric matrix B
     * @return The eigenvectors x and eigenvalues L.
     */
    public static IndependentPair<Matrix, double[]> symmetricGeneralisedEigenvectors(Matrix A, Matrix B) {
    	if ((A.getRowDimension() != A.getColumnDimension()) || (B.getRowDimension() != B.getColumnDimension())) 
        	throw new IllegalArgumentException("Input matrices must be square");
    	
    	int dim = A.getRowDimension();
    	DenseMatrix vecs = new DenseMatrix(A.getArray());
    	DenseVector W = new DenseVector(dim);
        
    	sygvd(1, "V", "U", vecs, new DenseMatrix(B.getArray()), W);
        
    	Matrix evecs = new Matrix(dim, dim);
    	final double[][] evecsData = evecs.getArray();
    	final double[] vecsData = vecs.getData();
    	for (int r=0; r<dim; r++)
    		for (int c=0; c<dim; c++)
    			evecsData[r][c] = vecsData[r + c * dim];
    	
        return new IndependentPair<Matrix, double[]>(evecs, W.getData());
    }
    
    /**
     * Solve the general problem A x = L B x. 
     * The returned eigenvalues ordered in a decreasing manner.
     *
     * @param A symmetric matrix A
     * @param B symmetric matrix B
     * @return The eigenvectors x and eigenvalues L.
     */
    public static IndependentPair<Matrix, double[]> symmetricGeneralisedEigenvectorsSorted(Matrix A, Matrix B) {
    	if ((A.getRowDimension() != A.getColumnDimension()) || (B.getRowDimension() != B.getColumnDimension())) 
        	throw new IllegalArgumentException("Input matrices must be square");
    	
    	int dim = A.getRowDimension();
    	DenseMatrix vecs = new DenseMatrix(A.getArray());
    	DenseVector W = new DenseVector(dim);
        
    	sygvd(1, "V", "U", vecs, new DenseMatrix(B.getArray()), W);
        
    	Matrix evecs = new Matrix(dim, dim);
    	final double[][] evecsData = evecs.getArray();
    	final double[] vecsData = vecs.getData();
    	final double[] valsData = W.getData();
    	
    	int [] indices = ArrayUtils.range(0, valsData.length-1);
    	ArrayUtils.parallelQuicksortDescending(valsData, indices);
    	
    	for (int r=0; r<dim; r++)
    		for (int c=0; c<dim; c++)
    			evecsData[r][c] = vecsData[r + indices[c] * dim];
    	
        return new IndependentPair<Matrix, double[]>(evecs, valsData);
    }
    
    /**
     * Solve the general problem A x = L B x. 
     * The returned eigenvalues ordered in a decreasing manner. Only the
     * top numVecs eigenvalues and vectors are returned.
     *
     * @param A symmetric matrix A
     * @param B symmetric matrix B
     * @param numVecs number of eigenvalues/eigenvectors
     * @return The eigenvectors x and eigenvalues L.
     */
    public static IndependentPair<Matrix, double[]> symmetricGeneralisedEigenvectorsSorted(Matrix A, Matrix B, int numVecs) {
    	if ((A.getRowDimension() != A.getColumnDimension()) || (B.getRowDimension() != B.getColumnDimension())) 
        	throw new IllegalArgumentException("Input matrices must be square");
    	
    	int dim = A.getRowDimension();
    	DenseMatrix vecs = new DenseMatrix(A.getArray());
    	DenseVector W = new DenseVector(dim);
        
    	sygvd(1, "V", "U", vecs, new DenseMatrix(B.getArray()), W);
        
    	Matrix evecs = new Matrix(dim, numVecs);
    	final double[][] evecsData = evecs.getArray();
    	final double[] vecsData = vecs.getData();
    	final double[] valsData = W.getData();
    	
    	int [] indices = ArrayUtils.range(0, valsData.length-1);
    	ArrayUtils.parallelQuicksortDescending(valsData, indices);
    	
    	for (int r=0; r<dim; r++)
    		for (int c=0; c<numVecs; c++)
    			evecsData[r][c] = vecsData[r + indices[c] * dim];
    	
        return new IndependentPair<Matrix, double[]>(evecs, valsData);
    }
}
