package org.openimaj.demos.classification;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.validation.ValidationData;
import org.openimaj.feature.DiskCachingFeatureExtractor;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.basic.KNNAnnotator;
import org.openimaj.ml.clustering.ByteCentroidsResult;

public class CT101Tests {
	private static final class HistogramExtractorImplementation implements FeatureExtractor<DoubleFV, Record<MBFImage>> {
		@Override
		public DoubleFV extractFeature(Record<MBFImage> object) {
			final BlockHistogramModel hm = new BlockHistogramModel(8, 8, 6, 6, 6);
			hm.estimateModel(object.getImage());

			return hm.toSingleHistogram();
		}
	}

	private static final class BoVWExtractorImplementation implements FeatureExtractor<DoubleFV, Record<MBFImage>> {
		private DoGSIFTEngine engine;
		private BagOfVisualWords<byte[]> bovw;

		public BoVWExtractorImplementation() throws IOException {
			engine = new DoGSIFTEngine();
			bovw = new BagOfVisualWords<byte[]>(IOUtils.read(
					new File("/Users/jsh2/quantisers/mirflickr-oi-sift2x-akm1000.voc"), ByteCentroidsResult.class)
					.defaultHardAssigner());
		}

		@Override
		public DoubleFV extractFeature(Record<MBFImage> object) {
			System.out.println("Extracting " + object.getID());
			return bovw.aggregate(engine.findFeatures(object.getImage().flatten())).asDoubleFV();
		}
	}

	public static void displayAverages() throws IOException {
		final VFSGroupDataset<MBFImage> data = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);

		final ResizeProcessor rp = new ResizeProcessor(200, 200, true);

		for (final String key : data.keySet()) {
			System.out.println(key);
			System.out.println(data.get(key).size());

			final MBFImage avg = new MBFImage(200, 200, ColourSpace.RGB);
			final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);

			for (final MBFImage img : data.get(key)) {

				final MBFImage small = img.process(rp);

				final int x = (200 - small.getWidth()) / 2;
				final int y = (200 - small.getHeight()) / 2;

				tmp.fill(RGBColour.WHITE);
				tmp.drawImage(small, x, y);

				avg.addInplace(tmp);
			}

			avg.divideInplace((float) data.get(key).size() + 1);

			DisplayUtilities.display(avg);
		}
	}

	public static void classification() throws IOException {
		final VFSGroupDataset<Record<MBFImage>> data = Caltech101.getData(ImageUtilities.MBFIMAGE_READER);

		final GroupedRandomSplits<String, Record<MBFImage>> splits = new GroupedRandomSplits<String, Record<MBFImage>>(
				data, 30, 10);

		for (final ValidationData<GroupedDataset<String, ListDataset<Record<MBFImage>>, Record<MBFImage>>> vd : splits
				.createIterable(1))
		{
			final KNNAnnotator<Record<MBFImage>, String, DoubleFV> knn = KNNAnnotator.create(
					new DiskCachingFeatureExtractor<DoubleFV, Record<MBFImage>>(new File(
							"/Users/jsh2/feature-cache/caltech101/bowv-1000"), new BoVWExtractorImplementation()),
					DoubleFVComparison.EUCLIDEAN);
			knn.train(vd.getTrainingDataset());

			final ClassificationEvaluator<CMResult<String>, String, Record<MBFImage>> eval = new ClassificationEvaluator<CMResult<String>, String, Record<MBFImage>>(
					knn, vd.getValidationDataset(), new CMAnalyser<Record<MBFImage>, String>(CMAnalyser.Strategy.SINGLE));

			final Map<Record<MBFImage>, ClassificationResult<String>> guesses = eval.evaluate();
			final CMResult<String> result = eval.analyse(guesses);

			System.out.println(result.getDetailReport());
		}
	}

	public static void main(String[] args) throws IOException {
		// final VFSGroupDataset<Record<MBFImage>> data =
		// Caltech101.getData(ImageUtilities.MBFIMAGE_READER);
		//
		// final Record<MBFImage> r = data.getRandomInstance();
		//
		// final MBFImage image = r.getImage();
		//
		// if (r.getBounds() != null) {
		// image.drawShape(r.getContour(), 3, RGBColour.RED);
		// image.drawShape(r.getBounds(), 3, RGBColour.BLUE);
		//
		// DisplayUtilities.display(image);
		// }

		classification();
	}
}
