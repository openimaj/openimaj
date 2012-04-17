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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openimaj.io.ReadWriteable;
import org.openimaj.twitter.collection.TwitterStatusListUtils;

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
	public Object geo;
	/**
	 * 
	 */
	public Object coordinates;
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
    /**
	 * 
	 */
    public String created_at;
	private Map<String,Object> analysis = new HashMap<String,Object>();
	private boolean invalid = false;
	
	
	/**
	 * Conveniance for the readers, produces an empty TwitterStatus.
	 */
	public TwitterStatus() {}
	
	
	/**
	 * @return the tweet is either a delete notice, a scrub geo notice or some other non-status tweet
	 */
	public boolean isInvalid(){
		return invalid;
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		TwitterStatus status  = TwitterStatus.fromString(in.nextLine(),this.getClass());
		if(status.text == null && this.analysis.size() == 0) {
			this.invalid  = true;
			return;
		}
		this.invalid = false;
		try {
			this.assignFrom(status);
		} catch (Exception e) {
			throw new IOException(e);
		}
		
	}

	private void assignFrom(TwitterStatus fromJson) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = this.getClass().getFields();
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
		if(!equal) 
			return false;
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
		return clone(TwitterStatus.class);
	}
	
	/**
	 * Clones the tweet to the given class.
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public <T extends TwitterStatus> T clone(Class<T> clazz){
		return gson.fromJson(gson.toJson(this), clazz);
	}

	/**
	 * Convenience to allow writing of just the analysis to a writer
	 * @param outputWriter
	 * @param selectiveAnalysis
	 */
	public void writeASCIIAnalysis(PrintWriter outputWriter,List<String> selectiveAnalysis) {
		writeASCIIAnalysis(outputWriter,selectiveAnalysis,new ArrayList<String>());
	}
	
	/**
	 * Convenience to allow writing of just the analysis and some status information to a writer
	 * 
	 * @param outputWriter
	 * @param selectiveAnalysis
	 * @param selectiveStatus
	 */
	public void writeASCIIAnalysis(PrintWriter outputWriter,List<String> selectiveAnalysis,List<String> selectiveStatus) {
		Map<String,Object> toOutput = new HashMap<String,Object>();
		Map<String,Object> analysisBit = new HashMap<String,Object>();
		toOutput.put("analysis", analysisBit);
		for (String analysisKey : selectiveAnalysis) {
			analysisBit.put(analysisKey,getAnalysis(analysisKey));
		}
		for (String status : selectiveStatus) {
			try {
				
				Field f = this.getClass().getField(status);
				toOutput.put(status, f.get(this));
			} catch (SecurityException e) {
				System.err.println("Invalid field: " + status);
			} catch (NoSuchFieldException e) {
				System.err.println("Invalid field: " + status);
			} catch (IllegalArgumentException e) {
				System.err.println("Invalid field: " + status);
			} catch (IllegalAccessException e) {
				System.err.println("Invalid field: " + status);
			}
		}
		gson.toJson(toOutput, outputWriter);
	}
	
	/**
	 * Create a tweet from a string
	 * @param line either tweet json, otherwise the tweet text
	 * @return a new tweet built around the tweet
	 */
	public static TwitterStatus fromString(String line)
	{
		return fromString(line,TwitterStatus.class);
	}
	
	/**
	 * A stricter version of {@link #fromString(String)}. If the string is not valid JSON, throw an exception
	 * @param line either tweet json, otherwise the tweet text
	 * @return a new tweet built around the tweet
	 */
	public static TwitterStatus fromJSONString(String line)
	{
		TwitterStatus status = gson.fromJson(line, TwitterStatus.class);
		return status;
	}
	/**
	 * Create a tweet from a string
	 * @param line either tweet json, otherwise the tweet text
	 * @param clazz the twitter status class to create
	 * @return a new tweet built around the tweet
	 */
	public static TwitterStatus fromString(String line, Class<? extends TwitterStatus> clazz) {
		TwitterStatus status = null;
		try {
			// try reading the string as json
			status = gson.fromJson(line, clazz);
			status.assignFrom(status);
		} catch (Exception e) {
//			System.out.println("could not parse:" + e.getMessage() + "\n" + line );
		}
		if(status==null){ 
			status  = TwitterStatusListUtils.newInstance(clazz);
			status .text = line;
		}
		if(status.text == null && status.analysis.size() == 0)
		{
			status.invalid = true;
		}
		return status ;
	}
	
	/**
	 * @return get the created_at date as a java date
	 * @throws ParseException 
	 */
	public DateTime createdAt() throws ParseException{
		DateTimeFormatter parser= DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");
		if(created_at == null) return null;
		return parser.parseDateTime(created_at);
	}
	
}
