package org.openimaj.image.model.eigen;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.crossvalidation.CrossValidationData;
import org.openimaj.experiment.dataset.crossvalidation.StratifiedGroupedKFoldIterable;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.ROCAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.ROCAnalysisResult;
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
import org.openimaj.ml.annotation.AnnotatedObject;
import org.openimaj.ml.annotation.ScoredAnnotation;
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
		
		int tp = 0, fp = 0;
		for (CrossValidationData<GroupedDataset<Integer, ListDataset<FImage>, FImage>> cv : 
			new StratifiedGroupedKFoldIterable<Integer, FImage>(dataset, 5)) 
		{
			GroupedDataset<Integer, ListDataset<FImage>, FImage> training = cv.getTrainingDataset();
			
			EigenImages ei = new EigenImages(10);
			ei.train( DatasetAdaptors.asList(training) );
			
			KNNAnnotator<FImage, Integer, EigenImages, DoubleFV> knn = new KNNAnnotator<FImage, Integer, EigenImages, DoubleFV>(ei, DoubleFVComparison.EUCLIDEAN);
			
			for (Integer grp : training.getGroups()) {
				for (FImage inst : training.getInstances(grp)) {
					knn.train(new AnnotatedObject<FImage, Integer>(inst, grp));
				}
			}
			
//			GroupedDataset<Integer, ListDataset<FImage>, FImage> validation = cv.getValidationDataset();
//			for (Integer expectedGrp : validation.getGroups()) {
//				for (FImage inst : validation.getInstances(expectedGrp)) {
//					List<ScoredAnnotation<Integer>> anns = knn.annotate(inst);
//				
//					System.out.println("expected: " + expectedGrp + " actual: " + anns.get(0).annotation);
//				
//					if (anns.get(0).annotation.equals(expectedGrp))
//						tp++;
//					else
//						fp++;
//				}
//			}
			ClassificationEvaluator<ROCAnalysisResult<Integer>, Integer, FImage> eval = 
				new ClassificationEvaluator<ROCAnalysisResult<Integer>, Integer, FImage>(knn, cv.getValidationDataset(), new ROCAnalyser<FImage, Integer>());
			ROCAnalysisResult<Integer> res = eval.analyse(eval.evaluate());
			System.out.println(res);
		}
		
		System.out.println("Accuracy: ");
		System.out.println((float)tp / (float)(tp + fp));
	}
}
