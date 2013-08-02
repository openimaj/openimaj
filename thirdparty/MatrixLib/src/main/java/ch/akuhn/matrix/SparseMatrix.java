package ch.akuhn.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import ch.akuhn.matrix.Vector.Entry;

public class SparseMatrix extends Matrix {

    private int columns;

    private List<Vector> rows;

    public SparseMatrix(double[][] values) {
        this.columns = values[0].length;
        this.rows = new ArrayList<Vector>(values.length);
        for (double[] each: values) addRow(each);
    }

    public SparseMatrix(int rows, int columns) {
        this.columns = columns;
        this.rows = new ArrayList<Vector>(rows);
        for (int times=0; times<rows; times++) addRow();
    }

    @Override
    public double add(int row, int column, double sum) {
        return rows.get(row).add(column, sum);
    }

    public int addColumn() {
        columns++;
        for (Vector each: rows)
            ((SparseVector) each).resizeTo(columns);
        return columns - 1;
    }

    public int addRow() {
        rows.add(new SparseVector(columns));
        return rowCount() - 1;
    }

    protected int addRow(double[] values) {
        rows.add(new SparseVector(values));
        return rowCount() - 1;
    }

    public void addToRow(int row, Vector values) {
        Vector v = rows.get(row);
        for (Entry each: values.entries())
            v.add(each.index, each.value);
    }

    public double[][] asDenseDoubleDouble() {
        double[][] dense = new double[rowCount()][columnCount()];

        for (int ri = 0; ri<rows.size(); ri++) {
        	Vector row = rows.get(ri);

            for (Entry column: row.entries()) {
                dense[ri][column.index] = column.value;
            }
        }
        return dense;
    }

    @Override
    public int columnCount() {
        return columns;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SparseMatrix && rows.equals(((SparseMatrix) obj).rows);
    }

    @Override
    public double get(int row, int column) {
        return rows.get(row).get(column);
    }

    @Override
    public int hashCode() {
        return rows.hashCode();
    }

    @Override
    public double put(int row, int column, double value) {
        return rows.get(row).put(column, value);
    }

    @Override
    public Iterable<Vector> rows() {
        return Collections.unmodifiableCollection(rows);
    }

    @Override
    public Vector row(int row) {
    	return this.rows.get(row);
    }

    @Override
    public int rowCount() {
        return rows.size();
    }

    /**
     * Sets the row, no check is made on {@link SparseVector#size()}
     * Use with care.
     * @param row
     * @param values
     */
    public void setRow(int row, SparseVector values) {
        rows.set(row, values);
    }

    @Override
    public int used() {
        int used = 0;
        for (Vector each: rows)
            used += each.used();
        return used;
    }

    public void trim() {
        for (Vector each: rows) {
            ((SparseVector) each).trim();
        }
    }

    public static SparseMatrix readFrom(Scanner scan) {
        int columns = scan.nextInt();
        int rows = scan.nextInt();
        int used = scan.nextInt();
        SparseMatrix matrix = new SparseMatrix(rows, columns);
        for (int row = 0; row < rows; row++) {
            int len = scan.nextInt();
            for (int i = 0; i < len; i++) {
                int column = scan.nextInt();
                double value = scan.nextDouble();
                matrix.put(row, column, value);
            }
        }
        assert matrix.used() == used;
        return matrix;
    }

    public static SparseMatrix random(int n, int m, double density) {
    	Random random = new Random();
    	SparseMatrix A = new SparseMatrix(n, m);
    	for (int i = 0; i < n; i++) {
    		for (int j = 0; j < m; j++) {
				if (random.nextDouble() > density) continue;
				A.put(i, j, random.nextDouble());
    		}
		}
    	return A;
    }

    @Override
	public Vector mult(Vector dense) {
		assert dense.size() == this.columnCount();
		double[] y = new double[this.rowCount()];
		double[] x = ((DenseVector) dense).values;
		for (int i = 0; i < y.length; i++) {
			SparseVector row = (SparseVector) rows.get(i);
			double sum = 0;
			for (int k = 0; k < row.used; k++) {
				sum += x[row.keys[k]] * row.values[k];
			}
			y[i] = sum;
		}
		return Vector.wrap(y);
	}

	@Override
    public Vector transposeMultiply(Vector dense) {
		assert dense.size() == this.rowCount();
		double[] y = new double[this.columnCount()];
		double[] x = ((DenseVector) dense).values;
		for (int i = 0; i < x.length; i++) {
			SparseVector row = (SparseVector) rows.get(i);
			for (int k = 0; k < row.used; k++) {
				y[row.keys[k]] += x[i] * row.values[k];
			}
		}
		return Vector.wrap(y);
    }

	@Override
	public Matrix newInstance(int rows, int cols) {
		return new SparseMatrix(rows, cols);
	}
}
