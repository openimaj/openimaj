package ch.akuhn.matrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.akuhn.matrix.Vector.Entry;

/** Two-dimensional table of floating point numbers.
 *<P>
 * @author Adrian Kuhn
 *
 */
public abstract class Matrix {

    public double add(int row, int column, double value) {
        return put(row, column, get(row, column) + value);
    }

    public Iterable<Vector> rows() {
    	return vecs(/*isRow*/ true);
    }
    
    private Iterable<Vector> vecs(final boolean isRow) {
    	return new Iterable<Vector>() {
			@Override
			public Iterator<Vector> iterator() {
				return new Iterator<Vector>() {

					private int count = 0;
					
					@Override
					public boolean hasNext() {
						return count < (isRow ? rowCount() : columnCount());
					}

					@Override
					public Vector next() {
						if (!hasNext()) throw new NoSuchElementException();
						return new Vec(count++, isRow);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
    }
    
    
    public Iterable<Vector> columns() {
    	return vecs(/*isRow*/ false);
    }

    public abstract int columnCount();

    public double density() {
        return (double) used() / elementCount();
    }
    
    public int elementCount() {
    	return rowCount() * columnCount();
    }
    
    public abstract double get(int row, int column);

    public abstract double put(int row, int column, double value);

    public abstract int rowCount();

    public abstract int used();

    /** @throws IOException 
     * @see http://tedlab.mit.edu/~dr/svdlibc/SVD_F_ST.html */
    public void storeSparseOn(Appendable appendable) throws IOException {
        // this stores the transposed matrix, but as we will transpose it again
        // when reading it, this can be done without loss of generality.
    	appendable.append(this.columnCount() + " ");
    	appendable.append(this.rowCount() + " ");
    	appendable.append(this.used() + "\r");
        for (Vector row: rows()) {
        	appendable.append(row.used() +"\r");
            for (Entry each: row.entries()) {
            	appendable.append(each.index + " " + each.value + " ");
            }
            appendable.append("\r");
        }
    }

    public void storeSparseOn(String fname) throws IOException {
    	FileWriter fw = new FileWriter(new File(fname));
        storeSparseOn(fw);
        fw.close();
    }
    
    public Vector row(int row) {
    	return new Vec(row, /*isRow*/ true);
    }
    
    public Vector column(int column) {
    	return new Vec(column, /*isRow*/ false);
    }
    
    public double[][] asArray() {
		double[][] result = new double[rowCount()][columnCount()];
		for (int x = 0; x < result.length; x++) {
			for (int y = 0; y < result[x].length; y++) {
				result[x][y] = get(x,y);
			}
		}
		return result;
	}

	public static int indexOf(Vector vec) {
		return ((Vec) vec).index0;
	}

	private class Vec extends Vector {
		
		int index0;
    	private boolean isRow;
    	
    	Vec(int n, boolean isRow) {
    		this.isRow = isRow;
    		this.index0 = n;
    	}
    	
		@Override
		public int size() {
			return isRow ? columnCount() : rowCount();
		}
			
		@Override
		public double put(int index, double value) {
			return isRow ? Matrix.this.put(this.index0, index, value)
					: Matrix.this.put(index, this.index0, value);
		}
			
		@Override
		public double get(int index) {
			return isRow ? Matrix.this.get(this.index0, index)
					: Matrix.this.get(index, this.index0);
		}

		@Override
		public boolean equals(Vector v, double epsilon) {
			throw new Error("Not yet implemented");
		}

		@Override
		public Vector times(double scalar) {
			throw new Error("Not yet implemented");
		}

		@Override
		public Vector timesEquals(double scalar) {
			throw new Error("Not yet implemented");
		}
    }

	/** Returns <code>y = Ax</code>.
	 * 
	 */
	public Vector mult(Vector x) {
		assert x.size() == this.columnCount();
		Vector y = Vector.dense(this.rowCount());
		int i = 0; for (Vector row: rows()) y.put(i++, row.dot(x));
		return y;
	}
	
	/** Returns <code>y = (A^T)x</code>.
	 * 
	 */
	public Vector transposeMultiply(Vector x) {
		assert x.size() == this.rowCount();
		Vector y = Vector.dense(this.columnCount());
		int i = 0; for (Vector row: rows()) row.scaleAndAddTo(x.get(i++), y);
		return y;
	}
	
	/** Returns <code>y = (A^T)Ax</code>.
	 *<P> 
	 * Useful for doing singular decomposition using ARPACK's dsaupd routine.
	 * 
	 */
	public Vector transposeNonTransposeMultiply(Vector x) {
		return this.transposeMultiply(this.mult(x));
	}

	public static Matrix from(int n, int m, double... values) {
		assert n * m == values.length;
		double[][] data = new double[n][];
		for (int i = 0; i < n; i++) data[i] = Arrays.copyOfRange(values, i*m, (i+1)*m);
		return new DenseMatrix(data);
	}

	public static Matrix dense(int n, int m) {
		return new DenseMatrix(n, m);
	}

	public boolean isSquare() {
		return columnCount() == rowCount();
	}

	public double[] asColumnMajorArray() {
		double[] data = new double[columnCount() * rowCount()];
		int n = columnCount();
		int i = 0; 
		for (Vector row: rows()) {
			for (Entry each: row.entries()) {
				data[i + each.index * n] = each.value;
			}
			i++;
		}
		return data;
	}

	public static SparseMatrix sparse(int n, int m) {
		return new SparseMatrix(n, m);
	}
	
	public double max() {
		return Util.max(this.unwrap(), Double.NaN);
	}
	
	public double min() {
		return Util.min(this.unwrap(), Double.NaN);
	}
	
	public double mean() {
		double[][] values = unwrap();
		return Util.sum(values) / Util.count(values);
	}
	
	public double[][] unwrap() {
		throw new IllegalStateException("cannot unwrap instance of " + this.getClass().getSimpleName());
	}

	public double[] rowwiseMean() {
		double[] mean = new double[rowCount()];
		int i = 0;
		for (Vector row: rows()) mean[i++] = row.mean();
		return mean;
	}

	public int[] getHistogram() {
		return Util.getHistogram(this.unwrap(), 100);
	}
	
}
