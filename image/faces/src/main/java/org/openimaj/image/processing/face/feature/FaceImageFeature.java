package org.openimaj.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.FImage2FloatFV;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.alignment.ScalingAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;

/**
 * A {@link FacialFeature} that is just the pixel values
 * of a (possibly aligned) face detection. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FaceImageFeature implements FacialFeature, FeatureVectorProvider<FloatFV> {
	/**
	 * A {@link FacialFeatureExtractor} for producing {@link FaceImageFeature}s.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <T> Type of {@link DetectedFace}
	 */
	public static class Extractor<T extends DetectedFace> implements FacialFeatureExtractor<FaceImageFeature, T> {
		FaceAligner<T> aligner;
		
		/**
		 * Construct with a {@link ScalingAligner} with its default resolution
		 */
		public Extractor() {
			this(new ScalingAligner<T>());
		}
		
		/**
		 * Construct with an aligner
		 * @param aligner the aligner
		 */
		public Extractor(FaceAligner<T> aligner) {
			this.aligner = aligner;
		}
		
		@Override
		public FaceImageFeature extractFeature(T face) {
			FImage faceImage = aligner.align(face);
			FloatFV feature = FImage2FloatFV.INSTANCE.extractFeature(faceImage);
			
			return new FaceImageFeature(feature);
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}
	}

	private FloatFV feature;
	
	/**
	 * Construct with the given feature
	 * 
	 * @param feature
	 */
	public FaceImageFeature(FloatFV feature) {
		this.feature = feature;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		feature = new FloatFV();
		feature.readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return this.getClass().getName().getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		feature.writeBinary(out);
	}

	@Override
	public FloatFV getFeatureVector() {
		return feature;
	}
}
