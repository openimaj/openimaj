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
package org.openimaj.hadoop.tools.twitter.token.outputmode.stats;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectLongProcedure;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.openimaj.text.nlp.patterns.EdgePunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.EmoticonPatternProvider;
import org.openimaj.text.nlp.patterns.PatternProvider;
import org.openimaj.text.nlp.patterns.PunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.TimePatternProvider;
import org.openimaj.text.nlp.patterns.TwitterStuffPatternProvider;
import org.openimaj.text.nlp.patterns.URLPatternProvider;

public class StatsWordMatch {
	private HashMap<String, Pattern> available;
	private TObjectLongMap<String> counts;

	public StatsWordMatch() {
		this.available = new HashMap<String,Pattern>();
		addAvail(new EmoticonPatternProvider());
		addAvail(new URLPatternProvider());
		addAvail(new TimePatternProvider());
		addAvail(new PunctuationPatternProvider());
		TwitterStuffPatternProvider tpp = new TwitterStuffPatternProvider();
		addAvail("TwitterStuff.hashtags", tpp.hashtagPatternString());
		addAvail("TwitterStuff.retweets", tpp.retweetPatternString());
		addAvail("TwitterStuff.username", tpp.usernamePatternString());
		addAvail("EdgePunctuation",EdgePunctuationPatternProvider.edgePuncPattern());
		this.counts = new TObjectLongHashMap<String>();
	}

	private void addAvail(PatternProvider pp) {
		String name = pp.getClass().getName().split("PatternProvider")[0];
		name = name.substring(pp.getClass().getPackage().getName().length() + 1);
		addAvail(name,pp);
		
	}
	
	private void addAvail(String name, PatternProvider pp) {
		addAvail(name,pp.patternString());
	}
	
	private void addAvail(String name, String pattern) {
		
		this.available.put(name, Pattern.compile(pattern,Pattern.UNICODE_CASE|Pattern.CASE_INSENSITIVE));
	}
	
	
	public void updateStats(String word, long count){
		boolean added = false;
		String tokenise = " %s ";
		String formattedWord = String.format(tokenise,word);
		for (Entry<String, Pattern> spp: this.available.entrySet()) {
			String name = spp.getKey();
			Pattern pp = spp.getValue();
			if(pp.matcher(formattedWord).find()){
				this.counts.adjustOrPutValue(name,count,count);
				added=true;
			}
		}
		if(!added){
//			System.out.println("Adding to other: '" + word + "'");
			this.counts.adjustOrPutValue("Other", count, count);
		}
	}
	
	@Override
	public String toString(){
		final StringBuffer buffer = new StringBuffer();
		buffer.append("Type Stats:\n");
		final String format = "%s: %d\n";
		this.counts.forEachEntry(new TObjectLongProcedure<String>(){
			@Override
			public boolean execute(String stat, long count) {
				buffer.append(String.format(format,stat,count));
				return true;
			}			
		});
		return buffer.toString();
	}
}
