package org.openimaj.experiment.gmm.retrieval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.openimaj.data.dataset.ReadableListDataset;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.image.Image;
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
