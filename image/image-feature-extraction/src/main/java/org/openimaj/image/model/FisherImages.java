package org.openimaj.image.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.FImage2DoubleFV;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.math.matrix.algorithm.LinearDiscriminantAnalysis;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis;
import org.openimaj.math.matrix.algorithm.pca.ThinSvdPrincipalComponentAnalysis;
import org.openimaj.ml.training.BatchTrainer;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Implementation of Fisher Images (aka "FisherFaces").
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FisherImages implements BatchTrainer<IndependentPair<?, FImage>>, 
FeatureExtractor<DoubleFV, FImage>, 
ReadWriteableBinary 
{
	private int numComponents;
	private int width;
	private int height;
	private Matrix basis;
	private double[] mean;

	/**
	 * Construct with the given number of components.
	 * @param numComponents the number of components
	 */
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

	/**
	 * Train on a map of data.
	 * @param data the data
	 */
	public void train(Map<?, ? extends List<FImage>> data) {
		List<IndependentPair<?, FImage>> list = new ArrayList<IndependentPair<?,FImage>>();
		
		for (Entry<?, ? extends List<FImage>> e : data.entrySet()) {
			for (FImage i : e.getValue()) {
				list.add(IndependentPair.pair(e.getKey(), i));
			}
		}
		
		train(list);
	}
	
	/**
	 * Train on a grouped dataset.
	 * @param <KEY> The group type 
	 * @param data the data
	 */
	public <KEY> void train(GroupedDataset<KEY, ? extends ListDataset<FImage>, FImage> data) {
		List<IndependentPair<?, FImage>> list = new ArrayList<IndependentPair<?,FImage>>();
		
		for (KEY e : data.getGroups()) {
			for (FImage i : data.getInstances(e)) {
				list.add(IndependentPair.pair(e, i));
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

	private double[] project(double [] vector) {
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
	 * Draw an eigenvector as an image
	 * @param num the index of the eigenvector to draw.
	 * @return an image showing the eigenvector.
	 */
	public FImage visualise(int num) {
		return new FImage(ArrayUtils.reshape(getBasisVector(num), width, height));
	}
}
