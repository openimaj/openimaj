package org.openimaj.experiment.gmm.retrieval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	private static final String UKBENCH_ROOT = "/Users/ss/Experiments/ukbench";
	private ResizeProcessor resize;
	private UKBenchGroupDataset<IRecord<FImage>> dataset;
	private DiskCachingFeatureExtractor<MixtureOfGaussians, IRecord<FImage>> gmmExtract;
	
	public UKBenchGMMExperiment() {
		this.dataset = new UKBenchGroupDataset<IRecord<FImage>>(
				UKBENCH_ROOT + "/full", 
				new FileObjectReader<FImage>(ImageUtilities.FIMAGE_READER)
		);
		
		
		final DSiftFeatureExtractor feature = new DSiftFeatureExtractor();
		final GMMFromFeatures gmmFunc = new GMMFromFeatures(3,CovarianceType.Diagonal);
		resize = new ResizeProcessor(640, 480);
		
		Function<FImage, MixtureOfGaussians> combined = new Function<FImage,MixtureOfGaussians>(){
			
			@Override
			public MixtureOfGaussians apply(FImage in) {
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
			new File(UKBENCH_ROOT + "/gmm/dsift"), 
			FeatureExtractionFunction.wrap(IRecordWrapper.wrap(combined))
		);
		
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		UKBenchGMMExperiment exp = new UKBenchGMMExperiment();
		List<MixtureOfGaussians> gaus1 = exp.extractGroupGaussians(0);
		List<MixtureOfGaussians> gaus2 = exp.extractGroupGaussians(1);
		
		SampledMultivariateDistanceComparator dist = new SampledMultivariateDistanceComparator();
		
		System.out.printf("o0i0 vs self = %2.5f\n",dist.compare(gaus1.get(0), gaus1.get(0)));
		System.out.printf("o0i0 vs o0i1 = %2.5f\n",dist.compare(gaus1.get(0), gaus1.get(1)));
		System.out.printf("o0i0 vs o0i2 = %2.5f\n",dist.compare(gaus1.get(0), gaus1.get(2)));
		System.out.printf("o0i0 vs o0i3 = %2.5f\n",dist.compare(gaus1.get(0), gaus1.get(3)));
		System.out.printf("o0i0 vs o1i0 = %2.5f\n",dist.compare(gaus1.get(0), gaus2.get(0)));
		System.out.printf("o0i0 vs o1i1 = %2.5f\n",dist.compare(gaus1.get(0), gaus2.get(1)));
		System.out.printf("o0i0 vs o1i2 = %2.5f\n",dist.compare(gaus1.get(0), gaus2.get(2)));
		System.out.printf("o0i0 vs o1i3 = %2.5f\n",dist.compare(gaus1.get(0), gaus2.get(3)));
		
		
	}

	private List<MixtureOfGaussians> extractGroupGaussians(int i) {
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
