package org.openimaj.image.model.asm.datasets;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.DataUtils;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.image.Image;
import org.openimaj.io.InputStreamObjectReader;

/**
 * The IMM Face Database (a set of labelled faces with connected points).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@DatasetDescription(
		name = "The IMM Face Database",
		description = "A dataset consisting of 240 annotated monocular " +
				"images of 40 different human faces. Points of correspondence are placed " +
				"on each image so the dataset can be readily used for building statistical " +
				"models of shape.",
		creator = "Michael M. Nordstrom, Mads Larsen, Janusz Sierakowski, and Mikkel B. Stegmann",
		url = "http://www2.imm.dtu.dk/~aam/datasets/datasets.html",
		downloadUrls = {
				"http://datasets.openimaj.org/imm_face_db.zip"
		})
@Reference(
		type = ReferenceType.Article,
		author = { "M. B. Stegmann", "B. K. Ersb{\\o}ll", "R. Larsen" },
		title = "{FAME} -- A Flexible Appearance Modelling Environment",
		year = "2003",
		journal = "IEEE Trans. on Medical Imaging",
		pages = { "1319", "1331" },
		number = "10",
		publisher = "IEEE",
		volume = "22")
public class IMMFaceDatabase {
	private static final String DATA_ZIP = "imm_face_db.zip";
	private static final String DATA_DOWNLOAD_URL = "http://datasets.openimaj.org/imm_face_db.zip";

	/**
	 * Get a dataset of the IMM images and points. If the dataset hasn't been
	 * downloaded, it will be fetched automatically and stored in the OpenIMAJ
	 * data directory. The images in the dataset are grouped by their class.
	 * 
	 * @see DataUtils#getDataDirectory()
	 * 
	 * @param reader
	 *            the reader for images (can be <code>null</code> if you only
	 *            care about the points)
	 * @return the dataset
	 * @throws IOException
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> ShapeModelDataset<IMAGE> load(InputStreamObjectReader<IMAGE> reader)
			throws IOException
	{
		return ShapeModelDatasets.loadASFDataset(downloadAndGetPath(), reader);
	}

	private static String downloadAndGetPath() throws IOException {
		final File dataset = DataUtils.getDataLocation(DATA_ZIP);

		if (!(dataset.exists())) {
			dataset.getParentFile().mkdirs();
			FileUtils.copyURLToFile(new URL(DATA_DOWNLOAD_URL), dataset);
		}

		return "zip:file:" + dataset.toString();
	}

}
