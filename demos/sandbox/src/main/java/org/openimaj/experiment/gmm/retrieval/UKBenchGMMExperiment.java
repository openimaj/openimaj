package org.openimaj.experiment.gmm.retrieval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.feature.DiskCachingFeatureExtractor;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.io.ObjectReader;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.math.statistics.distribution.metrics.SampledMultivariateDistanceComparator;
import org.openimaj.ml.gmm.GaussianMixtureModelEM.CovarianceType;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.GlobalExecutorPool.DaemonThreadFactory;


/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class UKBenchGMMExperiment {
	private static final class IRecordWrapper<A,B>
			implements
			Function<UKBenchGMMExperiment.IRecord<A>, B> {
		Function<A, B> inner;

		public IRecordWrapper(Function<A,B> extract) {
			this.inner = extract;
		}

		@Override
		public B apply(IRecord<A> in) {
			return inner.apply(in.image);
		}

		public static <A,B> Function<IRecord<A>, B> wrap(Function<A,B> extract) {
			return new IRecordWrapper<A,B>(extract);
		}
	}
	private static class IRecord<IMAGE> implements Identifiable{

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
			return new IRecord<A>(id,payload);
		}
		
	}
	private static final class FileObjectReader<IMAGE> implements ObjectReader<IRecord<IMAGE>, FileObject> {
		InputStreamObjectReader<IMAGE> reader;
		
		public FileObjectReader(InputStreamObjectReader<IMAGE> reader) {
			this.reader = reader;
		}

		@Override
		public IRecord<IMAGE> read(FileObject source) throws IOException {
			String name = source.getName().getBaseName();
			IMAGE image = reader.read(source.getContent().getInputStream());
			return new IRecord<IMAGE>(name,image);
		}

		@Override
		public boolean canRead(FileObject source, String name) {
			try {
				return reader.canRead(source.getContent().getInputStream(), name);
			} catch (FileSystemException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private String ukbenchRoot = "/Users/ss/Experiments/ukbench";
	private ResizeProcessor resize;
	private UKBenchGroupDataset<IRecord<FImage>> dataset;
	private DiskCachingFeatureExtractor<MixtureOfGaussians, IRecord<FImage>> gmmExtract;
	
	public UKBenchGMMExperiment() {
		setup();
	}
	
	public UKBenchGMMExperiment(String root) {
		this.ukbenchRoot = root;
		setup();
	}

	private void setup() {
		this.dataset = new UKBenchGroupDataset<IRecord<FImage>>(
				ukbenchRoot + "/full", 
				new FileObjectReader<FImage>(ImageUtilities.FIMAGE_READER)
		);
		
		
		
		resize = new ResizeProcessor(640, 480);
		
		Function<FImage, MixtureOfGaussians> combined = new Function<FImage,MixtureOfGaussians>(){
			
			@Override
			public MixtureOfGaussians apply(FImage in) {
				final DSiftFeatureExtractor feature = new DSiftFeatureExtractor();
				final GMMFromFeatures gmmFunc = new GMMFromFeatures(3,CovarianceType.Diagonal);
				System.out.println("... resize");
				FImage process = in.process(resize);
				System.out.println("... dsift");
				LocalFeatureList<? extends LocalFeature<?, ? extends FeatureVector>> apply = feature.apply(process);
				System.out.println("... gmm");
				return gmmFunc.apply(apply);
			}
			
		};
		
		
		
		this.gmmExtract = 
		new DiskCachingFeatureExtractor<
			MixtureOfGaussians,
			IRecord<FImage>
		>(
			new File(ukbenchRoot + "/gmm/dsift"), 
			FeatureExtractionFunction.wrap(IRecordWrapper.wrap(combined))
		);
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String root = "/home/ss/Experiments/ukbench";
		if(args.length != 0){
			root = args[0];
		}
		UKBenchGMMExperiment exp = new UKBenchGMMExperiment(root);
		exp.extractGroupGaussians();
//		List<MixtureOfGaussians> gaus1 = exp.extractGroupGaussians(0);
//		List<MixtureOfGaussians> gaus2 = exp.extractGroupGaussians(1);
//		
//		SampledMultivariateDistanceComparator dist = new SampledMultivariateDistanceComparator();
//		
//		System.out.printf("o0i0 vs self = %2.5f\n",dist.compare(gaus1.get(0), gaus1.get(0)));
//		System.out.printf("o0i0 vs o0i1 = %2.5f\n",dist.compare(gaus1.get(0), gaus1.get(1)));
//		System.out.printf("o0i0 vs o0i2 = %2.5f\n",dist.compare(gaus1.get(0), gaus1.get(2)));
//		System.out.printf("o0i0 vs o0i3 = %2.5f\n",dist.compare(gaus1.get(0), gaus1.get(3)));
//		System.out.printf("o0i0 vs o1i0 = %2.5f\n",dist.compare(gaus1.get(0), gaus2.get(0)));
//		System.out.printf("o0i0 vs o1i1 = %2.5f\n",dist.compare(gaus1.get(0), gaus2.get(1)));
//		System.out.printf("o0i0 vs o1i2 = %2.5f\n",dist.compare(gaus1.get(0), gaus2.get(2)));
//		System.out.printf("o0i0 vs o1i3 = %2.5f\n",dist.compare(gaus1.get(0), gaus2.get(3)));
		
		
	}
	
	/**
	 * @return the mixture of gaussians for each group
	 */
	public Map<Integer,List<MixtureOfGaussians>> extractGroupGaussians() {
		final Map<Integer,List<MixtureOfGaussians>> groups = new HashMap<Integer, List<MixtureOfGaussians>>();
		ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new DaemonThreadFactory());
		Parallel.forIndex(0, this.dataset.size(), 1, new Operation<Integer>() {
			
			@Override
			public void perform(Integer i) {
				groups.put(i, extractGroupGaussians(i));
			}
		},pool);
		
		return groups;
	}
	
	public List<MixtureOfGaussians> extractGroupGaussians(int i) {
		System.out.printf("Extracting features for object %d...\n",i);
		return this.extractGroupGaussians(this.dataset.get(i));
	}

	public List<MixtureOfGaussians> extractGroupGaussians(UKBenchListDataset<IRecord<FImage>> ukbenchObject) {
		List<MixtureOfGaussians> gaussians = new ArrayList<MixtureOfGaussians>();
		int i = 0;
		for (IRecord<FImage> fimage : ukbenchObject) {
			System.out.printf("Extracting features for image %d...\n",i++);
			MixtureOfGaussians gmm = gmmExtract.extractFeature(fimage);
			gaussians.add(gmm);
		}
		return gaussians;
	}

	
}
