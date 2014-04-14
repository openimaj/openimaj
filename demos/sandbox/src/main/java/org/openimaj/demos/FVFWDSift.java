package org.openimaj.demos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analysis.pyramid.SimplePyramid;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.io.IOUtils;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

import scala.actors.threadpool.Arrays;

public class FVFWDSift {
	static interface DSFactory {
		DenseSIFT create();
	}

	@SuppressWarnings("unchecked")
	private static void extractPDSift(final File indir, final File outDir, final DSFactory factory) throws IOException
	{
		final List<String> skipped = new ArrayList<String>();

		Parallel.forEach(Arrays.asList(indir.listFiles()), new Operation<File>() {

			@Override
			public void perform(File dir) {
				try {
					if (!dir.isDirectory())
						return;

					final DenseSIFT sift = factory.create();

					for (final File imgfile : dir.listFiles()) {
						if (!imgfile.getName().endsWith(".jpg"))
							continue;

						final File outfile = new File(outDir, imgfile.getAbsolutePath().replace(indir.getAbsolutePath(),
								"").replace(".jpg", ".bin"));
						outfile.getParentFile().mkdirs();

						final FImage image = ImageUtilities.readF(imgfile);

						final SimplePyramid<FImage> pyr = new SimplePyramid<FImage>((float) Math.sqrt(2), 5,
								new FGaussianConvolve(1.0f));
						pyr.processImage(image);

						final LocalFeatureList<FloatDSIFTKeypoint> allKeys = new MemoryLocalFeatureList<FloatDSIFTKeypoint>();
						for (final FImage i : pyr) {
							sift.analyseImage(i);
							// System.out.println(i.width + " " + i.height + " "
							// + sift.getFloatKeypoints().size());

							final double scale = 160.0 / i.height;
							final LocalFeatureList<FloatDSIFTKeypoint> kps = sift.getFloatKeypoints();
							for (final FloatDSIFTKeypoint kp : kps) {
								kp.x *= scale;
								kp.y *= scale;
							}

							allKeys.addAll(kps);
						}
						for (final FloatDSIFTKeypoint kp : allKeys) {
							for (int i = 0; i < 128; i++) {
								// rootsift
								final int sgn = kp.descriptor[i] >= 0 ? 1 : -1;
								kp.descriptor[i] = (float) (sgn * Math.sqrt(kp.descriptor[i] * sgn));
							}
						}

						IOUtils.writeBinary(outfile, allKeys);

						System.out.println(imgfile + " " + allKeys.size());
					}
				} catch (final Exception e) {
					e.printStackTrace();
					System.err.println(e);
				}
			}
		});
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final DSFactory factory = new DSFactory() {
			@Override
			public DenseSIFT create() {
				return new DenseSIFT(1, 6);
			}
		};

		extractPDSift(
				new File("/Volumes/Raid/face_databases/lfw-centre-affine/"),
				new File("/Volumes/Raid/face_databases/lfw-centre-affine-pdsift/"),
				factory);
	}

}
