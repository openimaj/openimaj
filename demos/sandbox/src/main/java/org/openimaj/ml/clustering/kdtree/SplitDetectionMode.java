package org.openimaj.ml.clustering.kdtree;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleRealPointChecker;
import org.apache.commons.math.optimization.direct.NelderMead;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.SimplexSolver;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.openimaj.math.matrix.DiagonalMatrix;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.ObjectDoublePair;

import cc.mallet.optimize.Optimizable;
import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Util;
import ch.akuhn.matrix.Vector;
import scala.actors.threadpool.Arrays;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.VariableFactory;

/**
 * Given a vector, tell me the split
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface SplitDetectionMode{
	/**
	 * minimise for y: (y' * (D - W) * y) / ( y' * D * y );
	 * s.t. y = (1 + x) - b * (1 - x);
	 * s.t. b = k / (1 - k);
	 * s.t. k = sum(d(x > 0)) / sum(d);
	 * and
	 * s.t. x is an indicator (-1 for less than t, 1 for greater than or equal to t)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	public class OPTIMISED implements SplitDetectionMode {
		
		private DiagonalMatrix D;
		private SparseMatrix W;
		private MEAN mean;

		public OPTIMISED(DiagonalMatrix D, SparseMatrix W) {
			this.D = D;
			this.W = W;
			this.mean = new MEAN();
		}
		private ObjectDoublePair<double[]> indicator(double[] vec, double d) {
			double[] ind = new double[vec.length];
			double sumx = 0;
			for (int i = 0; i < ind.length; i++) {
				if(vec[i] > d){
					ind[i] = 1;
					sumx ++;
				}
				else{
					ind[i] = -1;
				}
			}
			return ObjectDoublePair.pair(ind, sumx);
		}
		@Override
		public double detect(final double[] vec) {
			double[] t = {this.mean.detect(vec)};
			MultivariateRealFunction func = new MultivariateRealFunction() {
				
				@Override
				public double value(double[] x) throws FunctionEvaluationException {
					ObjectDoublePair<double[]> ind = indicator(vec,x[0]);
					double sumd = MatlibMatrixUtils.sum(D);
					double k = ind.second / sumd;
					double b = k / (1-k);
					double[][] y = new double[1][vec.length];
					for (int i = 0; i < vec.length; i++) {
						y[0][i] = ind.first[i] + 1 - b * (1 - ind.first[i]);
					}
					SparseMatrix dmw = MatlibMatrixUtils.minusInplace(D, W);
					Vector yv = Vector.wrap(y[0]);
					double nom = new DenseMatrix(y).mult(dmw.transposeMultiply(yv)).get(0); // y' * ( (D-W) * y)
					double denom = new DenseMatrix(y).mult(D.transposeMultiply(yv)).get(0);
					return nom/denom;
				}

			}; 
			
//			
			RealPointValuePair ret;
			try {
				NelderMead nelderMead = new NelderMead();
				nelderMead.setConvergenceChecker(new SimpleRealPointChecker(0.0001, -1));
				ret = nelderMead.optimize(func, GoalType.MINIMIZE, t);
				return ret.getPoint()[0];
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Reverting to mean");
			}
			return t[0];
		}

	}

	/**
	 * Splits clusters becuase they don't have exactly the same value!
	 */
	public static class MEDIAN implements SplitDetectionMode{
		@Override
		public double detect(double[] col) {
			double mid = ArrayUtils.quickSelect(col, col.length/2);
			if(ArrayUtils.minValue(col) == mid) 
				mid += Double.MIN_NORMAL;
			if(ArrayUtils.maxValue(col) == mid) 
				mid -= Double.MIN_NORMAL;
			return 0;
		}
	}
	
	/**
	 * Use the mean to split
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class MEAN implements SplitDetectionMode{

		@Override
		public double detect(double[] vec) {
			return new Mean().evaluate(vec);
		}
		
	}
	
	/**
	 * Find the median, attempt to find a value which keeps clusters together
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class VARIABLE_MEDIAN implements SplitDetectionMode{

		private double tolchange;
		/**
		 * Sets the change tolerance to 0.1 (i.e. if the next value is different by more than value * 0.1, we switch)
		 */
		public VARIABLE_MEDIAN() {
			this.tolchange = 0.0001;
		}
		
		/**
		 * @param tol if the next value is different by more than value * tol we found a border
		 */
		public VARIABLE_MEDIAN(double tol) {
			this.tolchange = tol;
		}
		
		@Override
		public double detect(double[] vec) {
			Arrays.sort(vec);
			// Find the median index
			int medInd = vec.length/2;
			double medVal = vec[medInd];
			if(vec.length % 2 == 0){
				medVal += vec[medInd+1];
				medVal /= 2.;
			}
			
			
			boolean maxWithinTol = withinTol(medVal,vec[vec.length-1]);
			boolean minWithinTol = withinTol(medVal,vec[0]);
			if(maxWithinTol && minWithinTol) 
			{
				// degenerate case, the min and max are not beyond the tolerance, return the median
				return medVal;
			}
			// The split works like:
			// < val go left
			// >= val go right
			if(maxWithinTol){
				// search left
				for (int i = medInd; i > 0; i--) {
					if(!withinTol(vec[i],vec[i-1])){
						return vec[i];
					}
				}
			}
			else{
				// search right
				for (int i = medInd; i < vec.length-1; i++) {
					if(!withinTol(vec[i],vec[i+1])){
						return vec[i+1];
					}
				}
			}
			
			
			
			return 0;
		}

		private boolean withinTol(double a, double d) {
			return Math.abs(a - d) / Math.abs(a) < this.tolchange;
		}
		
	};
	public abstract double detect(double[] vec);
}