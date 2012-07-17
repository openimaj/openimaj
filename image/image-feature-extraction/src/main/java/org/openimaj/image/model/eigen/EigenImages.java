package org.openimaj.image.model.eigen;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapDataset;
import org.openimaj.experiment.dataset.crossvalidation.CrossValidationData;
import org.openimaj.experiment.dataset.crossvalidation.GroupedKFoldIterator;
import org.openimaj.experiment.dataset.crossvalidation.GroupedLeaveOneOutIterator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.DoubleFV2FImage;
import org.openimaj.image.feature.FImage2DoubleFV;
import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.ml.annotation.Annotated;
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
	
	static class IdentifiableFImage implements Identifiable, Annotated<FImage, String> {
		FImage img;
		int person;
		int id;
		
		public IdentifiableFImage(FImage image, int s, int i) {
			this.img = image;
			this.person = s;
			this.id = i;
		}

		@Override
		public String getID() {
			return person + "." + id;
		}

		@Override
		public FImage getObject() {
			return img;
		}

		@Override
		public Collection<String> getAnnotations() {
			ArrayList<String> list = new ArrayList<String>();
			list.add(person + "."+id);
			return list;
		}
	}
	
	public static void main(String[] args) throws IOException {
		List<FImage> images = new ArrayList<FImage>();
		Map<String, ListDataset<IdentifiableFImage>> map = new HashMap<String, ListDataset<IdentifiableFImage>>();
		
		for (int s=1; s<=40; s++) {
			ListDataset<IdentifiableFImage> list = new ListDataset<IdentifiableFImage>();
			map.put(s+"", list);
			
			for (int i=1; i<=10; i++) {
				File file = new File("/Users/jon/Downloads/att_faces/s" + s + "/" + i + ".pgm");
				FImage image = ImageUtilities.readF(file);
				images.add( image );
				
				list.add(new IdentifiableFImage(image, s, i));
			}
		}

		MapDataset<String, ListDataset<IdentifiableFImage>, IdentifiableFImage> dataset = 
			new MapDataset<String, ListDataset<IdentifiableFImage>, IdentifiableFImage>(map);
		
//		EigenImages ei = new EigenImages(50);
//		ei.train(images);
		
		int tp = 0, fp = 0;
		for (CrossValidationData<GroupedDataset<String, ListDataset<IdentifiableFImage>, IdentifiableFImage>> cv : 
			new GroupedKFoldIterator<String, IdentifiableFImage>(dataset, 5)) {
			
			ArrayList<FImage> tImages = new ArrayList<FImage>();
			for (IdentifiableFImage c : cv.getTrainingDataset())
				tImages.add(c.img);
			
			EigenImages ei = new EigenImages(10);
			ei.train(tImages);
			
			KNNAnnotator<FImage, String, EigenImages, DoubleFV> knn = new KNNAnnotator<FImage, String, EigenImages, DoubleFV>(ei, DoubleFVComparison.EUCLIDEAN);
			for (IdentifiableFImage inst : cv.getTrainingDataset()) {
				knn.train(inst);
			}
			
			for (IdentifiableFImage inst : cv.getValidationDataset()) {
				List<ScoredAnnotation<String>> anns = knn.annotate(inst.getObject());
				
				Collections.sort(anns);
				
				System.out.println(inst.getID() + " - expected: " + inst.person + " actual: " + anns.get(0).annotation );
				
				if (anns.get(0).annotation.equals(inst.person + ""))
					tp++;
				else
					fp++;
			}
		}
		
		System.out.println("Accuracy: ");
		System.out.println((float)tp / (float)(tp + fp));
	}
}
