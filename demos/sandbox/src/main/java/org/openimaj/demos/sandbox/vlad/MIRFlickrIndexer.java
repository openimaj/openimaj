package org.openimaj.demos.sandbox.vlad;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;

public class MIRFlickrIndexer {
	public static void main(String[] args) {
		final List<File> images = Arrays.asList((new
				File("/Volumes/My Book/Data/ukbench/images/")).listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.getName().endsWith(".jpg");
					}
				}));

		final File outDir = new File("/Volumes/My Book/Data/ukbench/features/sift-2x");
		outDir.mkdirs();

		Parallel.forEach(images, new Operation<File>() {
			@Override
			public void perform(File file) {
				try {
					final File out = new File(outDir, file.getName().replace("jpg", "sift"));

					if (out.exists())
						return;

					final DoGSIFTEngine engine = new DoGSIFTEngine();
					// engine.getOptions().setDoubleInitialImage(false);
					System.out.println(file);

					final FImage image = ImageUtilities.readF(file);
					// image = ResizeProcessor.resizeMax(image, 300);

					final LocalFeatureList<Keypoint> keys = engine.findFeatures(image);

					IOUtils.writeBinary(out, keys);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	// public static void main(String[] args) throws IOException {
	// final VLADIndexer indexer = VLADIndexer.read(new
	// File("/Users/jsh2/vlad-indexer-mirflickr25k-1x.dat"));
	//
	// final List<IntObjectPair<float[]>> index = new
	// ArrayList<IntObjectPair<float[]>>();
	// final List<IntObjectPair<float[]>> syncList =
	// Collections.synchronizedList(index);
	//
	// Parallel.forEach(Arrays.asList(new
	// File("/Volumes/Raid/mirflickr/sift-1x-300px").listFiles()), new
	// Operation<File>()
	// {
	//
	// @Override
	// public void perform(File f) {
	// if (!f.getName().endsWith(".sift"))
	// return;
	//
	// try {
	// System.out.println(f);
	//
	// final int id = Integer.parseInt(f.getName().replace("im",
	// "").replace(".sift", ""));
	//
	// final MemoryLocalFeatureList<Keypoint> keys =
	// MemoryLocalFeatureList.read(f, Keypoint.class);
	// final MemoryLocalFeatureList<FloatKeypoint> fkeys =
	// FloatKeypoint.convert(keys);
	//
	// for (final FloatKeypoint k : fkeys) {
	// HellingerNormaliser.normalise(k.vector, 0);
	// }
	//
	// indexer.index(fkeys, id, syncList);
	// } catch (final Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	//
	// IOUtils.writeToFile(index, new
	// File("/Users/jsh2/Desktop/mirflickr25k.idx"));
	// }
}
