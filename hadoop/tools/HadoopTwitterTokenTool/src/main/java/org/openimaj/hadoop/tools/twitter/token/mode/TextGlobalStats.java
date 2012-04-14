package org.openimaj.hadoop.tools.twitter.token.mode;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;
import org.openimaj.io.ReadWriteableASCII;

/**
 * Holds global statistics for text entries as defined in the enum {@link TextEntryType}
 * @author ss
 *
 */
public class TextGlobalStats implements ReadWriteableASCII {
	/**
	 * The types of stats that can be held
	 * @author ss
	 *
	 */
	public static enum TextEntryType{
		/**
		 * valid, this is the global counter 
		 */
		VALID, 
		/**
		 * invalid for some other reason 
		 */
		INVALID, 
		/**
		 * invalid because of malformed json 
		 */
		INVALID_JSON, 
		/**
		 * invalid because the entry being read had zero length 
		 */
		INVALID_ZEROLENGTH, 
		/**
		 * invalid because the entry being read had no time entry 
		 */
		INVALID_TIME, 
		/**
		 * an actual emit is made 
		 */
		ACUAL_EMITS,
	}
	
	private Map<TextEntryType,Long> values;
	/**
	 * initialise the values map
	 */
	public TextGlobalStats() {
		this.values = new HashMap<TextEntryType, Long>();
	}
	
	/**
	 * intitalise the global stats from some counters
	 * @param counters
	 */
	public TextGlobalStats(Counters counters) {
		this();
		for (TextEntryType type : TextEntryType.values()) {
			Counter c = counters.findCounter(type);
			if(c!=null){
				this.values.put(type, c.getValue());
			}
		}
	}

	/**
	 * @param type
	 * @param value
	 */
	public void setValue(TextEntryType type, Long value) {
		this.values.put(type, value);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		int count = Integer.parseInt(in.nextLine());
		for (int i = 0; i < count; i++) {
			TextEntryType type = TextEntryType.valueOf(in.next());
			long value = Long.parseLong(in.next());
			this.values.put(type, value);
		}
	}

	@Override
	public String asciiHeader() {
		return "GLOBALSTATS\n";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(this.values.size());
		for (Entry<TextEntryType, Long> typevalue : this.values.entrySet()) {
			out.println(typevalue.getKey() + " " + typevalue.getValue());
		}
	}

	/**
	 * @param type
	 * @return the value for the given type
	 */
	public long getValue(TextEntryType type) {
		return this.values.get(type);
	}
	
	
}
