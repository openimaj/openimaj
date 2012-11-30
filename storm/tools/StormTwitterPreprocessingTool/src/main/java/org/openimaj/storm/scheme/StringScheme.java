package org.openimaj.storm.scheme;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;

public final class StringScheme implements Scheme {
	private String stringName;

	public StringScheme() {
		stringName = "string";
	}

	public StringScheme(String stringName) {
		this.stringName = stringName;
	}

	@Override
	public List<Object> deserialize(byte[] ser) {
		String serStrings = new String(ser, Charset.forName("UTF-8"));
		List<Object> linesList = new ArrayList<Object>();
		linesList.add(serStrings);
		return linesList;
	}

	@Override
	public Fields getOutputFields() {
		return new Fields(stringName);
	}
}