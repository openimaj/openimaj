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
 * CIFAR-100 Dataset. Contains 60000 tiny images in 100 classes (600 per class).
 * There are 500 training images/class and 100 test. Each image is 32x32 pixels.
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
		name = "CIFAR-100",
		description = "This dataset is just like CIFAR-10, except it has 100 "
				+ "classes containing 600 images each. There are 500 training images "
				+ "and 100 testing images per class. The 100 classes in the CIFAR-100 "
				+ "are grouped into 20 superclasses. Each image comes with a \"fine\" "
				+ "label (the class to which it belongs) and a \"coarse\" label "
				+ "(the superclass to which it belongs).",
				creator = "Alex Krizhevsky, Vinod Nair, and Geoffrey Hinton",
				url = "http://www.cs.toronto.edu/~kriz/cifar.html",
				downloadUrls = {
				"http://datasets.openimaj.org/cifar/cifar-100-binary.tar.gz",
		})
public class CIFAR100Dataset extends CIFARDataset {
	private static final String DATA_TGZ = "cifar/cifar-100-binary.tar.gz";
	private static final String DOWNLOAD_URL = "http://datasets.openimaj.org/cifar/cifar-100-binary.tar.gz";

	private static final String TRAINING_FILE = "train.bin";
	private static final String TEST_FILE = "test.bin";
	private static final String FINE_CLASSES_FILE = "fine_label_names.txt";
	private static final String COARSE_CLASSES_FILE = "coarse_label_names.txt";

	private CIFAR100Dataset() {
	}

	private static String downloadAndGetPath() throws IOException {
		final File dataset = DataUtils.getDataLocation(DATA_TGZ);

		if (!(dataset.exists())) {
			dataset.getParentFile().mkdirs();
			FileUtils.copyURLToFile(new URL(DOWNLOAD_URL), dataset);
		}

		return "tgz:file:" + dataset.toString() + "!cifar-100-binary/";
	}

	/**
	 * Load the training images using the given reader. To load the images as
	 * {@link MBFImage}s, you would do the following: <code>
	 * CIFAR100Dataset.getTrainingImages(CIFAR100Dataset.MBFIMAGE_READER);
	 * </code>
	 *
	 * @param reader
	 *            the reader
	 * @param fineLabels
	 *            if true, then the fine labels will be used; otherwise the
	 *            coarse superclass labels will be used.
	 * @return the training image dataset
	 * @throws IOException
	 */
	public static <IMAGE> GroupedDataset<String, ListDataset<IMAGE>, IMAGE> getTrainingImages(BinaryReader<IMAGE> reader,
			boolean fineLabels)
			throws IOException
					{
		final MapBackedDataset<String, ListDataset<IMAGE>, IMAGE> dataset = new MapBackedDataset<String, ListDataset<IMAGE>, IMAGE>();

		final FileSystemManager fsManager = VFS.getManager();
		final FileObject base = fsManager.resolveFile(downloadAndGetPath());

		final List<String> classList = loadClasses(dataset, base, fineLabels);

		DataInputStream is = null;
		try {
			is = new DataInputStream(base.resolveFile(TRAINING_FILE).getContent().getInputStream());

			loadData(is, dataset, classList, reader, 50000, fineLabels);
		} finally {
			IOUtils.closeQuietly(is);
		}

		return dataset;
					}

	private static <IMAGE> List<String> loadClasses(final MapBackedDataset<String, ListDataset<IMAGE>, IMAGE> dataset,
			final FileObject base, boolean fine) throws FileSystemException, IOException
	{
		InputStream classStream = null;
		List<String> classList = null;
		try {
			if (fine)
				classStream = base.resolveFile(FINE_CLASSES_FILE).getContent().getInputStream();
			else
				classStream = base.resolveFile(COARSE_CLASSES_FILE).getContent().getInputStream();
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
			BinaryReader<IMAGE> reader, int num, boolean fine) throws IOException
	{

		for (int i = 0; i < num; i++) {
			final int coarseClz = is.read();
			final int fineClz = is.read();
			final int clz = fine ? fineClz : coarseClz;

			final String clzStr = classList.get(clz);
			final byte[] record = new byte[32 * 32 * 3];
			is.readFully(record);

			dataset.get(clzStr).add(reader.read(record));
		}
	}

	/**
	 * Load the test images using the given reader. To load the images as
	 * {@link MBFImage}s, you would do the following: <code>
	 * CIFAR100Dataset.getTestImages(CIFAR100Dataset.MBFIMAGE_READER);
	 * </code>
	 *
	 * @param reader
	 *            the reader
	 * @param fineLabels
	 *            if true, then the fine labels will be used; otherwise the
	 *            coarse superclass labels will be used.
	 * @return the test image dataset
	 * @throws IOException
	 */
	public static <IMAGE> GroupedDataset<String, ListDataset<IMAGE>, IMAGE> getTestImages(BinaryReader<IMAGE> reader,
			boolean fineLabels)
					throws IOException
					{
		final MapBackedDataset<String, ListDataset<IMAGE>, IMAGE> dataset = new MapBackedDataset<String, ListDataset<IMAGE>, IMAGE>();

		final FileSystemManager fsManager = VFS.getManager();
		final FileObject base = fsManager.resolveFile(downloadAndGetPath());

		final List<String> classList = loadClasses(dataset, base, fineLabels);

		DataInputStream is = null;
		try {
			is = new DataInputStream(base.resolveFile(TEST_FILE).getContent().getInputStream());
			loadData(is, dataset, classList, reader, 10000, fineLabels);
		} finally {
			IOUtils.closeQuietly(is);
		}

		return dataset;
					}
}
