package org.openimaj.image.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.FImage2DoubleFV;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.matrix.algorithm.LinearDiscriminantAnalysis;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.ml.training.BatchTrainer;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class FisherImages implements BatchTrainer<IndependentPair<?, FImage>>, 
FeatureExtractor<DoubleFV, FImage>, 
ReadWriteableBinary 
{
	private int numComponents;
	private int width;
	private int height;
	private Matrix basis;
	private double[] mean;

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

	public void train(Map<?, ? extends List<FImage>> data) {
		List<IndependentPair<?, FImage>> list = new ArrayList<IndependentPair<?,FImage>>();
		
		for (Entry<?, ? extends List<FImage>> e : data.entrySet()) {
			for (FImage i : e.getValue()) {
				list.add(IndependentPair.pair(e.getKey(), i));
			}
		}
		
		train(list);
	}
	
	@Override
	public void train(List<? extends IndependentPair<?, FImage>> data) {
		width = data.get(0).secondObject().width;
		height = data.get(0).secondObject().height;
		
		Map<Object, List<double[]>> mapData = new HashMap<Object, List<double[]>>();
		List<double[]> listData = new ArrayList<double[]>();
		for (IndependentPair<?, FImage> item : data) {
			List<double[]> fvs = mapData.get(item.firstObject());
			if (fvs == null) mapData.put(item.firstObject(), fvs = new ArrayList<double[]>());

			double[] fv = FImage2DoubleFV.INSTANCE.extractFeature(item.getSecondObject()).values;
			fvs.add(fv);
			listData.add(fv);
		}

		PrincipalComponentAnalysis pca = new ThinSvdPrincipalComponentAnalysis(numComponents);
		pca.learnBasis(listData);

		List<double[][]> ldaData = new ArrayList<double[][]>(mapData.size());
		for (Entry<?, List<double[]>> e : mapData.entrySet()) {
			List<double[]> vecs = e.getValue();
			double[][] classData = new double[vecs.size()][];

			for (int i=0; i<classData.length; i++) {
				classData[i] = pca.project(vecs.get(i));
			}

			ldaData.add(classData);
		}

		LinearDiscriminantAnalysis lda = new LinearDiscriminantAnalysis(numComponents);
		lda.learnBasis(ldaData);

		basis = pca.getBasis().times(lda.getBasis());
		mean = pca.getMean();
	}

	/**
	 * Project a vector by the basis. The vector
	 * is normalised by subtracting the mean and
	 * then multiplied by the basis.
	 * @param vector the vector to project
	 * @return projected vector
	 */
	public double[] project(double [] vector) {
		Matrix vec = new Matrix(1, vector.length);
		final double[][] vecarr = vec.getArray();

		for (int i=0; i<vector.length; i++)
			vecarr[0][i] = vector[i] - mean[i];

		return vec.times(basis).getColumnPackedCopy();
	}

	@Override
	public DoubleFV extractFeature(FImage object) {
		return new DoubleFV(project(FImage2DoubleFV.INSTANCE.extractFeature(object).values));
	}

	/**
	 * Get a specific basis vector as
	 * a double array. The returned array contains a
	 * copy of the data.
	 * 
	 * @param index the index of the vector
	 * 
	 * @return the eigenvector
	 */
	public double[] getBasisVector(int index) {
		double[] pc = new double[basis.getRowDimension()];
		double[][] data = basis.getArray();
		
		for (int r=0; r<pc.length; r++)
			pc[r] = data[r][index];
		
		return pc;
	}
	
	/**
	 * Draw a principal component as an image
	 * @param pc the index of the PC to draw.
	 * @return an image showing the PC.
	 */
	public FImage visualisePC(int pc) {
		return new FImage(ArrayUtils.reshape(getBasisVector(pc), width, height));
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
		
		FisherImages fi = new FisherImages(14);
		fi.train((Map<?, ? extends List<FImage>>) DatasetAdaptors.asMap(dataset));
		
		for (int i=0; i<14; i++)
			DisplayUtilities.display(fi.visualisePC(i).normalise());
	}

}
