package org.openimaj.demos.sandbox.dataset;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.Sanselan;
import org.openimaj.image.Image;
import org.openimaj.io.ObjectReader;
import org.openimaj.util.array.ArrayIterator;

public class ImageDirectoryDataset<IMAGE extends Image<?, IMAGE>> extends ReadableImageDataset<IMAGE> {
	private File[] files;

	public ImageDirectoryDataset(File directory, ObjectReader<IMAGE> reader) {
		super(reader);

		this.files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				try {
					return Sanselan.guessFormat(pathname) != ImageFormat.IMAGE_FORMAT_UNKNOWN;
				} catch (final Exception e) {
					return false;
				}
			}
		});
	}

	@Override
	public IMAGE getInstance(int index) {
		try {
			return read(files[index]);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int size() {
		return files.length;
	}

	private IMAGE read(File file) throws IOException {
		return reader.read(new BufferedInputStream(new FileInputStream(file)));
	}

	@Override
	public Iterator<IMAGE> iterator() {
		return new Iterator<IMAGE>() {
			ArrayIterator<File> filesIterator = new ArrayIterator<File>(files);

			@Override
			public boolean hasNext() {
				return filesIterator.hasNext();
			}

			@Override
			public IMAGE next() {
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
}
