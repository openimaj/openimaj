package org.openimaj.picslurper.client;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
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
import org.openimaj.feature.local.filter.ByteEntropyFilter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.io.IOUtils;
import org.openimaj.lsh.functions.DoubleGaussianFactory;
import org.openimaj.lsh.sketch.IntLSHSketcher;
import org.openimaj.picslurper.client.ZMQStreamingPicslurperClusterer.WriteableImageOutputHashes;
import org.openimaj.picslurper.output.WriteableImageOutput;
import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.modifier.LSBModifier;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.LongObjectPair;
import org.openimaj.util.queue.BoundedPriorityQueue;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import cern.jet.random.engine.MersenneTwister;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ZMQStreamingPicslurperClusterer {
	private Socket subscriber;
	private DoGSIFTEngine engine;
	final int ndims = 128;
	final double w = 6.0;
	final int nbits = 128;
	final float LOG_BASE = 0.001f;

	class WriteableImageOutputHashes {
		WriteableImageOutput image;
		List<int[]> hashes;

		public WriteableImageOutputHashes(WriteableImageOutput instance) {
			this.image = instance;
			hashes = new ArrayList<int[]>();
		}
	}

	IntLSHSketcher<double[]> sketcher;
	List<TIntObjectHashMap<Set<WriteableImageOutput>>> database;
	Set<LongObjectPair<WriteableImageOutputHashes>> imagesByTime = new TreeSet<LongObjectPair<WriteableImageOutputHashes>>(
			new Comparator<LongObjectPair<WriteableImageOutputHashes>>() {

				@Override
				public int compare(
						LongObjectPair<WriteableImageOutputHashes> o1,
						LongObjectPair<WriteableImageOutputHashes> o2) {
					return ((Long) o1.first).compareTo(o2.first);
				}

			});

	/**
	 * @throws UnsupportedEncodingException
	 */
	public ZMQStreamingPicslurperClusterer()
			throws UnsupportedEncodingException {
		// Prepare our context and subscriber
		ZMQ.Context context = ZMQ.context(1);
		subscriber = context.socket(ZMQ.SUB);

		subscriber.connect("tcp://leto:5563");
		subscriber.subscribe("IMAGE".getBytes("UTF-8"));

		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);

		final MersenneTwister rng = new MersenneTwister();

		final DoubleGaussianFactory gauss = new DoubleGaussianFactory(ndims,
				rng, w);
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

	/**
	 * @param count
	 * @return the top count list of sets from all hashtable bins
	 */
	public List<Set<WriteableImageOutput>> trending(int count) {
		final SimpleWeightedGraph<WriteableImageOutput, DefaultWeightedEdge> graph = new SimpleWeightedGraph<WriteableImageOutput, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		for (TIntObjectHashMap<Set<WriteableImageOutput>> set : this.database) {
			set.forEachEntry(new TIntObjectProcedure<Set<WriteableImageOutput>>() {
				@Override
				public boolean execute(int hashIndex, Set<WriteableImageOutput> itemSet) {
					for (WriteableImageOutput item: itemSet) {
						if(!graph.containsVertex(item)){
							graph.addVertex(item);
						}
					}
					List<WriteableImageOutput> itemList = new ArrayList<WriteableImageOutput>();
					itemList.addAll(itemSet);
					for (int i = 0; i < itemList.size(); i++) {
						WriteableImageOutput itemA = itemList.get(i);
						for (int j = i+1; j < itemList.size(); j++) {
							WriteableImageOutput itemB = itemList.get(j);
							DefaultWeightedEdge edge = graph.getEdge(itemA, itemB);
							if(edge==null){
								edge = graph.addEdge(itemA, itemB);
								graph.setEdgeWeight(edge, 1);
							}
							else{								
								graph.setEdgeWeight(edge, graph.getEdgeWeight(edge)+1);
							}
							
						}	
					}
					return true;
				}
			});
		}

		ConnectivityInspector<WriteableImageOutput, DefaultWeightedEdge> conn = new ConnectivityInspector<WriteableImageOutput, DefaultWeightedEdge>(graph);
		List<Set<WriteableImageOutput>> retList = conn.connectedSets();
		Collections.sort(retList, new Comparator<Set<WriteableImageOutput>>() {

					@Override
					public int compare(Set<WriteableImageOutput> o1,Set<WriteableImageOutput> o2) {
						return -1 * ((Integer) o1.size()).compareTo(o2.size());
					}

				});
		return retList.subList(0, count < retList.size() ? count : retList.size());
	}

	private void indexImage(WriteableImageOutput instance) throws IOException {
		for (File imageFile : instance.listImageFiles("/Volumes/LetoDisk")) {
			final List<Keypoint> features = extractFeatures(imageFile);
			WriteableImageOutputHashes imageHashes = new WriteableImageOutputHashes(
					instance);
			for (final Keypoint k : features) {
				final int[] sketch = sketcher.createSketch(logScale(k.ivec,
						LOG_BASE));
				imageHashes.hashes.add(sketch);
				for (int i = 0; i < sketch.length; i++) {
					final int sk = sketch[i];
					synchronized (database) {
						Set<WriteableImageOutput> s = database.get(i).get(sk);
						if (s == null)
							database.get(i).put(sk,
									s = new HashSet<WriteableImageOutput>());
						s.add(instance);
					}
				}
			}
			long time = System.currentTimeMillis();
			synchronized (this.imagesByTime) {
				this.imagesByTime
						.add(new LongObjectPair<WriteableImageOutputHashes>(
								time, imageHashes));
			}
		}
	}

	List<Keypoint> extractFeatures(File imageFile) throws IOException {
		final ByteEntropyFilter filter = new ByteEntropyFilter();

		final FImage image = ResizeProcessor.resizeMax(
				ImageUtilities.readF(imageFile), 150);
		final List<Keypoint> features = engine.findFeatures(image);
		return FilterUtils.filter(features, filter);
	}

	class CleanupRunner implements Runnable {

		@Override
		public void run() {

		}

	}

	class TrendingRunner implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				List<Set<WriteableImageOutput>> trending = trending(10);
				for (Set<WriteableImageOutput> set : trending) {
					System.out.println(String.format("[%d] %s", set.size(),set.toString()));
				}
			}
		}

	}

	/**
	 * @param args
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String args[]) throws UnsupportedEncodingException {
		ZMQStreamingPicslurperClusterer instance = new ZMQStreamingPicslurperClusterer();
		new Thread(instance.new CleanupRunner()).start();
		new Thread(instance.new TrendingRunner()).start();
		instance.run();
	}

	private void run() {
		while (true) {
			subscriber.recv(0);
			ByteArrayInputStream stream = new ByteArrayInputStream(
					subscriber.recv(0));
			WriteableImageOutput instance = null;
			try {
				instance = IOUtils.read(stream, WriteableImageOutput.class,
						"UTF-8");
				indexImage(instance);

				System.out.println("SUCCESS!");
			} catch (Throwable e) {
				System.err.println("FAILED: ");
				if(instance != null){
					System.err.println("instance.file = " + instance.file);
					System.err.println("instance.url = " + instance.url);
					
				}
				e.printStackTrace();
			}
		}
	}
}
