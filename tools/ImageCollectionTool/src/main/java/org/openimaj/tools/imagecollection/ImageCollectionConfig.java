package org.openimaj.tools.imagecollection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteableASCII;

import com.jayway.jsonpath.JsonPath;

/**
 * An ImageCollectionConfig can be written to and read from an ASCII str
 * @author ss
 *
 */
public class ImageCollectionConfig implements ReadWriteableASCII{

	
	private String json;

	public ImageCollectionConfig(){
		this.json = "{}";
	}
	

	public ImageCollectionConfig(String json) {
		this.json = json;
	}


	@Override
	public void readASCII(Scanner in) throws IOException {
		StringBuilder builder = new StringBuilder();
		while(in.hasNext())builder.append(in.next());
		json = builder.toString();
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.print(json);
	}

	public <T> T read(String path) throws ParseException {
		T i = JsonPath.read(this.json, path);
		return i;
	}


	public boolean containsValid(String videoTag) {
		String r;
		try {
			r = read(videoTag);
		} catch (ParseException e) {
			return false;
		}
		return r!=null;
	}

	
}
