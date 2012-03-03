/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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


public class MemoryTwitterStatusList<T extends TwitterStatus> extends ArrayList<T> implements TwitterStatusList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -785707085718120105L;

	@SuppressWarnings("unchecked")
	public MemoryTwitterStatusList(Collection<T> c) {
		for (T twitterStatus : c) {
			this.add(twitterStatus.clone((Class<T>)twitterStatus.getClass()));
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
	
	public static MemoryTwitterStatusList<TwitterStatus> read(File f) throws IOException {
		return read(new FileInputStream(f),FileUtils.countLines(f));
	}
	
	public static <T extends TwitterStatus>MemoryTwitterStatusList<T> read(File f,Class<T> clazz) throws IOException {
		return read(new FileInputStream(f),FileUtils.countLines(f),clazz);
	}
	public static MemoryTwitterStatusList<TwitterStatus> read(InputStream is, int nTweets) throws IOException {
		return read(new BufferedInputStream(is),nTweets,TwitterStatus.class);
	}
	public static <T extends TwitterStatus> MemoryTwitterStatusList<T> read(InputStream is, int nTweets,Class<T> clazz) throws IOException {
		return read(new BufferedInputStream(is),nTweets,clazz);
	}
		
	public static <T extends TwitterStatus> MemoryTwitterStatusList<T> read(BufferedInputStream is, int nTweets,Class<T> clazz) throws IOException {
		MemoryTwitterStatusList<T> list = new MemoryTwitterStatusList<T>();
		Scanner scanner = new Scanner(is);
		for (int i = 0; i < nTweets; i++) {
			T s = TwitterStatusListUtils.newInstance(clazz);
			s.readASCII(scanner);
			list.add(s);
		}
		
		return list;
	}
	
	public static MemoryTwitterStatusList<TwitterStatus> read(File f, String charset) throws IOException {
		return read(new FileInputStream(f),FileUtils.countLines(f));
	}
	public static MemoryTwitterStatusList<TwitterStatus> read(InputStream is, String charset, int nTweets) throws IOException {
		return read(new BufferedInputStream(is),nTweets);
	}
		
	public static <T extends TwitterStatus> MemoryTwitterStatusList<T> read(BufferedInputStream is, String charset, int nTweets, Class<T> clazz) throws IOException {
		MemoryTwitterStatusList<T> list = new MemoryTwitterStatusList<T>();
		Scanner scanner = new Scanner(is,charset);
		for (int i = 0; i < nTweets; i++) {
			T s = TwitterStatusListUtils.newInstance(clazz);
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
