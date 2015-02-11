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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.openimaj.data.dataset.ReadableListDataset;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.io.ObjectReader;

/**
 *
 * @param <IMAGE>
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class UKBenchListDataset<IMAGE> extends ReadableListDataset<IMAGE, FileObject> implements Identifiable{
	private int object;
	private List<String> ids;
	private FileObject base;
	
	/**
	 * @param path
	 * @param reader
	 * @param object
	 */
	public UKBenchListDataset(String path, ObjectReader<IMAGE, FileObject> reader, int object) {
		super(reader);
		this.object = object;
		this.ids = heldIDs();
		FileSystemManager fsManager;
		try {
			fsManager = VFS.getManager();
			this.base = fsManager.resolveFile(path);
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public IMAGE getInstance(int index) {
		FileObject fo = null; 
		try {
			fo = createFileObject(this.ids.get(index));
			return this.reader.read(fo);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if(fo!=null){
				try {
					fo.close();
				} catch (FileSystemException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private FileObject createFileObject(String string) {
		try {
			FileObject child = base.getChild(string);
			return child;
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public int numInstances() {
		return  4;
	}

	@Override
	public String getID() {
		List<String> ids = heldIDs();
		return String.format("UKBench{%s}",ids.toString());
	}
	private List<String> heldIDs() {
		List<String> ids = new ArrayList<String>();
		ids.add(String.format("ukbench%05d.jpg",object * 4 + 0));
		ids.add(String.format("ukbench%05d.jpg",object * 4 + 1));
		ids.add(String.format("ukbench%05d.jpg",object * 4 + 2));
		ids.add(String.format("ukbench%05d.jpg",object * 4 + 3));
		return ids;
	}
	/**
	 * @return the objects this group represents
	 */
	public int getObject() {
		return object;
	}
	
	

}
