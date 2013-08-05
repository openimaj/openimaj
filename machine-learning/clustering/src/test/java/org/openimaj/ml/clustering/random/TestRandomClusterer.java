package org.openimaj.ml.clustering.random;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.evaluation.cluster.analyser.FullMEAnalysis;
import org.openimaj.experiment.evaluation.cluster.analyser.FullMEClusterAnalyser;
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
		
		FullMEClusterAnalyser meca = new FullMEClusterAnalyser();
		FullMEAnalysis anal2 = meca.analyse(correct, estimate);
		
		RandomClusterer rc = new RandomClusterer(10,1);
		for (int i = 0; i < 10000; i++) {			
			SparseMatrix sm = new SparseMatrix(17,17);
			int[][] c1 = rc.performClustering(sm);
//			
			FullMEAnalysis anal1 = meca.analyse(correct, c1);
			if(anal2.decision.fscore(1) <= anal1.decision.fscore(1)){
				summarise(anal1,anal2, c1);
			}
			assertTrue(anal1.decision.fscore(1) < anal2.decision.fscore(1));
//			if(anal1.randIndex() > anal2.randIndex()){
//				System.out.println("RandI was beaten by adj randi");
//				summarise(anal1,anal2, c1);
//			}
//			if(anal1.fscore(1) > anal2.fscore(1)){
//				System.out.println("Fscore was beaten by adj randi");
//				summarise(anal1,anal2, c1);
//				
//			}
		}
		
		
	}
	private void summarise(FullMEAnalysis anal1,FullMEAnalysis anal2, int[][] c1) {
		System.out.println("Correct");
		System.out.println(new IndexClusters(correct));
		System.out.println("Estimated");
		System.out.println(new IndexClusters(estimate));
		System.out.println(anal2.getSummaryReport());
		System.out.println("Random");
		System.out.println(new IndexClusters(c1));
		System.out.println(anal1.getSummaryReport());
	}
	
	
}
