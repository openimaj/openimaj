package org.openimaj.twitter.collection;

import java.io.BufferedInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;

import org.openimaj.data.RandomData;
import org.openimaj.io.FileUtils;
import org.openimaj.twitter.TwitterStatus;


public class MemoryTwitterStatusList extends ArrayList<TwitterStatus> implements TwitterStatusList {

	/**
	 * 
	 */
	private static final long serialVersionUID = -785707085718120105L;

	public MemoryTwitterStatusList(Collection<? extends TwitterStatus> c) {
		for (TwitterStatus twitterStatus : c) {
			this.add(twitterStatus.clone());
		}
	}

	public MemoryTwitterStatusList() {
	}

	@Override
	public MemoryTwitterStatusList randomSubList(int nelem) {
		MemoryTwitterStatusList kl;

		if (nelem > size()) {
			kl = new MemoryTwitterStatusList(this);
			Collections.shuffle(kl);
		} else {
			int [] rnds = RandomData.getUniqueRandomInts(nelem, 0, this.size());
			kl = new MemoryTwitterStatusList();

			for (int idx : rnds)
				kl.add(this.get(idx));
		}

		return kl;
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		for (int i = 0; i < this.size(); i++) {
			this.get(i).writeASCII(out);
			out.println();
		}
	}

	@Override
	public String asciiHeader() {
		return "";
	}
	
	public static MemoryTwitterStatusList read(File f) throws IOException {
		return read(new FileInputStream(f),FileUtils.countLines(f));
	}
	public static MemoryTwitterStatusList read(InputStream is, int nTweets) throws IOException {
		return read(new BufferedInputStream(is),nTweets);
	}
		
	public static MemoryTwitterStatusList read(BufferedInputStream is, int nTweets) throws IOException {
		MemoryTwitterStatusList list = new MemoryTwitterStatusList();
		Scanner scanner = new Scanner(is);
		for (int i = 0; i < nTweets; i++) {
			TwitterStatus s = new TwitterStatus();
			s.readASCII(scanner);
			list.add(s);
		}
		
		return list;
	}
	
	public static MemoryTwitterStatusList read(File f, String charset) throws IOException {
		return read(new FileInputStream(f),FileUtils.countLines(f));
	}
	public static MemoryTwitterStatusList read(InputStream is, String charset, int nTweets) throws IOException {
		return read(new BufferedInputStream(is),nTweets);
	}
		
	public static MemoryTwitterStatusList read(BufferedInputStream is, String charset, int nTweets) throws IOException {
		MemoryTwitterStatusList list = new MemoryTwitterStatusList();
		Scanner scanner = new Scanner(is,charset);
		for (int i = 0; i < nTweets; i++) {
			TwitterStatus s = new TwitterStatus();
			s.readASCII(scanner);
			list.add(s);
		}
		
		return list;
	}
	

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public byte[] binaryHeader() {
		throw new UnsupportedOperationException();
	}

}
