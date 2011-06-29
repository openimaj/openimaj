package org.openimaj.image.processing.face.feature.ltp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TruncatedWeighting implements LTPWeighting {
	float threshold = 6;
	
	public TruncatedWeighting() {}
	
	public TruncatedWeighting(float threshold) {
		this.threshold = threshold;
	}
	
	@Override
	public float weightDistance(float distance) {
		return Math.min(distance, threshold);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		threshold = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(threshold);
	}
}
