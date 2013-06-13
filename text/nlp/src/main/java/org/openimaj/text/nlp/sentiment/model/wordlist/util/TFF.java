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
package org.openimaj.text.nlp.sentiment.model.wordlist.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.io.ReadableASCII;

/**
 * The TFF data format is the word clue format used by OpinionFinder. Details of
 * MPQA and this format can be found: http://www.cs.pitt.edu/mpqa/
 * <p>
 * The way to think about TFF entries are clues that a given word (or set of
 * words) give to the sentiment and subjectivity of a given phrase. There are
 * many clever ways to use this information highlighted in this paper:
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
@Reference(
		author = { "Janyce Wiebe", "Theresa Wilson", "Claire Cardie" },
		title = "Annotating expressions of opinions and emotions in language. ",
		type = ReferenceType.Article,
		year = "2005")
public class TFF implements ReadableASCII {
	/**
	 * The subjectivity leve
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static enum Type {
		/**
		 * very subjective
		 */
		strongsubj,
		/**
		 * weakly subjective
		 */
		weaksubj
	}

	/**
	 * The Part of Speech of this clue. i.e. the clue applies when the word is
	 * at this POS
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static enum Pos {
		/**
		 * Adjective
		 */
		adj,
		/**
		 * Adverb
		 */
		adverb,
		/**
		 * wherever seen
		 */
		anypos,
		/**
		 * seen as a noun
		 */
		noun,
		/**
		 * seen as a verb
		 */
		verb
	}

	/**
	 * The polarity of this word in this POS
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static enum Polarity {
		/**
		 * both positive and negative
		 */
		both,
		/**
		 * neutral
		 */
		neutral,
		/**
		 * negative
		 */
		negative,
		/**
		 * 
		 */
		weakneg,
		/**
		 * 
		 */
		strongneg,
		/**
		 * 
		 */
		positive,
		/**
		 * 
		 */
		strongpos,
		/**
		 * 
		 */
		weakpos
	}

	/**
	 * A particular clue
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class Clue {
		/**
		 * The clue subjectivity
		 */
		public Type type;
		/**
		 * The words involved in the clue
		 */
		public String[] words;
		/**
		 * The parts of speech of each word
		 */
		public Pos[] poses;
		/**
		 * Whether the words are stemmed
		 */
		public boolean[] stemmed;
		/**
		 * The source of this polarity
		 */
		public String polannsrc;
		/**
		 * the polarity of the clue
		 */
		public Polarity polarity;

		@Override
		public Clue clone() {
			final Clue entry = new Clue();
			entry.type = type;
			entry.polannsrc = polannsrc;
			entry.polarity = polarity;
			entry.words = Arrays.copyOf(words, words.length);
			entry.poses = Arrays.copyOf(poses, poses.length);
			entry.stemmed = Arrays.copyOf(stemmed, stemmed.length);
			return entry;
		}
	}

	/**
	 * Every clue in this TFF
	 */
	public ArrayList<Clue> entriesList;
	/**
	 * Every word mapped to each clue in this TFF
	 */
	public Map<String, List<Clue>> entriesMap;

	/**
	 * instatiate the clue map and the clue list
	 */
	public TFF() {
		entriesMap = new HashMap<String, List<Clue>>();
		entriesList = new ArrayList<Clue>();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		while (in.hasNextLine()) {
			final String line = in.nextLine();
			if (line.startsWith("#"))
				continue;
			final String[] parts = line.split(" ");
			final Clue entry = new Clue();
			for (final String part : parts) {
				final String[] namevalue = part.split("=");
				if (namevalue.length != 2)
					continue;
				final String name = namevalue[0];
				final String value = namevalue[1];
				if (name.equals("type"))
					entry.type = Enum.valueOf(Type.class, value);
				else if (name.equals("len")) {
					final int len = Integer.parseInt(value);
					entry.words = new String[len];
					entry.poses = new Pos[len];
					entry.stemmed = new boolean[len];
				}
				else if (name.startsWith("word")) {
					final int wordN = Integer.parseInt(name.substring(4)) - 1;
					entry.words[wordN] = value;
				}
				else if (name.startsWith("pos")) {
					final int posN = Integer.parseInt(name.substring(3)) - 1;
					entry.poses[posN] = Enum.valueOf(Pos.class, value);
				}
				else if (name.startsWith("stemmed")) {
					final int stemN = Integer.parseInt(name.substring(7)) - 1;
					entry.stemmed[stemN] = value.equals("y");
				}
				else if (name.equals("polannsrc")) {
					entry.polannsrc = value;
				}
				else if (name.equals("mpqapolarity")) {
					entry.polarity = Enum.valueOf(Polarity.class, value);
				}
				else {
					// casually ignore this one!
				}
			}
			this.entriesList.add(entry);
			for (final String string : entry.words) {
				List<Clue> wordEntries = this.entriesMap.get(string);
				if (wordEntries == null)
					this.entriesMap.put(string, wordEntries = new ArrayList<Clue>());
				wordEntries.add(entry);
			}
		}

	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public TFF clone() {
		final TFF tff = new TFF();
		for (final Clue entry : this.entriesList) {
			tff.entriesList.add(entry);
			for (final String string : entry.words) {
				List<Clue> wordEntries = tff.entriesMap.get(string);
				if (wordEntries == null)
					tff.entriesMap.put(string, wordEntries = new ArrayList<Clue>());
				wordEntries.add(entry);
			}
		}
		return tff;
	}

}
