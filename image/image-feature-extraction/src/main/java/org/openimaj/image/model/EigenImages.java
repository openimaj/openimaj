package org.openimaj.image.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.DoubleFV2FImage;
import org.openimaj.image.feature.FImage2DoubleFV;
import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.ml.pca.FeatureVectorPCA;
import org.openimaj.ml.training.BatchTrainer;
import org.openimaj.util.array.ArrayUtils;

/**
 * Implementation of EigenImages. Can be used for things like face recognition
 * ala the classic EigenFaces algorithm.
 * <p>
 * Fundamentally, the EigenImages technique is a way to perform dimensionality
 * reduction on an image using PCA. This implementation can be trained through
 * the {@link BatchTrainer} interface (which will internally normalise the data
 * and perform PCA). Once trained, instances can be used as
 * {@link FeatureExtractor}s to extract low(er) dimensional features from
 * images.
 * <p>
 * Methods are also provided to reconstruct an image from its feature vector
 * (see {@link #reconstruct(DoubleFV) and #reconstruct(double[])}, and to
 * visualise a specific principal component as an image (see
 * {@link #visualisePC(int)}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Turk, M.A.", "Pentland, A.P." },
		title = "Face recognition using eigenfaces",
		year = "1991",
		booktitle = "Computer Vision and Pattern Recognition, 1991. Proceedings CVPR '91., IEEE Computer Society Conference on",
		pages = { "586 ", "591" },
		month = "jun",
		number = "",
		volume = "",
		customData = {
				"keywords", "eigenfaces;eigenvectors;face images;face recognition system;face space;feature space;human faces;two-dimensional recognition;unsupervised learning;computerised pattern recognition;eigenvalues and eigenfunctions;",
				"doi", "10.1109/CVPR.1991.139758"
		})
public class EigenImages implements BatchTrainer<FImage>, FeatureExtractor<DoubleFV, FImage>, ReadWriteableBinary {
	private FeatureVectorPCA pca;
	private int width;
	private int height;

	/**
	 * Construct with the given number of principal components.
	 * 
	 * @param numComponents
	 *            the number of PCs
	 */
	public EigenImages(int numComponents) {
		pca = new FeatureVectorPCA(new ThinSvdPrincipalComponentAnalysis(numComponents));
	}

	@Override
	public DoubleFV extractFeature(FImage img) {
		final DoubleFV feature = FImage2DoubleFV.INSTANCE.extractFeature(img);

		return pca.project(feature);
	}

	@Override
	public void train(List<? extends FImage> data) {
		final double[][] features = new double[data.size()][];

		width = data.get(0).width;
		height = data.get(0).height;

		for (int i = 0; i < features.length; i++)
			features[i] = FImage2DoubleFV.INSTANCE.extractFeature(data.get(i)).values;

		pca.learnBasis(features);
	}

	/**
	 * Reconstruct an image from a weight vector
	 * 
	 * @param weights
	 *            the weight vector
	 * @return the reconstructed image
	 */
	public FImage reconstruct(DoubleFV weights) {
		return DoubleFV2FImage.extractFeature(pca.generate(weights), width, height);
	}

	/**
	 * Reconstruct an image from a weight vector
	 * 
	 * @param weights
	 *            the weight vector
	 * @return the reconstructed image
	 */
	public FImage reconstruct(double[] weights) {
		return new FImage(ArrayUtils.reshape(pca.generate(weights), width, height));
	}

	/**
	 * Draw a principal component as an image
	 * 
	 * @param pc
	 *            the index of the PC to draw.
	 * @return an image showing the PC.
	 */
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

	@Override
	public String toString() {
		return String.format("EigenImages[width=%d; height=%d; pca=%s]", width, height, pca);
	}
}
