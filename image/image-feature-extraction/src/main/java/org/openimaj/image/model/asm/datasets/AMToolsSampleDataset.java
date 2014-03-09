/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.model.asm.datasets;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.openimaj.data.DataUtils;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.image.Image;
import org.openimaj.io.InputStreamObjectReader;

/**
 * Tim Cootes's sample appearance modelling data
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@DatasetDescription(
		name = "Tim Cootes's sample appearance modelling data",
		description = "The sample data (images, points and connections) that come with Tim Cootes's am_tools software.",
		creator = "Tim Cootes",
		url = "http://www.isbe.man.ac.uk/~bim/software/am_tools_doc/",
		downloadUrls = {
				"http://datasets.openimaj.org/am_tools_data.zip"
		})
public class AMToolsSampleDataset {
	private static final String DATA_ZIP = "am_tools_data.zip";
	private static final String DATA_DOWNLOAD_URL = "http://datasets.openimaj.org/am_tools_data.zip";

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
		final String basePath = downloadAndGetPath();
		return ShapeModelDatasets.loadPTSDataset(basePath + "points/", basePath + "images/", basePath
				+ "models/face.parts", reader);
	}

	private static String downloadAndGetPath() throws IOException {
		final File dataset = DataUtils.getDataLocation(DATA_ZIP);

		if (!(dataset.exists())) {
			dataset.getParentFile().mkdirs();
			FileUtils.copyURLToFile(new URL(DATA_DOWNLOAD_URL), dataset);
		}

		return "zip:file:" + dataset.toString() + "!am_tools_data/";
	}

}
