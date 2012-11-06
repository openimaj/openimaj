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
package org.openimaj.tools.clusterquantiser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.StreamLocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * A {@link FeatureFile} backed by a stream or file. Doesn't require the list be
 * held in memory.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class StreamedFeatureFile extends FeatureFile {

	private LocalFeatureList<? extends LocalFeature<?, ?>> kpl;
	private Class<? extends Iterator<FeatureFileFeature>> iteratorType;

	/**
	 * Default constructor
	 */
	public StreamedFeatureFile() {
		this.iteratorType = KeypointListArrayIterator.class;
	}

	/**
	 * Construct with list
	 * 
	 * @param kpl
	 */
	public StreamedFeatureFile(LocalFeatureList<? extends LocalFeature<?, ?>> kpl) {
		this();
		this.kpl = kpl;
	}

	/**
	 * Construct with file
	 * 
	 * @param keypointFile
	 * @param clz
	 * @throws IOException
	 */
	public StreamedFeatureFile(File keypointFile, Class<? extends Keypoint> clz) throws IOException {
		this();
		this.kpl = FileLocalFeatureList.read(keypointFile, clz);
	}

	/**
	 * Construct with file
	 * 
	 * @param keypointFile
	 * @throws IOException
	 */
	public StreamedFeatureFile(File keypointFile) throws IOException {

		this(keypointFile, Keypoint.class);
	}

	/**
	 * Construct with stream
	 * 
	 * @param stream
	 * @throws IOException
	 */
	public StreamedFeatureFile(InputStream stream) throws IOException {
		this();
		this.kpl = StreamLocalFeatureList.read(stream, Keypoint.class);
	}

	/**
	 * Construct with stream
	 * 
	 * @param stream
	 * @param clz
	 * @throws IOException
	 */
	public StreamedFeatureFile(InputStream stream, Class<? extends Keypoint> clz) throws IOException {
		this();
		this.kpl = StreamLocalFeatureList.read(stream, clz);
	}

	/**
	 * Set the iterator type
	 * 
	 * @param cls
	 */
	public void setIteratorType(Class<? extends Iterator<FeatureFileFeature>> cls) {
		this.iteratorType = cls;
	}

	@Override
	public Iterator<FeatureFileFeature> iterator() {
		try {
			return this.iteratorType.getConstructor(LocalFeatureList.class).newInstance(kpl);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int size() {
		return kpl.size();
	}

	@Override
	public FeatureFileFeature get(int index) {
		int done = 0;
		for (final FeatureFileFeature fff : this) {
			if (done++ == index)
				return fff;
		}
		return null;
	}

	@Override
	public void close() {
	}
}
