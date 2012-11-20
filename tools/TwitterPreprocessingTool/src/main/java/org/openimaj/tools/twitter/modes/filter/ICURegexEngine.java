package org.openimaj.tools.twitter.modes.filter;

import java.util.ArrayList;

import sun.text.normalizer.UnicodeSet;


/**
 * The ICU Unicode engine using {@link UnicodeSet}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ICURegexEngine implements RegexEngine {
	private ArrayList<UnicodeSet> sets;

	/**
	 *
	 */
	public ICURegexEngine() {
		sets = new ArrayList<UnicodeSet>();
	}
	@Override
	public boolean matches(String str) {
		return false;
	}

	@Override
	public void add(String pat) {
		sets.add(new UnicodeSet("[" + pat + "]"));
	}

}
