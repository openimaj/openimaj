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
package org.openimaj.demos.sandbox;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.openimaj.feature.local.filter.ByteEntropyFilter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.lsh.functions.DoubleGaussianFactory;
import org.openimaj.lsh.sketch.IntLSHSketcher;
import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.function.Operation;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.modifier.LSBModifier;
import org.openimaj.util.pair.IntObjectPair;
import org.openimaj.util.parallel.Parallel;

import cern.jet.random.engine.MersenneTwister;

public class HashingTest {
	final int ndims = 128;
	final double w = 6.0;
	final int nbits = 128;
	final float LOG_BASE = 0.001f;

	IntLSHSketcher<double[]> sketcher;
	List<TIntObjectHashMap<Set<String>>> database;
	TObjectIntHashMap<String> counts = new TObjectIntHashMap<String>();

	public HashingTest() {
		final MersenneTwister rng = new MersenneTwister();

		final DoubleGaussianFactory gauss = new DoubleGaussianFactory(ndims, rng, w);
		final HashFunctionFactory<double[]> factory = new HashFunctionFactory<double[]>() {
			@Override
			public HashFunction<double[]> create() {
				return new LSBModifier<double[]>(gauss.create());
			}
		};

		sketcher = new IntLSHSketcher<double[]>(factory, nbits);
		database = new ArrayList<TIntObjectHashMap<Set<String>>>(sketcher.arrayLength());

		for (int i = 0; i < sketcher.arrayLength(); i++)
			database.add(new TIntObjectHashMap<Set<String>>());
	}

	static double[] logScale(byte[] v, float l) {
		final double[] dfv = new double[v.length];
		final double s = -Math.log(l);

		for (int i = 0; i < v.length; i++) {
			double d = (v[i] + 128.0) / 256.0;

			if (d < l)
				d = l;
			d = (Math.log(d) + s) / s;
			if (d > 1.0)
				d = 1.0;

			dfv[i] = d;
		}
		return dfv;
	}

	private void indexImage(File imageFile) throws IOException {
		final List<Keypoint> features = extractFeatures(imageFile);
		for (final Keypoint k : features) {
			final int[] sketch = sketcher.createSketch(logScale(k.ivec, LOG_BASE));

			for (int i = 0; i < sketch.length; i++) {
				final int sk = sketch[i];
				synchronized (database) {
					Set<String> s = database.get(i).get(sk);
					if (s == null)
						database.get(i).put(sk, s = new HashSet<String>());
					s.add(imageFile.toString());
				}
			}
		}

		counts.put(imageFile.toString(), features.size());
	}

	List<Keypoint> extractFeatures(File imageFile) throws IOException {
		final DoGSIFTEngine engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
		final ByteEntropyFilter filter = new ByteEntropyFilter();

		final FImage image = ResizeProcessor.resizeMax(ImageUtilities.readF(imageFile), 150);

		final List<Keypoint> features = engine.findFeatures(image);
		return FilterUtils.filter(features, filter);
	}

	List<IntObjectPair<String>> search(File imageFile) throws IOException {
		final TObjectIntHashMap<String> results = new TObjectIntHashMap<String>();

		for (final Keypoint k : extractFeatures(imageFile)) {
			final int[] sketch = sketcher.createSketch(logScale(k.ivec, LOG_BASE));

			final TObjectIntHashMap<String> featResults = new TObjectIntHashMap<String>();

			for (int i = 0; i < sketch.length; i++) {
				final int sk = sketch[i];

				final Set<String> r = database.get(i).get(sk);
				if (r != null) {
					for (final String file : r) {
						featResults.adjustOrPutValue(file, 1, 1);
						// results.adjustOrPutValue(file, 1, 1);
					}
				}
			}

			featResults.forEachEntry(new TObjectIntProcedure<String>() {
				@Override
				public boolean execute(String a, int b) {
					if (b >= 1)
						results.adjustOrPutValue(a, b, b);
					return true;
				}
			});
		}

		final List<IntObjectPair<String>> list = new ArrayList<IntObjectPair<String>>();

		for (final String k : results.keys(new String[results.size()])) {
			list.add(new IntObjectPair<String>(results.get(k), k));
		}

		Collections.sort(list, new Comparator<IntObjectPair<String>>() {
			@Override
			public int compare(IntObjectPair<String> paramT1, IntObjectPair<String> paramT2) {
				final int v1 = paramT1.first;
				final int v2 = paramT2.first;

				if (v1 == v2)
					return 0;
				return v1 < v2 ? 1 : 0;
			}
		});

		return list;
	}

	public static void main(String[] args) throws IOException {
		final HashingTest test = new HashingTest();
		final int nImages = 10200;

		Parallel.forIndex(0, nImages, 1, new Operation<Integer>() {
			volatile int count = 0;

			@Override
			public void perform(Integer i) {
				try {
					final File file = new File(String.format("/Users/jsh2/Data/ukbench/full/ukbench0%04d.jpg", i));
					System.out.println(file);
					test.indexImage(file);
					count++;
					System.out.println(count);
				} catch (final IOException e) {
				}
			}
		});
		System.out.println("done");

		final SimpleWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);

		for (int i = 0; i < nImages; i++) {
			final File filename = new File(String.format("/Users/jsh2/Data/ukbench/full/ukbench0%04d.jpg", i));

			graph.addVertex(filename.toString());
		}

		for (int i = 0; i < nImages; i++) {
			System.out.println("Query : " + i);
			final File filename = new File(String.format("/Users/jsh2/Data/ukbench/full/ukbench0%04d.jpg", i));
			final List<IntObjectPair<String>> res = test.search(filename);

			if (res.size() > 1) {
				for (final IntObjectPair<String> k : res) {
					if (k.second.toString().equals(filename.toString()))
						continue;

					final DefaultWeightedEdge edge = graph.addEdge(filename.toString(), k.second);
					if (edge != null)
						graph.setEdgeWeight(edge, k.first);
				}
			}
		}

		final ConnectivityInspector<String, DefaultWeightedEdge> conn = new ConnectivityInspector<String, DefaultWeightedEdge>(
				graph);
		final List<Set<String>> sets = conn.connectedSets();

		for (final Set<String> s : sets) {
			System.out.println(s);
		}
	}
}
