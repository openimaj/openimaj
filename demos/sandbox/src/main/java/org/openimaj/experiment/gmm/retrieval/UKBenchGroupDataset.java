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
package org.openimaj.experiment.gmm.retrieval;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ReadableGroupDataset;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.io.ObjectReader;

/**
 * A {@link GroupedDataset} of {@link UKBenchListDataset}s instances each of an
 * item in the UKBench experiment.
 *
 * UKBench can be provided in any form supported by {@link VFS}
 *
 * The UKBench files must be in one flat directory and named "ukbenchXXXXX.jpg"
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <IMAGE>
 *            The type of IMAGE in the dataset
 */
public class UKBenchGroupDataset<IMAGE>
		extends
		ReadableGroupDataset<Integer, UKBenchListDataset<IMAGE>, IMAGE, FileObject>
		implements
		Identifiable
{
	private static final int UKBENCH_OBJECTS = 2550;
	private Map<Integer, UKBenchListDataset<IMAGE>> ukbenchObjects;
	private FileObject base;

	/**
	 * @param path
	 * @param reader
	 */
	public UKBenchGroupDataset(String path, ObjectReader<IMAGE, FileObject> reader) {
		super(reader);
		this.ukbenchObjects = new HashMap<Integer, UKBenchListDataset<IMAGE>>();
		FileSystemManager manager;
		try {
			manager = VFS.getManager();
			this.base = manager.resolveFile(path);
		} catch (final FileSystemException e) {
			throw new RuntimeException(e);
		}

		for (int i = 0; i < UKBENCH_OBJECTS; i++) {
			this.ukbenchObjects.put(i, new UKBenchListDataset<IMAGE>(path, reader, i));
		}
	}

	@Override
	public String toString() {
		return String.format("%s(%d groups with a total of %d instances)", this.getClass().getName(), this.size(),
				this.numInstances());
	}

	@Override
	public String getID() {
		return base.getName().getBaseName();
	}

	@Override
	public Set<java.util.Map.Entry<Integer, UKBenchListDataset<IMAGE>>> entrySet() {
		return ukbenchObjects.entrySet();
	}

}
