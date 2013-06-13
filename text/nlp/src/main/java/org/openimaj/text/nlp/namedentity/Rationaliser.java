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
package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;

/**
 * This class aims to return a list of possible rationalizations of a word that
 * is out of vocabulary. Spell checking should have been used without success
 * before attempting to use this tool. Currently it just removes excessive
 * repetition.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class Rationaliser {

	ArrayList<Section> sections;

	public Rationaliser() {
	}

	/**
	 * @param word
	 * @return list of the rationalised possibilities
	 */
	public ArrayList<String> getCombinations(String word) {
		final ArrayList<String> result = new ArrayList<String>();
		final char[] characters = word.toCharArray();
		sections = new ArrayList<Section>();

		Character last = null;
		char lastCount = 0;
		StringBuffer unclassed = new StringBuffer();

		// Build Sections
		for (int i = 0; i < characters.length; i++) {
			final char current = characters[i];
			// Process first char for new unclassed section.
			if (last == null) {
				last = current;
				lastCount++;
				continue;
			}
			// if we get a repeat
			if (last.equals(current)) {
				// eat if it is a repeat repeat
				if (lastCount > 1)
					continue;
				else {
					// put valid buffer in section
					if (unclassed.length() > 0) {
						final StringBuffer pass = unclassed;
						unclassed = new StringBuffer();
						sections.add(new ValidSection(pass));
					}
					lastCount++;
					final RepeatSection n = new RepeatSection(new StringBuffer(last.toString()));
					sections.add(n);
					continue;
				}
			}
			// if it is not a repeat
			if (lastCount == 1)
				unclassed.append(last);
			last = current;
			lastCount = 1;
		}
		if (lastCount == 1)
			unclassed.append(last);
		if (unclassed.length() > 0)
			sections.add(new ValidSection(unclassed));

		// get all the combinations from the sections
		for (final StringBuffer sb : getSubCombinations(0)) {
			result.add(sb.toString());
		}
		return result;
	}

	private ArrayList<StringBuffer> getSubCombinations(int position) {
		if (position == sections.size() - 1)
			return sections.get(position).getCombinations();
		else {
			final ArrayList<StringBuffer> result = new ArrayList<StringBuffer>();
			for (final StringBuffer stringBuffer : sections.get(position).getCombinations()) {
				for (final StringBuffer stringBuffer2 : getSubCombinations(position + 1)) {
					result.add(new StringBuffer(new StringBuffer(stringBuffer).append(stringBuffer2)));
				}
			}
			return result;
		}
	}

	private abstract class Section {
		public StringBuffer value;

		public Section(StringBuffer value) {
			this.value = value;
		}

		public abstract ArrayList<StringBuffer> getCombinations();
	}

	private class ValidSection extends Section {
		public ValidSection(StringBuffer value) {
			super(value);
		}

		@Override
		public ArrayList<StringBuffer> getCombinations() {
			final ArrayList<StringBuffer> res = new ArrayList<StringBuffer>();
			res.add(new StringBuffer(this.value));
			return res;
		}
	}

	private class RepeatSection extends Section {

		public RepeatSection(StringBuffer value) {
			super(value);
		}

		@Override
		public ArrayList<StringBuffer> getCombinations() {
			final ArrayList<StringBuffer> res = new ArrayList<StringBuffer>();
			res.add(new StringBuffer(this.value));
			final StringBuffer two = new StringBuffer(this.value);
			two.append(value.toString().toCharArray()[0]);
			res.add(two);
			return res;
		}
	}

	public static void main(String[] args) {
		final Rationaliser rc = new Rationaliser();
		for (final String s : rc.getCombinations("BBBlaaaaddddiblah")) {
			System.out.println(s);
		}
	}

}
