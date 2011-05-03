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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteable;



public class SampleBatch implements ReadWriteable<SampleBatch> {
	public static final byte[] HEADER = "SAMPLEBATCH".getBytes();

	private FileType type;
	private File sampleSource;
	private int startIndex;
	private int endIndex;
	private int[] relativeIndexList;
	
	public SampleBatch(){
		
	}
	public SampleBatch(FileType type, File sampleSource, int startIndex, int endIndex, int[] relativeIndexList){
		this.endIndex = endIndex;
		this.startIndex = startIndex;
		this.type = type;
		this.sampleSource = sampleSource;
		this.relativeIndexList = relativeIndexList;
	}
	
	public SampleBatch(FileType type, File sampleSource, int startIndex,int endIndex) {
		this.type = type;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.sampleSource = sampleSource;
		this.relativeIndexList = null;
	}

	public byte[][] getStoredSamples() throws IOException{
		byte [][] f  = null;
		if(relativeIndexList == null){
			f = type.readFeatures(sampleSource);
		}
		else
		{
			f = type.readFeatures(sampleSource, relativeIndexList);
		}
		return f;
	}
	public byte[][] getStoredSamples(int[] indecies) throws IOException{
		byte [][] f  = null;
		f = type.readFeatures(sampleSource, indecies);
		return f;
	}
	
	public byte[][] getStoredSamples(int interestedStart, int interestedEnd) throws IOException {
		int[] interestedList = new int[interestedEnd - interestedStart];
		int j = 0;
		for(int i = interestedStart; i < interestedEnd; i++)interestedList[j++] = i; 
		return type.readFeatures(sampleSource, interestedList);
	}
	
	public int getStartIndex() {
		return startIndex;
	}
	
	public int getEndIndex() {
		return endIndex;
	}
	
	
	public static void writeSampleBatches(List<SampleBatch> sampleBatches,File sampleBatchOut) {
		DataOutputStream dos = null;
		try{
			FileOutputStream fos = new FileOutputStream(sampleBatchOut);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
			dos.writeInt(sampleBatches.size());
			for(SampleBatch sb : sampleBatches){
				IOUtils.writeBinary(bos, sb);
				dos.flush();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static List<SampleBatch> readSampleBatches(File sampleBatchOut) {
		FileInputStream fis = null;
		List<SampleBatch> sbl = new ArrayList<SampleBatch>();
		try{
			fis = new FileInputStream(sampleBatchOut);
			DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
			int toRead = dis.readInt();
			for(int i = 0; i < toRead; i++){
				dis.read(new byte[SampleBatch.HEADER.length]);
				SampleBatch sb = new SampleBatch().readBinary(dis);
				sbl.add(sb);
				System.err.printf("\r%8d / %8d", i, toRead);
			}
		} catch (IOException e) {
		}
		finally{
			try {
				fis.close();
			} catch (IOException e) {
				
			}
		}
		System.out.println(" Done!");
		return sbl;
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof SampleBatch)) return false;
		SampleBatch sbo = (SampleBatch) o;
		
		return 	sbo.endIndex == this.endIndex && 
				sbo.startIndex == this.startIndex && 
				Arrays.equals(sbo.relativeIndexList, this.relativeIndexList) &&
				sbo.sampleSource.getAbsolutePath().equals(this.sampleSource.getAbsolutePath()) &&
				sbo.type.equals(this.type);
	}
	public int[] getRelativeIndexList() {
		return this.relativeIndexList;
	}
	@Override
	public String toString(){
		String out = "";
		out += "|" + this.startIndex + "->" + this.endIndex + "| == " + (this.endIndex - this.startIndex);
		return out;
		
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(type.ordinal());
		out.writeInt(sampleSource.getAbsolutePath().length());
		byte[] path = sampleSource.getAbsolutePath().getBytes();
		out.write(path, 0, path.length);
		out.writeInt(startIndex);
		out.writeInt(endIndex);
		out.writeInt(relativeIndexList.length);
		for(int i = 0; i < relativeIndexList.length; i++){
			out.writeInt(relativeIndexList[i]);
		}
		
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SampleBatch readBinary(DataInput in) throws IOException {
		type = FileType.values()[in.readInt()];
		int pathLen = in.readInt();
		byte[] path = new byte[pathLen];
		in.readFully(path, 0, pathLen);
		sampleSource = new File(new String(path));
		startIndex = in.readInt();
		endIndex = in.readInt();
		int nRelativeIndexList = in.readInt();
		relativeIndexList = new int[nRelativeIndexList];
		for(int i = 0 ; i < nRelativeIndexList; i++){
			relativeIndexList[i] = in.readInt();
		}
		return this;
	}

	@Override
	public SampleBatch readASCII(Scanner in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] binaryHeader() {
		return SampleBatch.HEADER;
	}

	@Override
	public String asciiHeader() {
		return "ASCII" + new String(SampleBatch.HEADER);
	}
}
