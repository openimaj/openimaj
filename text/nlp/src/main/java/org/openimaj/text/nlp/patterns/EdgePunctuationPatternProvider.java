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
package org.openimaj.text.nlp.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Match edge punctuations and correct them such that they can be matched by a
 * simple space split
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class EdgePunctuationPatternProvider extends PatternProvider {

	protected String EdgePunct;
	protected String NotEdgePunct;
	protected String StartPunc;
	protected String EndPunc;

	/**
	 * @param punctuation
	 */
	public EdgePunctuationPatternProvider(PunctuationPatternProvider punctuation) {
		final String[] EdgePunctArr = new String[] { "'", "\"", "\\*", "\u201c", "\u201d", "\u2018", "\u2019", "\\<",
				"\\>", "\u00AB", "\u00BB", "{", "}", "\\(", "\\)", "\\[", "\\]", "\\\\", "\\|" };
		final HashSet<String> edgeSet = new HashSet<String>();
		for (final String string : EdgePunctArr) {
			edgeSet.add(string);
		}
		EdgePunct = "[" + StringUtils.join(EdgePunctArr, "") + "]";
		final List<String> puncArr = new ArrayList<String>();
		for (final String punc : punctuation.PunctCharsList) {
			if (edgeSet.contains(punc))
				continue;
			puncArr.add(punc);
		}

		NotEdgePunct = "(?:[a-zA-Z0-9]|" + "[" + StringUtils.join(puncArr, "") + "\\-]" + ")";
		// NotEdgePunct = "(?:[a-zA-Z0-9])";
		StartPunc = "\\s|^|[.,]|" + "[a-zA-Z0-9]";
		EndPunc = "\\s|$|[.,]|" + "[a-zA-Z0-9]";
	}

	/**
	 * @return the edge punctuation of the default
	 *         {@link EdgePunctuationPatternProvider}
	 */
	public static String edgePuncPattern() {
		return new EdgePunctuationPatternProvider(new PunctuationPatternProvider()) {
			@Override
			public String correctEdges(String s) {
				return null;
			}

			@Override
			public String patternString() {
				return null;
			}

		}.EdgePunct;
	}

	/**
	 * @param s
	 * @return given a string, match the edge punctuation and deal with it
	 *         somehow
	 */
	public abstract String correctEdges(String s);

	/**
	 * Left edge punctuations. Construct the edge pattern with
	 * (StartPunc)(EdgePunct+)(NotEdgePunct) and replaces with: $1$2 $3.
	 * 
	 * This solves this problem: "Here is (bracketed string)" is replaced with:
	 * "Here is ( bracketed string)"
	 * 
	 * by this class
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class Left extends EdgePunctuationPatternProvider {
		protected String EdgePunctLeft;

		/**
		 * @see EdgePunctuationPatternProvider#EdgePunctuationPatternProvider(PunctuationPatternProvider)
		 * @param punctuation
		 */
		public Left(PunctuationPatternProvider punctuation) {
			super(punctuation);
			EdgePunctLeft = String.format("(%s)(%s+)(%s)", StartPunc, EdgePunct, NotEdgePunct);
		}

		@Override
		public String patternString() {
			return EdgePunctLeft;
		}

		@Override
		public String correctEdges(String s) {
			// Matcher matcher = pattern().matcher(s);
			// while(matcher.find()){
			// System.out.println("Found RIGHT match: '" +
			// s.substring(matcher.start(),matcher.end()) + "'");
			// System.out.println("... ngroups: " + matcher.groupCount());
			// for(int i = 0; i < matcher.groupCount(); i++){
			// System.out.println("... ... '" + matcher.group(i) + "'");
			// }
			// }
			return pattern().matcher(s).replaceAll("$1$2 $3");
		}

	}

	/**
	 * Left edge punctuations. Construct the edge pattern with
	 * (StartPunc)(EdgePunct+)(NotEdgePunct) and replaces with: $1 $2$3.
	 * 
	 * This solves this problem: "Here is (bracketed string)" is replaced with:
	 * "Here is (bracketed string )"
	 * 
	 * by this class
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class Right extends EdgePunctuationPatternProvider {
		protected String EdgePunctRight;

		/**
		 * @param punctuation
		 *            currently unused
		 */
		public Right(PunctuationPatternProvider punctuation) {
			super(punctuation);
			EdgePunctRight = String.format("(%s)(%s+)(%s)", NotEdgePunct, EdgePunct, EndPunc);
			// System.out.println("Right match pattern: " + EdgePunctRight);
		}

		@Override
		public String patternString() {
			return EdgePunctRight;
		}

		@Override
		public String correctEdges(String s) {
			//
			// Matcher matcher = pattern().matcher(s);
			// while(matcher.find()){
			// System.out.println("Found RIGHT match: '" +
			// s.substring(matcher.start(),matcher.end()) + "'");
			// System.out.println("... ngroups: " + matcher.groupCount());
			// for(int i = 0; i < matcher.groupCount(); i++){
			// System.out.println("... ... '" + matcher.group(i) + "'");
			// }
			// }
			final String ret = pattern().matcher(s).replaceAll("$1 $2$3");
			return ret;
		}

	}

	static PunctuationPatternProvider punctuation = new PunctuationPatternProvider();
	static EdgePunctuationPatternProvider edgeleft = new EdgePunctuationPatternProvider.Left(punctuation);
	static EdgePunctuationPatternProvider edgeright = new EdgePunctuationPatternProvider.Right(punctuation);

	/**
	 * pads start/end brackets with a space so they can be correctly matched
	 * while not screwing up the rest of the text
	 * 
	 * @param text
	 * @return the corrected text
	 */
	public static String fixedges(String text) {
		String s = text;
		s = edgeleft.correctEdges(s);
		s = edgeright.correctEdges(s);
		;
		return s;
	}

}
