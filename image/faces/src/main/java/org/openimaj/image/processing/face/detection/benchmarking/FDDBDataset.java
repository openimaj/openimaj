package org.openimaj.image.processing.face.detection.benchmarking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.EllipticalDetectedFace;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;

public class FDDBDataset extends ListBackedDataset<FDDBRecord> {
	class Record implements FDDBRecord {
		String imageName;
		private List<DetectedFace> groundTruth;

		@Override
		public String getImageName() {
			return imageName;
		}

		@Override
		public FImage getFImage() {
			try {
				return ImageUtilities.readF(new File(imageBase, imageName + imageExtension));
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public MBFImage getMBFImage() {
			try {
				return ImageUtilities.readMBF(new File(imageBase, imageName + imageExtension));
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public List<? extends DetectedFace> getGroundTruth() {
			return groundTruth;
		}
	}

	File imageBase;
	final String imageExtension = ".jpg";

	public FDDBDataset(File fddbGroundTruth, File imageBase, boolean loadImages) throws IOException {
		this.data = new ArrayList<FDDBRecord>();
		this.imageBase = imageBase;

		read(fddbGroundTruth, loadImages);
	}

	private void read(File fddbGroundTruth, boolean loadImages) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fddbGroundTruth));

			String imageName;
			while ((imageName = br.readLine()) != null) {
				final Record r = new Record();
				r.imageName = imageName;
				r.groundTruth = new ArrayList<DetectedFace>();

				final int nDet = Integer.parseInt(br.readLine());
				for (int i = 0; i < nDet; i++) {
					final String[] parts = br.readLine().split("\\s+");

					if (parts.length != 6)
						throw new IOException("bad format");

					final double major = Double.parseDouble(parts[0]);
					final double minor = Double.parseDouble(parts[1]);
					final double theta = Double.parseDouble(parts[2]);
					final double x = Double.parseDouble(parts[3]);
					final double y = Double.parseDouble(parts[4]);
					final float confidence = Float.parseFloat(parts[5]);

					final Ellipse ellipse = EllipseUtilities.ellipseFromEquation(x, y, major, minor, theta);

					EllipticalDetectedFace detection;
					if (!loadImages) {
						detection = new EllipticalDetectedFace(ellipse, null, confidence);
					} else {
						detection = new EllipticalDetectedFace(ellipse, r.getFImage(), confidence);
					}

					r.groundTruth.add(detection);
				}
			}

		} finally {
			if (br != null)
				try {
					br.close();
				} catch (final IOException e) {
				}
		}
	}
}
