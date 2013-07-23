package ch.akuhn.matrix;

import java.util.Arrays;

/** Matrix where
 *<CODE>a<SUB>ij</SUB> = a<SUB>ji</SUB></CODE> for all elements.
 *<P> 
 * @author Adrian Kuhn
 *
 */
public class SymmetricMatrix extends DenseMatrix {

    public SymmetricMatrix(int size) {
    	super(size,size);
    }
    
    public SymmetricMatrix(double[][] values) {
    	super(values);
    }
    
    @Override
	protected void assertInvariant() throws IllegalArgumentException {
    	for (int n = 0; n < values.length; n++) {
			if (values[n].length != (n + 1)) throw new IllegalArgumentException();
		}
    }

	@Override
	protected double[][] makeValues(int rows, int columns) {
		assert rows == columns;
		double[][] values = new double[rows][];
		for (int n = 0; n < values.length; n++) values[n] = new double[n + 1];
		return values;
	}

	@Override
    public int columnCount() {
        return rowCount();
    }

    @Override
    public double get(int row, int column) {
        return row > column ? values[row][column] : values[column][row];
    }

    @Override
    public double put(int row, int column, double value) {
        return row > column ? (values[row][column] = value) : (values[column][row] = value);
    }

    @Override
    public int rowCount() {
        return values.length;
    }

	public static DenseMatrix fromSquare(double[][] square) {
		double[][] jagged = new double[square.length][];
		for (int i = 0; i < jagged.length; i++) {
			assert square[i].length == square.length;
			jagged[i] = Arrays.copyOf(square[i], i+1);
		}
		return new SymmetricMatrix(jagged);
	}

	public static DenseMatrix fromJagged(double[][] values) {
		return new SymmetricMatrix(values);
	}

	@Override
	public double[][] unwrap() {
		return values;
	}
	
	@Override
	public double[] rowwiseMean() {
		double[] mean = new double[rowCount()];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < i; j++) {
				mean[i] += values[i][j];
				mean[j] += values[i][j];
			}
		}
		for (int n = 0; n < mean.length; n++) mean[n] /= mean.length;
		return mean;
	}
	
    @Override
	public Vector mult(Vector v) {
        assert v.size() == values.length;
        double[] mult = new double[v.size()];
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < i; j++) {
                mult[i] += values[i][j] * v.get(j);
                mult[j] += values[i][j] * v.get(i);
            }
        }
        return Vector.wrap(mult);
    }
	
	
}
