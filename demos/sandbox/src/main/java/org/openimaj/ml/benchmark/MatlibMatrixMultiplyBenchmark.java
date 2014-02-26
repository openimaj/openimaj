package org.openimaj.ml.benchmark;

import java.util.Random;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.math.matrix.MeanVector;
import org.openimaj.time.Timer;

import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.SparseMatrix;
/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class MatlibMatrixMultiplyBenchmark {
	
	public static void main(String[] args) {
		SparseMatrix a = SparseMatrix.sparse(4, 1118);
		MatlibMatrixUtils.plusInplace(a, 1);
		SparseMatrix xtrow = MatlibMatrixUtils.transpose(SparseMatrix.random(1118,22917,1 - 0.9998818947086253));
		
		System.out.println("xtrow sparsity: " + MatlibMatrixUtils.sparsity(xtrow));
		
		MeanVector mv = new MeanVector();
		System.out.println("doing: a . xtrow");
		for (int i = 0; i < 10; i++) {
			Timer t = Timer.timer();
			MatlibMatrixUtils.dotProductTranspose(a, xtrow);
			
			mv.update(new double[]{t.duration()});
			System.out.println("time: " + mv.vec()[0]);
		}
		
	}
}
