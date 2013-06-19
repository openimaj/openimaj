package org.openimaj.demos.classification;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.demos.classification.Caltech101.Record;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
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
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.dense.gradient.dsift.ApproximateDenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.PyramidSpatialAggregator;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.ml.kernel.HomogeneousKernelMap;
import org.openimaj.ml.kernel.HomogeneousKernelMap.KernelType;
import org.openimaj.ml.kernel.HomogeneousKernelMap.WindowType;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IntFloatPair;
import org.openimaj.util.parallel.Parallel;

import de.bwaldvogel.liblinear.SolverType;

public class CT101PHOW {
	private static final File CACHE_DIR = new File("/Users/jsh2/feature-cache/caltech101/sppyr-2-4-fastphog-300");

	public static void main(String[] args) throws IOException {
		System.out.println("Loading dataset");
		final GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> data = Caltech101
				.getData(ImageUtilities.FIMAGE_READER);

		System.out.println("Creating training and testing splits");
		final GroupedRandomSplits<String, Record<FImage>> splits = new GroupedRandomSplits<String, Record<FImage>>(data,
				15, 15);

		final File voc = new File("/Users/jsh2/feature-cache/caltech101/sppyr-2-4-fastphog-300.voc");
		final HardAssigner<byte[], float[], IntFloatPair> assigner;
		if (!voc.exists()) {
			System.out.println("Learning vocabulary");
			assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30));

			voc.getParentFile().mkdirs();
			IOUtils.writeToFile(assigner, voc);
		} else {
			System.out.println("Loading vocabulary");
			assigner = IOUtils.readFromFile(voc);
		}

		Parallel.forEach(data, new Operation<Record<FImage>>() {
			DiskCachingFeatureExtractor<DoubleFV, Record<FImage>> extr = new
					DiskCachingFeatureExtractor<DoubleFV, Record<FImage>>(
							CACHE_DIR, new
							SpPHOWExtractorImplementation(
									assigner));

			@Override
			public void perform(Record<FImage> object) {
				System.out.println(object.getID());
				extr.extractFeature(object);
			}
		});

		System.out.println("Training classifier");
		final DiskCachingFeatureExtractor<DoubleFV, Record<FImage>> extractor = new DiskCachingFeatureExtractor<DoubleFV, Record<FImage>>(
				CACHE_DIR, new SpPHOWExtractorImplementation(
						assigner));

		final FeatureExtractor<DoubleFV, Record<FImage>> extractor2 = new FeatureExtractor<DoubleFV, Record<FImage>>() {
			HomogeneousKernelMap map = new HomogeneousKernelMap(KernelType.Chi2, WindowType.Rectangular);

			@Override
			public DoubleFV extractFeature(Record<FImage> object) {
				return map.evaluate(extractor.extractFeature(object));
			}
		};

		// final KNNAnnotator<Record<FImage>, String, DoubleFV> ann =
		// KNNAnnotator.create(extractor2,
		// DoubleFVComparison.EUCLIDEAN);
		final LiblinearAnnotator<Record<FImage>, String> ann = new LiblinearAnnotator<Record<FImage>, String>(
				extractor2, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 10.0, 0.00001);
		ann.train(splits.getTestDataset());

		System.out.println("Evaluating classifier");
		final ClassificationEvaluator<CMResult<String>, String, Record<FImage>> eval = new ClassificationEvaluator<CMResult<String>, String, Record<FImage>>(
				ann, splits.getTrainingDataset(), new CMAnalyser<Record<FImage>, String>(CMAnalyser.Strategy.SINGLE));
		final Map<Record<FImage>, ClassificationResult<String>> guesses = eval.evaluate();
		final CMResult<String> result = eval.analyse(guesses);

		System.out.println(result.getDetailReport());
	}

	private static final class SpPHOWExtractorImplementation implements FeatureExtractor<DoubleFV, Record<FImage>> {
		HardAssigner<byte[], float[], IntFloatPair> assigner;

		public SpPHOWExtractorImplementation(HardAssigner<byte[], float[], IntFloatPair> assigner) {
			this.assigner = assigner;
		}

		@Override
		public DoubleFV extractFeature(Record<FImage> object) {
			return getSpatialPHOW(object.getImage(), assigner);
		}

	}

	private static DoubleFV getSpatialPHOW(FImage image, HardAssigner<byte[], float[], IntFloatPair> assigner) {
		image = standardiseImage(image);

		final DenseSIFT dsift = new ApproximateDenseSIFT(3, 3, 4, 4, 4, 4, 8, 1.5f);
		final PyramidDenseSIFT pdsift = new PyramidDenseSIFT(dsift, 6, 4, 6, 8, 10);
		pdsift.analyseImage(image);

		final BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);
		// final BlockSpatialAggregator<byte[], SparseIntFV> spatial =
		// new BlockSpatialAggregator<byte[], SparseIntFV>(bovw, 2, 2);

		final PyramidSpatialAggregator<byte[], SparseIntFV> spatial =
				new PyramidSpatialAggregator<byte[], SparseIntFV>(bovw, 2, 4);

		return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
	}

	private static FImage standardiseImage(FImage img) {
		return ResizeProcessor.resizeMax(img, 640);
	}

	private static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(
			GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> sample)
	{
		final DenseSIFT dsift = new DenseSIFT(3, 3, 4, 4, 4, 4, 8, 1.5f);
		final PyramidDenseSIFT pdsift = new PyramidDenseSIFT(dsift, 6, 4, 6, 8, 10);

		LocalFeatureList<ByteDSIFTKeypoint> allkeys = new MemoryLocalFeatureList<ByteDSIFTKeypoint>();

		for (final Record<FImage> rec : sample) {
			final FImage img = standardiseImage(rec.getImage());

			pdsift.analyseImage(img);
			allkeys.addAll(pdsift.getByteKeypoints(0.005f));
		}

		if (allkeys.size() > 10000)
			allkeys = allkeys.subList(0, 10000);

		final ByteKMeans km = ByteKMeans.createExact(128, 300);
		final DataSource<byte[]> datasource = new LocalFeatureListDataSource<ByteDSIFTKeypoint, byte[]>(allkeys);
		final ByteCentroidsResult result = km.cluster(datasource);

		return result.defaultHardAssigner();
	}
}
