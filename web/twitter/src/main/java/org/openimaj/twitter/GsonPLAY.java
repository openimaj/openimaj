package org.openimaj.twitter;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonPLAY {
	private static Gson gson;
	static{
		gson = new GsonBuilder().
			serializeNulls().
			create();
	}
	public static void main(String args[]) throws UnsupportedEncodingException{
		String forceEncoding = "UTF-8";
//		String forceEncoding = "MacRoman";
		Reader reader = new InputStreamReader(GsonPLAY.class.getResourceAsStream("/org/openimaj/twitter/broken_json_tweets.txt"));
		Scanner s = new Scanner(GsonPLAY.class.getResourceAsStream("/org/openimaj/twitter/broken_json_tweets.txt"),forceEncoding);
		TwitterStatus status = gson.fromJson(reader, TwitterStatus.class);
		System.out.println(status.text);
		TwitterStatus statusClone = status.clone();
		System.out.println(statusClone.text);
		System.out.println("Byte Equal: " + Arrays.equals(status.text.getBytes(forceEncoding),statusClone.text.getBytes(forceEncoding)));
		System.out.println(".equal: " + status.equals(statusClone));
	}
}
