package org.openimaj.image.processing.face.feature.ltp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class GaussianWeighting implements LTPWeighting {
	private float sigma = 3;
	
	public GaussianWeighting() {}
	
	public GaussianWeighting(float sigma) {
		this.sigma= sigma;
	}
	
	@Override
	public float weightDistance(float distance) {
		return (float) Math.exp( -(distance / sigma) * (distance / sigma) / 2);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		sigma = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(sigma);
	}
}
