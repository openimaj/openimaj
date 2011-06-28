package org.openimaj.image.processing.face.feature.comparison;

import java.util.List;

import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.face.feature.ltp.LtpDtFeature;

public class LtpDtFeatureComparator implements FacialFeatureComparator<LtpDtFeature> {

	@Override
	public double compare(LtpDtFeature query, LtpDtFeature target) {
		List<List<Pixel>> slicePixels = query.ltpPixels;
		float distance = 0;
		
		for (int i=0; i<target.distanceMaps.length; i++) {
			List<Pixel> pixels = slicePixels.get(i);
			
			if (target.distanceMaps[i] == null || pixels == null)
				continue;
			
			for (Pixel p : pixels) {
				distance += target.distanceMaps[i].pixels[p.y][p.x];
			}
		}
		
		return distance;
	}

	@Override
	public boolean isAscending() {
		return true;
	}
}
