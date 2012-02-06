package org.openimaj.twitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class TwitterStatus implements ReadWriteable, Cloneable{

	private transient static Gson gson;
	
	static{
		gson = new GsonBuilder().
			serializeNulls().
			create();
	}
	
	public int retweet_count;
	public String in_reply_to_screen_name;
	public String text;
	public Map<String,Object> entities = null;
	public String geo;
	public String coordinates;
	public boolean retweeted;
	public String in_reply_to_status_id;
	public String in_reply_to_user_id;
	public boolean truncated;
    public long id;
	private Map<String,Object> analysis = new HashMap<String,Object>();
	
	
	public TwitterStatus() {
		
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		TwitterStatus status  = null;
		String line = in.nextLine();
		try {
			// try reading the string as json 
			status = gson.fromJson( line, TwitterStatus.class);
			this.copyFrom(status);
		} catch (Exception e) {}
		if(status==null){ 
			this.text = line;
		}
	}

	private void copyFrom(TwitterStatus fromJson) throws IllegalArgumentException, IllegalAccessException {
//		System.out.println("Copying from: " + fromJson);
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			if(Modifier.isStatic(field.getModifiers())) continue;
			field.set(this, field.get(fromJson));
		}
		try {
//			this.text = new String(new String(new String(text.toString().getBytes(),"UTF-8").getBytes(),"UTF-8").getBytes(),"UTF-8");
			this.text = new String(new String(text.toString().getBytes(),"UTF-8").getBytes(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.print(gson.toJson(this));
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
		String statusStr = gson.toJson(status);
		String thisStr = gson.toJson(this);
		return statusStr.equals(thisStr);
//		boolean allMapEq = true;
//		Field[] fields = this.getClass().getDeclaredFields();
//		Object v1;
//		Object v2;
//		for (Field field : fields) {
//			if(Modifier.isStatic(field.getModifiers())) continue;
//			if(field.getName().equals("analysis")) continue;
////			if(!field.getName().equals("text")) continue;
//			
//			try {
//				v1 = field.get(this);
//				v2 = field.get(status);
//				if(v1 instanceof String){
//					String v1str = v1.toString();
//					String v2str = v2.toString();
//					
//					
//					System.out.println(Arrays.toString(v1.toString().getBytes()));
//					System.out.println(Arrays.toString(v2.toString().getBytes()));
//					String formattedv1 = new String(v1str.getBytes(),"UTF-8");
//					String formattedv2 = new String(v2str.getBytes(),"UTF-8");
//					System.out.println(formattedv1.equals(formattedv2));
//					System.out.println(Arrays.toString(v2.toString().getBytes()));
//					System.out.println(Arrays.toString(v1.toString().getBytes("UTF-8")));
//					System.out.println(Arrays.toString(v2.toString().getBytes("UTF-8")));
//					System.out.println(Arrays.equals(v1str.getBytes(),v2str.getBytes()) && v1str.equals(v2str));
//					allMapEq &= (v1str== null && v2str == null) || (v1str != null && v2str != null && v1str.equals(v2str) );
//				}
//				else
//					allMapEq &= (v1== null && v2 == null) || (v1 != null && v2 != null && (v1.equals(v2)));
//				
//				if(!allMapEq)
//					break;
//			} catch( Exception e) {
//				allMapEq &= false;
//			}
//		}
//		if(!allMapEq)
//			return false;
//		// now check the individual analysis values
//		for(String key : this.analysis.keySet()){
//			v1 = this.analysis.get(key);
//			v2 = status.analysis.get(key);
//			if(v1 instanceof Number){
//				allMapEq &= ((Number)v1).doubleValue() == ((Number)v2).doubleValue();  
//			}
//			else{
//				allMapEq &= v1.equals(v2);
//			}
//			if(!allMapEq)
//				break;
//		}
//		return allMapEq;
		
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
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		try {
			IOUtils.writeASCII(s, this);
			TwitterStatus read = IOUtils.read(new ByteArrayInputStream(s.toByteArray()), TwitterStatus.class);
//			read.text = new String(new String(read.text.getBytes(),"UTF-8").getBytes(),"UTF-8");
			return read;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	
}
