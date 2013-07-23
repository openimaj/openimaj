package ch.akuhn.matrix;

import java.util.Arrays;

/** Dense matrix.
 * 
 * @author Adrian Kuhn
 *
 */
public class DenseMatrix extends Matrix {

    protected double[][] values;

    public DenseMatrix(double[][] values) {
        this.values = values;
        this.assertInvariant();
    }
    
    protected void assertInvariant() throws IllegalArgumentException {
    	if (values.length == 0) return;
    	int m = values[0].length;
		for (int n = 0; n < values.length; n++) {
			if (values[n].length != m) throw new IllegalArgumentException();
		}
	}

	public DenseMatrix(int rows, int columns) {
    	this.values = makeValues(rows,columns);
        this.assertInvariant();
    }

	protected double[][] makeValues(int rows, int columns) {
		return new double[rows][columns];
	}

	@Override
    public double add(int row, int column, double value) {
        return values[row][column] += value;
    }

    @Override
    public int columnCount() {
        return values[0].length;
    }

    @Override
    public double get(int row, int column) {
        return values[row][column];
    }

    @Override
    public double put(int row, int column, double value) {
        return values[row][column] = value;
    }

    @Override
    public int rowCount() {
        return values.length;
    }

    @Override
    public int used() {
        // TODO Auto-generated method stub
        throw null;
    }

    @Override
    public double[][] unwrap() {
    	return values;
    }

	public void fill(double constant) {
	    for (double[] row: values) Arrays.fill(row, constant);
	}

	public void applyMultiplication(double d) {
		Util.times(values, d);
	}
    
    
	
}
