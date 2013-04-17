package org.openimaj.demos.classification;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.image.Image;
import org.openimaj.io.ObjectReader;

public class Caltech101<IMAGE extends Image<?, IMAGE>> extends VFSGroupDataset<IMAGE> {
	private static final String downloadURL = "http://datasets.openimaj.org/Caltech101/101_ObjectCategories.zip";

	public Caltech101(ObjectReader<IMAGE> reader) throws IOException {
		super(getPath(), reader);
	}

	public Caltech101(String path, ObjectReader<IMAGE> reader) throws FileSystemException {
		super(path, reader);
	}

	private static String getPath() throws IOException {
		File dataDir = null;
		if (System.getProperty("openimaj.data.dir") != null) {
			dataDir = new File(System.getProperty("openimaj.data.dir"));
		} else {
			final String userHome = System.getProperty("user.home");
			dataDir = new File(userHome, "Data");
		}

		final File dataset = new File(dataDir, "Caltech101/101_ObjectCategories.zip");

		if (!(dataset.exists())) {
			dataset.getParentFile().mkdirs();
			FileUtils.copyInputStreamToFile(new URL(downloadURL).openStream(), dataset);
		}

		return "zip:file:" + dataset.toString() + "!101_ObjectCategories/";
	}
}
