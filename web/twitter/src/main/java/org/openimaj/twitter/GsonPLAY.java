package org.openimaj.twitter;

import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonPLAY {
	private static Gson gson;
	
	static{
//		escaped_gson = new GsonBuilder().
//			serializeNulls().
//			registerTypeAdapterFactory(ESCAPED_STRING_FACTORY ).
//			create();
		gson = new GsonBuilder().
			serializeNulls().
			create();
	}
	public static void main(String args[]) throws UnsupportedEncodingException{
	}
}
