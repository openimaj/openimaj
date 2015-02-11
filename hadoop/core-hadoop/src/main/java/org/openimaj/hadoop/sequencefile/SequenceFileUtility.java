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
package org.openimaj.hadoop.sequencefile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.SequenceFile.Metadata;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * Base class for a utility class that deals with specifically typed sequence
 * files.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <K>
 *            Key type
 * @param <V>
 *            Value type
 */
public abstract class SequenceFileUtility<K extends Writable, V extends Writable> implements Iterable<Entry<K, V>> {
	protected Configuration config = new Configuration();
	protected FileSystem fileSystem;
	protected Path sequenceFilePath;

	protected Writer writer;
	protected CompressionType compressionType = CompressionType.BLOCK;

	protected boolean isReader;

	protected String uuid;

	public SequenceFileUtility(String uriOrPath, boolean read) throws IOException {
		setup(convertToURI(uriOrPath), read);
	}

	public SequenceFileUtility(URI uri, boolean read) throws IOException {
		setup(uri, read);
	}

	public SequenceFileUtility(String uriOrPath, CompressionType compressionType) throws IOException {
		this.compressionType = compressionType;
		setup(convertToURI(uriOrPath), false);
	}

	public SequenceFileUtility(URI uri, CompressionType compressionType) throws IOException {
		this.compressionType = compressionType;
		setup(uri, false);
	}

	/**
	 * Get a list of all the reducer outputs in a directory. If the given
	 * path/uri is not a directory, then it is assumed that it is a SequenceFile
	 * and returned directly.
	 *
	 * @param uriOrPath
	 *            the path or uri
	 * @return the reducer outputs
	 * @throws IOException
	 */
	public static URI[] getReducerFiles(String uriOrPath) throws IOException {
		return getFiles(uriOrPath, "part-r-");
	}

	/**
	 * Get a list of all the sequence files (with a given name prefix) in a
	 * directory. If the given uri is not a directory, then it is assumed that
	 * it is a SequenceFile and returned directly.
	 *
	 * @param uriOrPath
	 *            the path or uri
	 * @param filenamePrefix
	 *            the prefix of the file name
	 * @return the matching files
	 * @throws IOException
	 */
	public static URI[] getFiles(String uriOrPath, final String filenamePrefix) throws IOException {
		final Configuration config = new Configuration();
		final URI uri = convertToURI(uriOrPath);
		final FileSystem fs = FileSystem.get(uri, config);
		final Path path = new Path(uri.toString());

		if (fs.getFileStatus(path).isDirectory()) {
			final FileStatus[] files = fs.listStatus(path, new PathFilter() {
				@Override
				public boolean accept(Path p) {
					return p.getName().startsWith(filenamePrefix);
				}
			});

			final URI[] uris = new URI[files.length];
			int i = 0;
			for (final FileStatus status : files) {
				uris[i++] = status.getPath().toUri();
			}
			return uris;
		} else {
			return new URI[] { uri };
		}
	}

	/**
	 * Get a list of all the sequence files (with a given name prefix) in the
	 * set of input paths. If a given uri is not a directory, then it is assumed
	 * that it is a SequenceFile and returned directly.
	 *
	 * @param uriOrPaths
	 *            the paths or uris
	 * @param filenamePrefix
	 *            the prefix of the file name
	 * @return the list of sequence files
	 * @throws IOException
	 */
	public static Path[] getFilePaths(String[] uriOrPaths, String filenamePrefix) throws IOException {
		final List<Path> pathList = new ArrayList<Path>();
		for (final String uriOrPath : uriOrPaths) {
			final Path[] paths = getFilePaths(uriOrPath, filenamePrefix);
			for (final Path path : paths) {
				pathList.add(path);
			}
		}
		return pathList.toArray(new Path[pathList.size()]);
	}

	/**
	 * Get a list of all the sequence files (with a given name prefix) in the
	 * set of input paths.
	 * <p>
	 * Optionally a subdirectory can be provided; if provided the subdirectory
	 * is appended to each path (i.e. PATH/subdirectory).
	 * <p>
	 * If the given uri is not a directory, then it is assumed that it is a
	 * single SequenceFile and returned directly.
	 *
	 * @param uriOrPaths
	 *            the URI or path to the directory/file
	 * @param subdir
	 *            the optional subdirectory (may be null)
	 * @param filenamePrefix
	 *            the prefix of the file name
	 * @return the list of sequence files
	 * @throws IOException
	 */
	public static Path[] getFilePaths(String[] uriOrPaths, String subdir, String filenamePrefix) throws IOException {
		final List<Path> pathList = new ArrayList<Path>();

		for (String uriOrPath : uriOrPaths) {
			if (subdir != null)
				uriOrPath += "/" + subdir;

			final Path[] paths = getFilePaths(uriOrPath, filenamePrefix);
			for (final Path path : paths) {
				pathList.add(path);
			}
		}
		return pathList.toArray(new Path[pathList.size()]);
	}

	/**
	 * Get a list of all the sequence files (with a given name prefix) in a
	 * directory. If the given uri is not a directory, then it is assumed that
	 * it is a SequenceFile and returned directly.
	 *
	 * @param uriOrPath
	 *            the path or uri
	 * @param filenamePrefix
	 *            the prefix of the file name
	 * @return the list of sequence files
	 * @throws IOException
	 */
	public static Path[] getFilePaths(String uriOrPath, final String filenamePrefix) throws IOException {
		final Configuration config = new Configuration();
		final URI uri = convertToURI(uriOrPath);
		final FileSystem fs = FileSystem.get(uri, config);

		final Path path = new Path(uri);

		if (fs.getFileStatus(path).isDirectory()) {
			final FileStatus[] files = fs.listStatus(path, new PathFilter() {
				@Override
				public boolean accept(Path p) {
					return p.getName().startsWith(filenamePrefix);
				}
			});

			final Path[] uris = new Path[files.length];
			int i = 0;
			for (final FileStatus status : files) {
				uris[i++] = status.getPath();
			}
			return uris;
		} else {
			return new Path[] { path };
		}
	}

	/**
	 * Get a list of all the sequence files whose names match the given regular
	 * expression in a directory. If the given uri is not a directory, then it
	 * is assumed that it is a SequenceFile and returned directly.
	 *
	 * @param uriOrPath
	 *            the path or uri
	 * @param regex
	 *            the regular expression to match
	 * @return a list of files
	 * @throws IOException
	 */
	public static URI[] getFilesRegex(String uriOrPath, final String regex) throws IOException {
		final Configuration config = new Configuration();
		final URI uri = convertToURI(uriOrPath);
		final FileSystem fs = FileSystem.get(uri, config);
		final Path path = new Path(uri.toString());

		if (fs.getFileStatus(path).isDirectory()) {
			final FileStatus[] files = fs.listStatus(path, new PathFilter() {
				@Override
				public boolean accept(Path p) {
					return (regex == null || p.getName().matches(regex));
				}
			});

			final URI[] uris = new URI[files.length];
			int i = 0;
			for (final FileStatus status : files) {
				uris[i++] = status.getPath().toUri();
			}
			return uris;
		} else {
			return new URI[] { uri };
		}
	}

	/**
	 * Return a list of the keys in the sequence file. Read mode only.
	 *
	 * @return keys.
	 */
	@SuppressWarnings("unchecked")
	public Map<K, Long> listKeysAndOffsets() {
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot read keys in write mode");
		}

		Reader reader = null;
		try {
			final Map<K, Long> keys = new LinkedHashMap<K, Long>();

			reader = createReader();
			final Class<K> keyClass = (Class<K>) reader.getKeyClass();
			K key = ReflectionUtils.newInstance(keyClass, config);
			final V val = ReflectionUtils.newInstance((Class<V>) reader.getValueClass(), config);
			long start = 0L;
			long end = 0L;
			while (reader.next(key, val)) {
				final long pos = reader.getPosition();
				if (pos != end) {
					start = end;
					end = pos;
				}
				keys.put(key, start);
				key = ReflectionUtils.newInstance(keyClass, config);
			}

			return keys;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	/**
	 * Go through a sequence file, applying each
	 * {@link RecordInformationExtractor} to each key, printing out the results
	 * in order to the provided {@link PrintStream}
	 *
	 * @param extractors
	 *            the {@link RecordInformationExtractor}s to apply
	 * @param stream
	 *            the stream to write to
	 * @param delim
	 */
	@SuppressWarnings("unchecked")
	public void extract(List<RecordInformationExtractor> extractors, PrintStream stream, String delim) {
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot read keys in write mode");
		}

		Reader reader = null;
		try {
			reader = createReader();

			final Class<K> keyClass = (Class<K>) reader.getKeyClass();
			K key = ReflectionUtils.newInstance(keyClass, config);
			final V val = ReflectionUtils.newInstance((Class<V>) reader.getValueClass(), config);
			long start = 0L;
			long end = 0L;
			int count = 0;
			while (reader.next(key, val)) {
				final long pos = reader.getPosition();
				if (pos != end) {
					start = end;
					end = pos;
				}

				// Apply the filters and print
				String recordString = "";
				for (final RecordInformationExtractor extractor : extractors) {
					recordString += extractor.extract(key, val, start, sequenceFilePath) + delim;
				}

				if (recordString.length() >= delim.length())
					recordString = recordString.substring(0, recordString.length() - delim.length());

				stream.println(recordString);
				count++;

				System.err.printf("\rOutputted: %10d", count);
				key = ReflectionUtils.newInstance(keyClass, config);
			}
			System.err.println();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	public SequenceFileUtility(String uriOrPath, CompressionType compressionType, Map<String, String> metadata)
			throws IOException
	{
		this.compressionType = compressionType;
		setup(convertToURI(uriOrPath), false);
	}

	public SequenceFileUtility(URI uri, CompressionType compressionType, Map<String, String> metadata) throws IOException
	{
		this.compressionType = compressionType;
		setup(uri, false);
	}

	/**
	 * Converts a string representing a file or uri to a uri object.
	 *
	 * @param uriOrPath
	 *            uri or path to convert
	 * @return uri
	 */
	public static URI convertToURI(String uriOrPath) {
		if (uriOrPath.contains("://")) {
			return URI.create(uriOrPath);
		} else {
			return new File(uriOrPath).toURI();
		}
	}

	private void setup(URI uri, boolean read) throws IOException {
		setup(uri, read, null);
	}

	private void setup(URI uri, boolean read, Map<String, String> metadata) throws IOException {
		fileSystem = getFileSystem(uri);
		sequenceFilePath = new Path(uri.toString());

		this.isReader = read;

		if (read) {
			Reader reader = null;

			try {
				reader = createReader();
				final Text uuidText = reader.getMetadata().get(new Text(MetadataConfiguration.UUID_KEY));
				if (uuidText != null)
					uuid = uuidText.toString();

				if (!reader.isCompressed())
					compressionType = CompressionType.NONE;
				else if (reader.isBlockCompressed())
					compressionType = CompressionType.BLOCK;
				else
					compressionType = CompressionType.RECORD;
			} catch (final Exception e) {
				throw new RuntimeException(e);
			} finally {
				if (reader != null)
					try {
						reader.close();
					} catch (final IOException e1) {
					}
			}
		} else {
			if (metadata == null) {
				metadata = new HashMap<String, String>();
			}

			if (!metadata.containsKey(MetadataConfiguration.UUID_KEY)) {
				uuid = UUID.randomUUID().toString();
				metadata.put(MetadataConfiguration.UUID_KEY, uuid);
			}

			// if the output directory is a directory, then create the file
			// inside the
			// directory with the name given by the uuid
			if (fileSystem.exists(sequenceFilePath) && fileSystem.getFileStatus(sequenceFilePath).isDirectory()) {
				sequenceFilePath = new Path(sequenceFilePath, uuid + ".seq");
			}

			writer = createWriter(metadata);
		}
	}

	@SuppressWarnings("unchecked")
	private Writer createWriter(Map<String, String> metadata) throws IOException {
		final Metadata md = new Metadata();

		for (final Entry<String, String> e : metadata.entrySet()) {
			md.set(new Text(e.getKey()), new Text(e.getValue()));
		}
		final Class<K> keyClass = (Class<K>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		final Class<V> valueClass = (Class<V>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[1];

		return SequenceFile.createWriter(fileSystem, config, sequenceFilePath, keyClass, valueClass, compressionType,
				new DefaultCodec(), null,
				md);
	}

	private Reader createReader() throws IOException {
		// if(this.fileSystem.getFileStatus(sequenceFilePath).isDir())
		// sequenceFilePath = new Path(sequenceFilePath,"part-r-00000");
		return new Reader(fileSystem, sequenceFilePath, config);
	}

	/**
	 * Get the UUID of this file
	 *
	 * @return UUID
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * Return the metadata map. Read mode only.
	 *
	 * @return metadata
	 */
	public Map<Text, Text> getMetadata() {
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot read metadata in write mode");
		}

		Reader reader = null;
		try {
			reader = createReader();
			final Map<Text, Text> metadata = reader.getMetadata().getMetadata();
			return metadata;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	/**
	 * Return a list of the keys in the sequence file. Read mode only.
	 *
	 * @return keys.
	 */
	@SuppressWarnings("unchecked")
	public List<K> listKeys() {
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot read keys in write mode");
		}

		Reader reader = null;
		try {
			final List<K> keys = new ArrayList<K>();

			reader = createReader();
			final Class<K> keyClass = (Class<K>) reader.getKeyClass();
			K key = ReflectionUtils.newInstance(keyClass, config);

			while (reader.next(key)) {
				keys.add(key);
				key = ReflectionUtils.newInstance(keyClass, config);
			}

			return keys;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	/**
	 * Extracts file to a directory. Read mode only.
	 *
	 * @param uriOrPath
	 *            path or uri to extract to.
	 * @throws IOException
	 */
	public void exportData(String uriOrPath) throws IOException {
		exportData(uriOrPath, NamingStrategy.KEY, new ExtractionState(), false, 0);
	}

	/**
	 * Extracts file to a directory. Read mode only.
	 *
	 * @param uriOrPath
	 *            path or uri to extract to.
	 * @param naming
	 *            the naming strategy
	 * @param extrState
	 *            the extraction state
	 * @param addExtension
	 *            if true, then file extensions are added to each record
	 *            automatically
	 * @param offset
	 *            offset from which to start. Can be used to reduce number of
	 *            files extracted.
	 * @throws IOException
	 */
	public void exportData(String uriOrPath, NamingStrategy naming, ExtractionState extrState, boolean addExtension,
			long offset)
			throws IOException
	{
		FileSystem fs = null;
		Path p = null;

		if (uriOrPath != null) {
			final URI uri = convertToURI(uriOrPath);

			fs = getFileSystem(uri);
			p = new Path(uri.toString());
		}

		exportData(fs, p, naming, extrState, addExtension, offset);
	}

	public static ZipOutputStream openZipOutputStream(String uriOrPath) throws IOException {
		final URI uri = convertToURI(uriOrPath);

		final FileSystem fs = getFileSystem(uri, new Configuration());
		final Path path = new Path(uri.toString());

		final ZipOutputStream zos = new ZipOutputStream(fs.create(path));
		zos.setLevel(Deflater.BEST_COMPRESSION);
		return zos;
	}

	/**
	 * Extracts file to a directory. Read mode only.
	 *
	 * @param uriOrPath
	 *            path or uri to extract to.
	 * @param naming
	 *            the naming strategy
	 * @param state
	 *            the extraction state
	 * @param addExtension
	 *            if true, then file extensions are added to each record
	 *            automatically
	 * @param offset
	 *            offset from which to start. Can be used to reduce number of
	 *            files extracted.
	 * @throws IOException
	 */
	public void exportDataToZip(String uriOrPath, NamingStrategy naming, ExtractionState state, boolean addExtension,
			long offset)
			throws IOException
	{
		if (uriOrPath != null) {

			ZipOutputStream zos = null;
			try {
				zos = openZipOutputStream(uriOrPath);
				exportDataToZip(zos, naming, state, addExtension, offset);
			} finally {
				if (zos != null)
					try {
						zos.close();
					} catch (final IOException e) {
					}
				;
			}
		}
	}

	/**
	 * Extracts file to a zip file. Read mode only.
	 *
	 * @param zos
	 *            The {@link ZipOutputStream} to write to
	 * @param naming
	 *            The naming strategy
	 * @param extrState
	 *            The extration state
	 * @param addExtension
	 *            if true, then file extensions are added to each record
	 *            automatically
	 * @param offset
	 *            offset from which to start. Can be used to reduce number of
	 *            files extracted.
	 * @throws IOException
	 */
	public void exportDataToZip(ZipOutputStream zos, NamingStrategy naming, ExtractionState extrState,
			boolean addExtension, long offset)
			throws IOException
	{
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot read keys in write mode");
		}

		Reader reader = null;
		try {
			reader = createReader();
			if (offset > 0)
				reader.seek(offset);

			@SuppressWarnings("unchecked")
			final K key = ReflectionUtils.newInstance((Class<K>) reader.getKeyClass(), config);
			@SuppressWarnings("unchecked")
			final V val = ReflectionUtils.newInstance((Class<V>) reader.getValueClass(), config);

			while (reader.next(key)) {

				if (extrState.allowNext()) {
					reader.getCurrentValue(val);

					String name = naming.getName(key, val, extrState, addExtension);

					while (name.startsWith("/"))
						name = name.substring(1);

					final ZipEntry ze = new ZipEntry(name);
					zos.putNextEntry(ze);
					writeZipData(zos, val);
					zos.closeEntry();

					extrState.tick();
				} else {
					extrState.tick();
				}
				if (extrState.isFinished())
					break;
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	/**
	 * Extracts file to a directory. Read mode only.
	 *
	 * @param fs
	 *            filesystem of output file
	 * @param dirPath
	 *            path to extract to
	 */
	public void exportData(FileSystem fs, Path dirPath) {
		exportData(fs, dirPath, NamingStrategy.KEY, new ExtractionState(), false, 0);
	}

	/**
	 * Extracts file to a directory. Read mode only.
	 *
	 * @param fs
	 *            filesystem of output file
	 * @param dirPath
	 *            path to extract to
	 * @param naming
	 *            the naming strategy
	 * @param extrState
	 *            the extraction state
	 * @param addExtension
	 *            if true, then file extensions are added to each record
	 *            automatically
	 * @param offset
	 *            offset from which to start. Can be used to reduce number of
	 *            files extracted.
	 */
	@SuppressWarnings("unchecked")
	public void exportData(FileSystem fs, Path dirPath, NamingStrategy naming, ExtractionState extrState,
			boolean addExtension, long offset)
	{
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot read keys in write mode");
		}

		Reader reader = null;
		try {
			if (fs != null)
				fs.mkdirs(dirPath);

			reader = createReader();
			if (offset > 0)
				reader.seek(offset);

			final K key = ReflectionUtils.newInstance((Class<K>) reader.getKeyClass(), config);
			final V val = ReflectionUtils.newInstance((Class<V>) reader.getValueClass(), config);

			while (reader.next(key)) {

				if (extrState.allowNext()) {
					reader.getCurrentValue(val);
					if (dirPath != null) {
						String name = naming.getName(key, val, extrState, addExtension);
						if (name.startsWith("/"))
							name = "." + name;

						final Path outFilePath = new Path(dirPath, name);
						// System.out.println("NP: " + np);
						System.out.println("Path: " + outFilePath);
						writeFile(fs, outFilePath, val);
						extrState.tick();
					} else {
						System.out.println(key.toString());
						printFile(val);
						extrState.tick();
					}
				} else {
					extrState.tick();
				}
				if (extrState.isFinished())
					break;
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	@SuppressWarnings("unchecked")
	public void exportData(NamingStrategy np, ExtractionState nps, long offset, KeyValueDump<K, V> dump) {
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot read keys in write mode");
		}

		Reader reader = null;
		try {

			reader = createReader();
			if (offset > 0)
				reader.seek(offset);

			final K key = ReflectionUtils.newInstance((Class<K>) reader.getKeyClass(), config);
			final V val = ReflectionUtils.newInstance((Class<V>) reader.getValueClass(), config);

			while (reader.next(key)) {

				if (nps.allowNext()) {
					reader.getCurrentValue(val);
					dump.dumpValue(key, val);
				}
				nps.tick();
				if (nps.isFinished())
					break;
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	/**
	 * Close the underlying writer. Does nothing in read mode.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (writer != null)
			writer.close();
	}

	/**
	 * Get number of records in file. Read mode only.
	 *
	 * @return number of records
	 */
	public long getNumberRecords() {
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot read keys in write mode");
		}

		Reader reader = null;
		try {
			reader = createReader();

			final Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), config);

			long count = 0;
			while (reader.next(key)) {
				count++;
			}
			return count;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	/**
	 * @return the compression codec in use for this file.
	 */
	public Class<? extends CompressionCodec> getCompressionCodecClass() {
		if (!isReader)
			return DefaultCodec.class;

		Reader reader = null;
		try {
			reader = createReader();
			if (reader.getCompressionCodec() == null)
				return null;
			return reader.getCompressionCodec().getClass();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	/**
	 * @return he compression mode used for this sequence file.
	 */
	public CompressionType getCompressionType() {
		return compressionType;
	}

	/**
	 * Get the filesystem associated with a uri.
	 *
	 * @param uri
	 * @return the filesystem
	 * @throws IOException
	 */
	public FileSystem getFileSystem(URI uri) throws IOException {
		return getFileSystem(uri, config);
	}

	/**
	 * Get the filesystem associated with a uri.
	 *
	 * @param uri
	 * @param config
	 * @return the filesystem
	 * @throws IOException
	 */
	public static FileSystem getFileSystem(URI uri, Configuration config) throws IOException {
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem)
			fs = ((LocalFileSystem) fs).getRaw();
		return fs;
	}

	/**
	 * Get a path from a uri.
	 *
	 * @param uri
	 * @return the path
	 * @throws IOException
	 */
	public Path getPath(URI uri) throws IOException {
		return new Path(uri.toString());
	}

	/**
	 * Get the MD5 sum of a file
	 *
	 * @param fs
	 * @param p
	 * @return the md5sum
	 */
	public static String md5sum(FileSystem fs, Path p) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (final NoSuchAlgorithmException e1) {
			throw new RuntimeException(e1);
		}

		InputStream is = null;
		try {
			final byte[] buffer = new byte[8192];
			int read = 0;

			is = fs.open(p);
			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			final byte[] md5sum = digest.digest();

			final BigInteger bigInt = new BigInteger(1, md5sum);
			return bigInt.toString(16);
		} catch (final IOException e) {
			throw new RuntimeException("Unable to process file for MD5", e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (final IOException e) {
			}
		}
	}

	protected abstract V readFile(FileSystem fs, Path path) throws IOException;

	protected abstract void writeFile(FileSystem fs, Path path, V value) throws IOException;

	protected abstract void writeZipData(ZipOutputStream zos, V value) throws IOException;

	protected abstract void printFile(V value) throws IOException;

	/**
	 * Append data read from a file to the sequence file.
	 *
	 * @param key
	 * @param fs
	 * @param p
	 * @throws IOException
	 */
	public void appendFile(K key, FileSystem fs, Path p) throws IOException {
		if (isReader) {
			throw new UnsupportedOperationException("Cannot write data in read mode");
		}

		writer.append(key, readFile(fs, p));
	}

	/**
	 * Append data to a sequence file.
	 *
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void appendData(K key, V value) throws IOException {
		if (isReader) {
			throw new UnsupportedOperationException("Cannot write data in read mode");
		}
		writer.append(key, value);
	}

	/**
	 * Interface for objects that can make a key from a path
	 *
	 * @param <K>
	 */
	public interface KeyProvider<K> {
		K getKey(FileSystem fs, Path path);

		K getKey(FileSystem fs, Path path, Path base);
	}

	/**
	 * A class that provides Text keys by calculating a UUID from the MD5 of a
	 * file
	 */
	public static class MD5UUIDKeyProvider implements KeyProvider<Text> {
		@Override
		public Text getKey(FileSystem fs, Path path) {
			final UUID uuid = UUID.nameUUIDFromBytes(SequenceFileUtility.md5sum(fs, path).getBytes());
			return new Text(uuid.toString());
		}

		@Override
		public Text getKey(FileSystem fs, Path path, Path base) {
			return this.getKey(fs, path);
		}
	}

	/**
	 * A class that provides Text keys from the name of a file
	 */
	public static class FilenameKeyProvider implements KeyProvider<Text> {
		@Override
		public Text getKey(FileSystem fs, Path path) {
			return new Text(path.getName());
		}

		@Override
		public Text getKey(FileSystem fs, Path path, Path base) {
			return this.getKey(fs, path);
		}
	}

	/**
	 * A class that provides Text keys from the relative path + name of a file
	 */
	public static class RelativePathFilenameKeyProvider implements KeyProvider<Text> {
		@Override
		public Text getKey(FileSystem fs, Path path) {
			return new Text(path.toUri().getPath());
		}

		@Override
		public Text getKey(FileSystem fs, Path path, Path base) {
			return new Text(path.toUri().getPath().substring(base.toUri().getPath().length()));
		}
	}

	/**
	 * Append files to a sequenceFile.
	 *
	 * @param fs
	 *            The filesystem of the files being added.
	 * @param path
	 *            The path of the file(s) being added.
	 * @param recurse
	 *            If true, then subdirectories are also searched
	 * @param pathFilter
	 *            Filter for omitting files. Can be null.
	 * @param keyProvider
	 *            Object that can return a key for a given file.
	 * @return Paths and their respective keys for files that were added.
	 * @throws IOException
	 */
	public Map<Path, K> appendFiles(FileSystem fs, Path path, boolean recurse, PathFilter pathFilter,
			KeyProvider<K> keyProvider)
			throws IOException
	{
		final LinkedHashMap<Path, K> addedFiles = new LinkedHashMap<Path, K>();
		appendFiles(fs, path, recurse, pathFilter, keyProvider, addedFiles);
		return addedFiles;
	}

	private void appendFiles(final FileSystem fs, Path path, boolean recurse, PathFilter pathFilter,
			KeyProvider<K> keyProvider,
			Map<Path, K> addedFiles) throws IOException
	{
		if (fs.isFile(path)) {
			if (pathFilter == null || pathFilter.accept(path)) {
				final K key = keyProvider.getKey(fs, path);
				appendFile(key, fs, path);
				addedFiles.put(path, key);
			}
		} else if (recurse) {
			// fs.listStatus(path);
			final FileStatus[] status = fs.listStatus(path, new PathFilter() {

				@Override
				public boolean accept(Path potential) {
					try {
						fs.getStatus(potential);
						return true;
					} catch (final IOException e) {
						return false;
					}
				}

			});
			for (final FileStatus stat : status) {
				appendFiles(fs, stat.getPath(), path.getParent(), pathFilter, keyProvider, addedFiles);
			}
		}
	}

	private void appendFiles(FileSystem fs, Path path, Path base, PathFilter pathFilter, KeyProvider<K> keyProvider,
			Map<Path, K> addedFiles)
			throws IOException
	{
		if (fs.isFile(path)) {
			if (pathFilter == null || pathFilter.accept(path)) {
				final K key = keyProvider.getKey(fs, path, base);
				appendFile(key, fs, path);
				addedFiles.put(path, key);
			}
		} else {
			try {
				final FileStatus[] status = fs.listStatus(path);

				for (final FileStatus stat : status) {
					appendFiles(fs, stat.getPath(), base, pathFilter, keyProvider, addedFiles);
				}
			} catch (final Throwable e) {
				System.err.println("Failed listing status on path: " + path);
			}
		}
	}

	public void writePathMap(Map<Path, K> map) throws IOException {
		final Path p = new Path(sequenceFilePath.getParent(), sequenceFilePath.getName().substring(0,
				sequenceFilePath.getName().lastIndexOf("."))
				+ "-map.txt");
		FSDataOutputStream dos = null;
		PrintWriter pw = null;

		try {
			dos = fileSystem.create(p);
			pw = new PrintWriter(dos);

			for (final Entry<Path, K> e : map.entrySet()) {
				pw.println(e.getValue() + " " + e.getKey());
			}
		} finally {
			if (pw != null)
				pw.close();
			if (dos != null)
				try {
					dos.close();
				} catch (final IOException e) {
				}
		}
	}

	/**
	 * Search for the record identified by queryKey.
	 *
	 * @param queryKey
	 *            the key.
	 * @param offset
	 *            the offset from which to commence search
	 * @return the found value, or null.
	 */
	@SuppressWarnings("unchecked")
	public V find(K queryKey, long offset) {
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot find key in write mode");
		}

		Reader reader = null;
		try {
			reader = createReader();
			if (offset > 0)
				reader.seek(offset);

			final K key = ReflectionUtils.newInstance((Class<K>) reader.getKeyClass(), config);

			while (reader.next(key)) {
				System.out.println(key);
				if (key.equals(queryKey)) {
					final V val = ReflectionUtils.newInstance((Class<V>) reader.getValueClass(), config);

					reader.getCurrentValue(val);

					return val;
				}
			}
			return null;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (final IOException e1) {
				}
		}
	}

	/**
	 * Search for the record identified by queryKey. Uses a linear search from
	 * the beginning of the file.
	 *
	 * @param queryKey
	 * @return the found value, or null.
	 */
	public V find(K queryKey) {
		return find(queryKey, 0);
	}

	/**
	 * Find a record and write the value to a file.
	 *
	 * @param key
	 * @param uriOrPath
	 * @param offset
	 * @return false if record not found, true otherwise.
	 * @throws IOException
	 */
	public boolean findAndExport(K key, String uriOrPath, long offset) throws IOException {
		FileSystem fs = null;
		Path p = null;

		if (uriOrPath != null) {
			final URI uri = convertToURI(uriOrPath);

			fs = getFileSystem(uri);
			p = new Path(uri.toString());
		}

		return findAndExport(key, fs, p, offset);
	}

	/**
	 * Find a record and write the value to a file.
	 *
	 * @param key
	 * @param fs
	 * @param dirPath
	 * @param offset
	 * @return false if record not found, true otherwise.
	 * @throws IOException
	 */
	public boolean findAndExport(K key, FileSystem fs, Path dirPath, long offset) throws IOException {
		final V value = find(key, offset);

		if (value == null)
			return false;

		if (fs != null && fs != null) {
			final Path outFilePath = new Path(dirPath, key.toString());
			writeFile(fs, outFilePath, value);
		} else {
			printFile(value);
		}

		return true;
	}

	public Path getSequenceFilePath() {
		return sequenceFilePath;
	}

	class SequenceFileEntry implements Entry<K, V> {
		K key;
		V value;

		public SequenceFileEntry(K k, V v) {
			key = k;
			value = v;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			this.value = value;
			return value;
		}
	}

	class SequenceFileIterator implements Iterator<Entry<K, V>> {
		Reader reader = null;
		Entry<K, V> next;
		boolean shouldMove = true;

		@SuppressWarnings("unchecked")
		public SequenceFileIterator() {
			try {
				reader = createReader();

				next = new SequenceFileEntry(ReflectionUtils.newInstance((Class<K>) reader.getKeyClass(), config),
						ReflectionUtils.newInstance(
								(Class<V>) reader.getValueClass(), config));
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean hasNext() {
			tryGetNext();
			return next != null;
		}

		private void tryGetNext() {
			if (next != null && shouldMove) {
				shouldMove = false;
				try {
					if (!reader.next(next.getKey(), next.getValue())) {
						next = null;
						try {
							reader.close();
						} catch (final IOException e1) {
						}
					}
				} catch (final IOException e) {
					try {
						reader.close();
					} catch (final IOException e1) {
					}
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public Entry<K, V> next() {
			tryGetNext();

			if (next == null) {
				throw new NoSuchElementException();
			}
			shouldMove = true;
			return next;
		}

		@Override
		public void remove() {
		}
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		if (!isReader) {
			throw new UnsupportedOperationException("Cannot iterate in write mode");
		}

		return new SequenceFileIterator();
	}
}
