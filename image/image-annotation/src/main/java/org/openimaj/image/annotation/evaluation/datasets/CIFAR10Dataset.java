package org.openimaj.image.annotation.evaluation.datasets;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.DataUtils;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.cifar.BinaryReader;

/**
 * CIFAR-10 Dataset. Contains 60000 tiny images in 10 classes (6000 per class).
 * Each image is 32x32 pixels.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Krizhevsky, A.", "Hinton, G." },
		title = "Learning multiple layers of features from tiny images",
		year = "2009",
		journal = "Master's thesis, Department of Computer Science, University of Toronto",
		publisher = "Citeseer")
@DatasetDescription(
		name = "CIFAR-10",
		description = "The CIFAR-10 dataset consists of 60000 32x32 colour "
				+ "images in 10 classes, with 6000 images per class. There are "
				+ "50000 training images and 10000 test images. The dataset is "
				+ "divided into five training batches and one test batch, each "
				+ "with 10000 images. The test batch contains exactly 1000 "
				+ "randomly-selected images from each class. The training batches "
				+ "contain the remaining images in random order, but some training "
				+ "batches may contain more images from one class than another. "
				+ "Between them, the training batches contain exactly 5000 images "
				+ "from each class.",
		creator = "Alex Krizhevsky, Vinod Nair, and Geoffrey Hinton",
		url = "http://www.cs.toronto.edu/~kriz/cifar.html",
		downloadUrls = {
				"http://datasets.openimaj.org/cifar/cifar-10-binary.tar.gz",
		})
public class CIFAR10Dataset extends CIFARDataset {
	private static final String DATA_TGZ = "cifar/cifar-10-binary.tar.gz";
	private static final String DOWNLOAD_URL = "http://datasets.openimaj.org/cifar/cifar-10-binary.tar.gz";

	private static final String[] TRAINING_FILES = {
			"data_batch_1.bin",
			"data_batch_2.bin",
			"data_batch_3.bin",
			"data_batch_4.bin",
			"data_batch_5.bin" };
	private static final String TEST_FILE = "test_batch.bin";
	private static final String CLASSES_FILE = "batches.meta.txt";

	private CIFAR10Dataset() {
	}

	private static String downloadAndGetPath() throws IOException {
		final File dataset = DataUtils.getDataLocation(DATA_TGZ);

		if (!(dataset.exists())) {
			dataset.getParentFile().mkdirs();
			FileUtils.copyURLToFile(new URL(DOWNLOAD_URL), dataset);
		}

		return "tgz:file:" + dataset.toString() + "!cifar-10-batches-bin/";
	}

	/**
	 * Load the training images using the given reader. To load the images as
	 * {@link MBFImage}s, you would do the following: <code>
	 * CIFAR10Dataset.getTrainingImages(CIFAR10Dataset.MBFIMAGE_READER);
	 * </code>
	 *
	 * @param reader
	 *            the reader
	 * @return the training image dataset
	 * @throws IOException
	 */
	public static <IMAGE> GroupedDataset<String, ListDataset<IMAGE>, IMAGE> getTrainingImages(BinaryReader<IMAGE> reader)
			throws IOException
	{
		final MapBackedDataset<String, ListDataset<IMAGE>, IMAGE> dataset = new MapBackedDataset<String, ListDataset<IMAGE>, IMAGE>();

		final FileSystemManager fsManager = VFS.getManager();
		final FileObject base = fsManager.resolveFile(downloadAndGetPath());

		final List<String> classList = loadClasses(dataset, base);

		for (final String t : TRAINING_FILES) {
			DataInputStream is = null;
			try {
				is = new DataInputStream(base.resolveFile(t).getContent().getInputStream());

				loadData(is, dataset, classList, reader);
			} finally {
				IOUtils.closeQuietly(is);
			}
		}

		return dataset;
	}

	private static <IMAGE> List<String> loadClasses(final MapBackedDataset<String, ListDataset<IMAGE>, IMAGE> dataset,
			final FileObject base) throws FileSystemException, IOException
			{
		InputStream classStream = null;
		List<String> classList = null;
		try {
			classStream = base.resolveFile(CLASSES_FILE).getContent().getInputStream();
			classList = IOUtils.readLines(classStream);
		} finally {
			IOUtils.closeQuietly(classStream);
		}

		for (final String clz : classList)
			dataset.put(clz, new ListBackedDataset<IMAGE>());
		return classList;
			}

	private static <IMAGE> void loadData(DataInputStream is,
			MapBackedDataset<String, ListDataset<IMAGE>, IMAGE> dataset, List<String> classList,
			BinaryReader<IMAGE> reader) throws IOException
	{

		for (int i = 0; i < 10000; i++) {
			final int clz = is.read();
			final String clzStr = classList.get(clz);
			final byte[] record = new byte[WIDTH * HEIGHT * 3];
			is.readFully(record);

			dataset.get(clzStr).add(reader.read(record));
		}
	}

	/**
	 * Load the test images using the given reader. To load the images as
	 * {@link MBFImage}s, you would do the following: <code>
	 * CIFAR10Dataset.getTestImages(CIFAR10Dataset.MBFIMAGE_READER);
	 * </code>
	 *
	 * @param reader
	 *            the reader
	 * @return the test image dataset
	 * @throws IOException
	 */
	public static <IMAGE> GroupedDataset<String, ListDataset<IMAGE>, IMAGE> getTestImages(BinaryReader<IMAGE> reader)
			throws IOException
	{
		final MapBackedDataset<String, ListDataset<IMAGE>, IMAGE> dataset = new MapBackedDataset<String, ListDataset<IMAGE>, IMAGE>();

		final FileSystemManager fsManager = VFS.getManager();
		final FileObject base = fsManager.resolveFile(downloadAndGetPath());

		final List<String> classList = loadClasses(dataset, base);

		DataInputStream is = null;
		try {
			is = new DataInputStream(base.resolveFile(TEST_FILE).getContent().getInputStream());
			loadData(is, dataset, classList, reader);
		} finally {
			IOUtils.closeQuietly(is);
		}

		return dataset;
	}
}
