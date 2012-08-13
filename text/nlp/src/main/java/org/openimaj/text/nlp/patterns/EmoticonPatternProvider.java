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

import java.util.Arrays;
import java.util.Comparator;

import org.openimaj.text.util.RegexUtil;


/**
 * A regex for a set of emoticons
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class EmoticonPatternProvider extends PatternProvider {
	String[] EmoticonsDNArr = new String[] {
			":\\)",":\\(",":-\\)",">:\\]",":o\\)",":3",":c\\)",":>","=\\]","8\\)","=\\)",
			":\\}",":\\^\\)",">:D\\)",":-D",":D","8-D","8D","x-D","xD","X-D","XD","=-D","=D",
			"=-3","=3\\)","8-\\)",":-\\)\\)",":\\)\\)",">-\\[",":-\\(",":\\(",":-c",":c",":-<",":<",
			":-\\[",":\\[","\\[:",":\\{",">[._]>","<[._]<",">v<","\\^[._]\\^","\\>[._]\\<",":-\\|\\|","D:<","D+:","D8","D;","D=","DX",
			"v[.]v","D-\\':",">;\\]",";-\\)",";\\)","\\*-\\)","\\*\\)",";-\\]",";\\]",";D",";^\\)",">:[pP]",
			":-[pP]",":[pP]","X-[pP]","x-[pP]","x[pP]","X[pP]",":-[pP]",":[pP]",";[pP]","=[pP]",":-b",":b",">:o",">:O",":-O",
			":O",":0","o_O","o_0","o[.]O","8-0",">:\\",">:/",":-/",":-[.]",":/",":\\\\",
			"=\\/","=\\",":S",":\\|",":-\\|",">:X",":-X",":X",":-#",":#",":$","O:-\\)","0:-3",
			"0:3","O:-\\)","O:\\)","0;^\\)",">:\\)",">;\\)",">:-\\)",":\\'-\\(",":\\'\\(",":\\'-\\)",":\\'\\)",
			";\\)\\)",";;\\)","<3","8-\\}",">:D<","=\\)\\)","=\\(\\(","x\\(","X\\(",":-\\*",":\\*",":\\\">","~X\\(",":-?;",
			"\\-[._]\\-[']?","u[._]u[']?","\\*-\\*","[.]_[.]","[*]--[*]",
			 "\\([ ]*c[ ]*\\)","\\([ ]*tm[ ]*\\)", //THIS IS ABSOLUTELY DISGUSTING, IT SHOULD NOT BE HERE
		};

	String EmoticonsDN = RegexUtil.regex_or_match(longestfirst(EmoticonsDNArr));
//	String EmoticonsDN = regex_or(EmoticonsDNArr);

	@Override
	public String patternString(){
		return EmoticonsDN;
	}

	private String[] longestfirst(String[] emoticons) {
		Arrays.sort(emoticons,new Comparator<String>(){

			@Override
			public int compare(String s1, String s2) {
				int s1Longer = s1.length() - s2.length();
				if(s1Longer > 0) return -1;
				else if(s1Longer < 0) return 1;
				else{
					return s1.compareTo(s2);
				}
			}

		});
		return emoticons;
	}

	@Override
	public PatternProvider combine(PatternProvider other) {
		return new CombinedPatternProvider(this,other);
	}
}
