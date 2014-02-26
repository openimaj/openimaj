package org.openimaj.ml.benchmark;

import java.util.Random;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.openimaj.demos.sandbox.flickr.geo.GlobalFlickrColour;
import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.math.matrix.MeanVector;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.time.Timer;

import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseColumnMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;
import gov.sandia.cognition.math.matrix.mtj.SparseRowMatrix;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class CFMatrixMultiplyBenchmark {
	
	public static void main(String[] args) {
		SparseMatrix a = SparseMatrixFactoryMTJ.INSTANCE.copyMatrix(SparseMatrixFactoryMTJ.INSTANCE.createWrapper(new FlexCompRowMatrix(4, 1118)));
		CFMatrixUtils.plusInplace(a, 1);
		SparseRowMatrix xtrow = CFMatrixUtils.randomSparseRow(1118,22917,0d,1d,1 - 0.9998818947086253, new Random(1));
		SparseColumnMatrix xtcol = CFMatrixUtils.randomSparseCol(1118,22917,0d,1d,1 - 0.9998818947086253, new Random(1));
		
		System.out.println("xtrow sparsity: " + CFMatrixUtils.sparsity(xtrow));
		System.out.println("xtcol sparsity: " + CFMatrixUtils.sparsity(xtcol));
		System.out.println("Equal: " + CFMatrixUtils.fastsparsedot(a,xtcol).equals(a.times(xtcol), 0));
		MeanVector mv = new MeanVector();
		System.out.println("doing: a . xtcol");
		for (int i = 0; i < 10; i++) {
			Timer t = Timer.timer();
			CFMatrixUtils.fastsparsedot(a,xtcol);
			mv.update(new double[]{t.duration()});
			System.out.println("time: " + mv.vec()[0]);
		}
		
		
		mv.reset();
		System.out.println("doing: a . xtcol");
		for (int i = 0; i < 10; i++) {
			Timer t = Timer.timer();
			a.times(xtcol);
			mv.update(new double[]{t.duration()});
			System.out.println("time: " + mv.vec()[0]);
		}
		mv.reset();
		System.out.println("doing: a . xtrow");
		for (int i = 0; i < 10; i++) {
			Timer t = Timer.timer();
			a.times(xtrow);
			mv.update(new double[]{t.duration()});
			System.out.println("time: " + mv.vec()[0]);
		}
	}
}
