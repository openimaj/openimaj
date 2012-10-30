package org.openimaj.tools.twitter.modes.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.kohsuke.args4j.Option;

/**
 * Uses {@link Pattern} to match regex
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JavaRegexEngine implements RegexEngine {
	enum RegexPatternMode{
		CASE_INSENSITIVE {
			@Override
			public int ord() {
				// TODO Auto-generated method stub
				return 2;
			}
		},// 2
		MULTILINE {
			@Override
			public int ord() {
				// TODO Auto-generated method stub
				return 8;
			}
		},// 8
		DOTALL {
			@Override
			public int ord() {
				// TODO Auto-generated method stub
				return 32;
			}
		},// 32
		UNICODE_CASE {
			@Override
			public int ord() {
				// TODO Auto-generated method stub
				return 64;
			}
		}, //64
		CANON_EQ {
			@Override
			public int ord() {
				// TODO Auto-generated method stub
				return 128;
			}
		}; // 128
		public abstract int ord();
	}

	@Option(name="--regex-pattern-mode", aliases="-rpm", required=false, usage="The integer representing the mode handed to java's Pattern. All provided modes are logically OR-ed together", metaVar="STRING", multiValued=true)
	List<RegexPatternMode> regexModes = new ArrayList<RegexPatternMode>();

	private List<Pattern> patterns;

	/**
	 *
	 */
	public JavaRegexEngine() {
		patterns = new ArrayList<Pattern>();
	}




	@Override
	public void add(String regex) {
		int patternMode = 0;
		for (RegexPatternMode mode : this.regexModes) {
			patternMode |= mode.ord();
		}
		this.patterns.add(Pattern.compile(regex, patternMode));
	}

	@Override
	public boolean matches(String str) {
		for (Pattern p : this.patterns) {
			if(p.matcher(str).matches()){
				return true;
			}
		}
		return false;
	}

}
