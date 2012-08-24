package org.openimaj.demos.sandbox;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.local.filter.ByteEntropyFilter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.lsh.functions.DoubleArrayHashFunction;
import org.openimaj.lsh.functions.DoubleArrayPStableGaussianFactory;
import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.pair.IntObjectPair;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;

import cern.jet.random.engine.MersenneTwister;

public class HashingTest {
	final int nhashes = 32;
	int nInts = 4;
	DoubleArrayHashFunction[][] hashes = new DoubleArrayHashFunction[nInts][nhashes];

	TIntObjectHashMap<Set<String>>[] database = new TIntObjectHashMap[nInts];

	public HashingTest() {
		final MersenneTwister rng = new MersenneTwister();
		final DoubleArrayPStableGaussianFactory generator = new DoubleArrayPStableGaussianFactory(128, rng, 8);

		for (int i = 0; i < nInts; i++) {
			database[i] = new TIntObjectHashMap<Set<String>>();
			for (int j = 0; j < nhashes; j++)
				hashes[i][j] = generator.create();
		}
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

	int[] createSketch(byte[] fv) {
		final double[] dfv = logScale(fv, 0.001F);
		final int[] hash = new int[nInts];

		for (int i = 0; i < nInts; i++) {
			for (int j = 0; j < nhashes; j++) {
				final int hc = Math.abs(hashes[i][j].computeHashCode(dfv) % 2);
				hash[i] = (hash[i] << 1) | hc;
			}
		}
		return hash;
	}

	private void indexImage(File imageFile) throws IOException {
		final DoGSIFTEngine engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
		final ByteEntropyFilter filter = new ByteEntropyFilter();

		final FImage image = ResizeProcessor.resizeMax(ImageUtilities.readF(imageFile), 300);

		List<Keypoint> features = engine.findFeatures(image);
		features = FilterUtils.filter(features, filter);

		for (final Keypoint k : features) {
			final int[] sketch = createSketch(k.ivec);

			for (int i = 0; i < nInts; i++) {
				final int sk = sketch[i];
				synchronized (database) {
					Set<String> s = database[i].get(sk);
					if (s == null)
						database[i].put(sk, s = new HashSet<String>());
					s.add(imageFile.toString());
				}
			}
		}
	}

	List<IntObjectPair<String>> search(File imageFile) throws IOException {
		final TObjectIntHashMap<String> results = new TObjectIntHashMap<String>();

		final DoGSIFTEngine engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
		final ByteEntropyFilter filter = new ByteEntropyFilter();

		final FImage image = ResizeProcessor.resizeMax(ImageUtilities.readF(imageFile), 300);

		List<Keypoint> features = engine.findFeatures(image);
		features = FilterUtils.filter(features, filter);

		for (final Keypoint k : features) {
			final int[] sketch = createSketch(k.ivec);

			for (int i = 0; i < nInts; i++) {
				final int sk = sketch[i];

				final Set<String> r = database[i].get(sk);
				if (r != null) {
					for (final String file : r)
						results.adjustOrPutValue(file, 1, 1);
				}
			}
		}

		final List<IntObjectPair<String>> list = new ArrayList<IntObjectPair<String>>();

		for (final String k : results.keys(new String[1])) {
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

		Parallel.For(0, 1000, 1, new Operation<Integer>() {
			@Override
			public void perform(Integer i) {
				try {
					test.indexImage(new File(String.format("/Users/jon/Data/ukbench/full/ukbench0%04d.jpg", i)));
				} catch (final IOException e) {
				}
			}
		});
		System.out.println("done");

		for (int i = 0; i < 1; i++) {
			System.out.println("Query : " + i);
			final List<IntObjectPair<String>> res = test.search(new File(String.format(
					"/Users/jon/Data/ukbench/full/ukbench0%04d.jpg", i)));

			System.out.println(res.size());

			for (final IntObjectPair<String> k : res) {
				System.out.println(k.second + " " + k.first);
			}
			System.out.println();
		}
	}
}
