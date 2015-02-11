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
package org.openimaj.experiment.gmm.retrieval;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.feature.CachingFeatureExtractor;
import org.openimaj.feature.DiskCachingFeatureExtractor;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.io.ObjectReader;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.math.statistics.distribution.metrics.SampledMultivariateDistanceComparator;
import org.openimaj.ml.gmm.GaussianMixtureModelEM.CovarianceType;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.IntDoublePair;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.GlobalExecutorPool.DaemonThreadFactory;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class UKBenchGMMExperiment {
	private final class FImageFileObjectReader implements
			ObjectReader<FImage, FileObject> {
		@Override
		public FImage read(FileObject source) throws IOException {
			return ImageUtilities.FIMAGE_READER.read(source.getContent()
					.getInputStream());
		}

		@Override
		public boolean canRead(FileObject source, String name) {
			InputStream inputStream = null;
			try {
				inputStream = source.getContent().getInputStream();
				return ImageUtilities.FIMAGE_READER.canRead(inputStream, name);
			} catch (FileSystemException e) {
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			return false;
		}
	}

	private final class URLFileObjectReader implements
			ObjectReader<URL, FileObject> {
		@Override
		public URL read(FileObject source) throws IOException {
			return source.getURL();
		}

		@Override
		public boolean canRead(FileObject source, String name) {
			try {
				return (source.getURL() != null);
			} catch (FileSystemException e) {
				return false;
			}
		}
	}

	private static final class IRecordWrapper<A, B> implements
			Function<UKBenchGMMExperiment.IRecord<A>, B> {
		Function<A, B> inner;

		public IRecordWrapper(Function<A, B> extract) {
			this.inner = extract;
		}

		@Override
		public B apply(IRecord<A> in) {
			return inner.apply(in.image);
		}

		public static <A, B> Function<IRecord<A>, B> wrap(Function<A, B> extract) {
			return new IRecordWrapper<A, B>(extract);
		}
	}

	private static class IRecord<IMAGE> implements Identifiable {

		private String id;
		private IMAGE image;

		public IRecord(String id, IMAGE image) {
			this.id = id;
			this.image = image;
		}

		@Override
		public String getID() {
			return this.id;
		}

		public static <A> IRecord<A> wrap(String id, A payload) {
			return new IRecord<A>(id, payload);
		}

	}

	private static final class IRecordReader<IMAGE> implements
			ObjectReader<IRecord<IMAGE>, FileObject> {
		ObjectReader<IMAGE, FileObject> reader;

		public IRecordReader(ObjectReader<IMAGE, FileObject> reader) {
			this.reader = reader;
		}

		@Override
		public IRecord<IMAGE> read(FileObject source) throws IOException {
			String name = source.getName().getBaseName();
			IMAGE image = reader.read(source);
			return new IRecord<IMAGE>(name, image);
		}

		@Override
		public boolean canRead(FileObject source, String name) {
			return reader.canRead(source, name);
		}
	}

	private String ukbenchRoot = "/Users/ss/Experiments/ukbench";
	private ResizeProcessor resize;
	private UKBenchGroupDataset<IRecord<URL>> dataset;
	private FeatureExtractor<MixtureOfGaussians,IRecord<URL>> gmmExtract;
	final SampledMultivariateDistanceComparator comp = new SampledMultivariateDistanceComparator();

	public UKBenchGMMExperiment() {
		setup();
	}

	public UKBenchGMMExperiment(String root) {
		this.ukbenchRoot = root;
		setup();
	}

	private void setup() {
		this.dataset = new UKBenchGroupDataset<IRecord<URL>>(
				ukbenchRoot + "/full",
				// new IRecordReader<FImage>(new FImageFileObjectReader())
				new IRecordReader<URL>(new URLFileObjectReader()));

		resize = new ResizeProcessor(640, 480);

		Function<URL, MixtureOfGaussians> combined = new Function<URL, MixtureOfGaussians>() {

			@Override
			public MixtureOfGaussians apply(URL in) {
				
				final DSiftFeatureExtractor feature = new DSiftFeatureExtractor();
				final GMMFromFeatures gmmFunc = new GMMFromFeatures(3,CovarianceType.Diagonal);
				System.out.println("... resize");
				FImage process = null;
				try {
					process = ImageUtilities.readF(in).process(resize);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				System.out.println("... dsift");
				LocalFeatureList<? extends LocalFeature<?, ? extends FeatureVector>> apply = feature
						.apply(process);
				System.out.println("... gmm");
				return gmmFunc.apply(apply);
			}

		};
		this.gmmExtract = new CachingFeatureExtractor<MixtureOfGaussians, IRecord<URL>>(
				new DiskCachingFeatureExtractor<MixtureOfGaussians, IRecord<URL>>(
						new File(ukbenchRoot + "/gmm/dsift"),
						FeatureExtractionFunction.wrap(IRecordWrapper.wrap(combined)))
		);
	}

	static class UKBenchGMMExperimentOptions {
		@Option(name = "--input", aliases = "-i", required = true, usage = "Input location", metaVar = "STRING")
		String input = null;

		@Option(name = "--pre-extract-all", aliases = "-a", required = false, usage = "Preextract all", metaVar = "BOOLEAN")
		boolean preextract = false;
		
		@Option(name = "--object", aliases = "-obj", required = false, usage = "Object", metaVar = "Integer")
		int object = -1;
		
		@Option(name = "--image", aliases = "-img", required = false, usage = "Image", metaVar = "Integer")
		int image = -1;
	}

	static class ObjectRecord extends IndependentPair<Integer, IRecord<URL>> {

		public ObjectRecord(Integer obj1, IRecord<URL> obj2) {
			super(obj1, obj2);
		}

	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws CmdLineException
	 */
	public static void main(String[] args) throws IOException, CmdLineException {
		UKBenchGMMExperimentOptions opts = new UKBenchGMMExperimentOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		parser.parseArgument(args);
		final UKBenchGMMExperiment exp = new UKBenchGMMExperiment(opts.input);
		if (opts.preextract){
			System.out.println("Preloading all ukbench features...");
			exp.extractGroupGaussians();			
		}
		
		if(opts.object == -1 || opts.image == -1){			
			exp.applyToEachGroup(new Operation<UKBenchListDataset<IRecord<URL>>>() {
				
				@Override
				public void perform(UKBenchListDataset<IRecord<URL>> group) {
					int object = group.getObject();
					for (int i = 0; i < group.size(); i++) {
						double score = exp.score(object, i);
						System.out.printf("Object %d, image %d, score: %2.2f\n",object,i,score);
					}
				}
			});
		} else {
			double score = exp.score(opts.object, opts.image);
			System.out.printf("Object %d, image %d, score: %2.2f\n",opts.object,opts.image,score);
		}
	}

	protected MixtureOfGaussians extract(IRecord<URL> item) {
		return this.gmmExtract.extractFeature(item);
	}

	private void applyToEachGroup(Operation<UKBenchListDataset<IRecord<URL>>> operation) {
		for (int i = 0; i < this.dataset.size(); i++) {
			operation.perform(this.dataset.get(i));
		}

	}

	private void applyToEachImage(Operation<ObjectRecord> operation) {
		for (int i = 0; i < this.dataset.size(); i++) {
			UKBenchListDataset<IRecord<URL>> ukBenchListDataset = this.dataset.get(i);
			for (IRecord<URL> iRecord : ukBenchListDataset) {
				operation.perform(new ObjectRecord(i, iRecord));
			}
		}
	}
	
	public double score(int object, int image) {
		System.out.printf("Scoring Object %d, Image %d\n",object,image);
		IRecord<URL> item = this.dataset.get(object).get(image);
		final MixtureOfGaussians thisGMM = extract(item);
		final List<IntDoublePair> scored = new ArrayList<IntDoublePair>();
		applyToEachImage(new Operation<UKBenchGMMExperiment.ObjectRecord>() {

			@Override
			public void perform(ObjectRecord object) {
				MixtureOfGaussians otherGMM = extract(object.getSecondObject());
				
				double distance = comp.compare(thisGMM, otherGMM);
				scored.add(IntDoublePair.pair(object.firstObject(), distance));
				if(scored.size() % 200 == 0){
					System.out.printf("Loaded: %2.1f%%\n", 100 * (float)scored.size() / (dataset.size()*4));
				}
			}
		});
		
		Collections.sort(scored, new Comparator<IntDoublePair>(){

			@Override
			public int compare(IntDoublePair o1, IntDoublePair o2) {
				return -Double.compare(o1.second, o2.second);
			}
			
		});
		double good = 0;
		for (int i = 0; i < 4; i++) {
			if(scored.get(i).first == object) good+=1; 
		}
		return good/4f;
	}

	/**
	 * @return the mixture of gaussians for each group
	 */
	public Map<Integer, List<MixtureOfGaussians>> extractGroupGaussians() {
		final Map<Integer, List<MixtureOfGaussians>> groups = new HashMap<Integer, List<MixtureOfGaussians>>();
		ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(1,
						new DaemonThreadFactory());
		final double TOTAL = this.dataset.size() * 4;
		Parallel.forIndex(0, this.dataset.size(), 1, new Operation<Integer>() {

			@Override
			public void perform(Integer i) {
				groups.put(i, extractGroupGaussians(i));
				if(groups.size() % 200 == 0){
					System.out.printf("Loaded: %2.1f%%\n", 100 * groups.size() * 4 / TOTAL);
				}
			}
		}, pool);

		return groups;
	}

	public List<MixtureOfGaussians> extractGroupGaussians(int i) {
		return this.extractGroupGaussians(this.dataset.get(i));
	}

	public List<MixtureOfGaussians> extractGroupGaussians( UKBenchListDataset<IRecord<URL>> ukbenchObject) {
		List<MixtureOfGaussians> gaussians = new ArrayList<MixtureOfGaussians>();
		int i = 0;
		for (IRecord<URL> imageURL : ukbenchObject) {
			MixtureOfGaussians gmm = gmmExtract.extractFeature(imageURL);
			gaussians.add(gmm);
		}
		return gaussians;
	}

}
