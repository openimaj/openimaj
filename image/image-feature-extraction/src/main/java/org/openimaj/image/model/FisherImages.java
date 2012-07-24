package org.openimaj.image.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.matrix.algorithm.LinearDiscriminantAnalysis;
import org.openimaj.ml.training.BatchTrainer;

public class FisherImages implements BatchTrainer<FImage>, FeatureExtractor<DoubleFV, FImage>, ReadWriteableBinary {
	private int numComponents;
	private int width;
	private int height;
	
	public FisherImages(int numComponents) {
		this.numComponents = numComponents;
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		width = in.readInt();
		height = in.readInt();
		numComponents = in.readInt();
	}

	@Override
	public byte[] binaryHeader() {
		return "FisI".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(width);
		out.writeInt(height);
		out.writeInt(numComponents);
	}

	@Override
	public void train(List<? extends FImage> data) {
		LinearDiscriminantAnalysis lda = new LinearDiscriminantAnalysis(numComponents);
		
		
	}

	@Override
	public DoubleFV extractFeature(FImage object) {
		// TODO Auto-generated method stub
		return null;
	}
}
