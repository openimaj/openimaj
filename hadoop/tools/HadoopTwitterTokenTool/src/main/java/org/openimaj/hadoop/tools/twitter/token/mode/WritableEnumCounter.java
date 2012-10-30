/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
		if(!(in.hasNextLine())){
			System.out.println("WritableEnumCounter has no next line? weird?");
			return;// not sure why this would happen?
		}
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
		if(values == null)return 0;
		return this.values.get(type);
	}


}
