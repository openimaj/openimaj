/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools.reddit;

import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Period;
import org.kohsuke.args4j.Option;
import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;
import org.openimaj.util.pair.LongLongPair;

/**
 * The time split attempts to read times from the json items provided and splits
 * by WEEK,DAY,HOUR or MINUTE 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TimeSplitMode extends SplitMode {
	private static final String outputFormat = "%s.%s.s%s.e%s.json";
	
	enum TimeSplitPeriod{
		WEEK {
			@Override
			public LongLongPair startEnd(long time) {
				DateTime dt = new DateTime(time);
				DateTime start = dt.withDayOfWeek(DateTimeConstants.MONDAY);
				DateTime end = dt.plus(weekPeriod).withDayOfWeek(DateTimeConstants.MONDAY);
				
				return LongLongPair.pair(start.getMillis(), end.getMillis());
			}
		},DAY {
			@Override
			public LongLongPair startEnd(long time) {
				DateTime dt = new DateTime(time);
				DateTime start = dt.withHourOfDay(0);
				DateTime end = dt.plus(dayPeriod).withHourOfDay(0);
				
				return LongLongPair.pair(start.getMillis(), end.getMillis());
			}
		},HOUR {
			@Override
			public LongLongPair startEnd(long time) {
				DateTime dt = new DateTime(time);
				DateTime start = dt.withMinuteOfHour(0);
				DateTime end = dt.plus(hourPeriod).withMinuteOfHour(0);
				
				return LongLongPair.pair(start.getMillis(), end.getMillis());
			}
		},MINUTE {
			@Override
			public LongLongPair startEnd(long time) {
				DateTime dt = new DateTime(time);
				DateTime start = dt.withSecondOfMinute(0);
				DateTime end = dt.plus(minutePeriod).withSecondOfMinute(0);
				
				return LongLongPair.pair(start.getMillis(), end.getMillis());
			}
		};
		Period weekPeriod = new Period(0, 0, 1, 0, 0, 0, 0, 0);
		Period dayPeriod = new Period(0, 0, 0, 1, 0, 0, 0, 0);
		Period hourPeriod = new Period(0, 0, 0, 0, 1, 0, 0, 0);
		Period minutePeriod = new Period(0, 0, 0, 0, 0, 1, 0, 0);
		public abstract LongLongPair startEnd(long time);
	}
	
	@Option(name="--split-period", aliases="-sp", required=false, usage="the file split period.")
	protected TimeSplitPeriod period = TimeSplitPeriod.MINUTE;
	
	@Override
	public void output(List<ReadableWritableJSON> read) {
//		Map<String,List<ReadableWritableJSON>> fileOutputs = new HashMap<String, List<ReadableWritableJSON>>();
//		for (ReadableWritableJSON readableWritableJSON : read) {
//			@SuppressWarnings("unchecked")
//			List<Map<String,Object>> items = (List<Map<String, Object>>) ((Map<String,Object>)readableWritableJSON.get("data")).get("children");
//			
//		}
//		return null;
	}

}
