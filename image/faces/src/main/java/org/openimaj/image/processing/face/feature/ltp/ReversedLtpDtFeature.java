package org.openimaj.image.processing.face.feature.ltp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.FacialFeatureFactory;
import org.openimaj.io.IOUtils;

/**
 * LTP based feature using a truncated Euclidean distance transform
 * to estimate the distances within each slice.
 * 
 * Based on: 
 * "Enhanced Local Texture Feature Sets for Face Recognition 
 * Under Difficult Lighting Conditions" by Xiaoyang Tan and 
 * Bill Triggs.
 *
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ReversedLtpDtFeature extends AbstractLtpDtFeature {
	public static class Factory<Q extends DetectedFace> implements FacialFeatureFactory<ReversedLtpDtFeature, Q> {
		private static final long serialVersionUID = 1L;
		
		LTPWeighting weighting;
		FaceAligner<Q> aligner;
		
		protected Factory() {}
		
		public Factory(FaceAligner<Q> aligner, LTPWeighting weighting) {
			this.aligner = aligner;
			this.weighting = weighting;
		}
		
		@Override
		public ReversedLtpDtFeature createFeature(Q detectedFace, boolean isquery) {
			ReversedLtpDtFeature f = new ReversedLtpDtFeature();
			
			FImage face = aligner.align(detectedFace);
			FImage mask = aligner.getMask();
			
			f.initialise(face, mask, weighting, isquery);
			
			return f;
		}
		
		@Override
		public void readBinary(DataInput in) throws IOException {
			String weightingClass = in.readUTF();
			weighting = IOUtils.newInstance(weightingClass);
			weighting.readBinary(in);
			
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
			out.writeUTF(weighting.getClass().getName());
			weighting.writeBinary(out);
			
			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);
		}

		@Override
		public Class<ReversedLtpDtFeature> getFeatureClass() {
			return ReversedLtpDtFeature.class;
		}
		
		@Override
		public String toString() {
			return "ReversedLtpDtFeature.Factory[weighting="+weighting+"]";
		}
	}
	
	protected void initialise(FImage face, FImage mask, LTPWeighting weighting, boolean isQuery) {
		FImage npatch = normaliseImage(face, mask);
		
		ltpPixels = extractLTPSlicePixels(npatch);
		
		if (isQuery)
			distanceMaps = extractDistanceTransforms(constructSlices(ltpPixels, face.width, face.height), weighting);
	}
}
