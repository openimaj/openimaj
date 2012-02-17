package org.openimaj.image.feature.local.engine;

import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.collector.Collector;
import org.openimaj.image.feature.local.detector.dog.collector.OctaveKeypointCollector;
import org.openimaj.image.feature.local.detector.dog.extractor.GradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.NullOrientationExtractor;
import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveGridFinder;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointVisualizer;

public class BasicGridSIFTEngine implements Engine<Keypoint, FImage> {
	DoGSIFTEngineOptions<FImage> options;
	
	public BasicGridSIFTEngine() {
		options = new DoGSIFTEngineOptions<FImage>();
	}
	
	@Override
	public LocalFeatureList<Keypoint> findFeatures(FImage image) {
		OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder = new BasicOctaveGridFinder<GaussianOctave<FImage>, FImage>();
		
		Collector<GaussianOctave<FImage>, Keypoint, FImage> collector = new OctaveKeypointCollector<FImage>(
				new GradientFeatureExtractor(
//					new DominantOrientationExtractor(
//							options.peakThreshold, 
//							new OrientationHistogramExtractor(
//									options.numOriHistBins, 
//									options.scaling, 
//									options.smoothingIterations, 
//									options.samplingSize
//							)
//					),
					new NullOrientationExtractor(),
					new SIFTFeatureProvider(
							options.numOriBins, 
							options.numSpatialBins, 
							options.valueThreshold, 
							options.gaussianSigma
					), 
					options.magnificationFactor * options.numSpatialBins
				)
		);
		
		finder.setOctaveInterestPointListener(collector);
		
		options.setOctaveProcessor(finder);
		
		GaussianPyramid<FImage> pyr = new GaussianPyramid<FImage>(options);
		pyr.process(image);
		
		return collector.getFeatures();
	}
	
	public static void main(String[] args) throws IOException {
		FImage image = ImageUtilities.readF(BasicOctaveGridFinder.class.getResourceAsStream("/org/openimaj/OpenIMAJ.png"));
		MBFImage cimg = new MBFImage(image.clone(), image.clone(), image.clone());
		
		BasicGridSIFTEngine engine = new BasicGridSIFTEngine();
		
		LocalFeatureList<Keypoint> features = engine.findFeatures(image);
		
		KeypointVisualizer.drawPatchesInline(cimg, features, RGBColour.RED, null);
		DisplayUtilities.display(cimg);
	}
}
