package org.openimaj.hadoop.tools.twitter.token.outputmode.stats;

import gnu.trove.TObjectLongHashMap;
import gnu.trove.TObjectLongProcedure;

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
	private TObjectLongHashMap<String> counts;

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
