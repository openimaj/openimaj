package org.openimaj.ml.clustering.random;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.evaluation.cluster.analyser.MEAnalysis;
import org.openimaj.experiment.evaluation.cluster.analyser.MEClusterAnalyser;
import org.openimaj.ml.clustering.IndexClusters;

import ch.akuhn.matrix.SparseMatrix;

/**
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestRandomClusterer {
	
	private int[][] correct;
	private int[][] estimate;

	/**
	 * 
	 */
	@Before
	public void prepare(){
		this.correct = new int[][]{
			new int[]{0, 1, 2, 3, 4, 5, 6, 7},
			new int[]{8, 9, 10, 11, 12},
			new int[]{13, 14, 15, 16}
		};
		this.estimate = new int[][]{
			new int[]{0, 1, 2, 3, 4, 8},
			new int[]{5, 9, 10, 11, 12, 13},
			new int[]{6, 7, 14, 15, 16}
		};
	}
	/**
	 * @throws Exception
	 */
	@Test
	public void testRC() throws Exception {
		
		MEClusterAnalyser meca = new MEClusterAnalyser();
		MEAnalysis anal2 = meca.analyse(correct, estimate);
		
		RandomClusterer rc = new RandomClusterer(3,1);
		for (int i = 0; i < 10; i++) {			
			SparseMatrix sm = new SparseMatrix(17,17);
			int[][] c1 = rc.performClustering(sm);
//			System.out.println(new IndexClusters(this.correct));
//			System.out.println(new IndexClusters(this.estimate));
//			System.out.println(new IndexClusters(c1));
			MEAnalysis anal1 = meca.analyse(correct, c1);
			assertTrue(anal1.adjRandInd < anal2.adjRandInd);
		}
		
		
	}
	
	
}
