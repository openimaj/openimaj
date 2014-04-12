package org.openimaj.demos;

import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.alignment.FaceAligner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

import scala.actors.threadpool.Arrays;

public class FVFWDetectAlign {
	static interface FDFactory {
		FKEFaceDetector create();
	}

	@SuppressWarnings("unchecked")
	private static void extractFaces(final File indir, final File outDir, final FDFactory factory,
			final FaceAligner<KEDetectedFace> aligner) throws IOException
	{
		final List<String> skipped = new ArrayList<String>();

		Parallel.forEach(Arrays.asList(indir.listFiles()), new Operation<File>() {

			@Override
			public void perform(File dir) {
				try {
					if (!dir.isDirectory())
						return;

					final FKEFaceDetector detector = factory.create();

					for (final File imgfile : dir.listFiles()) {
						if (!imgfile.getName().endsWith(".jpg"))
							continue;

						System.out.println(imgfile);
						final File outfile = new File(outDir, imgfile.getAbsolutePath().replace(indir.getAbsolutePath(),
								""));
						outfile.getParentFile().mkdirs();

						final FImage aligned = extractAndAlignFace(ImageUtilities.readF(imgfile), detector, aligner);

						if (aligned == null) {
							synchronized (skipped) {
								skipped.add(imgfile.toString());
							}
						} else {
							ImageUtilities.write(aligned, outfile);
						}
					}
				} catch (final Exception e) {
					e.printStackTrace();
					System.err.println(e);
				}
			}
		});

		final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outDir, "skipped.txt")));
		IOUtils.writeLines(skipped, "\n", writer);
		writer.close();
	}

	private static FImage
			extractAndAlignFace(FImage img, FKEFaceDetector detector, FaceAligner<KEDetectedFace> aligner)
	{
		final List<KEDetectedFace> faces = detector.detectFaces(img);

		if (faces.size() == 1)
			return aligner.align(faces.get(0));

		return null;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final FDFactory factory = new FDFactory() {
			@Override
			public FKEFaceDetector create() {
				final FaceDetector<DetectedFace, FImage> inner = new FaceDetector<DetectedFace, FImage>() {
					@Override
					public void readBinary(DataInput in) throws IOException {
						// do nothing
					}

					@Override
					public byte[] binaryHeader() {
						return null;
					}

					@Override
					public void writeBinary(DataOutput out) throws IOException {
						// do nothing
					}

					@Override
					public List<DetectedFace> detectFaces(FImage image) {
						final List<DetectedFace> faces = new ArrayList<DetectedFace>();

						final int dw = Math.round(image.width / 2.2f);
						final int dh = Math.round(image.height / 2.2f);
						final int x = (image.width - dw) / 2;
						final int y = (image.height - dh) / 2;
						final Rectangle bounds = new Rectangle(x, y, dw, dh);

						faces.add(new DetectedFace(bounds, image.extractROI(bounds), 1f));

						return faces;
					}
				};

				return new FKEFaceDetector(inner, 1.5f);
			}
		};

		final AffineAligner aligner = new AffineAligner(125, 160, 0.1f);

		extractFaces(new File("/Volumes/Raid/face_databases/lfw"), new File(
				"/Volumes/Raid/face_databases/lfw-centre-affine/"), factory, aligner);
	}
}
