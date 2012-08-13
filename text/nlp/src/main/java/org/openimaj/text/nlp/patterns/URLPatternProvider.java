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

import java.util.regex.Pattern;

/**
 * Various kinds of URL Pattern
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class URLPatternProvider extends PatternProvider {

	/**
	 * 
	 * Implementation of the URL regex from
	 * http://daringfireball.net/2010/07/improved_regex_for_matching_urls
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         <ss@ecs.soton.ac.uk>
	 * 
	 */
	public static class DFURLPatternProvider extends URLPatternProvider {
		/**
		 * 
		 */
		public DFURLPatternProvider() {
			Url = "\\b" + "(" + // Capture 1: entire matched URL
					"(?:" + "https?://" + // http or https protocol
					"|" + // or
					"www\\d{0,3}[.]" + // "www.", "www1.", "www2." ... "www999."
					"|" + // or
					// "([\\S]+[.])+[a-z]{2,4}/" + // looks like domain
					// name followed by a slash
					"[A-Za-z0-9.\\-]+[.][a-z]{2,4}/" + // looks
														// like
														// domain
														// name
														// followed
														// by a
														// slash
					")" + "(?:" + // One or more:
					"[^\\s()<>]+" + // Run of non-space, non-()<>
					"|" + // or
					"\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)" + // balanced
																	// parens,
																	// up to 2
																	// levels
					")+" + "(?:" + // End with:
					"\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)" + // balanced
																	// parens,
																	// up to 2
																	// levels
					"|" + // or
					"[^\\s`!()\\[\\]{};:'\".,<>?\u00AB\u00BB\u201C\u201D\u2018\u2019]" + // not
																							// a
																							// space
																							// or
																							// one
																							// of
																							// these
																							// punct
																							// chars
					")" + ")";
		}
	}

	protected String Url;

	/**
	 * @param punctuation
	 * @param entity
	 */
	public URLPatternProvider(PunctuationPatternProvider punctuation, EntityPatternProvider entity) {
		// final String validLettersAndNumbers = "[a-z\\u00a1-\\uffff0-9]";
		final String validLettersAndNumbersAndDots = "[a-z\\u00a1-\\uffff0-9\\-.]";
		/*
		 * final String hostNamePart = "(?:" + validLettersAndNumbers + "+-?)*"
		 * + validLettersAndNumbers + "+"; // something // or //
		 * something-something // but // never // just // something-
		 */Url = "\\b" +
		// protocol identifier
				"(?:(?:https?://|ftp://|www\\d{0,3}[.]))" +
				// user:pass authentication
				"(?:\\S+(?::\\S*)?@)?" + "(?:" +
				// IP address exclusion
				// private & local networks
				"(?!10(?:\\.\\d{1,3}){3})"
				//
				+ "(?!127(?:\\.\\d{1,3}){3})"
				//
				+ "(?!169\\.254(?:\\.\\d{1,3}){2})"
				//
				+ "(?!192\\.168(?:\\.\\d{1,3}){2})"
				//
				+ "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})"
				//
				+
				// IP address dotted notation octets
				// excludes loopback network 0.0.0.0
				// excludes reserved space >= 224.0.0.0
				// excludes network & broacast addresses
				// (first & last IP address of each class)
				"(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])"
				//
				+ "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}"
				//
				+ "(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))" + "|"
				//
				+
				// // host name
				// "(?:"+hostNamePart +")"
				// +
				// // domain name
				// "(?:\\."+hostNamePart+")*"
				// +
				"(?:" + validLettersAndNumbersAndDots + ")+" +
				// TLD identifier
				"(?:\\.(?:[a-z\\u00a1-\\uffff]{2,4})" + ")" + ")" +
				// port number
				"(?::\\d{2,5})?" +
				// resource path
				"(?:/[^\\s]*)?";
	}

	/**
	 * 
	 */
	public URLPatternProvider() {
		this(new PunctuationPatternProvider(), new EntityPatternProvider());
	}

	@Override
	public String patternString() {
		return String.format("(%s)", Url);
	}

	@Override
	public Pattern pattern() {
		return Pattern.compile(patternString(), Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
	}

}
