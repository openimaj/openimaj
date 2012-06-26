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

import org.openimaj.text.util.RegexUtil;

/**
 * Regex for numbers with a decimal point or command separated. Greedy search, won't stop with 10,000 in the number 10,000,000.
 * 
 * I
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ComplicatedNumberPatternProvider extends PatternProvider{
	private static final String[] after_number = new String[]{"[^\\d,\\-a-z.]","$"};
	private static final String[] number_ends = new String[]{"\\.\\d+"};
//	String Number = "(?:\\b|[$])\\d+" + pos_lookahead(regex_or("[^,\\d]","$"));
	
	String NumNum = String.format("(?:\\b|[$])\\d+%s",RegexUtil.regex_or_match(number_ends))+ RegexUtil.pos_lookahead(RegexUtil.regex_or_match(after_number));
	String NumberWithCommas = "(?:\\b|[$])(?:\\d+,)+\\d{3}(?:.\\d+)?"+ RegexUtil.pos_lookahead(RegexUtil.regex_or_match(after_number));
	
	@Override
	public String patternString() {
		return RegexUtil.regex_or_match(NumNum,NumberWithCommas);
	}
}
