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
import org.openimaj.data.identity.Identifiable;
import org.openimaj.io.ObjectReader;

/**
 * A {@link GroupedDataset} of {@link VFSListDataset}s backed by directories of
 * items (either locally or remotely), or items stored in a hierarchical
 * structure within a compressed archive.
 * <p>
 * This implementation only supports a basic grouped dataset with {@link String}
 * keys created from the names of directories, and {@link VFSListDataset} values
 * from all the readable files within each directory.
 * <p>
 * As an example, this class can be used to easily create a
 * {@link GroupedDataset} from a directory containing directories of images:
 * 
 * <pre>
 * GroupDataset&lt;String, VFSListDataset&lt;FImage&gt;, FImage&gt; dataset = new VFSGroupDataset&lt;FImage&gt;(
 * 		&quot;/path/to/directory/of/images&quot;,
 * 		ImageUtilities.FIMAGE_READER);
 * </pre>
 * 
 * a zip file of directories of images:
 * 
 * <pre>
 * GroupDataset&lt;String, VFSListDataset&lt;FImage&gt;, FImage&gt; dataset = new VFSGroupDataset&lt;FImage&gt;(
 * 		&quot;zip:file:/path/to/images.zip&quot;, ImageUtilities.FIMAGE_READER);
 * </pre>
 * 
 * or even a remote zip of directories of images hosted via http:
 * 
 * <pre>
 * GroupDataset&lt;String, VFSListDataset&lt;FImage&gt;, FImage&gt; dataset = new VFSGroupDataset&lt;FImage&gt;(
 * 		&quot;zip:http://localhost/&tilde;jsh2/thumbnails.zip&quot;, ImageUtilities.FIMAGE_READER);
 * </pre>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <INSTANCE>
 *            The type of instance in the dataset
 */
public class VFSGroupDataset<INSTANCE> extends ReadableGroupDataset<String, VFSListDataset<INSTANCE>, INSTANCE>
		implements
			Identifiable
{
	private Map<String, VFSListDataset<INSTANCE>> files = new HashMap<String, VFSListDataset<INSTANCE>>();
	private Map<String, FileObject> directoryInfo = new HashMap<String, FileObject>();
	private FileObject base;

	/**
	 * Construct a grouped dataset from any virtual file system source (local
	 * directory, remote zip file, etc). Only the child directories under the
	 * given path will be used to create groups; the contents of any
	 * sub-directories will be merged automatically. Only directories with
	 * readable items as children will be included in the resultant dataset.
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
			final VFSListDataset<INSTANCE> list = new VFSListDataset<INSTANCE>(folder.getName().getURI(), reader);

			if (list.size() > 0)
				files.put(folder.getName().getBaseName(), list);
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

	@Override
	public String getID() {
		return base.getName().getBaseName();
	}
}
