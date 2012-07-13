package org.openimaj.image.model.eigen;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.FImage2DoubleFV;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.ml.pca.FeatureVectorPCA;
import org.openimaj.ml.training.BatchTrainer;

public class EigenImages implements BatchTrainer<FImage>, FeatureExtractor<DoubleFV, FImage> {
	private FeatureVectorPCA pca;
	
	public EigenImages(int numComponents) {
		pca = new FeatureVectorPCA(new ThinSvdPrincipalComponentAnalysis(numComponents));
	}

	@Override
	public DoubleFV extractFeature(FImage object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void train(List<? extends FImage> data) {
		List<FeatureVector> features = new ArrayList<FeatureVector>(data.size());
		
		for (FImage img : data)
			features.add(FImage2DoubleFV.INSTANCE.extractFeature(img));
		
		pca.learnBasis(features);
	}
}
