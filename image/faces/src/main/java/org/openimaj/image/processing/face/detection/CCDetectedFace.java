package org.openimaj.image.processing.face.detection;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.math.geometry.shape.Rectangle;

public class CCDetectedFace extends DetectedFace {
	ConnectedComponent connectedComponent;
	
	public CCDetectedFace(Rectangle bounds, FImage patch, ConnectedComponent cc) {
		super(bounds, patch);
		this.connectedComponent = cc;
	}

	public ConnectedComponent getConnectedComponent() {
		return connectedComponent;
	}
}
