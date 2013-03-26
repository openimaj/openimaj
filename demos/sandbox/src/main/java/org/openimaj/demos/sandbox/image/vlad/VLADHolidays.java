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
package org.openimaj.demos.sandbox.image.vlad;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openimaj.data.identity.Identifiable;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEvaluator;
import org.openimaj.experiment.evaluation.retrieval.analysers.IREvalAnalyser;
import org.openimaj.experiment.evaluation.retrieval.analysers.IREvalResult;
import org.openimaj.feature.FVComparator;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.SparseFloatFV;
import org.openimaj.feature.SparseFloatFVComparison;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.FloatLocalFeatureAdaptor;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.image.feature.local.keypoints.SIFTGeoKeypoint;
import org.openimaj.image.feature.local.keypoints.SIFTGeoKeypoint.SIFTGeoLocation;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.hard.ExactFloatAssigner;
import org.openimaj.util.pair.DoubleObjectPair;
import org.openimaj.util.queue.BoundedPriorityQueue;

public class VLADHolidays {
	static class Document implements Identifiable {
		String name;

		public Document(String name) {
			this.name = name;
		}

		@Override
		public String getID() {
			return name;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Document)
				return name.equals(((Document) obj).name);
			return false;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends FeatureVector> void main(String[] args) throws IOException {
		final Map<Document, T> data;
		final FVComparator<T> comp;

		final boolean vladMode = false;
		if (vladMode) {
			data = (Map<Document, T>) getVLADFeatures();
			comp = (FVComparator<T>) FloatFVComparison.EUCLIDEAN;
		} else {
			data = (Map<Document, T>) getBoVWFeatures();
			comp = (FVComparator<T>) SparseFloatFVComparison.EUCLIDEAN;
		}

		// Perform experiment
		final Map<Integer, Set<Document>> groundTruth = new HashMap<Integer, Set<Document>>();
		final Map<Integer, List<Document>> ret = new HashMap<Integer, List<Document>>();
		for (final Document k : data.keySet()) {
			final int i = Integer.parseInt(k.name.replace(".siftgeo", ""));
			final int q = i - (i % 100);

			if (q != i) {
				if (!groundTruth.containsKey(q))
					groundTruth.put(q, new HashSet<Document>());

				groundTruth.get(q).add(k);
			} else {
				final List<DoubleObjectPair<Document>> res = search(k, data, comp, 1000);
				ret.put(q, DoubleObjectPair.getSecond(res));
			}
		}

		final RetrievalEvaluator<IREvalResult, Document, Integer> eval = new RetrievalEvaluator<IREvalResult, VLADHolidays.Document, Integer>(
				ret, groundTruth, new IREvalAnalyser<Integer, Document>());

		final Map<Integer, List<Document>> evalRes = eval.evaluate();
		final IREvalResult finalRes = eval.analyse(evalRes);

		System.out.println(finalRes.getSummaryReport());
	}

	/**
	 * Load the raw features and create VLAD representations.
	 * 
	 * @return
	 * @throws IOException
	 */
	private static Map<Document, FloatFV> getVLADFeatures() throws IOException {
		final Map<Document, FloatFV> vladData = new HashMap<Document, FloatFV>();

		final FloatCentroidsResult centroids = readFvecs(new File(
				"/Users/jsh2/Downloads/cvpr2010/data/clust_k64.fvecs"));

		final ExactFloatAssigner assigner = new ExactFloatAssigner(centroids);

		final VLAD<float[]> vlad = new VLAD<float[]>(assigner, centroids, true);

		for (final File f : new File("/Users/jsh2/Downloads/siftgeo/").listFiles()) {
			System.out.println("Loading " + f.getName());
			final LocalFeatureList<SIFTGeoKeypoint> keys = SIFTGeoKeypoint.read(f);

			final List<FloatLocalFeatureAdaptor<SIFTGeoLocation>> fkeys = FloatLocalFeatureAdaptor.wrap(keys);

			final MultidimensionalFloatFV fv = vlad.aggregate(fkeys);

			vladData.put(new Document(f.getName()), fv);
		}

		return vladData;
	}

	/**
	 * Load the raw features and create BoVW representations.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected static Map<Document, SparseFloatFV> getBoVWFeatures() throws IOException {
		final Map<Document, SparseFloatFV> vladData = new HashMap<Document, SparseFloatFV>();

		final FloatCentroidsResult centroids = readFvecs(new File(
				"/Users/jsh2/Downloads/clust/clust_flickr60_k20000.fvecs"));
		// "/Users/jsh2/Downloads/cvpr2010/data/clust_k64.fvecs"));

		final ExactFloatAssigner assigner = new ExactFloatAssigner(centroids);
		// final ApproximateFloatEuclideanAssigner assigner = new
		// ApproximateFloatEuclideanAssigner(centroids);

		final BagOfVisualWords<float[]> bovw = new BagOfVisualWords<float[]>(assigner);

		int N = 0;
		final float[] n = new float[assigner.size()];

		for (final File f : new File("/Users/jsh2/Downloads/siftgeo/").listFiles()) {
			N++;
			System.out.println("Loading " + f.getName());
			final LocalFeatureList<SIFTGeoKeypoint> keys = SIFTGeoKeypoint.read(f);

			final List<FloatLocalFeatureAdaptor<SIFTGeoLocation>> fkeys = FloatLocalFeatureAdaptor.wrap(keys);

			final SparseIntFV fv = bovw.aggregate(fkeys);

			final SparseFloatFV fv2 = new SparseFloatFV(fv.length());
			float sum = 0;
			for (final org.openimaj.util.array.SparseIntArray.Entry i : fv.values.entries()) {
				sum += (i.value * i.value);
				fv2.values.set(i.index, i.value);
				n[i.index] += i.value;
			}
			sum = (float) Math.sqrt(sum);
			for (final org.openimaj.util.array.SparseFloatArray.Entry i : fv2.values.entries()) {
				fv2.values.set(i.index, i.value / sum);
			}

			vladData.put(new Document(f.getName()), fv2);
		}

		for (int i = 0; i < n.length; i++) {
			n[i] = (float) Math.log(N / n[i]);
		}

		for (final SparseFloatFV fv : vladData.values()) {
			for (final org.openimaj.util.array.SparseFloatArray.Entry i : fv.values.entries()) {
				fv.values.set(i.index, i.value * n[i.index]);
			}
		}

		return vladData;
	}

	/**
	 * The search function. This computes the distance between the query and
	 * every other document, and stores the best results. The query document is
	 * omitted from the results list.
	 * 
	 * @param queryDoc
	 *            the query identifier (assumed to be in the set of docs to be
	 *            searched)
	 * @param features
	 *            the features to search
	 * @param comp
	 *            the comparator to use
	 * @param limit
	 *            the number of top-matching docs to retain
	 * @return the ranked list of results
	 */
	private static <T extends FeatureVector> List<DoubleObjectPair<Document>> search(Document queryDoc,
			Map<Document, T> features, FVComparator<T> comp, int limit)
	{
		final BoundedPriorityQueue<DoubleObjectPair<Document>> queue = new BoundedPriorityQueue<DoubleObjectPair<Document>>(
				limit,
				new Comparator<DoubleObjectPair<Document>>() {

					@Override
					public int compare(DoubleObjectPair<Document> o1, DoubleObjectPair<Document> o2) {
						return Double.compare(o1.first, o2.first);
					}
				});

		final T query = features.get(queryDoc);
		if (query != null) {
			for (final Entry<Document, T> e : features.entrySet()) {
				if (e.getValue() == query || e.getValue() == null)
					continue;

				final T that = e.getValue();
				final double distance = comp.compare(query, that);

				queue.add(new DoubleObjectPair<Document>(distance, e.getKey()));
			}
		}

		return queue.toOrderedListDestructive();
	}

	/**
	 * Function to read an fvecs file. Because the local features are stored as
	 * signed bytes (-127..128) we offset the elements by -128 to make the
	 * ranges compatible.
	 * 
	 * @param file
	 *            the .fvecs file to read
	 * @return the centroids from the .fvecs file
	 * @throws IOException
	 */
	private static FloatCentroidsResult readFvecs(File file) throws IOException {
		final DataInputStream dis = new DataInputStream(new FileInputStream(file));

		final List<float[]> data = new ArrayList<float[]>();
		final byte[] tmpArray = new byte[516];
		final ByteBuffer buffer = ByteBuffer.wrap(tmpArray);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		while (true) {
			try {
				dis.readFully(tmpArray);
				buffer.rewind();

				if (buffer.getInt() != 128) {
					throw new IOException("Unexpected length");
				}

				final float[] array = new float[128];
				for (int i = 0; i < 128; i++) {
					array[i] = buffer.getFloat() - 128;
				}

				data.add(array);
			} catch (final EOFException e) {
				final FloatCentroidsResult f = new FloatCentroidsResult();
				f.centroids = data.toArray(new float[data.size()][]);
				dis.close();
				return f;
			}
		}
	}
}
