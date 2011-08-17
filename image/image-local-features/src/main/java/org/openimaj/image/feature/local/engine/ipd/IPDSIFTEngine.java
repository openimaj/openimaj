package org.openimaj.image.feature.local.engine.ipd;

import org.openimaj.image.feature.local.detector.ipd.collector.CircularInterestPointFeatureCollector;
import org.openimaj.image.feature.local.detector.ipd.collector.InterestPointFeatureCollector;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.interest.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;

public class IPDSIFTEngine extends AbstractIPDSIFTEngine<InterestPointData> {

	public IPDSIFTEngine(InterestPointDetector<InterestPointData> detector) {
		super(detector);
	}

	@Override
	public InterestPointFeatureCollector<InterestPointData> constructCollector(InterestPointGradientFeatureExtractor extractor) {
		return new CircularInterestPointFeatureCollector(extractor);
	}

}
