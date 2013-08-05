package org.openimaj.experiment.evaluation.cluster.analyser;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.dataset.cluster.ClusertEvalDataset;
import org.openimaj.logger.LoggerUtils;



/**
 * Test the {@link RandomBaselineClusterAnalyser} using data provided with cluster eval:
 * http://chris.de-vries.id.au/2013/06/clustereval-10-release.html
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestRandomBaselineClusterAnalyser{
	private static final String GROUNDTRUTH = "/org/openimaj/experiment/dataset/cluster/inex10_single_label.txt";
	private static final String ESTIMATES = "/org/openimaj/experiment/dataset/cluster/inex10_clusters_50.txt";
	private final static Logger logger = Logger.getLogger(TestRandomBaselineClusterAnalyser.class);
	private ClusertEvalDataset groundTruth;
	private ClusertEvalDataset estimate;

	/**
	 * @throws IOException 
	 * 
	 */
	@Before
	public void begin() throws IOException{
		LoggerUtils.prepareConsoleLogger();
		Logger.getLogger(NMIClusterAnalyser.class).setLevel(Level.ERROR);
		logger.debug("Loading groundtruth");
		this.groundTruth = new ClusertEvalDataset(TestRandomBaselineClusterAnalyser.class.getResourceAsStream(GROUNDTRUTH));
		logger.debug("Loading estimate");
		this.estimate = new ClusertEvalDataset(TestRandomBaselineClusterAnalyser.class.getResourceAsStream(ESTIMATES));
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
		RandomBaselineClusterAnalyser<DecisionClusterAnalyser, DecisionAnalysis> rrandi = new RandomBaselineClusterAnalyser<>(new DecisionClusterAnalyser());
		RandomBaselineClusterAnalysis<DecisionAnalysis> annrandi = rrandi.analyse(gt, es);
		logger.debug(annrandi.getSummaryReport());
	}
	
}
