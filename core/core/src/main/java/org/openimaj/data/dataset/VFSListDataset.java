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
package org.openimaj.data.dataset;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.io.IOUtils;
import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.io.ObjectReader;
import org.openimaj.util.array.ArrayIterator;

/**
 * A {@link ListDataset} backed by a directory of items (either locally or
 * remotely), or items stored in a compressed archive.
 * <p>
 * As an example, this class can be used to easily create a {@link ListDataset}
 * from a directory of images:
 * 
 * <pre>
 * ListDataset&lt;FImage&gt; dataset = new VFSListDataset&lt;FImage&gt;(&quot;/path/to/directory/of/images&quot;,
 * 		ImageUtilities.FIMAGE_READER);
 * </pre>
 * 
 * a zip file of images:
 * 
 * <pre>
 * ListDataset&lt;FImage&gt; dataset = new VFSListDataset&lt;FImage&gt;(
 * 		&quot;zip:file:/path/to/images.zip&quot;, ImageUtilities.FIMAGE_READER);
 * </pre>
 * 
 * or even a remote zip of images hosted via http:
 * 
 * <pre>
 * ListDataset&lt;FImage&gt; dataset = new VFSListDataset&lt;FImage&gt;(
 * 		&quot;zip:http://localhost/&tilde;jsh2/thumbnails.zip&quot;, ImageUtilities.FIMAGE_READER);
 * </pre>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <INSTANCE>
 *            The type of instance in the dataset
 */
public class VFSListDataset<INSTANCE> extends ReadableListDataset<INSTANCE, FileObject> implements Identifiable {
	/**
	 * An adaptor that lets {@link InputStreamObjectReader}s be used as a
	 * {@link ObjectReader} with a {@link FileObject} source type.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 * @param <INSTANCE>
	 *            The type of instance that the {@link InputStreamObjectReader}
	 *            produces
	 */
	public static class FileObjectISReader<INSTANCE> implements ObjectReader<INSTANCE, FileObject> {
		private InputStreamObjectReader<INSTANCE> streamReader;

		/**
		 * Construct with the given {@link InputStreamObjectReader}
		 * 
		 * @param reader
		 *            the {@link InputStreamObjectReader}
		 */
		public FileObjectISReader(InputStreamObjectReader<INSTANCE> reader) {
			this.streamReader = reader;
		}

		@Override
		public INSTANCE read(FileObject source) throws IOException {
			FileContent content = null;
			try {
				content = source.getContent();
				return streamReader.read(content.getInputStream());
			} finally {
				if (content != null)
					content.close();
			}
		}

		@Override
		public boolean canRead(FileObject source, String name) {
			BufferedInputStream stream = null;
			try {
				stream = new BufferedInputStream(source.getContent().getInputStream());

				return IOUtils.canRead(streamReader, stream, source.getName().getBaseName());
			} catch (final IOException e) {
				// ignore
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (final IOException e) {
						// ignore
					}
				}
			}

			return false;
		}

	}

	private FileObject[] files;
	private FileObject base;

	/**
	 * Construct a list dataset from any virtual file system source (local
	 * directory, remote zip file, etc).
	 * 
	 * @see "http://commons.apache.org/proper/commons-vfs/filesystems.html"
	 * @param path
	 *            the file system path or uri. See the Apache Commons VFS2
	 *            documentation for all the details.
	 * @param reader
	 *            the {@link InputStreamObjectReader} that reads the data from
	 *            the VFS
	 * @throws FileSystemException
	 *             if an error occurs accessing the VFS
	 */
	public VFSListDataset(final String path, final InputStreamObjectReader<INSTANCE> reader) throws FileSystemException {
		this(path, new FileObjectISReader<INSTANCE>(reader));
	}

	/**
	 * Construct a list dataset from any virtual file system source (local
	 * directory, remote zip file, etc).
	 * 
	 * @see "http://commons.apache.org/proper/commons-vfs/filesystems.html"
	 * @param path
	 *            the file system path or uri. See the Apache Commons VFS2
	 *            documentation for all the details.
	 * @param reader
	 *            the {@link ObjectReader} that reads the data from the VFS
	 * @throws FileSystemException
	 *             if an error occurs accessing the VFS
	 */
	public VFSListDataset(final String path, final ObjectReader<INSTANCE, FileObject> reader) throws FileSystemException {
		super(reader);

		final FileSystemManager fsManager = VFS.getManager();
		base = fsManager.resolveFile(path);

		files = base.findFiles(new FileSelector() {

			@Override
			public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
				return true;
			}

			@Override
			public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
				if (fileInfo.getFile().getType() == FileType.FILE) {
					return IOUtils.canRead(reader, fileInfo.getFile(), fileInfo.getFile().getName().getBaseName());
				}

				return false;
			}
		});
	}

	/**
	 * Get the underlying file descriptors of the files in the dataset
	 * 
	 * @return the array of file objects
	 */
	public FileObject[] getFileObjects() {
		return files;
	}

	/**
	 * Get the underlying file descriptor for a particular instance in the
	 * dataset.
	 * 
	 * @param index
	 *            index of the instance
	 * 
	 * @return the file object corresponding to the instance
	 */
	public FileObject getFileObject(int index) {
		return files[index];
	}

	@Override
	public INSTANCE getInstance(int index) {
		try {
			return read(files[index]);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int numInstances() {
		if (files == null)
			return 0;
		return files.length;
	}

	private INSTANCE read(FileObject file) throws IOException {
		return reader.read(file);
	}

	@Override
	public Iterator<INSTANCE> iterator() {
		return new Iterator<INSTANCE>() {
			ArrayIterator<FileObject> filesIterator = new ArrayIterator<FileObject>(files);

			@Override
			public boolean hasNext() {
				return filesIterator.hasNext();
			}

			@Override
			public INSTANCE next() {
				try {
					return read(filesIterator.next());
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void remove() {
				filesIterator.remove();
			}
		};
	}

	@Override
	public String getID(int index) {
		try {
			return base.getName().getRelativeName(files[index].getName());
		} catch (final FileSystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return String.format("%s(%d instances)", this.getClass().getName(), this.files.length);
	}

	@Override
	public String getID() {
		return base.getName().getBaseName();
	}
}
