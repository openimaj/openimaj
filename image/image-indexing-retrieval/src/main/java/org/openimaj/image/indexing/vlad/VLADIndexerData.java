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
package org.openimaj.image.indexing.vlad;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.FloatLocalFeatureAdaptor;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocalFeatureExtractor;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.pq.FloatProductQuantiser;
import org.openimaj.knn.pq.IncrementalFloatADCNearestNeighbours;
import org.openimaj.ml.pca.FeatureVectorPCA;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.function.Function;

/**
 * Class representing the data required to build a VLAD + PCA +
 * product-quantisation based image index.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class VLADIndexerData {
	private VLAD<float[]> vlad;
	private FeatureVectorPCA pca;
	private FloatProductQuantiser pq;
	private LocalFeatureExtractor<LocalFeature<?, ?>, MBFImage> extractor;
	private Function<List<? extends LocalFeature<?, ?>>, List<FloatLocalFeatureAdaptor<?>>> postProcess;

	/**
	 * Construct with the given data
	 * 
	 * @param vlad
	 *            the VLAD extractor
	 * @param pca
	 *            the PCA basis
	 * @param pq
	 *            the product quantiser
	 * @param extractor
	 *            the raw local feature extractor
	 * @param postProcess
	 *            the process to apply to the raw features before VLAD
	 *            aggregation
	 */
	public VLADIndexerData(VLAD<float[]> vlad, FeatureVectorPCA pca, FloatProductQuantiser pq,
			LocalFeatureExtractor<LocalFeature<?, ?>, MBFImage> extractor,
			Function<List<? extends LocalFeature<?, ?>>, List<FloatLocalFeatureAdaptor<?>>> postProcess)
	{
		this.vlad = vlad;
		this.pca = pca;
		this.pq = pq;
		this.extractor = extractor;
		this.postProcess = postProcess;
	}

	/**
	 * Extract the PCA-projected VLAD feature from the given raw local features.
	 * The local features will be post-processed before being aggregated using
	 * {@link VLAD} and projected by the PCA basis.
	 * 
	 * @param features
	 *            the raw local features
	 * @return the pca-vlad aggregated representation of the image
	 */
	public float[] extractPcaVlad(List<? extends LocalFeature<?, ?>> features) {
		final MultidimensionalFloatFV keys = vlad.aggregate(postProcess.apply(features));

		if (keys == null)
			return null;

		final DoubleFV subspaceVector = pca.project(keys).normaliseFV(2);
		return ArrayUtils.convertToFloat(subspaceVector.values);
	}

	/**
	 * Extract the PCA-projected VLAD feature from the given image. The local
	 * features will be post-processed before being aggregated using
	 * {@link VLAD} and projected by the PCA basis.
	 * 
	 * @param image
	 *            the image to extract from
	 * @return the pca-vlad aggregated representation of the image
	 */
	public float[] extractPcaVlad(MBFImage image) {
		return extractPcaVlad(extractor.extractFeature(image));
	}

	/**
	 * Extract the product-quantised PCA-projected VLAD feature from the given
	 * raw local features. The local features will be post-processed before
	 * being aggregated using {@link VLAD} and projected by the PCA basis.
	 * 
	 * @param features
	 *            the raw local features
	 * @return the product-quantised pca-vlad aggregated representation of the
	 *         image
	 */
	public byte[] extractPQPcaVlad(List<? extends LocalFeature<?, ?>> features) {
		final MultidimensionalFloatFV keys = vlad.aggregate(postProcess.apply(features));

		if (keys == null)
			return null;

		final DoubleFV subspaceVector = pca.project(keys).normaliseFV(2);
		return pq.quantise(ArrayUtils.convertToFloat(subspaceVector.values));
	}

	/**
	 * Extract the product-quantisedPCA-projected VLAD feature from the given
	 * image. The local features will be post-processed before being aggregated
	 * using {@link VLAD} and projected by the PCA basis.
	 * 
	 * @param image
	 *            the image to extract from
	 * @return the product-quantised pca-vlad aggregated representation of the
	 *         image
	 */
	public byte[] extractPQPcaVlad(MBFImage image) {
		return extractPQPcaVlad(extractor.extractFeature(image));
	}

	/**
	 * Get the product quantiser
	 * 
	 * @return get the product quantiser
	 */
	public FloatProductQuantiser getProductQuantiser() {
		return pq;
	}

	/**
	 * Create an {@link IncrementalFloatADCNearestNeighbours} pre-prepared to
	 * index data
	 * 
	 * @return a new {@link IncrementalFloatADCNearestNeighbours}
	 */
	public IncrementalFloatADCNearestNeighbours createIncrementalIndex() {
		return new IncrementalFloatADCNearestNeighbours(pq, pca.getMean().length);
	}

	/**
	 * Index the given features into the given nearest neighbours object by
	 * converting them to the PCA-VLAD representation and then
	 * product-quantising.
	 * 
	 * @param features
	 *            the features to index
	 * @param nn
	 *            the nearest neighbours object
	 * @return the index at which the features were added in the nearest
	 *         neighbours object
	 */
	public int index(List<? extends LocalFeature<?, ?>> features, IncrementalFloatADCNearestNeighbours nn) {
		return nn.add(extractPcaVlad(features));
	}

	/**
	 * Index the given image into the given nearest neighbours object by
	 * extracting the PCA-VLAD representation and then product-quantising.
	 * 
	 * @param image
	 *            the image to index
	 * @param nn
	 *            the nearest neighbours object
	 * @return the index at which the features were added in the nearest
	 *         neighbours object
	 */
	public int index(MBFImage image, IncrementalFloatADCNearestNeighbours nn) {
		return nn.add(extractPcaVlad(image));
	}

	/**
	 * Write this {@link VLADIndexerData} object to the given file. The file can
	 * be re-read using the {@link #read(File)} method.
	 * 
	 * @param file
	 *            the file to write to
	 * @throws IOException
	 *             if an error occurs
	 */
	public void write(File file) throws IOException {
		IOUtils.writeToFile(this, file);
	}

	/**
	 * Write this {@link VLADIndexerData} object to the given stream. The
	 * {@link #read(InputStream)} can read from a stream to reconstruct the
	 * {@link VLADIndexerData}.
	 * 
	 * @param os
	 *            the stream
	 * @throws IOException
	 *             if an error occurs
	 */
	public void write(OutputStream os) throws IOException {
		IOUtils.write(this, new DataOutputStream(os));
	}

	/**
	 * Read a {@link VLADIndexerData} object to the given file created with the
	 * {@link #write(File)} method.
	 * 
	 * @param file
	 *            the file to read from
	 * @return the newly read {@link VLADIndexerData} object.
	 * @throws IOException
	 *             if an error occurs
	 */
	public static VLADIndexerData read(File file) throws IOException {
		return IOUtils.readFromFile(file);
	}

	/**
	 * Read a {@link VLADIndexerData} object to the given stream created with
	 * the {@link #write(OutputStream)} method.
	 * 
	 * @param is
	 *            the stream to read from
	 * @return the newly read {@link VLADIndexerData} object.
	 * @throws IOException
	 *             if an error occurs
	 */
	public static VLADIndexerData read(InputStream is) throws IOException {
		return IOUtils.read(new DataInputStream(is));
	}

	/**
	 * Get the {@link VLAD} aggregator instance
	 * 
	 * @return the {@link VLAD} aggregator
	 */
	public VLAD<float[]> getVLAD() {
		return vlad;
	}

	/**
	 * Get the dimensionality of the float vectors extracted from the pca-vlad
	 * process.
	 * 
	 * @return the dimensionality.
	 */
	public int numDimensions() {
		return this.pca.getEigenValues().length;
	}

	/**
	 * @return the pca
	 */
	public FeatureVectorPCA getPCA() {
		return pca;
	}

	/**
	 * @return the extractor
	 */
	public LocalFeatureExtractor<LocalFeature<?, ?>, MBFImage> getExtractor() {
		return extractor;
	}

	/**
	 * @return the postProcess
	 */
	public Function<List<? extends LocalFeature<?, ?>>, List<FloatLocalFeatureAdaptor<?>>> getPostProcess() {
		return postProcess;
	}

}
