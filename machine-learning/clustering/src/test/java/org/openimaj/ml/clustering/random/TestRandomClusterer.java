/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
		for (int i = 0; i < 100; i++) {			
			SparseMatrix sm = new SparseMatrix(17,17);
			int[][] c1 = rc.performClustering(sm);
//			
			FullMEAnalysis anal1 = meca.analyse(correct, c1);
			if(anal2.fscore.fscore(1) <= anal1.fscore.fscore(1)){
				summarise(anal1,anal2, c1);
			}
			assertTrue(anal1.fscore.fscore(1) < anal2.fscore.fscore(1));
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
