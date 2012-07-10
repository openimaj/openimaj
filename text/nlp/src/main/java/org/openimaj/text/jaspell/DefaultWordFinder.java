package org.openimaj.text.jaspell;

import java.text.BreakIterator;
import java.util.*;

/**
 * A word finder for normal text documents, which searches text for sequences
 * of words and text blocks.This class also defines common methods and behaviour for the various word finding
 * subclasses.
 *
 * @see java.util.StringTokenizer
 * @see java.text.BreakIterator
 * @see TeXWordFinder
 * @see XMLWordFinder
 * @author Bruno Martins
 */
public class DefaultWordFinder {

	/** A string with the current word for the word finder. */
	protected String currentWord;
	
	/** A string with the word next to the current one. */
	protected String nextWord;
	
	/** The index of the current word in the input text. */
	protected int currentWordPos;
	
	/** The index of the next word in the input text. */
	protected int nextWordPos;
	
	/** The index of the current segment in the input text. */
	protected int currentSegmentPos;
	
	/** The index of the next segment in the input text. */
	protected int nextSegmentPos;

	/** A boolean flag indicating if the current word marks the begining of a sentence. */
	protected boolean startsSentence;
	
	/** The input text. */
	protected String text;
	
	/** Solve the tokenization hard cases. */
	protected boolean solveHardCases = false;
	
	/**
	 *  An iterator over the input text.
     *
	 * @see java.text.BreakIterator  
	 */
	protected BreakIterator sentenceIterator;

	/**
	 * Returns the current word N-gram from the input. An N-gram is defined as the
	 * word sequence between the current position and the next n words.
     *
     * @param n Number of consecutive words on the n-grams.
	 * @return A String with the current word N-gram.
	 */
	public String currentWordGram(int n) {
		String segment = currentSegment();
		StringBuffer wordGram = new StringBuffer(); 
		if(segment==null) return null;
		String s[] = splitWords(segment);
		int j = 0;
		StringBuffer s2 = new StringBuffer(s[j++]); 
		for( int k=j; k<n && k<s.length; k++) {
			s2.append(" ");
			s2.append(s[k]);
		}
		return s2.toString();
	}
	
	/**
	 * Returns the current word N-gram from the input. An N-gram is defined as the
	 * character sequence between the current position and the next n characters.
     *
	 * @param n Number of consecutive characters on the n-grams.
	 * @return A String with the current word N-gram.
	 */
	public String currentNGram(int n) {
		int pos = currentWordPos;
		StringBuffer aux = new StringBuffer();
		for (int i=currentWordPos; i<currentWordPos+n && i<text.length(); i++) aux.append((char)text.charAt(i));
		return aux.toString();
	}

	
	/**
	 * Returns the current text segment from the input. A segment is defined as the
	 * character sequence between the current position and the next non-alphanumeric character,
	 * considering also white spaces.
     *
	 * @return A String with the current text segment.
	 */
	public String currentSegment() {
		if(currentSegmentPos>=text.length()) return null;
		if(nextSegmentPos>currentSegmentPos) return text.substring(currentSegmentPos,nextSegmentPos);
		while(currentSegmentPos<text.length()) {
			if (!isWordChar(text,currentSegmentPos)) currentSegmentPos++;
			else break;
		}
		if(currentSegmentPos>=text.length()) return null;
		nextSegmentPos = currentSegmentPos+1;		
		while(nextSegmentPos<text.length()) {
			if (isWordChar(text,nextSegmentPos) || text.charAt(nextSegmentPos)==' ') nextSegmentPos++;
			else break;
		}
		if (solveHardCases) {
			String segment = text.substring(currentSegmentPos,nextSegmentPos);
			String segment2 = solveHardCases(segment);
			if(segment2.length()>segment.length()) replaceSegment(segment2);
		}
		return text.substring(currentSegmentPos,nextSegmentPos); 
	}	
	
	/**
	 * Returns the next text segment from the input. A segment is defined as the
	 * character sequence between the current position and the next non-alphanumeric character,
	 * considering also white spaces.If there are no more segments to return, it retuns a null String.
	 *
	 * @return A String with the next text segment.
	 */
	public String nextSegment() {
		if(currentSegmentPos>=text.length()) return null;
		currentSegmentPos = nextSegmentPos;
		int oldWordPos = -1;
		while (true) {
		 	next();
			if(currentWordPos==oldWordPos) break;
			if(currentWordPos>=currentSegmentPos) break;
			oldWordPos = currentWordPos;
		}
		return currentSegment();
	}	

     /**
	 * Replaces the current text segment. After a call to this method,
	 * a call to currentSegment() returns the new text segment and a call to getText()
	 * returns the text supplied to this WordFinder with the current segment replaced. 
     * 
     * @param newSegment A String with the new text segment.
     */
	public void replaceSegment(String newSegment) {
		String currentSegment = currentSegment();
		if(currentSegmentPos>=text.length() || currentSegment==null) return;
		StringBuffer sb = new StringBuffer(text.substring(0, currentSegmentPos));
		sb.append(newSegment);
		sb.append(text.substring(currentSegmentPos + currentSegment.length()));
		int diff = newSegment.length() - currentSegment.length();
		nextSegmentPos +=  diff;
		if (nextWord != null) nextWordPos +=diff;
		text = sb.toString();
		sentenceIterator.setText(text);
		int start = currentWordPos;
		sentenceIterator.following(start);
		startsSentence = sentenceIterator.current() == start;
	}
	
	/**
	 * Constructor for DefaultWordFinder.
	 *
	 * @param inText A String with the input text to tokenize.
	 */
	public DefaultWordFinder(String inText) {
		setText(inText);
	}

	/**
	 * Constructor for DefaultWordFinder.
	 */
	public DefaultWordFinder() {
		this("");
	}

	/**
	 * Returns the text associated with this DefaultWordFinder.
	 *
	 *@return A String with the text associated with this DefaultWordFinder.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Changes the text associates with this DefaultWordFinder.
	 * 
	 * @param newText The new String with the input text to tokenize.
	 */
	public void setText(String newText) {
		text = newText;
		currentWord = new String("");
		nextWord = new String("");
		currentWordPos = 0;
		nextWordPos = 0;
		currentSegmentPos = 0;
		nextSegmentPos = 0;
		startsSentence = true;
		sentenceIterator = BreakIterator.getSentenceInstance();
		sentenceIterator.setText(text);
		next();
	}

	/**
	 * Returns the current word in the text.
	 *
	 * @return A String with the current word in the text.
	 */
	public String current() {
		return currentWord;
	}

	/**
	 * Tests if there are more words available from the text.
	 *
	 * @return true if and only if there is at least one word in the
	 *              string after the current position, and false otherwise.
	 */
	public boolean hasNext() {
		return nextWord != null;
	}

	/**
	 * Replaces the current word in the text. After a call to this method,
	 * a call to current() returns the new word and a call to getText() returns the
	 * text supplied to this WordFinder with the current word replaced. 
	 * 
	 * @param newWord A string with the replacement word.
	 */
	public void replace(String newWord) {
		if (currentWord == null) return;
		StringBuffer sb = new StringBuffer(text.substring(0, currentWordPos));
		sb.append(newWord);
		sb.append(text.substring(currentWordPos + currentWord.length()));
		int diff = newWord.length() - currentWord.length();
		nextSegmentPos +=  diff;
		if (nextWord != null) nextWordPos += diff; 
		text = sb.toString();
		sentenceIterator.setText(text);
		int start = currentWordPos;
		sentenceIterator.following(start);
		startsSentence = sentenceIterator.current() == start;
	}

	/**
	 * Replaces the current bigram (current word and the next as returned by lookahead) in
	 * the text. After a call to this method, a call to current() returns the Bigram and a 
	 * call to getText() returns the text supplied to this WordFinder with the current
	 * Bigram replaced. 
	 * 
	 * @param newBigram A string with the replacement Bigram.
	 */
	public void replaceBigram(String newBigram) {
		int startPos = currentWordPos;
		String next = lookAhead();
		if(next!=null) next();
		if (currentWord == null)	return;
		StringBuffer sb = new StringBuffer(text.substring(0, startPos));
		sb.append(newBigram);
		sb.append(text.substring(currentWordPos + currentWord.length()));
		int diff = newBigram.length() - currentWord.length();
		nextSegmentPos +=  diff;
		if (nextWord != null) nextWordPos += diff; 
		text = sb.toString();
		sentenceIterator.setText(text);
		int start = currentWordPos;
		sentenceIterator.following(start);
		startsSentence = sentenceIterator.current() == start;
	}

	/**
	 * Retuns the next word without advancing the tokenizer, cheking if the character
	 * separating both words is an empty space. This is usefull for getting BiGrams from
	 * the text.
	 * 
	 * @return The next word in the text, or null.
	 */
	public String lookAhead () {
		if (nextWord == null) return null;
		if (text.charAt(nextWordPos-1)==' ') return nextWord;
		else return null;  
	}


	/**
	 * Checks if the current word marks the begining of a sentence.
	 * 
	 * @return true if the current word marks the begining of
	 *               a sentence and false otherwise.
	 */
	public boolean startsSentence() {
		if (currentWord == null) return false;
		return startsSentence;
	}

	/**
	 * Produces a string representation of this word finder by returning
	 * the associated text.
	 */
	public String toString() {
		return text;
	}

	/**
	 * Checks if the character at a given position in a String is part of a word.
	 * Special characters such as '.' or '-' are considered alphanumeric or not depending
	 * on the surrounding characters. 
	 * 
	 * @param text The text String.
	 * @param posn The position for the character in the String.
	 * @return true if the character at the given position is alphanumeric and false otherwise.
	 */
	protected static boolean isWordChar(String text, int posn) {
		if(posn<0 || posn>=text.length()) return false;
		boolean out = false;
		char curr = text.charAt(posn);
		if ((posn == 0) || (posn == text.length() - 1)) {
			return Character.isLetterOrDigit(curr);
		}
		char prev = text.charAt(posn - 1);
		char next = text.charAt(posn + 1);
		String prevWord = "" + prev;
		for (int i=posn - 2; i>=0; i--) {
			char chr = text.charAt(i);
			if(chr==' ' || chr=='\t' || chr=='\n' || chr=='\r') break;
			prevWord = chr + prevWord;
		}
		String prevWordLowerCase = prevWord.toLowerCase();
		switch (curr) {
			case '\'' : out = (Character.isLetter(prev) && Character.isLetter(next));
						  out |= (Character.isDigit(prev) && (!Character.isLetterOrDigit(next) || next=='\''));
						  break;
			case '$' : out = (Character.isDigit(prev) && Character.isDigit(next));
						  out |= (!Character.isLetterOrDigit(prev) && Character.isDigit(next));
						  break;
			case '@' : out = (Character.isLetterOrDigit(prev) && Character.isLetterOrDigit(next));
						   break;
			case '.' : out = (Character.isDigit(prev) && Character.isDigit(next));
						 out |= Character.isLetter(next) && prevWord.indexOf('@')>0; 
						 out |= prevWord.startsWith("http://") && Character.isLetterOrDigit(next); 
						 out |= prevWord.startsWith("ftp://") && Character.isLetterOrDigit(next);						 
						 out |= prevWord.startsWith("www") && Character.isLetterOrDigit(next);
						 // Common abreviations
						 out |= prevWordLowerCase.equals("lda") && (next==' ' || Character.isLetter(next));
						 out |= prevWordLowerCase.equals("sr") && (next==' ' || Character.isLetter(next)); 
						 out |= prevWordLowerCase.equals("sra") && (next==' ' || Character.isLetter(next));
						 out |= prevWordLowerCase.equals("sr(a)") && (next==' ' || Character.isLetter(next));
						 out |= prevWordLowerCase.equals("dr") && (next==' ' || Character.isLetter(next));
						 out |= prevWordLowerCase.equals("dra") && (next==' ' || Character.isLetter(next));
						 out |= prevWordLowerCase.equals("dr(a)") && (next==' ' || Character.isLetter(next));
						 out |= prevWordLowerCase.equals("exmo") && (next==' ' || Character.isLetter(next));
						 out |= prevWordLowerCase.equals("exma") && (next==' ' || Character.isLetter(next));
						 out |= prevWordLowerCase.equals("exmo(a)") && (next==' ' || Character.isLetter(next));
						 break;
			case ',' : out = (Character.isDigit(prev) && Character.isDigit(next));
						 break;
			case 'º' :
			case 'ª' :
			case '£' :
			case '€' :
			case '%' : out = (Character.isDigit(prev) && !Character.isLetterOrDigit(next));
						   out |= prevWord.startsWith("http://") && Character.isDigit(next); 
						   out |= prevWord.startsWith("ftp://") && Character.isDigit(next);
						 break;
			case ':' : out = (Character.isDigit(prev) && Character.isDigit(next));
						 out |= prevWord.startsWith("http");
						 out |= prevWord.startsWith("ftp");
						 break;
			case '/' : out = (Character.isDigit(prev) && Character.isDigit(next));
						 out |= prevWord.startsWith("http:");
						 out |= prevWord.startsWith("ftp:");
						 out |= prevWord.startsWith("www.");
						 break;
			case '=' : out = prevWord.startsWith("http://") && prevWord.indexOf("?")!=-1 && Character.isLetterOrDigit(next);
						  out |= prevWord.startsWith("ftp://") && prevWord.indexOf("?")!=-1 && Character.isLetterOrDigit(next);
						  break;
			case '?' :
			case '~' : out = prevWord.startsWith("http://") && Character.isLetterOrDigit(next); 
						  out |= prevWord.startsWith("ftp://") && Character.isLetterOrDigit(next);
						  break;
			case '+' :
			case '*' : out = (Character.isDigit(prev) && Character.isDigit(next));
						 break;
			case '_'  : out = (Character.isDigit(prev) && Character.isDigit(next));
						   out = (Character.isLetter(prev) && Character.isLetter(next) && StringUtils.isUpperCase(next));			  
				          break;
			case '-'  : out = (Character.isDigit(prev) && Character.isDigit(next));
						  out = (Character.isLetter(prev) && Character.isLetter(next) && StringUtils.isUpperCase(next));			  
						  break;
			default : out = Character.isLetterOrDigit(curr);
		}
		return out;
	}

	/**
	 * Checks if a given character is alphanumeric.
	 *
	 * @param c The char to check.
	 * @return true if the given character is alphanumeric and false otherwise.
	 */
	protected static boolean isWordChar(char c) {
		boolean out = false;
		if (Character.isLetterOrDigit(c) || (c == '\'')) {
			out = true;
		}
		return out;
	}

	/**
	 * Ignore all characters from the text after the first occurence of a given character.
	 *   
	 * @param index A starting index for the text from where characters should be ignored
	 * @param startIgnore The character that marks the begining of the sequence to be ignored.
	 *
	 * @return the index in the text marking the begining of the ignored sequence, or -1 if no
	 *				 sequence was ignored (the supplied character does not occur in the text).
	 */
	protected int ignore(int index, char startIgnore) {
		return ignore(index, new Character(startIgnore), null);
	}

	/**
	 * Ignore all characters from the text between the first occurence of a given character
	 * and the next occurence of another given character.
	 *   
	 * @param index A starting index for the text from where characters should be ignored.
	 * @param startIgnore The character that marks the begining of the sequence to be ignored.
	 * @param endIgnore The character that marks the ending of the sequence to be ignored.
	 *
	 * @return the index in the text marking the begining of the ignored sequence, or -1 if no
	 *				 sequence was ignored (the supplied starting character does not occur in the text).
	 */
	protected int ignore(int index, char startIgnore, char endIgnore) {
		return ignore(
			index,
			new Character(startIgnore),
			new Character(endIgnore));
	}

	/**
	 * Ignore all characters from the text between the first occurence of a given character
	 * and the next occurence of another given character.
	 *   
	 * @param index A starting index for the text from where characters should be ignored.
	 * @param startIgnore The character that marks the begining of the sequence to be ignored.
	 * @param endIgnore The character that marks the ending of the sequence to be ignored, or null
	 * 			 if all the next characters from the text are to be ignored.
	 *
	 * @return the index in the text marking the begining of the ignored sequence, or -1 if no
	 *				 sequence was ignored (the supplied starting character does not occur in the text).
	 */
	protected int ignore(int index, Character startIgnore, Character endIgnore) {
		if(index<0 || index>=text.length()) return -1;
		int newIndex = index;
		if (newIndex < text.length()) {
			Character curChar = new Character(text.charAt(newIndex));
			if (curChar.equals(startIgnore)) {
				newIndex++;
				while (newIndex < text.length()) {
					curChar = new Character(text.charAt(newIndex));
					if (endIgnore != null && curChar.equals(endIgnore)) {
						newIndex++;
						break;
					} else if (
						endIgnore == null
							&& !Character.isLetterOrDigit(curChar.charValue())) {
						break;
					}
					newIndex++;
				}
			}
		}
		return newIndex;
	}

	/**
	 * Ignore all characters from the text between the first occurence of a given String
	 * and the next occurence of another given String.
	 *   
	 * @param index A starting index for the text from where characters should be ignored.
	 * @param startIgnore The String that marks the begining of the sequence to be ignored.
	 * @param endIgnore The String that marks the ending of the sequence to be ignored.
	 *
	 * @return the index in the text marking the begining of the ignored sequence, or -1 if no
	 *				 sequence was ignored (the supplied starting String does not occur in the text).
	 */
	protected int ignore(int index, String startIgnore, String endIgnore) {
		int newIndex = index;
		int len = text.length();
		int slen = startIgnore.length();
		int elen = endIgnore.length();
		if (!((newIndex + slen) >= len)) {
			String seg = text.substring(newIndex, newIndex + slen);
			if (seg.equals(startIgnore)) {
				newIndex += slen;
				cycle : while (true) {
					if (newIndex == (text.length() - elen)) break cycle;
					String ss = text.substring(newIndex, newIndex + elen);
					if (ss.equals(endIgnore)) {
						newIndex += elen;
						break cycle;
					} else newIndex++;
				}
			}
		}
		return newIndex;
	}

	/**
	 * This method scans the text from the end of the last word, and returns a
	 * String corresponding to the next word. If there are no more words to
	 * return, it retuns a null String.
	 *
	 * @return the next word.
	 */
	public String next() {
	  if (nextWord == null) return null;
	  currentWord = nextWord;
	  currentWordPos = nextWordPos;
	  int current = sentenceIterator.current();
	  if (current == currentWordPos) startsSentence = true; else {
		  startsSentence = false;
		  if (currentWordPos + currentWord.length() > current) sentenceIterator.next();
	  }
	  int i = currentWordPos + currentWord.length();
	  boolean finished = false;
	  while (i < text.length() && !finished) {
		if (isWordChar(text,i)) {
		  nextWordPos = i;
		  int end = getNextWordEnd(text, i);
		  nextWord = text.substring(i, end);
		  finished = true;
		}
		i++;
	  }
	  if (!finished) nextWord = null;
	  if (solveHardCases) {
		  String aux = solveHardCases(currentWord);
		  int diff = aux.length() - currentWord.length();
		  if(diff>0) {
			StringBuffer sb = new StringBuffer(text.substring(0, currentWordPos));
			sb.append(aux);
			sb.append(text.substring(currentWordPos + currentWord.length()));
			currentWord = aux.substring(0,aux.indexOf(" "));
			nextWord = aux.substring(aux.indexOf(" ")+1);
			nextSegmentPos +=  diff;
			text = sb.toString();
			sentenceIterator.setText(text);
			int start = currentWordPos;
			sentenceIterator.following(start);
			startsSentence = sentenceIterator.current() == start;
		  } 
	  }
	  return currentWord;
	}

	/**
	 * Returns the position in the string <em>after</em> the end of the next word.
	 *
	 * Note that this return value should not be used as an index into the string
	 * without checking first that it is in range, since it is possible for the
	 * value <code>text.length()</code> to be returned by this method.
	 *	
	 * @param text A string with the text to check.
	 * @param startPos the starting position in the text to check.
	 * @return the index position in the string after the end of the next word.
	 */
	private static int getNextWordEnd(String text, int startPos) {
		for (int i = startPos; i < text.length(); i++) {
		  if (!isWordChar(text,i))
			return i;
		}
		return text.length();
	}
	
	/**
	 * Splits a given String into an array with its constituent words.
	 * 
	 * @param text A String.
	 * @return An array with the words extracted from the String.
	 */
	public static String[] splitWords ( String text ) {
		List aux = new Vector();
		DefaultWordFinder finder = new DefaultWordFinder(text);
		String str;
		while((str=finder.next())!=null) aux.add(str);
		return (String [])(aux.toArray(new String[0]));
	}

	/**
	 * Splits a given String into an array with its constituent text segments.
	 * 
	 * @param text A String.
	 * @return An array with the text segments extracted from the String.
	 */
	public static String[] splitSegments ( String text ) {
		List aux = new Vector();
		DefaultWordFinder finder = new DefaultWordFinder(text);
		String str;
		while((str=finder.nextSegment())!=null) aux.add(str);
		return (String [])(aux.toArray(new String[0]));
	}

	/**
	 * Splits a given String into an array with its constituent word n-grams.
	 * 
	 * @param text A String.
	 * @param n Number of consecutive words on the n-grams.
	 * @return An array with the word n-grams extracted from the String.
	 */
	public static String[] splitWordGrams ( String text, int n ) {
		String aux[] = splitSegments(text);
		List list = new Vector();
		for (int i=0; i<aux.length; i++) {
			String s[] = splitWords(aux[i]);
			int j = 0;
			do {
				StringBuffer s2 = new StringBuffer(s[j++]); 
				for( int k=j; k<n && k<s.length; k++) {
					s2.append(" ");
					s2.append(s[k]);
				}
				list.add(s2.toString());
			} while (j<s.length-n);
		}
		return (String [])(list.toArray(new String[0]));	
	}

	
	/**
	 * Splits a given String into an array with its constituent character n-grams.
	 * 
	 * @param text A String.
	 * @param n Number of consecutive characters on the n-grams.
	 * @return An array with the character n-grams extracted from the String.
	 */
	public static String[] splitNGrams ( String text, int n ) {
		int lastn[] = new int[n];
		List list = new Vector();
		for (int i=0; i<text.length(); i++) {
			if(i<n) lastn[i] = 0; else {
				for (int j=0; j<n-1; j++) lastn[j] = lastn[j+1];
				lastn[n-1] = text.charAt(i);
				StringBuffer aux = new StringBuffer();
				for (int j=0; j<n; j++) aux.append((char)lastn[j]);
				list.add(aux.toString());
			}
		}
		return (String [])(list.toArray(new String[0]));
	}
	
	/**
	 * Resolves the hard tokenization cases which envolve splitting the original
	 * word in two words (e.g. doesn't  -> "does not").
	 * 
	 * TODO: Disambiguate some cases.
	 *
	 * @param text A string.
	 * @return The string with the hard cases solved.
	 */
	private static String solveHardCases ( String text ) {
		String tokens[] = text.split(" ");
		StringBuffer newString = new StringBuffer();
		for(int i=0; i<tokens.length; i++ ) {
			if(i!=0) newString.append(" ");
			String aux = tokens[i].toLowerCase();
			if(aux.equals("daquilo")) aux = tokens[i].charAt(0) + "e aquilo";
			else if(aux.equals("disso")) aux = tokens[i].charAt(0) +  "e isso";
			else if(aux.equals("disto")) aux = tokens[i].charAt(0) + "e isto";
			else if(aux.equals("dele")) aux = tokens[i].charAt(0) + "e ele";
			else if(aux.equals("dela")) aux = tokens[i].charAt(0) + "e ela";
			else if(aux.equals("deles")) aux = tokens[i].charAt(0) +  "e eles";
			else if(aux.equals("delas")) aux = tokens[i].charAt(0) + "e elas";
			else if(aux.equals("do")) aux = tokens[i].charAt(0) + "e o";
			else if(aux.equals("dos")) aux = tokens[i].charAt(0) + "e os";
			else if(aux.equals("da")) aux = tokens[i].charAt(0) + "e a";
			else if(aux.equals("das")) aux = tokens[i].charAt(0) + "e as";
			else if(aux.equals("pelo")) aux = tokens[i].charAt(0) + "or o";
			else if(aux.equals("pela")) aux = tokens[i].charAt(0) + "or a";
			else if(aux.equals("pelos")) aux = tokens[i].charAt(0) + "or os";
			else if(aux.equals("pelas")) aux = tokens[i].charAt(0) + "or as";
			else if(aux.equals("p'lo")) aux = tokens[i].charAt(0) + "or o";
			else if(aux.equals("p'la")) aux = tokens[i].charAt(0) + "or a";
			else if(aux.equals("p'los")) aux = tokens[i].charAt(0) + "or os";
			else if(aux.equals("p'las")) aux = tokens[i].charAt(0) + "or as";
			else if(aux.equals("p'ra")) aux = tokens[i].charAt(0) + "ara a";
			else if(aux.equals("p'ro")) aux = tokens[i].charAt(0) + "ara o";
			else if(aux.equals("p'ras")) aux = tokens[i].charAt(0) + "ara as";
			else if(aux.equals("p'ros")) aux = tokens[i].charAt(0) + "ara os";
			else if(aux.equals("deste")) aux = tokens[i].charAt(0) + "e este";
			else if(aux.equals("destes")) aux = tokens[i].charAt(0) + "e estes";
			else if(aux.equals("desta")) aux = tokens[i].charAt(0) + "e esta";
			else if(aux.equals("destas")) aux = tokens[i].charAt(0) + "e estas";
			else if(aux.equals("desse")) aux = tokens[i].charAt(0) + "e esse";
			else if(aux.equals("desses")) aux = tokens[i].charAt(0) + "e esses";
			else if(aux.equals("dessa")) aux = tokens[i].charAt(0) + "e essa";
			else if(aux.equals("dessas")) aux = tokens[i].charAt(0) + "e essas";
			else if(aux.equals("i'm")) aux = tokens[i].charAt(0) + " am";
			else if(aux.equals("don't")) aux = tokens[i].charAt(0) + "o not";
			else if(aux.equals("won't")) aux = tokens[i].charAt(0) + "ill not";
			else if(aux.equals("haven't")) aux = tokens[i].charAt(0) + "ave not";
			else if(aux.equals("does't")) aux = tokens[i].charAt(0) + "oes not";
			else if(aux.equals("dessa")) aux = tokens[i].charAt(0) + "e essa";
			else if(aux.equals("dessas")) aux = tokens[i].charAt(0) + "e essas";
			else if(aux.equals("na")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em a" : "em a";
			else if(aux.equals("nas")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em as" : "em as";
			else if(aux.equals("no")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em o" : "em o";
			else if(aux.equals("nos")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em os" : "em os";
			else if(aux.equals("num")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em um" : "em um";
			else if(aux.equals("nuns")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em uns" : "em uns";
			else if(aux.equals("nele")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em ele" : "em ele";
			else if(aux.equals("nela")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em ela" : "em ela";
			else if(aux.equals("neles")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em eles" : "em eles";
			else if(aux.equals("nelas")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em elas" : "em elas";
			else if(aux.equals("nisto")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em isto" : "em isto";
			else if(aux.equals("naquilo")) aux = Character.isUpperCase(tokens[i].charAt(0)) ? "Em aquilo" : "em aquilo";
			newString.append(aux);
		}
		return newString.toString();
	}

}