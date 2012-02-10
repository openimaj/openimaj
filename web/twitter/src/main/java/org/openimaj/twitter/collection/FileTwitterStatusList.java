package org.openimaj.twitter.collection;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.openimaj.io.FileUtils;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.util.list.AbstractFileBackedList;


public class FileTwitterStatusList extends AbstractFileBackedList<TwitterStatus> implements TwitterStatusList {

	protected FileTwitterStatusList(int size, File file, String charset) {
		super(size, false, 0, -1, file, TwitterStatus.class,charset);
	}
	
	protected FileTwitterStatusList(int size, File file) {
		super(size, false, 0, -1, file, TwitterStatus.class);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -785707085718120105L;

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		for (int i = 0; i < this.size; i++) {
			if(i != 0) out.println();
			this.get(i).writeASCII(out);
		}
	}

	@Override
	public String asciiHeader() {
		return "";
	}
	
	public static FileTwitterStatusList read(File f) throws IOException {
		int size = FileUtils.countLines(f);
		return new FileTwitterStatusList(size, f);
	}
	
	public static FileTwitterStatusList read(File f,String charset) throws IOException {
		int size = FileUtils.countLines(f);
		return new FileTwitterStatusList(size, f,charset);
	}
	
	public static FileTwitterStatusList read(File f,int size) throws IOException {
		return new FileTwitterStatusList(size, f);
	}
	
	public static FileTwitterStatusList read(File f,String charset,int size) throws IOException {
		return new FileTwitterStatusList(size, f,charset);
	}
	

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public byte[] binaryHeader() {
		throw new UnsupportedOperationException();
	}

	
	
	@Override
	protected AbstractFileBackedList<TwitterStatus> newInstance(int newSize, boolean isBinary, int newHeaderLength, int recordLength, File file) {
		return new FileTwitterStatusList(newSize,file,this.charset);
	}

}
