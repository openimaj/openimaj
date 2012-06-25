package org.openimaj.demos.sandbox;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.local.filter.ByteEntropyFilter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.lsh.functions.DoubleEuclidean;
import org.openimaj.lsh.functions.DoubleHashFunction;
import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;

import cern.colt.Arrays;
import cern.jet.random.engine.MersenneTwister;

public class HashingTest {
	DoubleHashFunction [][] hashes = new DoubleHashFunction[4][32];

	TIntObjectHashMap<Set<String>> [] database = new TIntObjectHashMap[4];

	public HashingTest() {
		DoubleEuclidean generator = new DoubleEuclidean(8.0 / 256.0);
		generator.norm = false;
		MersenneTwister rng = new MersenneTwister();

		for (int i=0; i<4; i++) {
			database[i] = new TIntObjectHashMap<Set<String>>();
			for (int j=0; j<32; j++)
				hashes[i][j] = generator.create(128, rng);
		}
	}

	private FImage resizeMax(FImage in, int maxDim) {
		final int width = in.width;
		final int height = in.height;

		int newWidth, newheight;
		if (width < maxDim && height < maxDim) { 
			return in;
		} else if(width < height) {
			newheight = maxDim;
			float resizeRatio = ((float)maxDim/(float)height);
			newWidth = (int) (width * resizeRatio);
		} else {
			newWidth = maxDim;
			float resizeRatio = ((float)maxDim/(float)width);
			newheight = (int) (height * resizeRatio);
		}

		return ResizeProcessor.resample(in, newWidth, newheight);
	}

	double[] logScale(byte[] v, float l) {
		double [] dfv = new double[v.length];
		double s = -Math.log(l);

		for (int i=0; i<v.length; i++) {
			double d = (v[i] + 128.0) / 256.0;

			if (d < l) d = l;
			d = (Math.log(d) + s) / s;
			if (d > 1.0) d = 1.0;

			dfv[i] = d;
		}
		return dfv;
	}


	private int [] createSketch(byte[] fv) {
		double [] dfv = logScale(fv,  0.001F);
		int [] hash = new int[4];

		for (int i=0; i<4; i++) { 
			for (int j=0; j<32; j++) {
				hash[i] = (hash[i] << 1) | hashes[i][j].computeHashCode(dfv, 1);
			}
		}
		return hash;
	}

	private void indexImage(File imageFile) throws IOException {
		DoGSIFTEngine engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
		ByteEntropyFilter filter = new ByteEntropyFilter();

		FImage image = resizeMax(ImageUtilities.readF(imageFile), 300);

		List<Keypoint> features = engine.findFeatures(image);
		features = FilterUtils.filter(features, filter);

		for (Keypoint k : features) {
			int[] sketch = createSketch(k.ivec);

			for (int i=0; i<4; i++) {
				int sk = sketch[i];
				synchronized(database) {
					Set<String> s = database[i].get(sk);
					if (s == null) database[i].put(sk, s = new HashSet<String>());
					s.add(imageFile.toString());
				}
			}
		}
	}

	TObjectIntHashMap<String> search(File imageFile) throws IOException {
		TObjectIntHashMap<String> results = new TObjectIntHashMap<String>();

		DoGSIFTEngine engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
		ByteEntropyFilter filter = new ByteEntropyFilter();

		FImage image = resizeMax(ImageUtilities.readF(imageFile), 300);

		List<Keypoint> features = engine.findFeatures(image);
		features = FilterUtils.filter(features, filter);

		for (Keypoint k : features) {
			int[] sketch = createSketch(k.ivec);

			for (int i=0; i<4; i++) {
				int sk = sketch[i];

				Set<String> r = database[i].get(sk);
				if (r != null) {
					for (String file : r)
						results.adjustOrPutValue(file, 1, 1);
				}
			}
		}
		return results;
	}

	public static void main(String[] args) throws IOException {
		final HashingTest test = new HashingTest();

		Parallel.For(0, 1000, 1, new Operation<Integer>() {
			@Override
			public void perform(Integer i) {
				try {
					test.indexImage(new File(String.format("/Users/jon/Data/ukbench/full/ukbench0%04d.jpg", i)));
				} catch (IOException e) { }
			}});
		System.out.println("done");

		for (int i=0; i<1; i++) {
			System.out.println("Query : " + i);
			TObjectIntHashMap<String> res = test.search(new File(String.format("/Users/jon/Data/ukbench/full/ukbench0%04d.jpg", i)));
			for (String k : res.keys(new String[1])) {
				System.out.println(k + " " + res.get(k));
			}
			System.out.println();
		}
	}
}
