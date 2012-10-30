package org.openimaj.tools.twitter.modes.filter;

import java.util.ArrayList;

import com.ibm.icu.text.UnicodeSet;

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
		for (UnicodeSet set : sets) {
			if(set.containsAll(str))
				return true;
		}
		return false;
	}

	@Override
	public void add(String pat) {
		sets.add(new UnicodeSet("[" + pat + "]"));
	}

}
