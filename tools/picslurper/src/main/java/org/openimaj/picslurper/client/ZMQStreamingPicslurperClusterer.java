package org.openimaj.picslurper.client;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.local.filter.ByteEntropyFilter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.io.IOUtils;
import org.openimaj.lsh.functions.DoubleGaussianFactory;
import org.openimaj.lsh.sketch.IntLSHSketcher;
import org.openimaj.picslurper.output.WriteableImageOutput;
import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.modifier.LSBModifier;
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

	IntLSHSketcher<double[]> sketcher;
	List<TIntObjectHashMap<Set<WriteableImageOutput>>> database;

	/**
	 * @throws UnsupportedEncodingException
	 */
	public ZMQStreamingPicslurperClusterer() throws UnsupportedEncodingException {
		// Prepare our context and subscriber
		ZMQ.Context context = ZMQ.context(1);
		subscriber = context.socket(ZMQ.SUB);

		subscriber.connect("tcp://localhost:5563");
		subscriber.subscribe("IMAGE".getBytes("UTF-8"));

		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);

		final MersenneTwister rng = new MersenneTwister();

		final DoubleGaussianFactory gauss = new DoubleGaussianFactory(ndims, rng, w);
		final HashFunctionFactory<double[]> factory = new HashFunctionFactory<double[]>() {
			@Override
			public HashFunction<double[]> create() {
				return new LSBModifier<double[]>(gauss.create());
			}
		};

		sketcher = new IntLSHSketcher<double[]>(factory, nbits);
		database = new ArrayList<TIntObjectHashMap<Set<WriteableImageOutput>>>(sketcher.arrayLength());

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

	private void indexImage(WriteableImageOutput instance) throws IOException {
		for (File imageFile : instance.listImageFiles()) {
			final List<Keypoint> features = extractFeatures(imageFile);
			for (final Keypoint k : features) {
				final int[] sketch = sketcher.createSketch(logScale(k.ivec, LOG_BASE));

				for (int i = 0; i < sketch.length; i++) {
					final int sk = sketch[i];
					synchronized (database) {
						Set<WriteableImageOutput> s = database.get(i).get(sk);
						if (s == null)
							database.get(i).put(sk, s = new HashSet<WriteableImageOutput>());
						s.add(instance);
					}
				}
			}
		}
	}

	List<Keypoint> extractFeatures(File imageFile) throws IOException {
		final ByteEntropyFilter filter = new ByteEntropyFilter();

		final FImage image = ResizeProcessor.resizeMax(ImageUtilities.readF(imageFile), 150);
		final List<Keypoint> features = engine.findFeatures(image);
		return FilterUtils.filter(features, filter);
	}
	/**
	 * @param args
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String args[]) throws UnsupportedEncodingException {
		ZMQStreamingPicslurperClusterer instance = new ZMQStreamingPicslurperClusterer();
		instance.run();
	}

	private void run() {
		while (true) {
			subscriber.recv(0);
			ByteArrayInputStream stream = new ByteArrayInputStream(subscriber.recv(0));
			WriteableImageOutput instance;
			try {
				instance = IOUtils.read(stream, WriteableImageOutput.class, "UTF-8");
//				System.out.println(instance.file);

				indexImage(instance);

				System.out.println("SUCCESS!");
			} catch (IOException e) {
				System.out.println("FAILED!");
				e.printStackTrace();
			}
		}
	}
}
