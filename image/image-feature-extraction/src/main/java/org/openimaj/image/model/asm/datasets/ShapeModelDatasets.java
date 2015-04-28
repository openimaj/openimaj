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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.Image;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.io.ObjectReader;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.point.PointListConnections;
import org.openimaj.util.pair.IndependentPair;

/**
 * Utilities for creating with {@link ShapeModelDataset} instances.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class ShapeModelDatasets
{
	/**
	 * Basic in memory dataset
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <IMAGE>
	 *            type of the images in the collection
	 */
	private static class BasicDataset<IMAGE extends Image<?, IMAGE>>
			extends
			ListBackedDataset<IndependentPair<PointList, IMAGE>> implements ShapeModelDataset<IMAGE>
	{
		private PointListConnections connections;

		public BasicDataset(List<IndependentPair<PointList, IMAGE>> data, PointListConnections connections) {
			this.data = data;
			this.connections = connections;
		}

		@Override
		public PointListConnections getConnections() {
			return connections;
		}

		@Override
		public List<PointList> getPointLists() {
			return IndependentPair.getFirst(this);
		}

		@Override
		public List<IMAGE> getImages() {
			return IndependentPair.getSecond(this);
		}
	}

	/**
	 * File-backed dataset
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <IMAGE>
	 *            type of the images in the collection
	 */
	private abstract static class FileBackedDataset<IMAGE extends Image<?, IMAGE>>
			extends
			VFSListDataset<IndependentPair<PointList, IMAGE>> implements ShapeModelDataset<IMAGE>
	{
		protected PointListConnections connections;

		public FileBackedDataset(String path, ObjectReader<IndependentPair<PointList, IMAGE>, FileObject> reader,
				PointListConnections conns)
				throws IOException
		{
			super(path, reader);
			this.connections = conns;
		}

		@Override
		public PointListConnections getConnections() {
			return connections;
		}

		@Override
		public List<PointList> getPointLists() {
			return IndependentPair.getFirst(this);
		}

		@Override
		public List<IMAGE> getImages() {
			return IndependentPair.getSecond(this);
		}
	}

	private static class ASFDataset<IMAGE extends Image<?, IMAGE>> extends FileBackedDataset<IMAGE> {
		private static class ASFReader<IMAGE extends Image<?, IMAGE>>
				implements
				ObjectReader<IndependentPair<PointList, IMAGE>, FileObject>
		{
			private static String[] SUPPORTED_IMAGE_EXTS = { "jpg", "jpeg", "bmp", "png" };

			private InputStreamObjectReader<IMAGE> imReader;

			public ASFReader(InputStreamObjectReader<IMAGE> reader) {
				this.imReader = reader;
			}

			@Override
			public IndependentPair<PointList, IMAGE> read(FileObject source) throws IOException {
				final PointList pl = new PointList();
				BufferedReader br = null;

				try {
					br = new BufferedReader(new InputStreamReader(source.getContent().getInputStream()));

					String line;
					while ((line = br.readLine()) != null) {
						if (!line.startsWith("#")) {
							final String[] parts = line.split("\\s+");

							if (parts.length < 7)
								continue;

							final float x = Float.parseFloat(parts[2].trim());
							final float y = Float.parseFloat(parts[3].trim());

							pl.points.add(new Point2dImpl(x, y));
						}
					}
				} finally {
					if (br != null)
						try {
							br.close();
						} catch (final IOException e) {
							// ignore
						}
				}

				IMAGE image = null;
				if (imReader != null) {
					for (final String ext : SUPPORTED_IMAGE_EXTS) {
						String name = source.getName().getBaseName();
						name = name.substring(0, name.lastIndexOf(".") + 1) + ext;
						final FileObject file = source.getParent().getChild(name);

						if (file != null && file.exists()) {
							InputStream imstream = null;
							try {
								imstream = file.getContent().getInputStream();
								image = imReader.read(imstream);
								break;
							} catch (final IOException e) {
								// ignore
							} finally {
								if (imstream != null) {
									try {
										imstream.close();
									} catch (final IOException e) {
										// ignore
									}
								}
							}
						}
					}
				}

				if (image != null)
					pl.scaleXY(image.getWidth(), image.getHeight());

				return new IndependentPair<PointList, IMAGE>(pl, image);
			}

			@Override
			public boolean canRead(FileObject source, String name) {
				return name.endsWith(".asf");
			}
		}

		public ASFDataset(String path, InputStreamObjectReader<IMAGE> reader) throws IOException {
			super(path, new ASFReader<IMAGE>(reader), null);
			readConnections();
		}

		void readConnections() throws IOException
		{
			connections = new PointListConnections();
			final FileObject firstASF = this.getFileObject(0);
			BufferedReader br = null;

			try {
				br = new BufferedReader(new InputStreamReader(firstASF.getContent().getInputStream()));

				String line;
				while ((line = br.readLine()) != null) {
					if (!line.startsWith("#")) {
						final String[] parts = line.split("\\s+");

						if (parts.length < 7)
							continue;

						final int from = Integer.parseInt(parts[4].trim());
						final int to = Integer.parseInt(parts[6].trim());

						connections.addConnection(from, to);
					}
				}
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (final IOException e) {
						// ignore
					}
					;
				}
			}
		}
	}

	private static class PTSDataset<IMAGE extends Image<?, IMAGE>> extends FileBackedDataset<IMAGE> {
		private static class PTSReader<IMAGE extends Image<?, IMAGE>>
				implements
				ObjectReader<IndependentPair<PointList, IMAGE>, FileObject>
		{
			private static String[] SUPPORTED_IMAGE_EXTS = { "jpg", "jpeg", "bmp", "png" };

			private InputStreamObjectReader<IMAGE> imReader;
			private FileObject ptsPath;
			private FileObject imgsPath;

			public PTSReader(InputStreamObjectReader<IMAGE> imReader, String ptsPath, String imgsPath) throws IOException
			{
				this.imReader = imReader;

				final FileSystemManager fsManager = VFS.getManager();

				this.ptsPath = fsManager.resolveFile(ptsPath);
				this.imgsPath = fsManager.resolveFile(imgsPath);
			}

			@Override
			public IndependentPair<PointList, IMAGE> read(FileObject source) throws IOException {
				final PointList pl = new PointList();
				BufferedReader br = null;

				try {
					br = new BufferedReader(new InputStreamReader(source.getContent().getInputStream()));
					br.readLine();
					br.readLine();
					br.readLine();

					String line;
					while ((line = br.readLine()) != null) {
						if (!line.startsWith("}") && line.trim().length() > 0) {
							final String[] parts = line.split("\\s+");

							final float x = Float.parseFloat(parts[0].trim());
							final float y = Float.parseFloat(parts[1].trim());

							pl.points.add(new Point2dImpl(x, y));
						}
					}
				} finally {
					if (br != null)
						try {
							br.close();
						} catch (final IOException e) {
						}
				}

				IMAGE image = null;
				if (this.imReader != null) {
					final String relPath = ptsPath.getName().getRelativeName(source.getName());
					for (final String ext : SUPPORTED_IMAGE_EXTS) {
						final String imRelPath = relPath.substring(0, relPath.lastIndexOf(".") + 1) + ext;
						final FileObject imgPath = imgsPath.resolveFile(imRelPath);

						if (imgPath.exists()) {
							InputStream imstream = null;
							try {
								imstream = imgPath.getContent().getInputStream();
								image = imReader.read(imstream);
								break;
							} catch (final IOException e) {
								// ignore
							} finally {
								if (imstream != null) {
									try {
										imstream.close();
									} catch (final IOException e) {
										// ignore
									}
								}
							}
							break;
						}
					}
				}

				return IndependentPair.pair(pl, image);
			}

			@Override
			public boolean canRead(FileObject source, String name) {
				return name.endsWith(".pts") && !name.equals("dummy.pts");
			}
		}

		public PTSDataset(String imgsPath, String ptsPath, String modelPath, InputStreamObjectReader<IMAGE> reader)
				throws IOException
		{
			super(ptsPath, new PTSReader<IMAGE>(reader, ptsPath, imgsPath), null);
			readConnections(modelPath);
		}

		void readConnections(String path)
				throws IOException
		{
			BufferedReader br = null;
			try {
				final FileSystemManager fsManager = VFS.getManager();

				br = new BufferedReader(new InputStreamReader(fsManager.resolveFile(path).getContent().getInputStream()));
				this.connections = new PointListConnections();

				String line;
				while ((line = br.readLine()) != null) {
					if (!line.trim().startsWith("indices"))
						continue;

					final String[] data = line.trim().replace("indices(", "").replace(")", "").split(",");
					final boolean isOpen = (br.readLine().contains("open_boundary"));

					int prev = Integer.parseInt(data[0]);
					for (int i = 1; i < data.length; i++) {
						final int next = Integer.parseInt(data[i]);
						connections.addConnection(prev, next);
						prev = next;
					}

					if (!isOpen) {
						connections.addConnection(Integer.parseInt(data[data.length - 1]), Integer.parseInt(data[0]));
					}
				}
			} finally {
				try {
					if (br != null)
						br.close();
				} catch (final IOException e) {
				}
			}
		}

	}

	private ShapeModelDatasets() {
	}

	/**
	 * Create a dataset with the given data.
	 * 
	 * @param data
	 *            the image-pointset pairs
	 * @param connections
	 *            the connections across the points
	 * @return the dataset
	 */
	public static <IMAGE extends Image<?, IMAGE>> ShapeModelDataset<IMAGE> create(
			List<IndependentPair<PointList, IMAGE>> data, PointListConnections connections)
	{
		return new BasicDataset<IMAGE>(data, connections);
	}

	/**
	 * Load a dataset from ASF format files as used by the IMM dataset. If the
	 * images are present, they will also be loaded (images must have the same
	 * name as the corresponding ASF files, but with a different extension).
	 * 
	 * @see IMMFaceDatabase
	 * @see "http://commons.apache.org/proper/commons-vfs/filesystems.html"
	 * @param path
	 *            the file system path or uri. See the Apache Commons VFS2
	 *            documentation for all the details.
	 * @param reader
	 *            the reader with which to load the images
	 * 
	 * @return the dataset
	 * @throws IOException
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> ShapeModelDataset<IMAGE> loadASFDataset(String path,
			InputStreamObjectReader<IMAGE> reader) throws IOException
	{
		return new ASFDataset<IMAGE>(path, reader);
	}

	/**
	 * Load a dataset from PTS format files as used by Tim Cootes's ASM/AAM
	 * tools. If the images are present, they will also be loaded (images must
	 * have the same name as the corresponding PTS files, but with a different
	 * extension).
	 * 
	 * @param ptsDirPath
	 *            the directory containing the pts files
	 * @param imgDirPath
	 *            the directory containing the images
	 * @param modelFilePath
	 *            the path to the model (connections) file
	 * 
	 * @see IMMFaceDatabase
	 * @see "http://commons.apache.org/proper/commons-vfs/filesystems.html"
	 * @param reader
	 *            the reader with which to load the images
	 * 
	 * @return the dataset
	 * @throws IOException
	 *             if an error occurs
	 */
	public static <IMAGE extends Image<?, IMAGE>> ShapeModelDataset<IMAGE> loadPTSDataset(String ptsDirPath,
			String imgDirPath, String modelFilePath,
			InputStreamObjectReader<IMAGE> reader) throws IOException
	{
		return new PTSDataset<IMAGE>(imgDirPath, ptsDirPath, modelFilePath, reader);
	}
}
