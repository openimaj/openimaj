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
package org.openimaj.tools.twitter.modes.filter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.kohsuke.args4j.Option;
import org.openimaj.twitter.USMFStatus;

/**
 * The grep functionality. Should only be used as a post filter most of the time
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DateFilter extends TwitterPreprocessingPredicate {
	
	@Option(name="--date-start", aliases="-from", required=false, usage="The start date", metaVar="STRING", multiValued=true)
	String startDateStr;
	DateTime startDate;
	@Option(name="--end-start", aliases="-to", required=false, usage="The start date", metaVar="STRING")
	String endDateStr;
	DateTime endDate;
	@Option(name="--date-range", aliases="-drng", required=false, usage="Comma delimited start,end date range", metaVar="STRING", multiValued=true)
	List<String> dateRanges = new ArrayList<String>();
	List<Interval> intervals = new ArrayList<Interval>();
	
	
	
	
	@Override
	public boolean test(USMFStatus twitterStatus) {
		DateTime date;
		
		try {
			date = twitterStatus.createdAt();
		} catch (ParseException e) {
			System.out.println("Failed to parse: " + twitterStatus);
			return false;
		}
		
		if(date == null) {
			System.out.println("no date for: " + twitterStatus);
			return false;
		}
		// valid date, is it after the start and before the end?
		
		if(startDate!=null && date.isBefore(startDate)) {
			System.out.println(date + " is before " + startDate);
			return false;
		}
		if(endDate!=null && date.isAfter(endDate)) {
			System.out.println(date + " is after " + endDate);
			return false;
		}
		// We are both after the start and after the end, but are we within one of the intervals?
		boolean match = this.intervals.size() == 0;
		for (Interval  interval : this.intervals) {
			match = interval.contains(date);
			if(match) return match; //it is inside one of the intervals
		}
		
		return match;
	}
	
	@Override
	public void validate() {
		if(startDateStr != null){
			startDate = DateTimeFormat.forPattern("Y/M/d").parseDateTime(startDateStr);
		}
		
		if(endDateStr != null){
			endDate = DateTimeFormat.forPattern("Y/M/d").parseDateTime(endDateStr);
		}
		
		for (String dateRange : this.dateRanges) {
			String[] dRangeSplit = dateRange.split(",");
			if(dRangeSplit.length!=2){
				continue;
			}
			DateTime start = DateTimeFormat.forPattern("Y/M/d").parseDateTime(dRangeSplit[0]);
			DateTime end = DateTimeFormat.forPattern("Y/M/d").parseDateTime(dRangeSplit[1]);
			this.intervals.add(new Interval(start, end));
			
		}
	}

}
