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
import org.openimaj.text.nlp.sentiment.model.wordlist.util.TFF.Clue;

/**
 * The TFF data format is the word clue format used by OpinionFinder.
 * Details of MPQA and this format can be found: http://www.cs.pitt.edu/mpqa/
 * 
 * The way to think about TFF entries are clues that a given word (or set of words) give to the sentiment and subjectivity of
 * a given phrase. There are many clever ways to use this information highlighted in this paper:
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@Reference(
	author = { "Janyce Wiebe","Theresa Wilson","Claire Cardie" }, 
	title = "Annotating expressions of opinions and emotions in language. ", 
	type = ReferenceType.Article, 
	year = "2005"
)
public class TFF implements ReadableASCII{
	/**
	 * The subjectivity leve
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static enum Type{
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
	 * The Part of Speech of this clue. i.e. the clue applies when the word is at this POS
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static enum Pos{
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
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static enum Polarity{
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
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Clue{
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
		public Clue clone(){
			Clue entry = new Clue();
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
	public Map<String,List<Clue>> entriesMap;
	
	/**
	 * instatiate the clue map and the clue list
	 */
	public TFF() {
		entriesMap = new HashMap<String, List<Clue>>();
		entriesList = new ArrayList<Clue>();
	}
	
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		while(in.hasNextLine()){			
			String line = in.nextLine();
			if(line.startsWith("#")) continue;
			String[] parts = line.split(" ");
			Clue entry = new Clue();
			for (String part : parts) {
				String[] namevalue = part.split("=");
				if(namevalue.length != 2) continue;
				String name = namevalue[0];
				String value = namevalue[1];
				if(name.equals("type")) entry.type = Enum.valueOf(Type.class,  value);
				else if(name.equals("len")){
					int len = Integer.parseInt(value);
					entry.words = new String[len];
					entry.poses = new Pos[len];
					entry.stemmed = new boolean[len];
				}
				else if(name.startsWith("word")){
					int wordN = Integer.parseInt(name.substring(4)) - 1;
					entry.words[wordN] = value;
				}
				else if(name.startsWith("pos")){
					int posN = Integer.parseInt(name.substring(3)) - 1;
					entry.poses[posN] = Enum.valueOf(Pos.class, value);
				}
				else if(name.startsWith("stemmed")){
					int stemN = Integer.parseInt(name.substring(7)) - 1;
					entry.stemmed[stemN] = value.equals("y");
				}
				else if(name.equals("polannsrc")){
					entry.polannsrc = value;
				}
				else if(name.equals("mpqapolarity")){
					entry.polarity = Enum.valueOf(Polarity.class, value);
				}
				else{
					// casually ignore this one!
				}
			}
			this.entriesList.add(entry);
			for (String string : entry.words) {
				List<Clue> wordEntries = this.entriesMap.get(string);
				if(wordEntries == null) this.entriesMap.put(string, wordEntries = new ArrayList<Clue>()); 
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
		TFF tff = new TFF();
		for (Clue entry : this.entriesList) {
			tff.entriesList.add(entry);
			for (String string : entry.words) {
				List<Clue> wordEntries = tff.entriesMap.get(string);
				if(wordEntries == null) tff.entriesMap.put(string, wordEntries = new ArrayList<Clue>()); 
				wordEntries.add(entry);
			}
		}
		return tff;
	}
	
}
