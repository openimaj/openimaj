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
package org.openimaj.experiment.evaluation.cluster.analyser;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.cluster.ClusterEvalDataset;
import org.openimaj.logger.LoggerUtils;



/**
 * Test the {@link RandomBaselineClusterAnalyser} using data provided with cluster eval:
 * http://chris.de-vries.id.au/2013/06/clustereval-10-release.html
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestRandomBaselineClusterAnalyser{
//	private static final String GROUNDTRUTH = "/org/openimaj/experiment/dataset/cluster/inex10_single_label.txt";
//	private static final String ESTIMATES = "/org/openimaj/experiment/dataset/cluster/inex10_clusters_50.txt";
	private static final String GROUNDTRUTH = "/org/openimaj/experiment/dataset/cluster/clusters.txt";
	private static final String ESTIMATES = "/org/openimaj/experiment/dataset/cluster/estimates.txt";
	private final static Logger logger = Logger.getLogger(TestRandomBaselineClusterAnalyser.class);
	private ClusterEvalDataset groundTruth;
	private ClusterEvalDataset estimate;

	/**
	 * @throws IOException 
	 * 
	 */
	@Before
	public void begin() throws IOException{
		LoggerUtils.prepareConsoleLogger();
		Logger.getLogger(NMIClusterAnalyser.class).setLevel(Level.ERROR);
		logger.debug("Loading groundtruth");
		this.groundTruth = new ClusterEvalDataset(TestRandomBaselineClusterAnalyser.class.getResourceAsStream(GROUNDTRUTH));
		logger.debug("Loading estimate");
		this.estimate = new ClusterEvalDataset(TestRandomBaselineClusterAnalyser.class.getResourceAsStream(ESTIMATES));
	}
	
	/**
	 * 
	 */
	@Test
	public void test(){
		logger.debug("Converting datasets to int array clusters");
		int[][] gt = this.groundTruth.toClusters();
		int[][] es = this.estimate.toClusters();
		
		logger.debug("Performing NMI random baseline analysis");
		RandomBaselineClusterAnalyser<NMIClusterAnalyser, NMIAnalysis> r = new RandomBaselineClusterAnalyser<NMIClusterAnalyser, NMIAnalysis>(new NMIClusterAnalyser());
		RandomBaselineClusterAnalysis<NMIAnalysis> ann = r.analyse(gt, es);
		logger.debug(ann.getSummaryReport());
		
		logger.debug("Performing purity random baseline analysis");
		RandomBaselineClusterAnalyser<PurityClusterAnalyser, PurityAnalysis> rpure = new RandomBaselineClusterAnalyser<PurityClusterAnalyser, PurityAnalysis>(new PurityClusterAnalyser());
		RandomBaselineClusterAnalysis<PurityAnalysis> annpure = rpure.analyse(gt, es);
		logger.debug(annpure.getSummaryReport());
		
		logger.debug("Performing randind random baseline analysis");
		RandomBaselineClusterAnalyser<RandomIndexClusterAnalyser, RandomIndexAnalysis> rrandi = new RandomBaselineClusterAnalyser<RandomIndexClusterAnalyser, RandomIndexAnalysis>(new RandomIndexClusterAnalyser());
		RandomBaselineClusterAnalysis<RandomIndexAnalysis> annrandi = rrandi.analyse(gt, es);
		logger.debug(annrandi.getSummaryReport());
	}
	
}
