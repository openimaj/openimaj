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
package org.openimaj.text.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.patterns.PatternProvider;

/**
 * A collection of utility functions for regex construction
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class RegexUtil {

	/**
	 * @param items
	 * @return match within brackets a set of "or"ed items
	 */
	public static String regex_or_match(String... items) {
		String r = StringUtils.join(items, "|");
		r = '(' + r + ')';
		return r;
	}

	/**
	 * @param patterns
	 * @return match wtihin brackets a set of "or"ed patterns (calling
	 *         {@link PatternProvider#patternString()})
	 */
	public static String regex_or_match(PatternProvider... patterns) {
		final String[] allpat = new String[patterns.length];
		int i = 0;
		for (final PatternProvider patternProvider : patterns) {
			allpat[i++] = patternProvider.patternString();
		}
		return regex_or_match(allpat);
	}

	/**
	 * @param items
	 * @return using {@link StringUtils#join(java.util.Collection, String)} or
	 *         together a list of strings
	 */
	public static String regex_or_match(List<String> items) {
		String r = StringUtils.join(items, "|");
		r = '(' + r + ')';
		return r;
	}

	/**
	 * @param puncs
	 * @return negate
	 */
	public static String regex_char_neg(List<String> puncs) {
		String r = StringUtils.join(puncs, "");
		r = "[" + r + ']';
		return r;
	}

	/**
	 * @param r
	 * @return construct a positive equality lookahead
	 */
	public static String pos_lookahead(String r) {
		return "(?=" + r + ')';
	}

	/**
	 * @param r
	 * @return construct a negative equality lookahead
	 */
	public static String neg_lookahead(String r) {
		return "(?!" + r + ')';
	}

	/**
	 * @param r
	 * @return construct an optional
	 */
	public static String optional(String r) {
		return String.format("(%s)?", r);
	}
}
