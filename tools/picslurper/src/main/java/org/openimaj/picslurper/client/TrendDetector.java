package org.openimaj.picslurper.client;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.lsh.functions.DoubleGaussianFactory;
import org.openimaj.lsh.sketch.IntLSHSketcher;
import org.openimaj.picslurper.output.WriteableImageOutput;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.modifier.LSBModifier;
import org.openimaj.util.pair.LongObjectPair;

import cern.jet.random.engine.MersenneTwister;

/**
 * A trend detector indexes new images and is able to tell you the n highest
 * trending sets of near duplicate images
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TrendDetector {
	final int ndims = 128;
	final double w = 6.0;
	final int nbits = 128;
	final float LOG_BASE = 0.001f;

	TrendDetectorFeatureExtractor extractor = new SIFTTrendFeatureMode();

	IntLSHSketcher<double[]> sketcher;
	List<TIntObjectHashMap<Set<WriteableImageOutput>>> database;
	Set<LongObjectPair<WriteableImageOutputHashes>> imagesByTime = new TreeSet<LongObjectPair<WriteableImageOutputHashes>>(
			new Comparator<LongObjectPair<WriteableImageOutputHashes>>() {

				@Override
				public int compare(
						LongObjectPair<WriteableImageOutputHashes> o1,
						LongObjectPair<WriteableImageOutputHashes> o2)
				{
					return ((Long) o1.first).compareTo(o2.first);
				}

			});

	/**
	 * instantiate the LSH
	 */
	public TrendDetector() {

		this.setFeatureExtractor(new SIFTTrendFeatureMode());
	}

	static double[] logScale(double[] v, float l) {
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

	/**
	 * @param count
	 * @return the top count list of sets from all hashtable bins
	 */
	public synchronized List<Set<WriteableImageOutput>> trending(int count) {
		final SimpleWeightedGraph<WriteableImageOutput, DefaultWeightedEdge> graph = new SimpleWeightedGraph<WriteableImageOutput, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		for (final TIntObjectHashMap<Set<WriteableImageOutput>> set : this.database) {
			set.forEachEntry(new TIntObjectProcedure<Set<WriteableImageOutput>>() {
				@Override
				public boolean execute(int hashIndex, Set<WriteableImageOutput> itemSet) {
					for (final WriteableImageOutput item : itemSet) {
						if (!graph.containsVertex(item)) {
							graph.addVertex(item);
						}
					}
					final List<WriteableImageOutput> itemList = new ArrayList<WriteableImageOutput>();
					itemList.addAll(itemSet);
					for (int i = 0; i < itemList.size(); i++) {
						final WriteableImageOutput itemA = itemList.get(i);
						for (int j = i + 1; j < itemList.size(); j++) {
							final WriteableImageOutput itemB = itemList.get(j);
							DefaultWeightedEdge edge = graph.getEdge(itemA, itemB);
							if (edge == null) {
								edge = graph.addEdge(itemA, itemB);
								graph.setEdgeWeight(edge, 1);
							}
							else {
								graph.setEdgeWeight(edge, graph.getEdgeWeight(edge) + 1);
							}

						}
					}
					return true;
				}
			});
		}

		final Set<DefaultWeightedEdge> edges = new HashSet<DefaultWeightedEdge>(graph.edgeSet());
		for (final DefaultWeightedEdge e : edges) {
			if (graph.getEdgeWeight(e) < 10)
				graph.removeEdge(e);
		}

		final ConnectivityInspector<WriteableImageOutput, DefaultWeightedEdge> conn = new ConnectivityInspector<WriteableImageOutput, DefaultWeightedEdge>(
				graph);
		final List<Set<WriteableImageOutput>> retList = conn.connectedSets();
		Collections.sort(retList, new Comparator<Set<WriteableImageOutput>>() {

			@Override
			public int compare(Set<WriteableImageOutput> o1, Set<WriteableImageOutput> o2) {
				return -1 * ((Integer) o1.size()).compareTo(o2.size());
			}

		});
		return retList.subList(0, count < retList.size() ? count : retList.size());
	}

	/**
	 * index a new image
	 * 
	 * @param instance
	 * @throws IOException
	 */
	public synchronized void indexImage(WriteableImageOutput instance) throws IOException {
		for (final File imageFile : instance.listImageFiles("/")) {
			WriteableImageOutput iclone = null;
			try {
				iclone = instance.clone();
				iclone.file = imageFile;
			} catch (final CloneNotSupportedException e) {

			}
			final List<? extends FeatureVectorProvider<? extends FeatureVector>> features = extractor
					.extractFeatures(imageFile);
			final WriteableImageOutputHashes imageHashes = new WriteableImageOutputHashes(iclone);

			for (final FeatureVectorProvider<? extends FeatureVector> k : features) {
				double[] fv = k.getFeatureVector().asDoubleVector();
				if (extractor.logScale()) {
					fv = logScale(fv, LOG_BASE);
				}
				final int[] sketch = sketcher.createSketch(fv);
				imageHashes.hashes.add(sketch);

				for (int i = 0; i < sketch.length; i++) {
					final int sk = sketch[i];
					synchronized (database) {
						Set<WriteableImageOutput> s = database.get(i).get(sk);
						if (s == null)
							database.get(i).put(sk, s = new HashSet<WriteableImageOutput>());

						s.add(iclone);

					}
				}
			}
			final long time = System.currentTimeMillis();
			synchronized (this.imagesByTime) {
				this.imagesByTime
						.add(new LongObjectPair<WriteableImageOutputHashes>(
								time, imageHashes));
			}
		}
	}

	public void setFeatureExtractor(TrendDetectorFeatureExtractor fe) {
		this.extractor = fe;
		final MersenneTwister rng = new MersenneTwister();

		final DoubleGaussianFactory gauss = new DoubleGaussianFactory(fe.nDimensions(), rng, w);
		final HashFunctionFactory<double[]> factory = new HashFunctionFactory<double[]>() {
			@Override
			public HashFunction<double[]> create() {
				return new LSBModifier<double[]>(gauss.create());
			}
		};

		sketcher = new IntLSHSketcher<double[]>(factory, nbits);
		database = new ArrayList<TIntObjectHashMap<Set<WriteableImageOutput>>>(
				sketcher.arrayLength());

		for (int i = 0; i < sketcher.arrayLength(); i++)
			database.add(new TIntObjectHashMap<Set<WriteableImageOutput>>());

	}

}
