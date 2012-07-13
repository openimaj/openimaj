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
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.USMFStatus;


/**
 * A List of {@link USMFStatus} instances held in memory (backed by an {@link ArrayList}.)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 * @param <T>
 */
public class MemoryTwitterStatusList<T extends USMFStatus> extends ArrayList<T> implements TwitterStatusList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -785707085718120105L;

	/**
	 * Consume a collection into this list 
	 * @param c
	 */
	@SuppressWarnings("unchecked")
	public MemoryTwitterStatusList(Collection<T> c) {
		for (T twitterStatus : c) {
			this.add(twitterStatus.clone((Class<T>)twitterStatus.getClass()));
		}
	}

	/**
	 * an empty list
	 */
	public MemoryTwitterStatusList() {
	}

	@Override
	public MemoryTwitterStatusList<T> randomSubList(int nelem) {
		MemoryTwitterStatusList<T> kl;

		if (nelem > size()) {
			kl = new MemoryTwitterStatusList<T>(this);
			Collections.shuffle(kl);
		} else {
			int [] rnds = RandomData.getUniqueRandomInts(nelem, 0, this.size());
			kl = new MemoryTwitterStatusList<T>();

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
	
	/**
	 * @param f
	 * @return read a file of {@link USMFStatus} instances into memory (i.e. formated as USMF)
	 * @throws IOException
	 */
	public static MemoryTwitterStatusList<USMFStatus> read(File f) throws IOException {
		return read(new FileInputStream(f),FileUtils.countLines(f));
	}
	
	/**
	 * @param <T>
	 * @param f
	 * @param clazz
	 * @return read a file as {@link USMFStatus} instances held ass clazz instances
	 * @throws IOException
	 */
	public static <T extends USMFStatus>MemoryTwitterStatusList<T> read(File f,Class<T> clazz) throws IOException {
		return read(new FileInputStream(f),FileUtils.countLines(f),clazz,USMFStatus.class);
	}
	/**
	 * @param is
	 * @param nStatus
	 * @return read nStatus from the input stream input memory
	 * @throws IOException
	 */
	public static MemoryTwitterStatusList<USMFStatus> read(InputStream is, int nStatus) throws IOException {
		return read(new BufferedInputStream(is),nStatus,USMFStatus.class,USMFStatus.class);
	}
	/**
	 * @param <T>
	 * @param is
	 * @param nStatus
	 * @param clazz
	 * @param readClass
	 * @return read nStatus instances into a {@link MemoryTwitterStatusList} held as clazz instances and read as readClass instances
	 * @throws IOException
	 */
	public static <T extends USMFStatus> MemoryTwitterStatusList<T> read(InputStream is, int nStatus,Class<T> clazz,Class<? extends GeneralJSON> readClass) throws IOException {
		return read(new BufferedInputStream(is),nStatus,clazz,USMFStatus.class);
	}
		
	/**
	 * @param <T>
	 * @param is
	 * @param nTweets
	 * @param clazz
	 * @param readClass
	 * @return read nStatus instances into a {@link MemoryTwitterStatusList} held as clazz instances and read as readClass instances
	 * @throws IOException
	 */
	public static <T extends USMFStatus> MemoryTwitterStatusList<T> read(BufferedInputStream is, int nTweets,Class<T> clazz,Class<? extends GeneralJSON> readClass) throws IOException {
		MemoryTwitterStatusList<T> list = new MemoryTwitterStatusList<T>();
		Scanner scanner = new Scanner(is);
		for (int i = 0; i < nTweets; i++) {
			T s = TwitterStatusListUtils.newInstance(clazz);
			s.setGeneralJSONClass(readClass);
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
