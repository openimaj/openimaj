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
 * A writeable hadoop {@link Counter} written using the enum <T> used with the counter
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> the enum type
 *
 */
public abstract class WritableEnumCounter<T extends Enum<?>> implements ReadWriteableASCII {
	private Map<T,Long> values;
	/**
	 * initialise the values map
	 */
	public WritableEnumCounter() {
		this.values = new HashMap<T, Long>();
	}
	
	/**
	 * intitalise the global stats from some counters
	 * @param counters the counters to look for the enum types in
	 * @param enumType the enum types
	 */
	public WritableEnumCounter(Counters counters,T[] enumType) {
		this();
		initCounts(enumType,counters);
	}

	private void initCounts(T[] values, Counters counters) {
		for (T type : values) {
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
	public void setValue(T type, Long value) {
		this.values.put(type, value);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		int count = Integer.parseInt(in.nextLine());
		for (int i = 0; i < count; i++) {
			T type = valueOf(in.next());
			long value = Long.parseLong(in.next());
			this.values.put(type, value);
		}
	}

	/**
	 * @param str
	 * @return The enum value of a given name
	 */
	public abstract T valueOf(String str);

	@Override
	public String asciiHeader() {
		return "GLOBALSTATS\n";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(this.values.size());
		for (Entry<T, Long> typevalue : this.values.entrySet()) {
			out.println(typevalue.getKey() + " " + typevalue.getValue());
		}
	}

	/**
	 * @param type
	 * @return the value for the given type
	 */
	public long getValue(T type) {
		return this.values.get(type);
	}
	
	
}
