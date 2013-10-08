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
package org.openimaj.image.annotation.evaluation.datasets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.openimaj.data.DataUtils;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.image.Image;
import org.openimaj.image.ImageProvider;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.io.ObjectReader;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;

/**
 * The CalTech101 image dataset. Contains 102 classes of image (101 objects +
 * background), and (for most images) outlines and bounding boxes of the object.
 * Images are approximately 300x200 pixels in size.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@DatasetDescription(
		name = "CalTech101",
		description = "Pictures of objects belonging to 101 categories. " +
				"About 40 to 800 images per category. Most categories have about " +
				"50 images. The size of each image is roughly 300 x 200 pixels.",
		creator = "Fei-Fei Li, Marco Andreetto, and Marc 'Aurelio Ranzato",
		url = "http://www.vision.caltech.edu/Image_Datasets/Caltech101/",
		downloadUrls = {
				"http://datasets.openimaj.org/Caltech101/101_ObjectCategories.zip",
				"http://datasets.openimaj.org/Caltech101/Annotations.zip"
		})
public class Caltech101 {
	private static final String IMAGES_ZIP = "Caltech101/101_ObjectCategories.zip";
	private static final String IMAGES_DOWNLOAD_URL = "http://datasets.openimaj.org/Caltech101/101_ObjectCategories.zip";
	private static final String ANNOTATIONS_ZIP = "Caltech101/Annotations.zip";
	private static final String ANNOTATIONS_DOWNLOAD_URL = "http://datasets.openimaj.org/Caltech101/Annotations.zip";

	private Caltech101() {
	}

	/**
	 * Get a dataset of the Caltech 101 images. If the dataset hasn't been
	 * downloaded, it will be fetched automatically and stored in the OpenIMAJ
	 * data directory. The images in the dataset are grouped by their class.
	 * 
	 * @see DataUtils#getDataDirectory()
	 * 
	 * @param reader
	 * @return a dataset of images
	 * @throws IOException
	 *             if a problem occurs loading the dataset
	 */
	public static <IMAGE extends Image<?, IMAGE>> VFSGroupDataset<IMAGE> getImages(InputStreamObjectReader<IMAGE> reader)
			throws IOException
	{
		return new VFSGroupDataset<IMAGE>(downloadAndGetImagePath(), reader);
	}

	private static String downloadAndGetImagePath() throws IOException {
		final File dataset = DataUtils.getDataLocation(IMAGES_ZIP);

		if (!(dataset.exists())) {
			dataset.getParentFile().mkdirs();
			FileUtils.copyURLToFile(new URL(IMAGES_DOWNLOAD_URL), dataset);
		}

		return "zip:file:" + dataset.toString() + "!101_ObjectCategories/";
	}

	private static String downloadAndGetAnnotationPath() throws IOException {
		final File dataset = DataUtils.getDataLocation(ANNOTATIONS_ZIP);

		if (!(dataset.exists())) {
			dataset.getParentFile().mkdirs();
			FileUtils.copyURLToFile(new URL(ANNOTATIONS_DOWNLOAD_URL), dataset);
		}

		return "zip:file:" + dataset.toString() + "!Annotations/";
	}

	/**
	 * A record in the Caltech 101 dataset. Contains the image together with
	 * (optional) metadata on the bounds of the object in the image as well as
	 * the class of object in the image.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <IMAGE>
	 *            The type of image that is loaded
	 */
	public static abstract class Record<IMAGE extends Image<?, IMAGE>> implements Identifiable, ImageProvider<IMAGE> {
		private Rectangle bounds;
		private Polygon contour;
		private String id;
		private String objectClass;

		protected Record(FileObject image) throws FileSystemException, IOException {
			final FileSystemManager fsManager = VFS.getManager();
			final FileObject imagesBase = fsManager.resolveFile(downloadAndGetImagePath());
			final FileObject annotationsBase = fsManager.resolveFile(downloadAndGetAnnotationPath());

			// get the id
			id = imagesBase.getName().getRelativeName(image.getName());

			// the class
			objectClass = image.getParent().getName().getBaseName();

			// find the annotation file
			final String annotationFileName = id.replace("image_", "annotation_").replace(".jpg", ".mat");
			final FileObject annotationFile = annotationsBase.resolveFile(annotationFileName);
			parseAnnotations(annotationFile);
		}

		private void parseAnnotations(FileObject annotationFile) throws IOException {
			if (!annotationFile.exists()) {
				return;
			}

			final MatFileReader reader = new MatFileReader(annotationFile.getContent().getInputStream());

			final MLDouble boxes = (MLDouble) reader.getMLArray("box_coord");
			this.bounds = new Rectangle(
					(float) (double) boxes.getReal(2) - 1,
					(float) (double) boxes.getReal(0) - 1,
					(float) (boxes.getReal(3) - boxes.getReal(2)) - 1,
					(float) (boxes.getReal(1) - boxes.getReal(0)) - 1);

			final double[][] contourData = ((MLDouble) reader.getMLArray("obj_contour")).getArray();
			this.contour = new Polygon();
			for (int i = 0; i < contourData[0].length; i++) {
				contour.points.add(
						new Point2dImpl((float) contourData[0][i] + bounds.x - 1,
								(float) contourData[1][i] + bounds.y - 1)
						);
			}
			contour.close();
		}

		@Override
		public String getID() {
			return id;
		}

		/**
		 * Get the bounds rectangle if it is available
		 * 
		 * @return the bounds
		 */
		public Rectangle getBounds() {
			return bounds;
		}

		/**
		 * Get the object polygon if it is available.
		 * 
		 * @return the contour
		 */
		public Polygon getContour() {
			return contour;
		}

		/**
		 * Get the class of the object depicted in the image.
		 * 
		 * @return the class
		 */
		public String getObjectClass() {
			return objectClass;
		}
	}

	/**
	 * An {@link ObjectReader} for {@link Record}s.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <IMAGE>
	 *            Type of image being read
	 */
	private static class RecordReader<IMAGE extends Image<?, IMAGE>> implements ObjectReader<Record<IMAGE>, FileObject> {
		private VFSListDataset.FileObjectISReader<IMAGE> imageReader;

		public RecordReader(InputStreamObjectReader<IMAGE> reader) {
			this.imageReader = new VFSListDataset.FileObjectISReader<IMAGE>(reader);
		}

		@Override
		public Record<IMAGE> read(final FileObject source) throws IOException {
			return new Record<IMAGE>(source) {

				@Override
				public IMAGE getImage() {
					try {
						return imageReader.read(source);
					} catch (final IOException e) {
						throw new RuntimeException(e);
					}
				}
			};
		}

		@Override
		public boolean canRead(FileObject source, String name) {
			InputStream stream = null;
			try {
				stream = source.getContent().getInputStream();

				return ImageUtilities.FIMAGE_READER.canRead(stream, source.getName().getBaseName());
			} catch (final FileSystemException e) {
				return false;
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (final IOException e) {
					}
				}
			}
		}
	}

	/**
	 * Get a dataset of the Caltech 101 images and metadata. If the dataset
	 * hasn't been downloaded, it will be fetched automatically and stored in
	 * the OpenIMAJ data directory. The images in the dataset are grouped by
	 * their class.
	 * 
	 * @see DataUtils#getDataDirectory()
	 * 
	 * @param reader
	 *            a reader for reading images (usually a
	 *            {@link ImageUtilities#FIMAGE_READER} or
	 *            {@link ImageUtilities#MBFIMAGE_READER}).
	 * @return a dataset of images and metadate
	 * @throws IOException
	 *             if a problem occurs loading the dataset
	 */
	public static <IMAGE extends Image<?, IMAGE>> VFSGroupDataset<Record<IMAGE>> getData(
			InputStreamObjectReader<IMAGE> reader) throws IOException
	{
		return new VFSGroupDataset<Record<IMAGE>>(downloadAndGetImagePath(), new RecordReader<IMAGE>(reader));
	}
}
