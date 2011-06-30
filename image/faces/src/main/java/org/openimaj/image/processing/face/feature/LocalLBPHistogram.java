package org.openimaj.image.processing.face.feature;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.dense.binarypattern.ExtendedLocalBinaryPattern;
import org.openimaj.image.feature.dense.binarypattern.UniformBinaryPattern;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.io.IOUtils;

public class LocalLBPHistogram implements FacialFeature, FeatureVectorProvider<FloatFV> {
	public static class Factory<T extends DetectedFace> implements FacialFeatureFactory<LocalLBPHistogram, T> {
		FaceAligner<T> aligner;
		int blocksX = 25;
		int blocksY = 25;
		int samples = 8;
		int radius = 1;

		protected Factory() {}

		public Factory(FaceAligner<T> aligner, int blocksX, int blocksY, int samples, int radius) {
			this.aligner = aligner;
			this.blocksX = blocksX;
			this.blocksY = blocksY;
			this.samples = samples;
			this.radius = radius;
		}

		@Override
		public LocalLBPHistogram createFeature(T detectedFace, boolean isquery) {
			LocalLBPHistogram f = new LocalLBPHistogram();

			FImage face = aligner.align(detectedFace);
			FImage mask = aligner.getMask();

			f.initialise(face, mask, blocksX, blocksY, samples, radius);

			return f;
		}

		@Override
		public void readBinary(DataInput in) throws IOException {
			String alignerClass = in.readUTF();
			aligner = IOUtils.newInstance(alignerClass);
			aligner.readBinary(in);

			blocksX = in.readInt();
			blocksY = in.readInt();
			radius = in.readInt();
			samples = in.readInt();
		}

		@Override
		public byte[] binaryHeader() {
			return this.getClass().getName().getBytes();
		}

		@Override
		public void writeBinary(DataOutput out) throws IOException {
			out.writeUTF(aligner.getClass().getName());
			aligner.writeBinary(out);

			out.writeInt(blocksX);
			out.writeInt(blocksY);
			out.writeInt(radius);
			out.writeInt(samples);
		}

		@Override
		public Class<LocalLBPHistogram> getFeatureClass() {
			return LocalLBPHistogram.class;
		}

		@Override
		public String toString() {
			return String.format("LocalLBPHistogram.Factory[blocksX=%d,blocksY=%d,samples=%d,radius=%d]", blocksX, blocksY, samples, radius);
		}
	}

	float [][][] histograms;
	transient FloatFV featureVector;

	protected void initialise(FImage face, FImage mask, int blocksX, int blocksY, int samples, int radius) {
		int [][] pattern = ExtendedLocalBinaryPattern.calculateLBP(face, radius, samples);
		boolean [][][] maps = UniformBinaryPattern.extractPatternMaps(pattern, samples);

		int bx = face.width / blocksX;
		int by = face.height / blocksY;
		histograms = new float[blocksY][blocksX][maps.length];

		//build histogram
		for (int p=0; p<maps.length; p++) {
			for (int y=0; y<blocksY; y++) {
				for (int x=0; x<blocksX; x++) {

					for (int j=0; j<by; j++) {
						for (int i=0; i<bx; i++) {
							if (maps[p][y*by + j][x*bx + i])
								histograms[y][x][p]++;					
						}
					}
				}
			}
		}

		//normalise
		for (int y=0; y<blocksY; y++) {
			for (int x=0; x<blocksX; x++) {
				float count = 0;
				for (int p=0; p<maps.length; p++) {
					count += histograms[y][x][p];
				}
				for (int p=0; p<maps.length; p++) {
					histograms[y][x][p] /= count;
				}
			}
		}
		
		updateFeatureVector();
	}

	protected void updateFeatureVector() {
		featureVector = new FloatFV(histograms.length * histograms[0].length * histograms[0][0].length);
		
		int i=0;
		for (int y=0; y<histograms.length; y++) {
			for (int x=0; x<histograms[0].length; x++) {
				for (int p=0; p<histograms[0][0].length; p++) {
					featureVector.values[i] = histograms[y][x][p];
					i++;
				}
			}
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "LBPH".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		int by = in.readInt();
		int bx = in.readInt();
		int p = in.readInt();

		histograms = new float[by][bx][p];

		for (int j=0; j<by; j++) {
			for (int i=0; i<bx; i++) {
				for (int k=0; k<p; k++) {
					histograms[j][i][k] = in.readFloat();
				}
			}
		}
		updateFeatureVector();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(histograms.length);
		out.writeInt(histograms[0].length);
		out.writeInt(histograms[0][0].length);

		for (float [][] hist1 : histograms) {
			for (float [] hist2 : hist1) {
				for (float h : hist2) {
					out.writeFloat(h);
				}
			}
		}
	}

	@Override
	public FloatFV getFeatureVector() {
		return featureVector;
	}
}
