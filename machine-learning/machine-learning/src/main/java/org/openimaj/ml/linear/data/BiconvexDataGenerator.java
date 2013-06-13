package org.openimaj.ml.linear.data;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixEntry;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

import java.util.Random;

import org.openimaj.util.pair.Pair;

/**
 * Data generated from a biconvex system of the form:
 * 
 * Y_n,t = U_:,t^T . X_n . W_t + rand()
 * 
 * Note that each n'th instance of Y can have values for T tasks.
 * 
 * The parameter matricies U and W can be independant of tasks or note.
 * 
 * The amount of random noise added can be controlled
 * 
 * The sparcity of U, W and X can be controlled, the sparcity of U and W is
 * consistent per generator. The sparcity of X changes per generation.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class BiconvexDataGenerator implements DataGenerator<Matrix> {

	private int seed;
	private int nusers;
	private int nfeatures;
	private int ntasks;
	private boolean indw;
	private boolean indu;
	private Random rng;
	private SparseMatrixFactoryMTJ smf;
	private Matrix u;
	private SparseMatrix w;
	private double noise;
	private double xsparcity;

	/**
	 * Generates a biconvex data generator.
	 */
	public BiconvexDataGenerator() {
		this(5, 10, 1, 0.3, 0, true, false, -1, 0.0001);
	}

	/**
	 * 
	 * @param nusers
	 *            The number of users (U is users x tasks, X is features x
	 *            users)
	 * @param nfeatures
	 *            The number of features (W is features x tasks)
	 * @param ntasks
	 *            The number of tasks (Y is 1 x tasks)
	 * @param sparcity
	 *            The chance that a row of U or W is zeros
	 * @param xsparcity
	 *            The chance that a column of U or W is zeros
	 * @param indw
	 *            If true, there is a column of W per task
	 * @param indu
	 *            If true, there is a column of U per task
	 * @param seed
	 *            If greater than or equal to zero, the rng backing this
	 *            generator is seeded
	 * @param noise
	 *            The random noise added to Y has random values for each Y
	 *            ranging from -noise to noise
	 */
	public BiconvexDataGenerator(
			int nusers, int nfeatures, int ntasks,
			double sparcity, double xsparcity,
			boolean indw, boolean indu,
			int seed, double noise

	)
	{
		this.seed = seed;
		this.nusers = nusers;
		this.nfeatures = nfeatures;
		this.ntasks = ntasks;
		this.indw = indw;
		this.indu = indu;
		this.noise = Math.abs(noise);
		this.xsparcity = xsparcity;
		this.smf = new SparseMatrixFactoryMTJ();

		if (this.seed >= 0)
			this.rng = new Random(this.seed);
		else
			this.rng = new Random();
		if (indu) {
			this.u = smf.createUniformRandom(nusers, ntasks, 0, 1, this.rng);
		}
		else {
			this.u = smf.createUniformRandom(nusers, 1, 0, 1, this.rng);
		}

		if (indw) {
			this.w = smf.createUniformRandom(nfeatures, ntasks, 0, 1, this.rng);
		}
		else {
			this.w = smf.createUniformRandom(nfeatures, 1, 0, 1, this.rng);
		}

		final Vector zeroUserWord = smf.createMatrix(1, ntasks).getRow(0);
		for (int i = 0; i < nusers; i++) {
			if (this.rng.nextDouble() < sparcity) {
				this.u.setRow(i, zeroUserWord);
			}
		}
		for (int i = 0; i < nfeatures; i++) {
			if (this.rng.nextDouble() < sparcity) {
				this.w.setRow(i, zeroUserWord);
			}
		}
	}

	private Matrix calcY(Matrix u, Matrix x, Matrix w) {
		final Matrix ut = u.transpose();
		final Matrix xt = x.transpose();
		final Matrix utdotxt = ut.times(xt);
		return utdotxt.times(w);
	}

	@Override
	public Pair<Matrix> generate() {
		Matrix x = smf.createUniformRandom(nfeatures, nusers, 0, 1, rng);
		final Matrix xSparse = smf.createMatrix(nfeatures, nusers);
		for (final MatrixEntry matrixEntry : x) {
			if (this.rng.nextDouble() >= this.xsparcity) {
				xSparse.setElement(matrixEntry.getRowIndex(), matrixEntry.getColumnIndex(), matrixEntry.getValue());
			}
		}
		x = xSparse;
		Matrix y = null;
		if (indw && indu) {
			y = smf.createMatrix(1, ntasks);
			for (int i = 0; i < this.ntasks; i++) {
				final Matrix subu = this.u.getSubMatrix(0, nusers - 1, i, i);
				final Matrix subw = this.w.getSubMatrix(0, nfeatures - 1, i, i);
				final Matrix yval = calcY(subu, x, subw);
				y.setSubMatrix(0, i, yval);
			}
		} else {
			y = calcY(u, x, w);
		}
		if (y.getNumColumns() < y.getNumRows()) {
			y = y.transpose();
		}
		if (this.noise != 0) {
			final SparseMatrix nm = smf.createUniformRandom(1, this.ntasks, -this.noise, this.noise, this.rng);
			y.plusEquals(nm);
		}
		return new Pair<Matrix>(x, y);
	}

	public Matrix getU() {
		return this.u;
	}

	public Matrix getW() {
		return this.w;
	}
}
