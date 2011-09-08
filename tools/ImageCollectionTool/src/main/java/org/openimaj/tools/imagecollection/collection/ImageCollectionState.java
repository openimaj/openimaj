package org.openimaj.tools.imagecollection.collection;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.openimaj.io.ReadWriteableASCII;

public class ImageCollectionState implements ReadWriteableASCII{
	public Map<String,String> state;
	public ImageCollectionState(){
		state = new HashMap<String,String>();
	}
	@Override
	public void readASCII(Scanner in) throws IOException {
		while(in.hasNext())
			state.put(in.next(), in.next());
	}
	@Override
	public String asciiHeader() {
		return "";
	}
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		for (Entry<String, String> entry : state.entrySet()) {
			out.format("%s %s\n",entry.getKey(),entry.getValue());
		}
	}
}
