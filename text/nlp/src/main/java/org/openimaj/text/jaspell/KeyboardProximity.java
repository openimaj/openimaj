package org.openimaj.text.jaspell;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of <code>String</code> similarity measures with basis on
 * Keyboard proximity. 
 * <p>
 * The keyboard is assumed to have a QWERTY layout, and we 
 * also make a distinction between keys on the same row of the keyboard
 * (which therefore should be "more similar") and adjacent keys.
 * <p>
 * A large percentage of spelling mistakes are typographical errors, mainly
 * due to keyboard typing slips (e.g. Jack becomes Hack since
 * h and j are next to each other on the keyboard). Handling these errors
 * is therefore of special importance.
 * 
 * @author Bruno Martins
 *
 */
public class KeyboardProximity {

	/** Associates keys with a <code>Map</code> of adjacent keys in the same row of the keyboard. */
	private static Map firstChars;

	/** Associates keys with a <code>Map</code> of adjacent keys but at diferent rows of the keyboard. */
	private static Map secondChars;

	/** The single instance of this class. */
	private static final KeyboardProximity theInstance = new KeyboardProximity();

	/**
	 *  Sole constructor, private because this is a Singleton class. 
	 */
	private KeyboardProximity() {
		firstChars = new HashMap();
		secondChars = new HashMap();
		Map temp;

		// q
		temp = new HashMap();
		temp.put("w", null);
		firstChars.put("q", temp);
		temp = new HashMap();
		temp.put("a", null);
		temp.put("1", null);
		firstChars.put("q", temp);

		// w
		temp = new HashMap();
		temp.put("q", null);
		temp.put("e", null);
		firstChars.put("w", temp);
		temp = new HashMap();
		temp.put("2", null);
		temp.put("s", null);
		temp.put("a", null);
		temp.put("d", null);
		secondChars.put("w", temp);

		// e
		temp = new HashMap();
		temp.put("w", null);
		temp.put("r", null);
		firstChars.put("e", temp);
		temp = new HashMap();
		temp.put("3", null);
		temp.put("d", null);
		temp.put("s", null);
		temp.put("f", null);
		secondChars.put("e", temp);

		// r
		temp = new HashMap();
		temp.put("e", null);
		temp.put("t", null);
		firstChars.put("r", temp);
		temp = new HashMap();
		temp.put("4", null);
		temp.put("f", null);
		temp.put("d", null);
		temp.put("g", null);
		secondChars.put("r", temp);

		// t
		temp = new HashMap();
		temp.put("r", null);
		temp.put("y", null);
		firstChars.put("t", temp);
		temp = new HashMap();
		temp.put("5", null);
		temp.put("g", null);
		temp.put("f", null);
		temp.put("h", null);
		secondChars.put("t", temp);

		// y
		temp = new HashMap();
		temp.put("t", null);
		temp.put("u", null);
		firstChars.put("y", temp);
		temp = new HashMap();
		temp.put("6", null);
		temp.put("h", null);
		temp.put("g", null);
		temp.put("j", null);
		secondChars.put("y", temp);

		// u
		temp = new HashMap();
		temp.put("y", null);
		temp.put("i", null);
		firstChars.put("u", temp);
		temp = new HashMap();
		temp.put("7", null);
		temp.put("j", null);
		temp.put("h", null);
		temp.put("k", null);
		secondChars.put("u", temp);

		// i
		temp = new HashMap();
		temp.put("u", null);
		temp.put("o", null);
		firstChars.put("i", temp);
		temp = new HashMap();
		temp.put("8", null);
		temp.put("k", null);
		temp.put("j", null);
		temp.put("l", null);
		secondChars.put("i", temp);

		// o
		temp = new HashMap();
		temp.put("i", null);
		temp.put("p", null);
		firstChars.put("o", temp);
		temp = new HashMap();
		temp.put("9", null);
		temp.put("l", null);
		temp.put("k", null);
		temp.put("ç", null);
		secondChars.put("o", temp);

		// p
		temp = new HashMap();
		temp.put("o", null);
		firstChars.put("p", temp);
		temp = new HashMap();
		temp.put("0", null);
		temp.put("ç", null);
		temp.put("l", null);
		temp.put("º", null);
		secondChars.put("p", temp);

		// z
		temp = new HashMap();
		temp.put("<", null);
		firstChars.put("z", temp);
		temp = new HashMap();
		temp.put("a", null);
		temp.put("s", null);
		secondChars.put("z", temp);

		// x
		temp = new HashMap();
		temp.put("z", null);
		temp.put("c", null);
		firstChars.put("x", temp);
		temp = new HashMap();
		temp.put("a", null);
		temp.put("s", null);
		temp.put("d", null);
		secondChars.put("x", temp);

		// c
		temp = new HashMap();
		temp.put("x", null);
		temp.put("v", null);
		firstChars.put("c", temp);
		temp = new HashMap();
		temp.put("s", null);
		temp.put("d", null);
		temp.put("f", null);
		secondChars.put("c", temp);

		// v
		temp = new HashMap();
		temp.put("c", null);
		temp.put("b", null);
		firstChars.put("v", temp);
		temp = new HashMap();
		temp.put("d", null);
		temp.put("f", null);
		temp.put("g", null);
		secondChars.put("v", temp);

		// b
		temp = new HashMap();
		temp.put("v", null);
		temp.put("n", null);
		firstChars.put("b", temp);
		temp = new HashMap();
		temp.put("f", null);
		temp.put("g", null);
		temp.put("h", null);
		secondChars.put("b", temp);

		// n
		temp = new HashMap();
		temp.put("b", null);
		temp.put("m", null);
		firstChars.put("n", temp);
		temp = new HashMap();
		temp.put("g", null);
		temp.put("h", null);
		temp.put("j", null);
		secondChars.put("n", temp);

		// m
		temp = new HashMap();
		temp.put("n", null);
		temp.put(",", null);
		firstChars.put("m", temp);
		temp = new HashMap();
		temp.put("h", null);
		temp.put("j", null);
		temp.put("k", null);
		secondChars.put("m", temp);

		// a
		temp = new HashMap();
		temp.put("s", null);
		firstChars.put("a", temp);
		temp = new HashMap();
		temp.put("q", null);
		temp.put("w", null);
		temp.put("z", null);
		temp.put("x", null);
		secondChars.put("a", temp);

		// s
		temp = new HashMap();
		temp.put("a", null);
		temp.put("d", null);
		firstChars.put("s", temp);
		temp = new HashMap();
		temp.put("q", null);
		temp.put("w", null);
		temp.put("e", null);
		temp.put("z", null);
		temp.put("x", null);
		temp.put("c", null);
		secondChars.put("s", temp);

		// d
		temp = new HashMap();
		temp.put("s", null);
		temp.put("f", null);
		firstChars.put("d", temp);
		temp = new HashMap();
		temp.put("w", null);
		temp.put("e", null);
		temp.put("r", null);
		temp.put("x", null);
		temp.put("c", null);
		temp.put("v", null);
		secondChars.put("d", temp);

		// f
		temp = new HashMap();
		temp.put("d", null);
		temp.put("g", null);
		firstChars.put("f", temp);
		temp = new HashMap();
		temp.put("e", null);
		temp.put("r", null);
		temp.put("t", null);
		temp.put("c", null);
		temp.put("v", null);
		temp.put("b", null);
		secondChars.put("f", temp);

		// g
		temp = new HashMap();
		temp.put("f", null);
		temp.put("h", null);
		firstChars.put("g", temp);
		temp = new HashMap();
		temp.put("r", null);
		temp.put("t", null);
		temp.put("y", null);
		temp.put("v", null);
		temp.put("b", null);
		temp.put("n", null);
		secondChars.put("g", temp);

		// h
		temp = new HashMap();
		temp.put("g", null);
		temp.put("j", null);
		firstChars.put("h", temp);
		temp = new HashMap();
		temp.put("t", null);
		temp.put("y", null);
		temp.put("u", null);
		temp.put("b", null);
		temp.put("n", null);
		temp.put("m", null);
		secondChars.put("h", temp);

		// j
		temp = new HashMap();
		temp.put("h", null);
		temp.put("k", null);
		firstChars.put("j", temp);
		temp = new HashMap();
		temp.put("y", null);
		temp.put("u", null);
		temp.put("i", null);
		temp.put("n", null);
		temp.put("m", null);
		temp.put(",", null);
		secondChars.put("j", temp);

		// k
		temp = new HashMap();
		temp.put("j", null);
		temp.put("l", null);
		firstChars.put("k", temp);
		temp = new HashMap();
		temp.put("u", null);
		temp.put("i", null);
		temp.put("o", null);
		temp.put("m", null);
		temp.put(",", null);
		temp.put(".", null);
		secondChars.put("k", temp);

		// l
		temp = new HashMap();
		temp.put("k", null);
		temp.put("ç", null);
		firstChars.put("l", temp);
		temp = new HashMap();
		temp.put("i", null);
		temp.put("o", null);
		temp.put("p", null);
		temp.put(",", null);
		temp.put(".", null);
		temp.put("-", null);
		secondChars.put("l", temp);
	}

	/**
	 * Return the single instance of this class.
	 *
	 * @return An instance of <code>KeyboardProximity</code>.
	 */
	public static KeyboardProximity getInstance() {
		return theInstance;
	}

	/**
	 *  Check if two characters are adjacent in the same row of the keyboard.
	 *
	 *@param       ch1 A character.
	 *@param       ch2 Another character.
	 *@return        true if the characters are adjacent in the same row of the keyboard and false otherwise.
	 */
	public static boolean isSameRow(char ch1, char ch2) {
		String aux = "" + ch1;
		HashMap auxh = (HashMap)(firstChars.get(aux));
		if (auxh == null) return false;
		String auxs2 = "" + ch2;
		return (auxh.containsKey(auxs2));
	}

	/**
	 *  Check if two characters are adjacent in the keyboard.
	 *
	 *@param       ch1 A character.
	 *@param       ch2 Another character.
	 *@return        true if the characters are adjacent in the keyboard and false otherwise.
	 */
	public static boolean isNear(char ch1, char ch2) {
		if (isSameRow(ch1, ch2)) return true;
		String aux = "" + ch1;
		HashMap auxh = (HashMap) (secondChars.get(aux));
		if (auxh == null) return false;
		String auxs2 = "" + ch2;
		return (auxh.containsKey(auxs2));
	}

	/**
	 *  Check if two <code>String</code> objects differ in only a given number of characters adjacent in the same row of the keyboard.
	 *
	 *@param       s1 A <code>String</code>.
	 *@param       s2 Another <code>String</code>.
	 *@param       n the number of diferent characters. 
	 *@return        true if the strings differ in only a given number of characters adjacent in the same row of the keyboard and false otherwise.
	 */
	public static boolean stringsSameRow(String s1, String s2, int n) {
		int c = s1.length();
		if (s2.length() != c) return false;
		for (int i = 0, j = 0; i < c && j < n; i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			if (c1 == c2)
				continue;
			if (!isSameRow(c1, c2))
				return false;
			else
				j++;
		}
		return true;
	}

	/**
	 *  Check if two <code>String</code> objects differ in only one character adjacent in the same row of the keyboard.
	 *
	 *@param       s1 A <code>String</code>.
	 *@param       s2 Another <code>String</code>.
	 *@return        true if the strings differ in only one character adjacent in the same row of the keyboard and false otherwise.
	 */
	public static boolean stringsSameRow(String s1, String s2) {
		return stringsSameRow(s1, s2, 1);
	}

	/**
	 *  Check if two <code>String</code> objects differ in only a given number of characters adjacent in the keyboard.
	 *
	 *@param       s1 A <code>String</code>.
	 *@param       s2 Another <code>String</code>.
	 *@param       n the maximum number of diferent characters. 
	 *@return        true if the strings differ in only a given number of characters adjacent in the keyboard and false otherwise.
	 */
	public static boolean stringsNear(String s1, String s2, int n) {
		int c = s1.length();
		if (s2.length() != c)
			return false;
		for (int i = 0, j = 0; i < c && j < n; i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			if (c1 == c2)
				continue;
			if (!isNear(c1, c2))
				return false;
			else
				j++;
		}
		return true;
	}

	/**
	 *  Check if two <code>String</code> objects differ in only one character adjacent in the keyboard.
	 *
	 *@param       s1 A <code>String</code>.
	 *@param       s2 Another <code>String</code>.
	 *@return        true if the strings differ in only one character adjacent in the keyboard and false otherwise.
	 */
	public static boolean stringsNear(String s1, String s2) {
		return stringsNear(s1, s2, 1);
	}


	/**
	 *  Returns the similarity between two <code>String</code> objects in terms of the number of 
	 *  different characters that are adjacent in the keyboard.
	 *
	 *@param       s1 A <code>String</code>.
	 *@param       s2 Another <code>String</code>.
	 *@return        -1 if the strings differ in more than adjacent characters, 0 if the strings are equal
	 *					  and the number of different characters otherwise.
	 */
	public static int similarityStringsNear(String s1, String s2) {
		int count = 0;
		int c = s1.length();
		if (s2.length() != c) return -1;
		for (int i = 0; i < c; i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			if (c1 == c2) continue;
			if (!isNear(c1, c2)) return -1;
			else count++;
		}
		return count;
	}


	/**
	 *  Returns the similarity between two <code>String</code> objects in terms of the number of 
	 *  different characters that are adjacent in the same row of the keyboard.
	 *
	 *@param       s1 A <code>String</code>.
	 *@param       s2 Another <code>String</code>.
	 *@return        -1 if the strings differ in more than adjacent characters, 0 if the strings are equal
	 *					  and the number of different characters otherwise.
	 */
	public static int similarityStringsSameRow(String s1, String s2) {
		int count = 0;
		int c = s1.length();
		if (s2.length() != c) return -1;
		for (int i = 0; i < c; i++) {
			char c1 = s1.charAt(i);
			char c2 = s2.charAt(i);
			if (c1 == c2) continue;
			if (!isSameRow(c1, c2)) return -1;
			else count++;
		}
		return count;
	}

}