package org.openimaj.text.jaspell;

/**
 *  Implementation of Phonetic similarity algorithms (Soundex, Metaphone
 *  and Double Metaphone), used  to reduce matching problems from wrong spellings.</p> <p>
 * 
 * A Soundex search algorithm takes a word, such as a person's name,
 *  as input and produces a character string which identifies a set 
 * of words that are (roughly) phonetically alike. It is very handy for 
 *  searching large databases when the user has incomplete data.</p> <p> 
  * 
 * The original Soundex algorithm was patented by Margaret O'Dell and 
 * Robert C. Russell in 1918. The method is based on the six phonetic
 * classifications of human speech sounds (bilabial, labiodental,
 * dental, alveolar, velar, and glottal), which in turn are based on where
 * you put your lips and tongue to make the sounds.</p> <p> 
  *
 *  As for the metaphone algorithm, it encodes English words phonetically, by 
 *  reducing them to 16 consonant sounds. It originally appeared in the 
 *  December 1990 issue of Computer Language by Lawrence Philips. 
 * Vowels are minimized as much as possible, and consenants
 * that have similiar sounds are converted to the same consenant
 * for example, 'v' and 'f' are both converted to 'f'.</p> <p>
  * 
 *  The double metaphone algorithm is a better variant, coding English words
 *  (and foreign words often heard in the United States) phonetically by 
 *  reducing them to 12 consonant sounds. It was also proposed by Lawrence Philips, in an 
 *  C/C++ Users Journal(tm) article entitled 
 * <a href="http://www.cuj.com/documents/s=8038/cuj0006philips/">
 *  The Double Metaphone Search Algorithm</a>. 
 *
 * @author Bruno Martins
 *
 */
public class Phonetic {

	/** The single instance of this class. */
	private static final Phonetic theInstance = new Phonetic();

	/**  The transformation codes used for the soundex algorithm. */
	private final static String soundexCodes[] = { "0AEIOUWYH",
		// to ignore
		"1BPFV",
		// code 1
		"2CSKGJQXZ",
		// code 2
		"3DT",
		// code 3
		"4L",
		// code 4
		"5MN",
		// code 5
		"6R"
		// code 6
	};

	/**
	 * Return the single instance of this class.
	 *
	 * @return An instance of <code>Phonetic</code>.
	 */
	public static Phonetic getInstance() {
		return theInstance;
	}

	/**
	 *  Calculate the Metaphone value of a given <code>String</code>. This method transforms
	 *  the parameter into a <code>String</code> according to the Metaphone algorithm.
	 *
	 *@param  word  The <code>String</code> to transform.
	 *@return  The Metaphone equivalent of the <code>String</code>.
	 */
	public static String getMetaphone(String word) {
		String tmp = word.toUpperCase();
		StringBuffer result = new StringBuffer();
		int start = 0;
		if (tmp.startsWith("KN")
			|| tmp.startsWith("GN")
			|| tmp.startsWith("PN")
			|| tmp.startsWith("AE")
			|| tmp.startsWith("WR")) {
			start = 1;
		}
		if (tmp.startsWith("X")) {
			result.append("S");
			start = 1;
		}
		if (tmp.startsWith("WH")) {
			result.append("W");
			start = 1;
		}
		for (int i = start; i < tmp.length(); i++) {
			if (i < tmp.length() - 1 && tmp.charAt(i) == tmp.charAt(i + 1)) {
				continue;
			}
			String leftContext = tmp.substring(0, i);
			String rightContext = tmp.substring(i + 1);
			switch (tmp.charAt(i)) {
				case 'B' :
					if (rightContext.length() == 0
						&& leftContext.endsWith("M")) {
						// silent
						break;
					}
					result.append("B");
					break;
				case 'C' :
					if (leftContext.endsWith("S")
						&& rightContext.startsWith("H")) {
						result.append("K");
						break;
					}
					if (rightContext.startsWith("IA")
						|| rightContext.startsWith("H")) {
						result.append("X");
						break;
					}
					if (rightContext.startsWith("I")
						|| rightContext.startsWith("E")
						|| rightContext.startsWith("Y")) {
						result.append("S");
						break;
					}
					result.append("K");
					break;
				case 'D' :
					if (rightContext.startsWith("GE")
						|| rightContext.startsWith("GY")
						|| rightContext.startsWith("GI")) {
						result.append("J");
					} else {
						result.append("T");
					}
					break;
				case 'F' :
					result.append("F");
					break;
				case 'G' :
					if (leftContext.endsWith("D")
						|| rightContext.startsWith("N")
						|| (i >= 1 && rightContext.startsWith("H"))) {
						// silent
						break;
					}
					if (rightContext.startsWith("I")
						|| rightContext.startsWith("E")
						|| rightContext.startsWith("Y")) {
						if (!leftContext.endsWith("G")) {
							result.append("J");
							break;
						}
					}
					result.append("K");
					break;
				case 'H' :
					if (leftContext.endsWith("G")
						|| leftContext.endsWith("S")
						|| leftContext.endsWith("P")
						|| leftContext.endsWith("C")
						|| leftContext.endsWith("T")) {
						// silent
						break;
					}
					if (i > 1 && i < tmp.length() - 1) {
						if ("AEIOUY".indexOf(tmp.charAt(i - 1)) >= 0
							&& "AEIOUY".indexOf(tmp.charAt(i + 1)) < 0) {
							// silent
							break;
						}
					}
					result.append("H");
					break;
				case 'J' :
					result.append("J");
					break;
				case 'K' :
					if (leftContext.endsWith("C")) {
						// silent
						break;
					}
					result.append("K");
					break;
				case 'L' :
					result.append("L");
					break;
				case 'M' :
					result.append("M");
					break;
				case 'N' :
					result.append("N");
					break;
				case 'P' :
					if (rightContext.startsWith("H")) {
						result.append("F");
						break;
					}
					result.append("P");
					break;
				case 'Q' :
					result.append("K");
					break;
				case 'R' :
					result.append("R");
					break;
				case 'S' :
					if (rightContext.startsWith("H")
						|| rightContext.startsWith("IO")
						|| rightContext.startsWith("IA")) {
						result.append("X");
						break;
					}
					result.append("S");
					break;
				case 'T' :
					if (rightContext.startsWith("H")) {
						result.append("0");
						break;
					}
					if (rightContext.startsWith("IO")
						|| rightContext.startsWith("IA")) {
						result.append("X");
						break;
					}
					result.append("T");
					break;
				case 'V' :
					result.append("F");
					break;
				case 'W' :
					if (i < tmp.length() - 1) {
						if ("AEIOUY".indexOf(tmp.charAt(i + 1)) >= 0) {
							result.append("W");
							break;
						}
					}
					// silent
					break;
				case 'X' :
					result.append("KS");
					break;
				case 'Y' :
					if (i < tmp.length() - 1) {
						if ("AEIOU".indexOf(tmp.charAt(i + 1)) >= 0) {
							result.append("Y");
							break;
						}
					}
					// silent
					break;
				case 'Z' :
					result.append("S");
					break;
				case 'A' :
				case 'E' :
				case 'I' :
				case 'O' :
				case 'U' :
					if (i == 0) {
						result.append("" + tmp.charAt(i));
					}
					break;
				default :
					break;
			}
		}
		return (result.toString());
	}

	/**
	 * Gets the index for the attribute of the Soundex transformation codes corresponding
	 *  to a given char.
	 *
	 *@param  c  A character.
	 *@return  The index for the attribute of the Soundex transformation codes.
	 */
	private static char getNum(char c) {
		for (int i = 0; i < soundexCodes.length; i++) {
			if (soundexCodes[i].indexOf(c) > 0) {
				return (soundexCodes[i].charAt(0));
			}
		}
		return ('0');
	}

	/**
	 *  Calculate the Soundex value of a given <code>String</code>. This method transforms
	 *  the parameter into a <code>String</code> according to the Soundex algorithm.
	 *
	 *@param  word  The <code>String</code> to transform.
	 *@return  The Soundex equivalent of the <code>String</code>.
	 */
	public static String getSoundex(String word) {
		String tmp = word.toUpperCase();
		String buf = new String();
		char last = '0';
		buf += tmp.charAt(0);
		for (int i = 1; i < tmp.length(); i++) {
			char x = getNum(tmp.charAt(i));
			if (x == last || x == '0') {
				continue;
			}
			buf += x;
		}
		return (buf.toString());
	}

	/**
	 *  Sole constructor, private because this is a Singleton class.
	 */
	private Phonetic() {
	}

	/*
	 * Internal variables used by the double metaphone implementation
	 */
	private static char[] replaceList =
		{
			'A',
			'B',
			'X',
			'S',
			'K',
			'J',
			'T',
			'F',
			'H',
			'L',
			'M',
			'N',
			'P',
			'R',
			'0' };
	private static final String[] myList = { "GN", "KN", "PN", "WR", "PS", "" };
	private static final String[] list1 = { "ACH", "" };
	private static final String[] list2 = { "BACHER", "MACHER", "" };
	private static final String[] list3 = { "CAESAR", "" };
	private static final String[] list4 = { "CHIA", "" };
	private static final String[] list5 = { "CH", "" };
	private static final String[] list6 = { "CHAE", "" };
	private static final String[] list7 = { "HARAC", "HARIS", "" };
	private static final String[] list8 = { "HOR", "HYM", "HIA", "HEM", "" };
	private static final String[] list9 = { "CHORE", "" };
	private static final String[] list10 = { "VAN ", "VON ", "" };
	private static final String[] list11 = { "SCH", "" };
	private static final String[] list12 = { "ORCHES", "ARCHIT", "ORCHID", "" };
	private static final String[] list13 = { "T", "S", "" };
	private static final String[] list14 = { "A", "O", "U", "E", "" };
	private static final String[] list15 =
		{ "L", "R", "N", "M", "B", "H", "F", "V", "W", " ", "" };
	private static final String[] list16 = { "MC", "" };
	private static final String[] list17 = { "CZ", "" };
	private static final String[] list18 = { "WICZ", "" };
	private static final String[] list19 = { "CIA", "" };
	private static final String[] list20 = { "CC", "" };
	private static final String[] list21 = { "I", "E", "H", "" };
	private static final String[] list22 = { "HU", "" };
	private static final String[] list23 = { "UCCEE", "UCCES", "" };
	private static final String[] list24 = { "CK", "CG", "CQ", "" };
	private static final String[] list25 = { "CI", "CE", "CY", "" };
	private static final String[] list27 = { " C", " Q", " G", "" };
	private static final String[] list28 = { "C", "K", "Q", "" };
	private static final String[] list29 = { "CE", "CI", "" };
	private static final String[] list30 = { "DG", "" };
	private static final String[] list31 = { "I", "E", "Y", "" };
	private static final String[] list32 = { "DT", "DD", "" };
	private static final String[] list33 = { "B", "H", "D", "" };
	private static final String[] list34 = { "B", "H", "D", "" };
	private static final String[] list35 = { "B", "H", "" };
	private static final String[] list36 = { "C", "G", "L", "R", "T", "" };
	private static final String[] list37 = { "EY", "" };
	private static final String[] list38 = { "LI", "" };
	private static final String[] list39 =
		{
			"ES",
			"EP",
			"EB",
			"EL",
			"EY",
			"IB",
			"IL",
			"IN",
			"IE",
			"EI",
			"ER",
			"" };
	private static final String[] list40 = { "ER", "" };
	private static final String[] list41 = { "DANGER", "RANGER", "MANGER", "" };
	private static final String[] list42 = { "E", "I", "" };
	private static final String[] list43 = { "RGY", "OGY", "" };
	private static final String[] list44 = { "E", "I", "Y", "" };
	private static final String[] list45 = { "AGGI", "OGGI", "" };
	private static final String[] list46 = { "VAN ", "VON ", "" };
	private static final String[] list47 = { "SCH", "" };
	private static final String[] list48 = { "ET", "" };
	private static final String[] list50 = { "JOSE", "" };
	private static final String[] list51 = { "SAN ", "" };
	private static final String[] list52 = { "SAN ", "" };
	private static final String[] list53 = { "JOSE", "" };
	private static final String[] list54 =
		{ "L", "T", "K", "S", "N", "M", "B", "Z", "" };
	private static final String[] list55 = { "S", "K", "L", "" };
	private static final String[] list56 = { "ILLO", "ILLA", "ALLE", "" };
	private static final String[] list57 = { "AS", "OS", "" };
	private static final String[] list58 = { "A", "O", "" };
	private static final String[] list59 = { "ALLE", "" };
	private static final String[] list60 = { "UMB", "" };
	private static final String[] list61 = { "ER", "" };
	private static final String[] list62 = { "P", "B", "" };
	private static final String[] list63 = { "IE", "" };
	private static final String[] list64 = { "ME", "MA", "" };
	private static final String[] list65 = { "ISL", "YSL", "" };
	private static final String[] list66 = { "SUGAR", "" };
	private static final String[] list67 = { "SH", "" };
	private static final String[] list68 =
		{ "HEIM", "HOEK", "HOLM", "HOLZ", "" };
	private static final String[] list69 = { "SIO", "SIA", "" };
	private static final String[] list70 = { "SIAN", "" };
	private static final String[] list71 = { "M", "N", "L", "W", "" };
	private static final String[] list72 = { "Z", "" };
	private static final String[] list73 = { "Z", "" };
	private static final String[] list74 = { "SC", "" };
	private static final String[] list75 =
		{ "OO", "ER", "EN", "UY", "ED", "EM", "" };
	private static final String[] list76 = { "ER", "EN", "" };
	private static final String[] list77 = { "I", "E", "Y", "" };
	private static final String[] list78 = { "AI", "OI", "" };
	private static final String[] list79 = { "S", "Z", "" };
	private static final String[] list80 = { "TION", "" };
	private static final String[] list81 = { "TIA", "TCH", "" };
	private static final String[] list82 = { "TH", "" };
	private static final String[] list83 = { "TTH", "" };
	private static final String[] list84 = { "OM", "AM", "" };
	private static final String[] list85 = { "VAN ", "VON ", "" };
	private static final String[] list86 = { "SCH", "" };
	private static final String[] list87 = { "T", "D", "" };
	private static final String[] list88 = { "WR", "" };
	private static final String[] list89 = { "WH", "" };
	private static final String[] list90 =
		{ "EWSKI", "EWSKY", "OWSKI", "OWSKY", "" };
	private static final String[] list91 = { "SCH", "" };
	private static final String[] list92 = { "WICZ", "WITZ", "" };
	private static final String[] list93 = { "IAU", "EAU", "" };
	private static final String[] list94 = { "AU", "OU", "" };
	private static final String[] list95 = { "C", "X", "" };

	/**
	 * Checks if a given word is slavo-germanic.
	 *  
	 * @param in A <code>String</code> with the word.
	 * @return true if the <code>String</code> corresponds to a slavo-germanic word and false otherwise.
	 */
	public final static boolean SlavoGermanic(String in) {
		if ((in.indexOf("W") > -1)
			|| (in.indexOf("K") > -1)
			|| (in.indexOf("CZ") > -1)
			|| (in.indexOf("WITZ") > -1))
			return true;
		return false;
	}

	/**
	 *  Checks if part of a given <code>String</code> is equal to any of the <code>String</code> objects supplied in a <code>List</code>.
	 *
	 * @param string The input <code>String</code>.
	 * @param start Starting position of the input <code>String</code> to check.
	 * @param length Lengh of the <code>String</code> to check, starting at the given starting position.
	 * @param list The <code>List</code> of <code>String</code> objects to check.
	 * @return true if part of a given <code>String</code> is equal to any of the strings supplied in a list and false otherwise.
	 */
	private static boolean stringAt(String string,int start,int length,String[] list) {
		if ((start < 0) || (start >= string.length()) || list.length == 0)	return false;
		String substr = string.substring(start, start + length);
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals(substr)) return true;
		}
		return false;
	} 

	/**
	 *  Calculates the Double Metaphone value of a given <code>String</code>. This method transforms
	 *  the parameter into a <code>String</code> according to the Double Metaphone algorithm.
	 *
	 *@param  word  The <code>String</code> to transform.
	 *@return The Double Metaphone equivalent of the <code>String</code>.
	 */
	public static String getDoubleMetaphone(String word) {
		StringBuffer primary = new StringBuffer(word.length() + 5);
		try {
		String in = word.toUpperCase() + "     ";
		int current = 0;
		int length = in.length();
		if (length < 1) return "";
		int last = length - 1;
		boolean isSlavoGermaic = SlavoGermanic(in);
		if (stringAt(in, 0, 2, myList))
			current += 1;
		if (in.charAt(0) == 'X') {
			primary.append((char) 'S');
			current += 1;
		}
		while (current < length) {
			switch (in.charAt(current)) {
				case 'A' :
				case 'E' :
				case 'I' :
				case 'O' :
				case 'U' :
				case 'Y' :
					if (current == 0)
						primary.append((char) 'A');
					current += 1;
					break;
				case 'B' :
					primary.append((char) 'P');
					if (in.charAt(current + 1) == 'B')
						current += 2;
					else
						current += 1;
					break;
				case '\u00C7' :
					primary.append((char) 'S');
					current += 1;
					break;
				case 'C' :
					if ((current > 1)
						&& !StringUtils.isVowel(in, current - 2, length)
						&& stringAt(in, (current - 1), 3, list1)
						&& (in.charAt(current + 2) != 'I')
						&& (in.charAt(current + 2) != 'E')
						|| stringAt(in, (current - 2), 6, list2)) {
						primary.append((char) 'K');
						current += 2;
						break;
					}
					if ((current == 0) && stringAt(in, current, 6, list3)) {
						primary.append((char) 'S');
						current += 2;
						break;
					}
					if (stringAt(in, current, 4, list4)) {
						primary.append((char) 'K');
						current += 2;
						break;
					}
					if (stringAt(in, current, 2, list5)) {
						if ((current > 0) && stringAt(in, current, 4, list6)) {
							primary.append((char) 'K');
							current += 2;
							break;
						}
						if ((current == 0)
							&& stringAt(in, (current + 1), 5, list7)
							|| stringAt(in, current + 1, 3, list8)
							&& !stringAt(in, 0, 5, list9)) {
							primary.append((char) 'K');
							current += 2;
							break;
						}
						if (stringAt(in, 0, 4, list10)
							|| stringAt(in, 0, 3, list11)
							|| stringAt(in, current - 2, 6, list12)
							|| stringAt(in, current + 2, 1, list13)
							|| (stringAt(in, current - 1, 1, list14)
								|| (current == 0))
							&& stringAt(in, current + 2, 1, list15)) {
							primary.append((char) 'K');
						} else {
							if (current > 0) {
								if (stringAt(in, 0, 2, list16))
									primary.append((char) 'K');
								else
									primary.append((char)'X');
							} else {
								primary.append((char)'X');
							}
						}
						current += 2;
						break;
					}
					if (stringAt(in, current, 2, list17)
						&& !stringAt(in, current, 4, list18)) {
						primary.append((char)'S');
						current += 2;
						break;
					}
					if (stringAt(in, current, 2, list19)) {
						primary.append((char)'X');
						current += 2;
						break;
					}
					if (stringAt(in, current, 2, list20)
						&& !((current == 1) && in.charAt(0) == 'M')) {
						if (stringAt(in, current + 2, 1, list21)
							&& !stringAt(in, current + 2, 2, list22)) {
							if (((current == 1)
								&& (in.charAt(current - 1) == 'A'))
								|| stringAt(in, (current - 1), 5, list23))
								primary.append("KS");
							else
								primary.append((char)'X');
							current += 3;
							break;
						} else {
							primary.append((char)'K');
							current += 2;
							break;
						}
					}
					if (stringAt(in, current, 2, list24)) {
						primary.append((char)'K');
						current += 2;
						break;
					} else if (stringAt(in, current, 2, list25)) {
						primary.append((char)'S');
						current += 2;
						break;
					}

					primary.append((char)'K');
					if (stringAt(in, current + 1, 2, list27))
						current += 3;
					else if (
						stringAt(in, current + 1, 1, list28)
							&& !stringAt(in, current + 1, 2, list29))
						current += 2;
					else
						current += 1;
					break;
				case 'D' :
					if (stringAt(in, current, 2, list30)) {
						if (stringAt(in, current + 2, 1, list31)) {
							primary.append((char)'J');
							current += 3;
							break;
						} else {
							primary.append("TK");
							current += 2;
							break;
						}
					}
					primary.append((char)'T');
					if (stringAt(in, current, 2, list32)) {
						current += 2;
					} else {
						current += 1;
					}
					break;
				case 'F' :
					if (in.charAt(current + 1) == 'F')
						current += 2;
					else
						current += 1;
					primary.append((char)'F');
					break;
				case 'G' :
					if (in.charAt(current + 1) == 'H') {
						if ((current > 0)
							&& !StringUtils.isVowel(in, current - 1, length)) {
							primary.append((char)'K');
							current += 2;
							break;
						}
						if (current < 3) {
							if (current == 0) {
								if (in.charAt(current + 2) == 'I')
									primary.append((char)'J');
								else
									primary.append((char)'K');
								current += 2;
								break;
							}
						}
						if ((current > 1)
							&& stringAt(in, current - 2, 1, list33)
							|| ((current > 2)
								&& stringAt(in, current - 3, 1, list34))
							|| ((current > 3)
								&& stringAt(in, current - 4, 1, list35))) {
							current += 2;
							break;
						} else {
							if ((current > 2)
								&& (in.charAt(current - 1) == 'U')
								&& stringAt(in, current - 3, 1, list36)) {
								primary.append((char)'F');
							} else {
								if ((current > 0)
									&& (in.charAt(current - 1) != 'I'))
									primary.append((char)'K');
							}
							current += 2;
							break;
						}
					}
					if (in.charAt(current + 1) == 'N') {
						if ((current == 1)
							&& StringUtils.isVowel(in, 0, length)
							&& !isSlavoGermaic) {
							primary.append("KN");
						} else {
							if (!stringAt(in, current + 2, 2, list37)
								&& (in.charAt(current + 1) != 'Y')
								&& !isSlavoGermaic) {
								primary.append('N');
							} else {
								primary.append("KN");
							}
						}
						current += 2;
						break;
					}
					if (stringAt(in, current + 1, 2, list38)
						&& !isSlavoGermaic) {
						primary.append("KL");
						current += 2;
						break;
					}
					if ((current == 0)
						&& ((in.charAt(current + 1) == 'Y')
							|| stringAt(in, current + 1, 2, list39))) {
						primary.append((char)'K');
						current += 2;
						break;
					}
					if ((stringAt(in, current + 1, 2, list40)
						|| (in.charAt(current + 1) == 'Y'))
						&& !stringAt(in, 0, 6, list41)
						&& !stringAt(in, current - 1, 1, list42)
						&& !stringAt(in, current - 1, 3, list43)) {
						primary.append((char)'K');
						current += 2;
						break;
					}
					if (stringAt(in, current + 1, 1, list44)
						|| stringAt(in, current - 1, 4, list45)) {
						if (stringAt(in, 0, 4, list46)
							|| stringAt(in, 0, 3, list47)
							|| stringAt(in, current + 1, 2, list48)) {
							primary.append((char)'K');
						} else {
							primary.append((char)'J');
						}
						current += 2;
						break;
					}
					if (in.charAt(current + 1) == 'G')
						current += 2;
					else
						current += 1;
					primary.append((char)'K');
					break;
				case 'H' :
					if (((current == 0)
						|| StringUtils.isVowel(in, current - 1, length))
						&& StringUtils.isVowel(in, current + 1, length)) {
						primary.append((char)'H');
						current += 2;
					} else {
						current += 1;
					}
					break;
				case 'J' :
					if (stringAt(in, current, 4, list50)
						|| stringAt(in, 0, 4, list51)) {
						if ((current == 0)
							&& (in.charAt(current + 4) == ' ')
							|| stringAt(in, 0, 4, list52)) {
							primary.append((char)'H');
						} else {
							primary.append((char)'J');
						}
						current += 1;
						break;
					}
					if ((current == 0) && !stringAt(in, current, 4, list53)) {
						primary.append((char)'J');
					} else {
						if (StringUtils.isVowel(in, current - 1, length)
							&& !isSlavoGermaic
							&& ((in.charAt(current + 1) == 'A')
								|| in.charAt(current + 1) == 'O')) {
							primary.append((char)'J');
						} else {
							if (current == last) {
								primary.append((char)'J');
							} else {
								if (!stringAt(in, current + 1, 1, list54)
									&& !stringAt(in, current - 1, 1, list55)) {
									primary.append((char)'J');
								}
							}
						}
					}
					if (in.charAt(current + 1) == 'J')
						current += 2;
					else
						current += 1;
					break;
				case 'K' :
					if (in.charAt(current + 1) == 'K')
						current += 2;
					else
						current += 1;
					primary.append((char)'K');
					break;
				case 'L' :
					if (in.charAt(current + 1) == 'L') {
						if (((current == (length - 3))
							&& stringAt(in, current - 1, 4, list56))
							|| ((stringAt(in, last - 1, 2, list57)
								|| stringAt(in, last, 1, list58))
								&& stringAt(in, current - 1, 4, list59))) {
							primary.append((char)'L');
							current += 2;
							break;
						}
						current += 2;
					} else
						current += 1;
					primary.append((char)'L');
					break;
				case 'M' :
					if ((stringAt(in, current - 1, 3, list60)
						&& (((current + 1) == last)
							|| stringAt(in, current + 2, 2, list61)))
						|| (in.charAt(current + 1) == 'M'))
						current += 2;
					else
						current += 1;
					primary.append((char)'M');
					break;
				case 'N' :
					if (in.charAt(current + 1) == 'N')
						current += 2;
					else
						current += 1;
					primary.append((char)'N');
					break;
				case '\u00D1' :
					current += 1;
					primary.append((char)'N');
					break;
				case 'P' :
					if (in.charAt(current + 1) == 'N') {
						primary.append((char)'F');
						current += 2;
						break;
					}
					if (stringAt(in, current + 1, 1, list62))
						current += 2;
					else
						current += 1;
					primary.append((char)'P');
					break;
				case 'Q' :
					if (in.charAt(current + 1) == 'Q')
						current += 2;
					else
						current += 1;
					primary.append((char)'K');
					break;
				case 'R' :
					if ((current == last)
						&& !isSlavoGermaic
						&& stringAt(in, current - 2, 2, list63)
						&& !stringAt(in, current - 4, 2, list64)) {
						//				primary.append((char)"");
					} else
						primary.append((char)'R');
					if (in.charAt(current + 1) == 'R')
						current += 2;
					else
						current += 1;
					break;
				case 'S' :
					if (stringAt(in, current - 1, 3, list65)) {
						current += 1;
						break;
					}
					if ((current == 0) && stringAt(in, current, 5, list66)) {
						primary.append((char)'X');
						current += 1;
						break;
					}
					if (stringAt(in, current, 2, list67)) {
						if (stringAt(in, current + 1, 4, list68))
							primary.append((char)'S');
						else
							primary.append((char)'X');
						current += 2;
						break;
					}
					if (stringAt(in, current, 3, list69)
						|| stringAt(in, current, 4, list70)) {
						primary.append((char)'S');
						current += 3;
						break;
					}
					if (((current == 0)
						&& stringAt(in, current + 1, 1, list71))
						|| stringAt(in, current + 1, 1, list72)) {
						primary.append((char)'S');
						if (stringAt(in, current + 1, 1, list73))
							current += 2;
						else
							current += 1;
						break;
					}
					if (stringAt(in, current, 2, list74)) {
						if (in.charAt(current + 2) == 'H')
							if (stringAt(in, current + 3, 2, list75)) {
								if (stringAt(in, current + 3, 2, list76)) {
									primary.append('X');
								} else {
									primary.append("SK");
								}
								current += 3;
								break;
							} else {
								primary.append((char)'X');
								current += 3;
								break;
							}
						if (stringAt(in, current + 2, 1, list77)) {
							primary.append((char)'S');
							current += 3;
							break;
						}
						primary.append("SK");
						current += 3;
						break;
					}
					if ((current == last)
						&& stringAt(in, current - 2, 2, list78)) {
						//primary.append((char)"");
					} else
						primary.append((char)'S');
					if (stringAt(in, current + 1, 1, list79))
						current += 2;
					else
						current += 1;
					break;
				case 'T' :
					if (stringAt(in, current, 4, list80)) {
						primary.append((char)'X');
						current += 3;
						break;
					}
					if (stringAt(in, current, 3, list81)) {
						primary.append((char)'X');
						current += 3;
						break;
					}
					if (stringAt(in, current, 2, list82)
						|| stringAt(in, current, 3, list83)) {
						if (stringAt(in, (current + 2), 2, list84)
							|| stringAt(in, 0, 4, list85)
							|| stringAt(in, 0, 3, list86)) {
							primary.append((char)'T');
						} else {
							primary.append((char)'0');
						}
						current += 2;
						break;
					}
					if (stringAt(in, current + 1, 1, list87)) {
						current += 2;
					} else
						current += 1;
					primary.append((char)'T');
					break;
				case 'V' :
					if (in.charAt(current + 1) == 'V')
						current += 2;
					else
						current += 1;
					primary.append((char)'F');
					break;
				case 'W' :
					if (stringAt(in, current, 2, list88)) {
						primary.append((char)'R');
						current += 2;
						break;
					}
					if ((current == 0)
						&& (StringUtils.isVowel(in, current + 1, length)
							|| stringAt(in, current, 2, list89))) {
						primary.append((char)'A');
					}
					if (((current == last)
						&& StringUtils.isVowel(in, current - 1, length))
						|| stringAt(in, current - 1, 5, list90)
						|| stringAt(in, 0, 3, list91)) {
						primary.append((char)'F');
						current += 1;
						break;
					}
					if (stringAt(in, current, 4, list92)) {
						primary.append("TS");
						current += 4;
						break;
					}
					current += 1;
					break;
				case 'X' :
					if (!((current == last)
						&& (stringAt(in, current - 3, 3, list93)
							|| stringAt(in, current - 2, 2, list94))))
						primary.append("KS");
					if (stringAt(in, current + 1, 1, list95))
						current += 2;
					else
						current += 1;
					break;
				case 'Z' :
					if (in.charAt(current + 1) == 'H') {
						primary.append((char)'J');
						current += 2;
						break;
					} else {
						primary.append((char)'S');
					}
					if (in.charAt(current + 1) == 'Z')
						current += 2;
					else
						current += 1;
					break;
				default :
					current += 1;
			}
		}
		} catch ( Exception e ) {}
		return primary.toString();
	}

}
