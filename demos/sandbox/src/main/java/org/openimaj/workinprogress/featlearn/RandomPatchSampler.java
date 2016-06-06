package org.openimaj.workinprogress.featlearn;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.openimaj.data.dataset.Dataset;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;

import com.google.common.collect.Lists;

public class RandomPatchSampler<IMAGE extends Image<?, IMAGE>> implements Iterable<IMAGE> {
	long seed = 0;
	int nsamples;
	int height;
	int width;
	Dataset<IMAGE> ds;

	public RandomPatchSampler(Dataset<IMAGE> data, int width, int height, int nsamples) {
		this.ds = data;
		this.width = width;
		this.height = height;
		this.nsamples = nsamples;
	}

	public List<IMAGE> getPatches() {
		return Lists.newArrayList(this);
	}

	@Override
	public Iterator<IMAGE> iterator() {
		return new Iterator<IMAGE>() {
			Random rng = new Random(seed);
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < nsamples;
			}

			@Override
			public IMAGE next() {
				i++;
				return getRandomPatch(ds.getRandomInstance());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			private IMAGE getRandomPatch(IMAGE instance) {
				final int x = rng.nextInt(instance.getWidth() - width - 1);
				final int y = rng.nextInt(instance.getHeight() - height - 1);

				return instance.extractROI(x, y, width, height);
			}
		};
	}

	public static List<FImage> loadPatches(File file) throws IOException {
		final List<FImage> list = new ArrayList<FImage>();

		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new BufferedInputStream(new
					FileInputStream(file)));
			final int width = dis.readInt();
			final int height = dis.readInt();
			final int nsamples = dis.readInt();

			for (int i = 0; i < nsamples; i++) {
				final FImage image = new FImage(width, height);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						image.pixels[y][x] = dis.readFloat();
					}
				}
				list.add(image);
			}
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (final IOException e1) {
				}
			}
		}

		return list;
	}

	// public void save(File file) throws IOException {
	// DataOutputStream dos = null;
	// try {
	// dos = new DataOutputStream(new FileOutputStream(file));
	// dos.writeInt(this.width);
	// dos.writeInt(this.height);
	// dos.writeInt(this.nsamples);
	//
	// for (final FImage image : this) {
	// for (int y = 0; y < height; y++) {
	// for (int x = 0; x < width; x++) {
	// dos.writeFloat(image.pixels[y][x]);
	// }
	// }
	// }
	// } finally {
	// if (dos != null) {
	// try {
	// dos.close();
	// } catch (final IOException e1) {
	// }
	// }
	// }
	// }

	public static void main(String[] args) throws IOException {
		final RandomPatchSampler<MBFImage> ps = new RandomPatchSampler<MBFImage>(
				Caltech101.getImages(ImageUtilities.MBFIMAGE_READER), 28, 28, 70000);

		final File TRAINING = new File("/Users/jon/Data/caltech101-patches/training/");
		final File TESTING = new File("/Users/jon/Data/caltech101-patches/testing/");

		TRAINING.mkdirs();
		TESTING.mkdirs();

		int i = 0;
		for (final MBFImage patch : ps) {
			System.out.println(i);
			if (i < 60000) {
				ImageUtilities.write(patch, new File(TRAINING, i + ".png"));
			} else {
				ImageUtilities.write(patch, new File(TESTING, (i - 60000) + ".png"));
			}
			i++;
		}
	}
}
