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
package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteable;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

/**
 * A pair of strings with 2 distinct counts: 
 * <ul>
 * <li>number of times the pair appears together in a document</li>
 * </ul>
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TokenPairCount extends Pair<String> implements ReadWriteable{
	
	private static final String TIMESPLIT = ".AT.";
	private static Pattern timeSplitPattern = Pattern.compile(TIMESPLIT);
	private static Pattern timePartPattern = Pattern.compile("T-?\\d+");
	private static Pattern timeIDPattern = Pattern.compile(".*T(.*?)" + Pattern.quote(TIMESPLIT) + "(.*)s",Pattern.DOTALL);
	
	/**
	 * Number of times this pair appears together
	 */
	public long paircount;
	public boolean isSingle;
	
	/**
	 * 
	 */
	public TokenPairCount() {
		super(null,null);
		this.isSingle = false;
	}
	
	/**
	 * @param tok1
	 * @param tok2
	 */
	public TokenPairCount(String tok1, String tok2) {
		super(tok1, tok2);
		isSingle = tok2 == null;
	}

	public TokenPairCount(String tok1) {
		this(tok1,null);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.isSingle = in.readBoolean();
		this.setFirstObject(in.readUTF());
		if(!isSingle)
			this.setSecondObject(in.readUTF());
		this.paircount = in.readLong();
	}

	@Override
	public byte[] binaryHeader() {
		return "B".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeBoolean(this.isSingle);
		out.writeUTF(this.firstObject());
		if(!this.isSingle)
			out.writeUTF(this.secondObject());
		out.writeLong(paircount);
	}

	public void add(TokenPairCount that) {
		this.paircount +=that.paircount;
		
	}
	
	/**
	 * @return identifier string without a count
	 */
	public String identifier(){
		long count = this.paircount;
		this.paircount = 0;
		String out = toString();
		this.paircount = count;
		return out;
	}
	
	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.writeASCII(writer, this);
		} catch (IOException e) {
			return "ERRORSTRING";
		}
		return writer.toString();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		this.isSingle = Boolean.parseBoolean(in.nextLine());
		this.setFirstObject(in.nextLine());
		if(!this.isSingle){
			this.setSecondObject(in.nextLine());
		}
		if(in.hasNextLine())
			this.paircount = Long.parseLong(in.nextLine());
	}

	@Override
	public String asciiHeader() {
		return "A";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(this.isSingle);
		out.println(this.firstObject());
		if(!this.isSingle){
			out.println(this.secondObject());
		}
		out.println(paircount);
	}

	/**
	 * Given a string, extract the time and TokenPairCount assuming the format:
	 * time + TokenPairCount#TIMESPLIT + {@link TokenPairCount#identifier()}
	 * @param string
	 * @return a time and TokenPairCount (with a zero count of course)
	 * @throws IOException 
	 */
	public static IndependentPair<Long, TokenPairCount> parseTimeTokenID(String string) throws IOException {
		Matcher matcher = timeIDPattern.matcher(string);
		if(!matcher.matches()) 
			throw new IOException("Ivalid time ID");
		long time = Long.parseLong(matcher.group(1));
		TokenPairCount tpc = IOUtils.fromString(matcher.group(2), TokenPairCount.class);
		return IndependentPair.pair(time, tpc);
	}

	public String identifier(long time) {
		return "T" + time + TIMESPLIT + identifier();
	}
	
	/**
	 * Generate a byte array identifier with some time stamp included. 
	 * This function writes time then calls {@link #writeBinary(DataOutput)}
	 * @param time
	 * @return a byte array encoded as: time,{@link TokenPairCount}
	 * @throws IOException
	 */
	public byte[] identifierBinary(long time) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
		dos.writeLong(time);
		writeBinary(dos);
		dos.flush();
		dos.close();
		return baos.toByteArray();
	}

	public static long timeFromBinaryIdentity(byte[] bytes) throws IOException {
		return timeFromBinaryIdentity(bytes,0,bytes.length);
	}
	
	public static long timeFromBinaryIdentity(byte[] bytes,int start, int length) throws IOException {
		DataInputStream dis = null ;
		try{
			dis = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(bytes,start,length)));
			return dis.readLong();
		}
		finally{
			dis.close();
		}
	}
	
}
