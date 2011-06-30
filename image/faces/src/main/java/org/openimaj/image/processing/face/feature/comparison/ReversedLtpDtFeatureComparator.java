package org.openimaj.image.processing.face.feature.comparison;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.face.feature.ltp.ReversedLtpDtFeature;

public class ReversedLtpDtFeatureComparator implements FacialFeatureComparator<ReversedLtpDtFeature> {

	@Override
	public double compare(ReversedLtpDtFeature query, ReversedLtpDtFeature target) {
		List<List<Pixel>> slicePixels = target.ltpPixels;
		float distance = 0;
		
		for (int i=0; i<query.distanceMaps.length; i++) {
			List<Pixel> pixels = slicePixels.get(i);
			double sliceDistance = 0;
			
			if (query.distanceMaps[i] == null || pixels == null)
				continue;
			
			for (Pixel p : pixels) {
				sliceDistance += query.distanceMaps[i].pixels[p.y][p.x];
			}
			distance += sliceDistance;
		}
		
		return distance;
	}

	@Override
	public boolean isAscending() {
		return true;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		//do nothing
	}

	@Override
	public byte[] binaryHeader() {
		//do nothing
		return null;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		//do nothing
	}
	
	@Override
	public String toString() {
		return "ReversedLtpDtFeatureComparator";
	}
}
