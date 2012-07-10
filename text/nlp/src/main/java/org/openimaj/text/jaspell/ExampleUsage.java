package org.openimaj.text.jaspell;

import java.util.List;

/**
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 *         This is just a quick example usage of the classes in this package to
 *         do a simple spell check on a word. SpellChecker can be initialised
 *         with several lists that would aid it in spell checking. The useful
 *         methods of SpellChecker to get results are:
 * 
 *         SpellCheck.spellCheck(various params)
 *         SpellCheck.findMostSimilar(various params)
 *         SpellCheck.findMostSimilarList(various params)
 * 
 * 
 */
public class ExampleUsage {

	static String en_dic_path = "src/main/java/org/openimaj/text/jaspell/dictionaries/english.txt";
	static String en_miss_spell_path = "src/main/java/org/openimaj/text/jaspell/dictionaries/en-common-misspells.txt";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpellChecker sc = new SpellChecker();
		try {
			sc.initialize(en_dic_path, en_miss_spell_path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> suggestions = sc.findMostSimilarList("ipad", false);
		for (String res : suggestions) {
			System.out.println(res);
		}
	}

}
