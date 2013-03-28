package org.openimaj.data.dataset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileTypeSelector;
import org.apache.commons.vfs2.VFS;
import org.openimaj.io.ObjectReader;

public class VFSGroupDataset<INSTANCE> extends ReadableGroupDataset<String, VFSListDataset<INSTANCE>, INSTANCE> {
	private Map<String, VFSListDataset<INSTANCE>> files = new HashMap<String, VFSListDataset<INSTANCE>>();
	private Map<String, FileObject> directoryInfo = new HashMap<String, FileObject>();
	private FileObject base;

	/**
	 * Construct a grouped dataset from any virtual file system source (local
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
	public VFSGroupDataset(final String path, final ObjectReader<INSTANCE> reader) throws FileSystemException {
		super(reader);

		final FileSystemManager fsManager = VFS.getManager();
		base = fsManager.resolveFile(path);

		final FileObject[] folders = base.findFiles(new FileTypeSelector(FileType.FOLDER));

		for (final FileObject folder : folders) {
			directoryInfo.put(folder.getName().getBaseName(), folder);
			files.put(folder.getName().getBaseName(), new VFSListDataset<INSTANCE>(folder.getName().getURI(), reader));
		}
	}

	/**
	 * Get the underlying file descriptors of the directories that form the
	 * groups of the dataset
	 * 
	 * @return the array of file objects
	 */
	public Map<String, FileObject> getGroupDirectories() {
		return directoryInfo;
	}

	/**
	 * Get the underlying file descriptor for a particular group in the dataset.
	 * 
	 * @param key
	 *            key of the group
	 * 
	 * @return the file object corresponding to the instance
	 */
	public FileObject getFileObject(String key) {
		return directoryInfo.get(key);
	}

	@Override
	public String toString() {
		return String.format("%s(%d groups with a total of %d instances)", this.getClass().getName(), this.size(),
				this.numInstances());
	}

	@Override
	public Set<Entry<String, VFSListDataset<INSTANCE>>> entrySet() {
		return files.entrySet();
	}
}
