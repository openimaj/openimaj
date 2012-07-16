package org.openimaj.image.model.eigen;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.DoubleFV;
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
		List<FImage> images = new ArrayList<FImage>();
		
		for (int s=1; s<=40; s++) {
			for (int i=1; i<=10; i++) {
				File image = new File("/Users/jon/Downloads/att_faces/s" + s + "/" + i + ".pgm");
				
				images.add( ImageUtilities.readF(image) );
			}
		}
		
		EigenImages ei = new EigenImages(300);
		ei.train(images);
		
//		DisplayUtilities.display(ei.reconstruct(new double[300]));
//		DisplayUtilities.display(ei.reconstruct(new double[] { 3*ei.pca.getEigenValue(0), 0, 0, 0 } ).normalise());
		
//		FImage image = ImageUtilities.readF(new File("/Users/jon/Downloads/att_faces/s" + 1 + "/" + 1 + ".pgm"));
//		DoubleFV feature = ei.extractFeature(image);
//		System.out.println(feature);
//		DisplayUtilities.display(image);
//		DisplayUtilities.display(ei.reconstruct(feature));
		
		for (int i=0; i<10; i++) {
			DisplayUtilities.display(Transforms.Grey_To_Colour(ei.visualisePC(i).normalise()));
		}
	}
}
