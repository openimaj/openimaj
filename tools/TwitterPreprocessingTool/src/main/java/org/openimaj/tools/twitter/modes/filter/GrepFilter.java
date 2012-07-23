package org.openimaj.tools.twitter.modes.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.arabidopsis.ahocorasick.AhoCorasick;
import org.kohsuke.args4j.Option;
import org.openimaj.twitter.USMFStatus;

/**
 * The grep functionality. Should only be used as a post filter most of the time
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GrepFilter extends TwitterPreprocessingFilter {
	
	@Option(name="--string-match", aliases="-sm", required=false, usage="Match these strings exactly, uses aho-corasick", metaVar="STRING", multiValued=true)
	List<String> stringMatch = new ArrayList<String>();
	AhoCorasick<String> searcher = null;
	
	@Option(name="--regexes", aliases="-r", required=false, usage="Match these regexes. Uses java pattern", metaVar="STRING", multiValued=true)
	List<String> regexStrings = new ArrayList<String>();
	List<Pattern> regex = new ArrayList<Pattern>();
	
	@Override
	public boolean filter(USMFStatus twitterStatus) {
		String text = twitterStatus.text;
		boolean match = searcher.search(text.getBytes()).hasNext();
		if(match) return match;
		// now do the slower regexes if there are any
		for (Pattern reg : this.regex) {
			match = reg.matcher(text).find();
			if(match) return match;
		}
		
		return match; // must be false
	}
	
	@Override
	public void validate() {
		searcher = new AhoCorasick<String>();
		for (String match : this.stringMatch) {
			searcher.add(match.getBytes(), match);	
		}
		searcher.prepare();
		
		for (String pat : this.regexStrings) {
			regex.add(Pattern.compile(pat));
		}
	}
}
