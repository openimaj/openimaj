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
package org.openimaj.docs.tutorial.fund.ml.class101;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.feature.DiskCachingFeatureExtractor;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Load dataset and take a sample");
		final GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = Caltech101
				.getData(ImageUtilities.FIMAGE_READER);
		final GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> data = GroupSampler.sample(allData, 5,
				false);

		System.out.println("Construct the base feature extractor");
		final DenseSIFT dsift = new DenseSIFT(5, 7);
		final PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 7);

		System.out.println("Create training and testing data");
		final GroupedRandomSplitter<String, Record<FImage>> splits = new GroupedRandomSplitter<String, Record<FImage>>(
				data, 15, 0, 15);

		System.out.println("Learn a vocabulary");
		final HardAssigner<byte[], float[], IntFloatPair> assigner = trainQuantiser(GroupedUniformRandomisedSampler
				.sample(splits.getTrainingDataset(), 30), pdsift);

		System.out.println("Define feature extractor");
		// final HomogeneousKernelMap map = new
		// HomogeneousKernelMap(KernelType.Chi2, WindowType.Rectangular);
		// final FeatureExtractor<DoubleFV, Record<FImage>> extractor = map
		// .createWrappedExtractor(new PHOWExtractor(pdsift, assigner));
		final FeatureExtractor<DoubleFV, Record<FImage>> extractor = new DiskCachingFeatureExtractor<DoubleFV, Caltech101.Record<FImage>>(
				new File("/Users/jsh2/feature_cache/c101-small"), new SpPHOWExtractorImplementation(pdsift, assigner));

		System.out.println("Construct and train classifier");
		final LiblinearAnnotator<Record<FImage>, String> ann = new LiblinearAnnotator<Record<FImage>, String>(
				extractor, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
		ann.train(splits.getTrainingDataset());

		System.out.println("Evaluate classifier");
		final ClassificationEvaluator<CMResult<String>, String, Record<FImage>> eval = new ClassificationEvaluator<CMResult<String>, String, Record<FImage>>(
				ann, splits.getTestDataset(), new CMAnalyser<Record<FImage>, String>(CMAnalyser.Strategy.SINGLE));
		final Map<Record<FImage>, ClassificationResult<String>> guesses = eval.evaluate();
		final CMResult<String> result = eval.analyse(guesses);

		System.out.println(result.getDetailReport());
	}

	private static final class SpPHOWExtractorImplementation implements FeatureExtractor<DoubleFV, Record<FImage>> {
		PyramidDenseSIFT<FImage> pdsift;
		HardAssigner<byte[], float[], IntFloatPair> assigner;

		public SpPHOWExtractorImplementation(PyramidDenseSIFT<FImage> pdsift,
				HardAssigner<byte[], float[], IntFloatPair> assigner)
		{
			this.pdsift = pdsift;
			this.assigner = assigner;
		}

		@Override
		public DoubleFV extractFeature(Record<FImage> object) {
			final FImage image = object.getImage();
			pdsift.analyseImage(image);

			final BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);

			final BlockSpatialAggregator<byte[], SparseIntFV> spatial = new BlockSpatialAggregator<byte[], SparseIntFV>(
					bovw, 2, 2);

			// final PyramidSpatialAggregator<byte[], SparseIntFV> spatial =
			// new PyramidSpatialAggregator<byte[], SparseIntFV>(bovw, 2, 4);

			return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
		}
	}

	private static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(
			GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> sample, PyramidDenseSIFT<FImage> pdsift)
	{
		List<LocalFeatureList<ByteDSIFTKeypoint>> allkeys = new ArrayList<LocalFeatureList<ByteDSIFTKeypoint>>();

		for (final Record<FImage> rec : sample) {
			final FImage img = rec.getImage();

			pdsift.analyseImage(img);
			allkeys.add(pdsift.getByteKeypoints(0.005f));
		}

		if (allkeys.size() > 10000)
			allkeys = allkeys.subList(0, 10000);

		final ByteKMeans km = ByteKMeans.createKDTreeEnsemble(300);
		final DataSource<byte[]> datasource = new LocalFeatureListDataSource<ByteDSIFTKeypoint, byte[]>(allkeys);
		final ByteCentroidsResult result = km.cluster(datasource);

		return result.defaultHardAssigner();
	}
}
