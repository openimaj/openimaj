package org.openimaj.tools.imagecollection.utils;

import java.util.Map;
import java.util.Map.Entry;

public class MetaMapUtils {
	public static String metaAsJson(Map<String,String> meta) {
		String out = "{";
		for(Entry<String,String> entry : meta.entrySet()){
			out += String.format("%s: %s,", entry.getKey(), entry.getValue());
		}
		if(meta.size() > 0) out = out.substring(0, out.length() -1);
		out += "}";
		return out;
	}
}
