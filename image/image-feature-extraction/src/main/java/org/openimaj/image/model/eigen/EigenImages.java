package org.openimaj.image.model.eigen;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.evaluation.AnalysisResult;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAggregator;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.experiment.validation.ValidationOperation;
import org.openimaj.experiment.validation.ValidationRunner;
import org.openimaj.experiment.validation.cross.StratifiedGroupedKFoldIterable;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.DoubleFV2FImage;
import org.openimaj.image.feature.FImage2DoubleFV;
import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.ml.annotation.basic.KNNAnnotator;
import org.openimaj.ml.pca.FeatureVectorPCA;
import org.openimaj.ml.training.BatchTrainer;
import org.openimaj.util.array.ArrayUtils;

public class EigenImages implements BatchTrainer<FImage>, FeatureExtractor<DoubleFV, FImage>, ReadWriteableBinary {
	private FeatureVectorPCA pca;
	private int width;
	private int height;

	public EigenImages(int numComponents) {
		pca = new FeatureVectorPCA(new ThinSvdPrincipalComponentAnalysis(numComponents));
	}

	@Override
	public DoubleFV extractFeature(FImage img) {
		DoubleFV feature = FImage2DoubleFV.INSTANCE.extractFeature(img);

		return pca.project(feature);
	}

	@Override
	public void train(List<? extends FImage> data) {
		double[][] features = new double[data.size()][];

		width = data.get(0).width;
		height = data.get(0).height;

		for (int i=0; i<features.length; i++)  
			features[i] = FImage2DoubleFV.INSTANCE.extractFeature(data.get(i)).values;

		pca.learnBasis(features);
	}

	public FImage reconstruct(DoubleFV weights) {
		return DoubleFV2FImage.extractFeature(pca.generate(weights), width, height);
	}

	public FImage reconstruct(double[] weights) {
		return new FImage(ArrayUtils.reshape(pca.generate(weights), width, height));
	}

	public FImage visualisePC(int pc) {
		return new FImage(ArrayUtils.reshape(pca.getPrincipalComponent(pc), width, height));
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		width = in.readInt();
		height = in.readInt();
		pca = IOUtils.read(in);
	}

	@Override
	public byte[] binaryHeader() {
		return "EigI".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(width);
		out.writeInt(height);
		IOUtils.write(pca, out);
	}

	public static void main(String[] args) throws IOException {
		MapBackedDataset<Integer, ListDataset<FImage>, FImage> dataset = 
			new MapBackedDataset<Integer, ListDataset<FImage>, FImage>();

		for (int s=1; s<=40; s++) {
			ListBackedDataset<FImage> list = new ListBackedDataset<FImage>();
			dataset.getMap().put(s, list);

			for (int i=1; i<=10; i++) {
				File file = new File("/Users/jsh2/Downloads/att_faces/s" + s + "/" + i + ".pgm");

				FImage image = ImageUtilities.readF(file);

				list.add(image);
			}
		}

		CMAggregator<Integer> aggregator = new CMAggregator<Integer>();
		AnalysisResult score = new ValidationRunner().run(aggregator, 
				new StratifiedGroupedKFoldIterable<Integer, FImage>(dataset, 5), 
				new ValidationOperation<GroupedDataset<Integer, ListDataset<FImage>, FImage>, CMResult<Integer>>() 
		{
			@Override
			public CMResult<Integer> evaluate(
					GroupedDataset<Integer, ListDataset<FImage>, FImage> training,
					GroupedDataset<Integer, ListDataset<FImage>, FImage> validation) {

				EigenImages ei = new EigenImages(10);
				ei.train( DatasetAdaptors.asList(training) );

				KNNAnnotator<FImage, Integer, EigenImages, DoubleFV> knn = new KNNAnnotator<FImage, Integer, EigenImages, DoubleFV>(ei, DoubleFVComparison.EUCLIDEAN);
				knn.train(training);

				ClassificationEvaluator<CMResult<Integer>, Integer, FImage> eval = 
					new ClassificationEvaluator<CMResult<Integer>, Integer, FImage>(knn, validation, new CMAnalyser<FImage, Integer>());
				
				return eval.analyse(eval.evaluate());
			}
		});

		System.out.println(score);
	}
}
