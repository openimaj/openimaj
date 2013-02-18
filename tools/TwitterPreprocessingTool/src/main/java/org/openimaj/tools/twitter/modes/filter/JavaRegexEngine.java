/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
