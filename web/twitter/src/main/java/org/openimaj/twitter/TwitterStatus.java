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
package org.openimaj.twitter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A twitter status. Tied heavily to the twitter status json format. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TwitterStatus implements ReadWriteable, Cloneable{

	/**
	 * The GSON instance
	 */
	public transient static Gson gson;
	
	static{
		gson = new GsonBuilder().
			serializeNulls().
			create();
	}
	
	/**
	 * 
	 */
	public int retweet_count;
	/**
	 * 
	 */
	public String in_reply_to_screen_name;
	/**
	 * 
	 */
	public String text;
	/**
	 * 
	 */
	public Map<String,Object> entities = null;
	/**
	 * 
	 */
	public Map<String,Object> user = null;
	/**
	 * 
	 */
	public String geo;
	/**
	 * 
	 */
	public String coordinates;
	/**
	 * 
	 */
	public boolean retweeted;
	/**
	 * 
	 */
	public String in_reply_to_status_id;
	/**
	 * 
	 */
	public String in_reply_to_user_id;
	/**
	 * 
	 */
	public boolean truncated;
	/**
	 * 
	 */
    public long id;
	private Map<String,Object> analysis = new HashMap<String,Object>();
	
	
	/**
	 * Conveniance for the readers, produces an empty TwitterStatus.
	 */
	public TwitterStatus() {}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		TwitterStatus status  = TwitterStatus.fromString(in.nextLine());
		try {
			this.assignFrom(status);
		} catch (Exception e) {
			throw new IOException(e);
		}
		
	}

	private void assignFrom(TwitterStatus fromJson) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			if(Modifier.isStatic(field.getModifiers())) continue;
			field.set(this, field.get(fromJson));
		}
	}
	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		gson.toJson(this, out);
	}
	

	@Override
	public String toString() {
		return this.text;
	}
	
	/**
	 * Add analysis to the analysis object. This is where all non twitter stuff should go
	 * @param <T> The type of data being saved
	 * @param annKey the key
	 * @param annVal the value
	 */
	public <T> void addAnalysis(String annKey, T annVal){
		if(annVal instanceof Number) this.analysis.put(annKey, ((Number)annVal).doubleValue());
		else this.analysis.put(annKey, annVal);
	}
	
	/**
	 * @param <T>
	 * @param name 
	 * @return the analysis under the name
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAnalysis(String name){
		return (T) this.analysis.get(name);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TwitterStatus)) return false;
		TwitterStatus status = (TwitterStatus)obj;
//		String statusStr = gson.toJson(status);
//		String thisStr = gson.toJson(this);
		boolean equal = true;
		equal = equalNonAnalysed(status);
		if(!equal) return false;
		equal = equalAnalysed(status);
		return equal;
	}

	private boolean equalAnalysed(TwitterStatus status) {
		Map<String, Object> thatanal = status.analysis;
		Map<String, Object> thisanal = this.analysis;
		for (String key : thatanal.keySet()) {
			// if this contains the same key, and the values for the key are equal
			if(!thisanal.containsKey(key))return false;
			Object thisobj = thisanal.get(key);
			Object thatobj = thatanal.get(key);
			if(thisobj.equals(thatobj)) continue;
			return false;
		}
		return true;
	}

	private boolean equalNonAnalysed(TwitterStatus that) {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			if(field.getName() == "analysis" || Modifier.isStatic(field.getModifiers())) continue;
			Object thisval;
			try {
				thisval = field.get(this);
				Object thatval = field.get(that);
				// If they are both null, or they are equal, continue
				if((thisval == null && thatval == null) || thisval.equals(thatval)) continue;
				return false;
					
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] binaryHeader() {
		return "BINARYTWITTERHEADER".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		throw new UnsupportedOperationException();
		
	}
	
	@Override
	public TwitterStatus clone(){
		return gson.fromJson(gson.toJson(this), TwitterStatus.class);
	}

	/**
	 * Conveniance to allow writing of just the analysis to a writer
	 * @param outputWriter
	 * @param selectiveAnalysis
	 */
	public void writeASCIIAnalysis(PrintWriter outputWriter,List<String> selectiveAnalysis) {
		Map<String,Map<String,Object>> toOutput = new HashMap<String,Map<String,Object>>();
		Map<String,Object> analysisBit = new HashMap<String,Object>();
		toOutput.put("analysis", analysisBit);
		for (String analysisKey : selectiveAnalysis) {
			analysisBit.put(analysisKey,getAnalysis(analysisKey));
		}
		gson.toJson(toOutput, outputWriter);
	}

	/**
	 * Create a tweet from a string
	 * @param line either tweet json, otherwise the tweet text
	 * @return a new tweet built around the tweet
	 */
	public static TwitterStatus fromString(String line) {
		TwitterStatus status = null;
		try {
			// try reading the string as json
			status = gson.fromJson(line, TwitterStatus.class);
			status.assignFrom(status);
		} catch (Exception e) {}
		if(status==null){ 
			status  = new TwitterStatus();
			status .text = line;
		}
		return status ;
	}
	
}
