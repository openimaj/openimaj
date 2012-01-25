package org.openimaj.twitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableASCII;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TwitterStatus implements ReadWriteableASCII{

	private transient static Gson gson;
	
	static{
		gson = new GsonBuilder().
			serializeNulls().
			create();
	}
	
	private int retweet_count;
	private String in_reply_to_screen_name;
	private String text;
	private Map<String,Object> analysis = new HashMap<String,Object>();
	
	
	public TwitterStatus() {
		
	}
	@Override
	public void readASCII(Scanner in) throws IOException {
		this.copyFrom(gson.fromJson(in.nextLine(), TwitterStatus.class));
	}

	private void copyFrom(TwitterStatus fromJson) {
		this.retweet_count = fromJson.retweet_count;
		this.text = fromJson.text;
		this.in_reply_to_screen_name = fromJson.in_reply_to_screen_name;
	}
	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(gson.toJson(this));
	}
	
	@Override
	public String toString() {
		return this.text;
	}
	
	public <T> void addAnalysis(String name, T analysis){
		this.analysis.put(name, analysis);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAnalysis(String name){
		return (T) this.analysis.get(name);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TwitterStatus)) return false;
		TwitterStatus status = (TwitterStatus)obj;
		boolean allMapEq = true;
		for(String key : this.analysis.keySet()){
			allMapEq &= this.analysis.get(key).equals(status.analysis.get(key));
		}
		return status.text.equals(this.text) && 
				(status.in_reply_to_screen_name == null || status.in_reply_to_screen_name.equals(in_reply_to_screen_name)) && 
				status.retweet_count == this.retweet_count;
	}
	
	public static void main(String[] args) throws IOException {
		InputStream stream = TwitterStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt");
		TwitterStatus status = IOUtils.read(stream, TwitterStatus.class);
		status.addAnalysis("someString", "with a value");
		status.addAnalysis("someInt", 1);
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		IOUtils.writeASCII(outStream , status);
		byte[] arr = outStream.toByteArray();
		TwitterStatus readStatus = IOUtils.read(new ByteArrayInputStream(arr) , TwitterStatus.class);
		
		System.out.println(status.equals(readStatus));
	}
}
