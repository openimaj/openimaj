package org.openimaj.twitter.collection;

import java.io.BufferedInputStream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Scanner;

import org.openimaj.data.RandomData;
import org.openimaj.io.FileUtils;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.util.list.AbstractFileBackedList;


public class FileTwitterStatusList extends AbstractFileBackedList<TwitterStatus> implements TwitterStatusList {

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
		return new FileTwitterStatusList(newSize,file);
	}

}
