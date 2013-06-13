package org.openimaj.math.matrix;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixEntry;
import gov.sandia.cognition.math.matrix.MatrixFactory;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorEntry;
import gov.sandia.cognition.math.matrix.mtj.DenseMatrixFactoryMTJ;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

public class CFMatrixUtils {

	public static Matrix abs(Matrix mat) {
		Matrix ret = mat.clone();
		int nrows = ret.getNumRows();
		int ncols = ret.getNumColumns();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				ret.setElement(r, c, Math.abs(mat.getElement(r, c)));
			}
		}
		return ret;
	}

	public static double absSum(Matrix mat) {
		double tot = 0;
		int nrows = mat.getNumRows();
		int ncols = mat.getNumColumns();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				tot += Math.abs(mat.getElement(r, c));
			}
		}
		return tot;
	}

	public static Matrix timesInplace(Matrix mat, double etat) {
		int nrows = mat.getNumRows();
		int ncols = mat.getNumColumns();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				mat.setElement(r, c, mat.getElement(r, c) * etat);
			}
		}
		return mat;
	}

	public static Matrix asMat(MLArray mlArray) {
		MLDouble mlArrayDbl = (MLDouble) mlArray;
		int rows = mlArray.getM();
		int cols = mlArray.getN();

		Matrix mat = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(rows, cols);

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				mat.setElement(r, c, mlArrayDbl.get(r, c));
			}
		}
		return mat;
	}

	public static double rowSparcity(Matrix mat) {
		double nrows = mat.getNumRows();
		double nsparse = 0;
		for (int r = 0; r < nrows; r++) {
			if(mat.getRow(r).sum() == 0){
				nsparse ++;
			}
		}
		return nsparse/nrows;
	}

	public static double colSparcity(Matrix mat) {
		double ncols = mat.getNumColumns();
		double nsparse = 0;
		for (int c = 0; c < ncols; c++) {
			if(mat.getColumn(c).sum() == 0){
				nsparse ++;
			}
		}
		return nsparse/ncols;
	}

	public static Matrix plusInplace(Matrix mat, double etat) {
		int nrows = mat.getNumRows();
		int ncols = mat.getNumColumns();
		for (int r = 0; r < nrows; r++) {
			for (int c = 0; c < ncols; c++) {
				mat.setElement(r, c, mat.getElement(r, c) + etat);
			}
		}
		return mat;
	}

	public static Vector diag(Matrix mat) {
		Vector ret;

		if(mat.getNumColumns() > mat.getNumRows()){
			ret = mat.getRow(0);
		}
		else{
			ret = mat.getColumn(0);
		}
		int rowcol = ret.getDimensionality();
		for (int rc = 0; rc < rowcol; rc++) {
			ret.setElement(rc,mat.getElement(rc, rc));
		}
		return ret;
	}

	public static Matrix vstack(MatrixFactory<? extends Matrix> matrixFactory, Matrix ... matricies) {
		int nrows = 0;
		int ncols = 0;
		for (Matrix matrix : matricies) {
			nrows+=matrix.getNumRows();
			ncols = matrix.getNumColumns();
		}
		Matrix ret = matrixFactory.createMatrix(nrows, ncols);
		int currentRow = 0;
		for (Matrix matrix : matricies) {
			ret.setSubMatrix(currentRow, 0, matrix);
			currentRow += matrix.getNumRows();
		}
		return ret;
	}

	public static Matrix vstack(Matrix ... matricies) {
		return vstack(MatrixFactory.getDefault(), matricies);
	}

	public static double[] getData(Matrix w) {
		return ((no.uib.cipr.matrix.DenseMatrix)DenseMatrixFactoryMTJ.INSTANCE.copyMatrix(w).getInternalMatrix()).getData();
	}

	public static double min(Matrix u) {
		double min = Double.MAX_VALUE;
		for (MatrixEntry matrixEntry : u) {
			min = Math.min(min, matrixEntry.getValue());
		}
		return min;
	}

	public static double max(Matrix u) {
		double max = -Double.MAX_VALUE;
		for (MatrixEntry matrixEntry : u) {
			max = Math.max(max, matrixEntry.getValue());
		}
		return max;
	}

	public static double min(Vector column) {
		double min = Double.MAX_VALUE;
		for (VectorEntry vectorEntry : column) {
			min = Math.min(min, vectorEntry.getValue());
		}
		return min;
	}

	public static double max(Vector column) {
		double max = -Double.MAX_VALUE;
		for (VectorEntry vectorEntry : column) {
			max = Math.max(max, vectorEntry.getValue());
		}
		return max;
	}

}
