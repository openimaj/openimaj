package org.openimaj.tools.localfeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.SIFTGeoKeypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;

/**
 * Simple tool to batch convert files in siftgeo format to lowe's sift format.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class SIFTGeoConverter {
	private static void getInputs(File file, List<File> files) {
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				getInputs(f, files);
			}
		} else {
			if (file.getName().endsWith(".siftgeo")) {
				files.add(file);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// final File inDir = new
		// File("/Volumes/My Book/Data/ukbench/features/hesaff");
		// final File outDir = new
		// File("/Volumes/My Book/Data/ukbench/features/hesaff-sift");
		final File inDir = new File(args[0]);
		final File outDir = new File(args[1]);

		outDir.mkdirs();

		final List<File> input = new ArrayList<File>();
		getInputs(inDir, input);

		Parallel.forEach(input, new Operation<File>() {
			@Override
			public void perform(File file) {
				try {
					System.out.println(file);

					final LocalFeatureList<SIFTGeoKeypoint> sgkeys = SIFTGeoKeypoint.read(file);
					final LocalFeatureList<Keypoint> keys = new MemoryLocalFeatureList<Keypoint>(128, sgkeys.size());

					for (final SIFTGeoKeypoint sg : sgkeys) {
						final Keypoint k = new Keypoint();
						k.ivec = sg.descriptor;
						k.x = sg.location.x;
						k.y = sg.location.y;
						k.ori = sg.location.orientation;
						k.scale = sg.location.scale;
						keys.add(k);
					}

					final File path = new File(file.getAbsolutePath()
							.replace(inDir.getAbsolutePath(), outDir.getAbsolutePath())
							.replace(".siftgeo", ".sift"));
					path.getParentFile().mkdirs();
					IOUtils.writeBinary(path, keys);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
