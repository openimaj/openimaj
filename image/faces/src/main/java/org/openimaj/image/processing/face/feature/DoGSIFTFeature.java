package org.openimaj.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A {@link FacialFeature} that uses DoG-SIFT features to 
 * describe a face.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class DoGSIFTFeature implements FacialFeature {
	public static class Factory implements FacialFeatureFactory<DoGSIFTFeature, DetectedFace> {
		/**
		 * Default constructor
		 */
		public Factory() {
			
		}
		
		@Override
		public void readBinary(DataInput in) throws IOException {
			// currently no state to write
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			// currently no state to read
		}

		@Override
		public Class<DoGSIFTFeature> getFeatureClass() {
			return DoGSIFTFeature.class;
		}

		@Override
		public DoGSIFTFeature createFeature(DetectedFace face, boolean isquery) {
			DoGSIFTFeature feature = new DoGSIFTFeature();
			feature.initialise(face);
			return feature;
		}
		
	}
	
	protected LocalFeatureList<Keypoint> keys;
	protected Rectangle bounds;

	protected void initialise(DetectedFace face) {
		DoGSIFTEngine engine = new DoGSIFTEngine();
		keys = engine.findFeatures(face.getFacePatch());
		bounds = face.getFacePatch().getBounds();
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		keys = MemoryLocalFeatureList.readNoHeader(in, Keypoint.class);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		keys.writeBinary(out);
	}

	/**
	 * @return the keys
	 */
	public LocalFeatureList<Keypoint> getKeys() {
		return keys;
	}

	/**
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		return bounds;
	}
}
