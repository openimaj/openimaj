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
package org.openimaj.experiment.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.openimaj.time.NanoTimer;
import org.openimaj.time.Timer;

import com.bethecoder.ascii_table.ASCIITable;

/**
 * A class for tracking various execution times and generating
 * statistics.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class TimeTracker {
	private static Map<String, SummaryStatistics> times = new HashMap<String, SummaryStatistics>();
	
	/**
	 * Accumulate the given duration into the statistics with the given identifier
	 * 
	 * @param identifier the identifier
	 * @param timer the timer to retrieve the dureation from
	 */
	public static synchronized void accumulate(String identifier, NanoTimer timer) {
		accumulate(identifier, timer.duration());
	}
	
	/**
	 * Accumulate the given duration into the statistics with the given identifier
	 * 
	 * @param identifier the identifier
	 * @param timer the timer to retrieve the dureation from
	 */
	public static synchronized void accumulate(String identifier, Timer timer) {
		accumulate(identifier, (long)(timer.duration() * 1e6));
	}
	
	/**
	 * Accumulate the given duration into the statistics with the given identifier
	 * 
	 * @param identifier the identifier
	 * @param nanoTime the duration to accumulate in nano seconds
	 */
	public static synchronized void accumulate(String identifier, long nanoTime) {
		SummaryStatistics t = times.get(identifier);
		
		if (t == null) times.put(identifier, t = new SummaryStatistics());
		
		t.addValue(nanoTime);
	}
	
	/**
	 * Reset all the previously accumulated times, returning them.
	 * @return the old times
	 */
	public static synchronized Map<String, SummaryStatistics> reset() {
		Map<String, SummaryStatistics> oldTimes = times;
		times = new HashMap<String, SummaryStatistics>();
		return oldTimes;
	}
	
	/**
	 * Get a copy of all the accumulated data
	 * @return a copy of all the accumulated data
	 */
	public static synchronized Map<String, SummaryStatistics> getTimes() {
		HashMap<String, SummaryStatistics> ret = new HashMap<String, SummaryStatistics>();
		
		for (Entry<String, SummaryStatistics> e : times.entrySet()) {
			ret.put(e.getKey(), e.getValue().copy());
		}
		
		return ret;
	}

	/**
	 * Add any times from the given map that are not present in the
	 * internal map to the internal map.
	 * 
	 * @param timesToAdd the times to add
	 */
	public static void addMissing(Map<String, SummaryStatistics> timesToAdd) {
		for (Entry<String, SummaryStatistics> e : timesToAdd.entrySet()) {
			if (!times.containsKey(e.getValue()))
				times.put(e.getKey(), e.getValue());
		}
	}
	
	/**
	 * Pretty-print a map of times
	 * 
	 * @param times the times
	 * @return a string representation of the times
	 */
	public static String format(Map<String, SummaryStatistics> times) {
		String [] header = {"Timer Identifier", "Recorded Time"};
		String [][] data = new String[times.size()][];
		
		int i = 0;
		for (Entry<String, SummaryStatistics> e : times.entrySet()) {
			data[i++] = new String [] { e.getKey(), format(e.getValue()) };
		}
		
		return ASCIITable.getInstance().getTable(header, data);
	}
	
	/**
	 * Pretty print a time
	 * @param ss the stats defining the time
	 * @return a string representing the time
	 */
	public static String format(SummaryStatistics ss) {
		if (ss.getN() == 1) {
			return formatTime(ss.getMean());
		} 
		return formatTime(ss.getMean(), ss.getStandardDeviation());
	}

	private static String formatTime(double time) {
		long ns = (long) time;
		
		if (ns < 1e3) {
			return time + "ns";
		}
		if (ns < 1e6) {
			return (time / 1e3) + "us";
		}
		if (ns < 1e9) {
			return (time / 1e6) + "ms";
		}
		
		double secs = (time / 1e9);
		if (secs < 60) {
			return String.format("%2.5ss", secs);
		}
		
		long mins = (long)(secs / 60);
		double rsecs = secs - (mins * 60);
		if (mins < 60) {
			return mins + "m" + String.format("%2.5ss", rsecs);
		}
	
		long hrs = (long)(mins / 60);
		long rmins = mins - (hrs * 60);
		if (hrs < 24) { 
			return hrs + "h" + rmins + "m" + String.format("%2.5ss", rsecs);
		}
		
		long days = (long)(hrs / 24);
		long rhrs = days - (hrs * 24);
		if (hrs < 24) { 
			return days + "d" + rhrs + "h" + rmins + "m" + String.format("%2.5ss", rsecs);
		}
	
		return time+"ns";
	}

	private static String formatTime(double mean, double standardDeviation) {
		return formatTime(mean) + " (SD = " +formatTime(standardDeviation) + ")";
	}
}
