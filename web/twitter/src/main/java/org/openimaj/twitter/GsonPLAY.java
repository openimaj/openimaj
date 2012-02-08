package org.openimaj.twitter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class GsonPLAY {
	private static Gson gson;
	
	public static final TypeAdapter<EscapedString> ESCAPED_STRING = new TypeAdapter<EscapedString>() {

		@Override
		public void write(JsonWriter out, EscapedString value) throws IOException {
			out.value(StringEscapeUtils.escapeJava(value.held));
		}

		@Override
		public EscapedString read(JsonReader in) throws IOException {
			EscapedString str = new EscapedString(TypeAdapters.STRING.read(in));
			return str;
		}
		
	};
	public static final TypeAdapterFactory ESCAPED_STRING_FACTORY = TypeAdapters.newFactory(EscapedString.class, ESCAPED_STRING);
	
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
