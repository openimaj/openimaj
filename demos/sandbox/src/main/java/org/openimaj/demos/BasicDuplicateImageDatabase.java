package org.openimaj.demos;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.local.filter.ByteEntropyFilter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.lsh.functions.DoubleGaussianFactory;
import org.openimaj.lsh.sketch.IntLSHSketcher;
import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.modifier.LSBModifier;
import org.openimaj.util.pair.ObjectIntPair;

import cern.jet.random.engine.MersenneTwister;

/**
 * <p>
 * An in-memory image duplicate database. Stores hashes of features as the basis
 * of determining duplicates.
 * </p>
 * <p>
 * This is a basic in-memory implementation of the approach described by Dong,
 * Wang and Li for high-confidence duplicate detection. It does not currently
 * implement the query expansion phase based on a graph cut of the duplicity
 * graph, but instead relies directly on a simple thresholded count of
 * collisions.
 * </p>
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            type of metadata being stored
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		author = { "Wei Dong", "Zhe Wang", "Kai Li" },
		title = "High-Confidence Near-Duplicate Image Detection",
		type = ReferenceType.Inproceedings,
		year = "2012",
		booktitle = "ACM International Conference on Multimedia Retrieval",
		customData = { "location", "Hong Kong, China" })
public class BasicDuplicateImageDatabase<T> {
	private final int ndims = 128;
	private final double w = 6.0;
	private final int nbits = 128;
	private final float LOG_BASE = 0.001f;
	private final int maxImageSize = 150;
	private final int seed = 0;
	private int minScore = 10;

	private DoGSIFTEngine engine;
	final ByteEntropyFilter filter;
	private IntLSHSketcher<double[]> sketcher;
	protected List<TIntObjectHashMap<Set<T>>> database;

	public BasicDuplicateImageDatabase() {
		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);

		filter = new ByteEntropyFilter();

		final MersenneTwister rng = new MersenneTwister(seed);
		final DoubleGaussianFactory gauss = new DoubleGaussianFactory(this.ndims, rng, w);
		final HashFunctionFactory<double[]> factory = new HashFunctionFactory<double[]>() {
			@Override
			public HashFunction<double[]> create() {
				return new LSBModifier<double[]>(gauss.create());
			}
		};

		sketcher = new IntLSHSketcher<double[]>(factory, nbits);
		database = new ArrayList<TIntObjectHashMap<Set<T>>>(sketcher.arrayLength());

		for (int i = 0; i < sketcher.arrayLength(); i++)
			database.add(new TIntObjectHashMap<Set<T>>());
	}

	/**
	 * Perform log-scaling element-wise on a feature
	 *
	 * @param v
	 *            the feature
	 * @return the scaled feature
	 */
	private double[] logScale(double[] v) {
		final double[] dfv = new double[v.length];
		final double s = -Math.log(LOG_BASE);

		for (int i = 0; i < v.length; i++) {
			double d = (v[i] + 128.0) / 256.0;

			if (d < LOG_BASE)
				d = LOG_BASE;
			d = (Math.log(d) + s) / s;
			if (d > 1.0)
				d = 1.0;

			dfv[i] = d;
		}
		return dfv;
	}

	/**
	 * Extract hashed features from an image
	 *
	 * @param image
	 *            the image to index
	 * @return the features
	 */
	public int[][] extractFeatures(FImage image) {
		image = ResizeProcessor.resizeMax(image, maxImageSize);

		final List<Keypoint> features = engine.findFeatures(image);
		final List<Keypoint> filtered = FilterUtils.filter(features, filter);

		final int[][] sketches = new int[filtered.size()][];
		for (int i = 0; i < filtered.size(); i++) {
			final Keypoint k = filtered.get(i);
			final double[] fv = logScale(k.getFeatureVector().asDoubleVector());
			sketches[i] = sketcher.createSketch(fv);
		}

		return sketches;
	}

	/**
	 * Extract hashed features from an image
	 *
	 * @param image
	 *            the image to index
	 * @return the features
	 */
	public int[][] extractFeatures(MBFImage image) {
		return extractFeatures(Transforms.calculateIntensityNTSC(image));
	}

	/**
	 * Index a new image based on its features
	 *
	 * @param features
	 *            the image to index
	 * @param metadata
	 *            the metadata for the image
	 */
	public synchronized void indexImage(int[][] features, T metadata) {
		for (final int[] sketch : features) {
			for (int i = 0; i < sketch.length; i++) {
				final int sk = sketch[i];
				Set<T> s = database.get(i).get(sk);
				if (s == null)
					database.get(i).put(sk, s = new HashSet<T>());

				s.add(metadata);
			}
		}
	}

	/**
	 * Search for a given image (represented by its features) in the database
	 *
	 * @param features
	 *            the features
	 * @return the list of matching images and their scores (bigger == more
	 *         chance of match)
	 */
	public List<ObjectIntPair<T>> search(int[][] features) {
		final TObjectIntHashMap<T> counter = new TObjectIntHashMap<>();

		for (final int[] sketch : features) {
			for (int i = 0; i < sketch.length; i++) {
				final int sk = sketch[i];
				final Set<T> s = database.get(i).get(sk);
				if (s != null) {
					for (final T key : s) {
						counter.adjustOrPutValue(key, 1, 1);
					}
				}
			}
		}

		final List<ObjectIntPair<T>> result = new ArrayList<>();

		counter.forEachEntry(new TObjectIntProcedure<T>() {
			@Override
			public boolean execute(T a, int b) {
				if (b > minScore)
					result.add(ObjectIntPair.pair(a, b));
				return false;
			}
		});

		Collections.sort(result, new Comparator<ObjectIntPair<T>>() {
			@Override
			public int compare(ObjectIntPair<T> o1, ObjectIntPair<T> o2) {
				return Integer.compare(o2.second, o1.second);
			}
		});

		return result;
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		// Construct DB
		final BasicDuplicateImageDatabase<String> database = new BasicDuplicateImageDatabase<>();

		// add some images
		final int[][] f1 = database.extractFeatures(ImageUtilities.readF(new URL(
				"http://comp3204.ecs.soton.ac.uk/cw/dog.jpg")));
		database.indexImage(f1, "dog");

		final int[][] f2 = database.extractFeatures(ImageUtilities.readF(new URL(
				"http://comp3204.ecs.soton.ac.uk/cw/cat.jpg")));
		database.indexImage(f2, "cat");

		// test to see that the images can be found
		System.out.println(database.search(f1).get(0).first); // should be dog
		System.out.println(database.search(f2).get(0).first); // should be cat

		// test with an unindexed image
		final int[][] f3 = database.extractFeatures(ImageUtilities.readF(new URL(
				"http://comp3204.ecs.soton.ac.uk/cw/hybrid_image.jpg")));
		System.out.println(database.search(f3)); // should be an empty list
	}
}
