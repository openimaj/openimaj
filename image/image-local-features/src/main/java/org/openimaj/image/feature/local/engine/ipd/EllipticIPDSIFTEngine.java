package org.openimaj.image.feature.local.engine.ipd;

import org.openimaj.image.feature.local.detector.ipd.collector.AffineInterestPointFeatureCollector;
import org.openimaj.image.feature.local.detector.ipd.collector.InterestPointFeatureCollector;
import org.openimaj.image.feature.local.detector.ipd.extractor.InterestPointGradientFeatureExtractor;
import org.openimaj.image.feature.local.interest.EllipticInterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;

public class EllipticIPDSIFTEngine extends AbstractIPDSIFTEngine<EllipticInterestPointData> {

	public EllipticIPDSIFTEngine(InterestPointDetector<EllipticInterestPointData> detector) {
		super(detector);
	}

	@Override
	public InterestPointFeatureCollector<EllipticInterestPointData> constructCollector(InterestPointGradientFeatureExtractor extractor) {
		return new AffineInterestPointFeatureCollector(extractor);
	}

}
