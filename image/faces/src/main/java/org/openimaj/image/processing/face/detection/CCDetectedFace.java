package org.openimaj.image.processing.face.detection;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.math.geometry.shape.Rectangle;

public class CCDetectedFace extends DetectedFace {
	ConnectedComponent connectedComponent;
	
	public CCDetectedFace() {
		super();
	}
	
	public CCDetectedFace(Rectangle bounds, FImage patch, ConnectedComponent cc) {
		super(bounds, patch);
		this.connectedComponent = cc;
	}

	public ConnectedComponent getConnectedComponent() {
		return connectedComponent;
	}

	/* (non-Javadoc)
	 * @see org.openimaj.image.processing.face.detection.DetectedFace#writeBinary(java.io.DataOutput)
	 */
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		connectedComponent.writeBinary(out);
	}

	/* (non-Javadoc)
	 * @see org.openimaj.image.processing.face.detection.DetectedFace#binaryHeader()
	 */
	@Override
	public byte[] binaryHeader() {
		return "CCDF".getBytes();
	}

	/* (non-Javadoc)
	 * @see org.openimaj.image.processing.face.detection.DetectedFace#readBinary(java.io.DataInput)
	 */
	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		connectedComponent.readBinary(in);
	}
	
	
}
