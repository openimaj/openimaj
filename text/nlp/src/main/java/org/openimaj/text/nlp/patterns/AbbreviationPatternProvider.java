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

public class AbbreviationPatternProvider extends PatternProvider {
	
	private String ArbitraryAbbrev;

	public AbbreviationPatternProvider(EntityPatternProvider entity) {
//		String[] Abbrevs1 = new String[]{"am","pm","us","usa","ie","eg"};
//		
//		this.Abbrevs = regexify_abbrev(Abbrevs1);
	//
		String BoundaryNotDot = RegexUtil.regex_or_match("\\s", "[\\u201c\\u201d\"?!,:;]", entity.patternString());
		String aa1 = "([A-Za-z]\\.){2,}" + RegexUtil.pos_lookahead(BoundaryNotDot);
		String aa2 = "([A-Za-z]\\.){1,}[A-Za-z]" + RegexUtil.pos_lookahead(BoundaryNotDot);
		this.ArbitraryAbbrev = RegexUtil.regex_or_match(aa1,aa2);
	}
	
//	private String[] regexify_abbrev(String[] a){
//		String[] out = new String[a.length];
//		for (int i = 0 ; i < a.length; i++) {
//			String s = a[i];
//			String dotted = "";
//			for (int j = 0; j < s.length(); j++) {
//				dotted += s.substring(j,j+1).toUpperCase() + "\\.";
//			}
//			out[i] = dotted;
//		}
//		return out;
//	}

	@Override
	public String patternString() {
		return this.ArbitraryAbbrev;
	}
	
}
