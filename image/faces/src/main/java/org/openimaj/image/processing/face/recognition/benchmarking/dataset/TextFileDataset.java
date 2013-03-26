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
package org.openimaj.image.processing.face.recognition.benchmarking.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

/**
 * A simple dataset of people and their images, backed by a 
 * text file with the following format:
 * <pre>
 *  personA,/path/to/image.jpg
 *  personA,/path/to/image1.jpg
 *  personB,/path/to/image2.jpg
 *  ...
 * </pre>
 * 
 * The default separator is a comma, but is user controllable.
 * <p>
 * In addition to allowing datasets to be read from a file, this
 * implementation also allows datasets to be created or appended
 * to through the {@link #add(String, File)} method.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class TextFileDataset extends MapBackedDataset<String, ListDataset<FImage>, FImage> {
	private class LazyImageList extends AbstractList<FImage> {
		List<File> files = new ArrayList<File>();
		
		@Override
		public FImage get(int index) {
			File f = files.get(index);
			
			if (f.isAbsolute()) {
				try {
					return ImageUtilities.readF(f);
				} catch (IOException e) {
					logger.warn(e);
					return null;
				}
			} else {
				try {
					return ImageUtilities.readF(new File(file.getParentFile(), f.toString()));
				} catch (IOException e) {
					logger.warn(e);
					return null;
				}
			}
		}

		@Override
		public int size() {
			return files.size();
		}
		
		@Override
		public String toString() {
			return files.toString();
		}
	}
	
	private static final Logger logger = Logger.getLogger(TextFileDataset.class);
	private String separator = ",";
	File file;
	BufferedWriter writer;
	
	/**
	 * Construct from the given file. The default separator
	 * of a comma "," will be used.
	 * 
	 * @param file the file
	 * @throws IOException if an error occurs
	 */
	public TextFileDataset(File file) throws IOException {
		this(file, ",");
	}
	
	/**
	 * Construct from the given file, using the given separator.
	 * @param file the file
	 * @param separator the separator
	 * @throws IOException if an error occurs
	 */
	public TextFileDataset(File file, String separator) throws IOException {
		this.file = file;
		this.separator = separator;
		
		if (file.exists())
			read();
		else
			openWriter();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		if (writer != null) {
			try { writer.close(); } catch (IOException e) {}
		}
		
		super.finalize();
	}

	private void read() throws IOException {
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(file));
			
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(separator);
				
				addInternal(parts[0].trim(), new File (parts[1].trim()));
			}
		} finally {
			if (br != null) try { br.close(); } catch (IOException e) {}
		}
	}

	private void addInternal(String person, File file) {
		ListBackedDataset<FImage> list = (ListBackedDataset<FImage>) map.get(person);
		
		if (list == null) map.put(person, list = new ListBackedDataset<FImage>(new LazyImageList()));
		((LazyImageList)list.getList()).files.add(file);
	}
	
	/**
	 * Add an instance to the dataset.  
	 * 
	 * @param person
	 * @param file
	 * @throws IOException
	 */
	public void add(String person, File file) throws IOException {
		if (writer == null)
			openWriter();
		
		writer.write(person+separator+file.getAbsolutePath()+"\n");
		writer.flush();
		
		addInternal(person, file);
	}

	private void openWriter() throws IOException {
		try {
			writer = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			writer = null;
			throw e;
		}
	}
	
	@Override
	public String toString() {
		return "Text File Dataset (" + file + ")";
	}
}
