/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
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
 * Implementation of Fisher Images (aka "FisherFaces"). PCA is used to avoid the
 * singular within-class scatter matrix.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Belhumeur, Peter N.", "Hespanha, Jo\\~{a}o P.", "Kriegman, David J." },
		title = "Eigenfaces vs. Fisherfaces: Recognition Using Class Specific Linear Projection",
		year = "1997",
		journal = "IEEE Trans. Pattern Anal. Mach. Intell.",
		pages = { "711", "", "720" },
		url = "http://dx.doi.org/10.1109/34.598228",
		month = "July",
		number = "7",
		publisher = "IEEE Computer Society",
		volume = "19",
		customData = {
				"issn", "0162-8828",
				"numpages", "10",
				"doi", "10.1109/34.598228",
				"acmid", "261512",
				"address", "Washington, DC, USA",
				"keywords", "Appearance-based vision, face recognition, illumination invariance, Fisher's linear discriminant."
		})
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
	 * 
	 * @param numComponents
	 *            the number of components
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
	 * 
	 * @param data
	 *            the data
	 */
	public void train(Map<?, ? extends List<FImage>> data) {
		final List<IndependentPair<?, FImage>> list = new ArrayList<IndependentPair<?, FImage>>();

		for (final Entry<?, ? extends List<FImage>> e : data.entrySet()) {
			for (final FImage i : e.getValue()) {
				list.add(IndependentPair.pair(e.getKey(), i));
			}
		}

		train(list);
	}

	/**
	 * Train on a grouped dataset.
	 * 
	 * @param <KEY>
	 *            The group type
	 * @param data
	 *            the data
	 */
	public <KEY> void train(GroupedDataset<KEY, ? extends ListDataset<FImage>, FImage> data) {
		final List<IndependentPair<?, FImage>> list = new ArrayList<IndependentPair<?, FImage>>();

		for (final KEY e : data.getGroups()) {
			for (final FImage i : data.getInstances(e)) {
				list.add(IndependentPair.pair(e, i));
			}
		}

		train(list);
	}

	@Override
	public void train(List<? extends IndependentPair<?, FImage>> data) {
		width = data.get(0).secondObject().width;
		height = data.get(0).secondObject().height;

		final Map<Object, List<double[]>> mapData = new HashMap<Object, List<double[]>>();
		final List<double[]> listData = new ArrayList<double[]>();
		for (final IndependentPair<?, FImage> item : data) {
			List<double[]> fvs = mapData.get(item.firstObject());
			if (fvs == null)
				mapData.put(item.firstObject(), fvs = new ArrayList<double[]>());

			final double[] fv = FImage2DoubleFV.INSTANCE.extractFeature(item.getSecondObject()).values;
			fvs.add(fv);
			listData.add(fv);
		}

		final PrincipalComponentAnalysis pca = new ThinSvdPrincipalComponentAnalysis(numComponents);
		pca.learnBasis(listData);

		final List<double[][]> ldaData = new ArrayList<double[][]>(mapData.size());
		for (final Entry<?, List<double[]>> e : mapData.entrySet()) {
			final List<double[]> vecs = e.getValue();
			final double[][] classData = new double[vecs.size()][];

			for (int i = 0; i < classData.length; i++) {
				classData[i] = pca.project(vecs.get(i));
			}

			ldaData.add(classData);
		}

		final LinearDiscriminantAnalysis lda = new LinearDiscriminantAnalysis(numComponents);
		lda.learnBasis(ldaData);

		basis = pca.getBasis().times(lda.getBasis());
		mean = pca.getMean();
	}

	private double[] project(double[] vector) {
		final Matrix vec = new Matrix(1, vector.length);
		final double[][] vecarr = vec.getArray();

		for (int i = 0; i < vector.length; i++)
			vecarr[0][i] = vector[i] - mean[i];

		return vec.times(basis).getColumnPackedCopy();
	}

	@Override
	public DoubleFV extractFeature(FImage object) {
		return new DoubleFV(project(FImage2DoubleFV.INSTANCE.extractFeature(object).values));
	}

	/**
	 * Get a specific basis vector as a double array. The returned array
	 * contains a copy of the data.
	 * 
	 * @param index
	 *            the index of the vector
	 * 
	 * @return the eigenvector
	 */
	public double[] getBasisVector(int index) {
		final double[] pc = new double[basis.getRowDimension()];
		final double[][] data = basis.getArray();

		for (int r = 0; r < pc.length; r++)
			pc[r] = data[r][index];

		return pc;
	}

	/**
	 * Draw an eigenvector as an image
	 * 
	 * @param num
	 *            the index of the eigenvector to draw.
	 * @return an image showing the eigenvector.
	 */
	public FImage visualise(int num) {
		return new FImage(ArrayUtils.reshapeFloat(getBasisVector(num), width, height));
	}
}
