package org.openimaj.text.jaspell;

import java.util.List;
import java.util.Vector;

/**
 * A word finder for XMLdocuments, which searches text for
 * sequences of letters, but ignores tags.
 *
 * @see DefaultWordFinder
 * @author      Bruno Martins
 */
public class XMLWordFinder extends DefaultWordFinder {

    /**
	 * Constructor for XMLWordFinder.
	 *
	 * @param inText A String with the input text to tokenize.
	 */
  public XMLWordFinder(String inText) {
	super(inText);
  }

  /**
   * Constructor for XMLWordFinder.
   */
  public XMLWordFinder() {
	super();
  }

  /**
   * Returns the current text segment from the input. A segment is defined as the
   * character sequence between the current position and the next non-alphanumeric character,
   * considering also white spaces.
   *
   * @return A String with the current text segment.
   */
  public String currentSegment () {
  	String seg = super.currentSegment();
  	while(seg!=null && seg.startsWith("<") && seg.endsWith("<")) {
  		nextSegment();
		seg = super.currentSegment();
  	}
    return seg;
  }
  
  /**
   * This method scans the text from the end of the last word, and returns a
   * String corresponding to the next word. If there are no more words to
   * return, it retuns a null String.
   *
   * @return the next word.
   */
  public String next() {
	if (!hasNext()) return null;
	if (currentWord == null) return null;
	currentWord = nextWord;
	currentWordPos = nextWordPos;
	int current = sentenceIterator.current();
	if (current == currentWordPos) startsSentence = true; else {
		startsSentence = false;
		if (currentWordPos + currentWord.length() > current) sentenceIterator.next();
	}
	int i = currentWordPos + currentWord.length();
	boolean finished = false;
	boolean started = false;
	search:      /* Find words. */
	while (i < text.length() && !finished) {
	  if (!started && isWordChar(text,i)) {
		nextWordPos = i++;
		started = true;
		continue search;
	  } else if (started) {
		if (isWordChar(text,i)) {
		  i++;
		  continue search;
		} else {
		  nextWord = text.substring(nextWordPos, i);
		  finished = true;
		  break search;
		}
	  }
	  //Ignore things inside tags.
	  i = ignore(i, '<', '>');
	  i++;
	}
	if (!started) nextWord = null;
	else if (!finished) nextWord= text.substring(nextWordPos, i);
	return currentWord;
  }

	/**
	 * Splits a given String into an array with its constituent words.
	 * 
	 * @param text A String.
	 * @return An array with the words extracted from the String.
	 */
	public static String[] splitWords ( String text ) {
		List aux = new Vector();
		XMLWordFinder finder = new XMLWordFinder(text);
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
		XMLWordFinder finder = new XMLWordFinder(text);
		String str;
		while((str=finder.nextSegment())!=null) aux.add(str);
		return (String [])(aux.toArray(new String[0]));
	}

}