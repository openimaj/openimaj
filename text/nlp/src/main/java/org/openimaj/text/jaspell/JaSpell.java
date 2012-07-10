package org.openimaj.text.jaspell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * This class provides a command line interface for the spelling checking package, 
 * similar in style to that of Aspell, also serving as an example of how to use the
 * package from another Java program.
 *
 * @see SpellChecker
 * @author      Bruno Martins
 */
public class JaSpell {

	/** The spelling checker. */
	private SpellChecker spellCheck = null;

	/**
	 * Print the command line options to the standard output.
	 */
	public static void printUsage() {
		System.out.println("Usage: JaSpell [options] <command>");
		System.out.println();
		System.out.println("<command> is one of the following:");
		System.out.println();
		System.out.println(
			"  -?|help                     Print this help message");
		System.out.println(
			"  -c|check <a_file>           Check a file for spelling errors");
		System.out.println(
			"  -l|list                     Check standard input for spelling errors (default)");
		System.out.println(
			"  -soundlike <a_word>         Return phonetic representations of a word");
		System.out.println();
		System.out.println("available [options] include:");
		System.out.println();
		System.out.println(
			"  --data-dir=<a_file>         Path for dictionary files (default is ./data)");
		System.out.println(
			"  --jargon=<a_file>           Path for jargon file (default is ./data/jargon.txt)");
		System.out.println(
			"  --misspells=<a_file>        Path for common misspells file (default is ./data/common-misspells.txt)");
		System.out.println(
			"  --lang=<pt|en|br|it|es|web> Language to use (default is Portuguese)");
		System.out.println(
			"  --mode=<str>                Text filtering mode (default is none)");
		System.out.println(
			"                                ** Options include : none, xml, tex, sequery");
		System.out.println(
			"  -x                          Equivalent to --mode=xml");
		System.out.println( 
			"  -t                          Equivalent to --mode=tex");
		System.out.println(
			"  -q                          Equivalent to --mode=sequery");
		System.out.println(
			"  --[dont-]ignore-case        Ignore character case (default is ignore)");
		System.out.println(
			"  --[dont-]ignore-accents     Ignore character accents (default is ignore)");
		System.out.println(
			"  --[dont-]use-relfreq        Use the relative frequency method (off by default)");
		System.out.println(
			"  --[dont-]use-bigrams        Use bigrams for context correction (off by default)");


		System.out.println();			
	}

	/**
	 * Main method.  
	 *
	 * @param args 	The command line input, tokenized.
	 */
	public static void main(String[] args) throws Exception {
		int i = 0, j;
		String arg;
		char flag;
		boolean stdin = false;
		boolean file = false;
		boolean modeText = false;
		boolean modeTeX = false;
		boolean modeXML = false;
		boolean modeQuery = false;
		boolean ignoreCase = false;
		boolean notIgnoreCase = false;
		boolean ignoreAccents = false;
		boolean notIgnoreAccents = false;
		boolean useFrequencyMethod = false;
		boolean notUseFrequencyMethod = false;
		boolean useBigrams = false;
		boolean notUseBigrams = false;
		String dataDir = null;
		String jargon = null;
		String misspells = null;
		String lang = null;
		String mode = null;
		BufferedReader reader = null;
		while (i < args.length && args[i].startsWith("-")) {
			arg = args[i++];

			// use this type of check for "wordy" arguments
			if ((arg.equals("-?") || arg.equals("-help"))) {
				if (args.length != 1)
					System.err.println("Error: Illegal arguments.");
				printUsage();
				return;
			} else if ((arg.equals("-l") || arg.equals("-list"))) {
				if (reader != null) {
					System.err.println("Error: Conflicting options.");
					printUsage();
					return;
				}
				reader = new BufferedReader(new InputStreamReader(System.in));
				stdin = true;
			} else if (arg.equals("--dont-ignore-case")) {
				notIgnoreCase = true;
			} else if (arg.equals("--dont-ignore-accents")) {
				notIgnoreAccents = true;
			} else if (arg.equals("--ignore-case")) {
				ignoreCase = true;
			} else if (arg.equals("--ignore-accents")) {
				ignoreAccents = true;
			} else if (arg.equals("--use-relfreq")) {
				useFrequencyMethod = true;
			} else if (arg.equals("--dont-use-relfreq")) {
				notUseFrequencyMethod = true;
			} else if (arg.equals("--use-bigrams")) {
				useBigrams = true;
			} else if (arg.equals("--dont-use-bigrams")) {
				notUseBigrams = true;
			} else if (arg.startsWith("--data-dir=")) {
				if (dataDir != null) {
					System.err.println("Error: Conflicting options.");
					printUsage();
					return;
				}
				dataDir = arg.substring(11);
				if (dataDir.length() == 0) {
					System.err.println("Error: Illegal argument.");
					printUsage();
					return;
				}
			} else if (arg.startsWith("--lang=")) {
				if (lang != null) {
					System.err.println("Error: Conflicting options.");
					printUsage();
					return;
				}
				lang = arg.substring(7);
				if (!(lang.equals("pt") || lang.equals("en") || lang.equals("es") || lang.equals("br") || lang.equals("it") || lang.equals("web"))) {
					System.err.println("Error: Illegal argument.");
					printUsage();
					return;
				} else if (lang.equals("pt")) {
					lang = "portuguese.txt";
				} else if (lang.equals("es")) {
					lang = "spanish.txt";
				} else if (lang.equals("br")) {
					lang = "brazilian.txt";
				} else if (lang.equals("it")) {
					lang = "italian.txt";
				} else if (lang.equals("web")) {
					lang = "web-dictionary.txt";
				} else {
					lang = "english.txt";
				}
			} else if (arg.startsWith("--jargon=")) {
				if (jargon != null) {
					System.err.println("Error: Conflicting options.");
					printUsage();
					return;
				}
				jargon = arg.substring(9);
				if (jargon.length() == 0) {
					System.err.println("Error: Illegal argument.");
					printUsage();
					return;
				}
			} else if (arg.startsWith("--misspells=")) {
				if (misspells != null) {
					System.err.println("Error: Conflicting options.");
					printUsage();
					return;
				}
				misspells = arg.substring(12);
				if (misspells.length() == 0) {
					System.err.println("Error: Illegal argument.");
					printUsage();
					return;
				}
			} else if (arg.startsWith("--mode=")) {
				if (mode != null) {
					System.err.println("Error: Conflicting options.");
					printUsage();
					return;
				}
				mode = arg.substring(7);
				if (mode.length() == 0) {
					System.err.println("Error: Illegal argument.");
					printUsage();
					return;
				}

				// use this type of check for arguments that require arguments
			} else if (arg.equals("-soundlike")) {
				if (i < args.length && args.length == 2) {
					String word = args[i++];
					System.out.println(
						"Phonetic code for "
							+ word
							+ "="
							+ Phonetic.getDoubleMetaphone(word));
					return;
				} else {
					System.err.println(
						"Error : Illegal options for soundlike.");
					printUsage();
					return;
				}
			} else if (arg.equals("-c") || arg.equals("-check")) {
				if (i < args.length && reader == null) {
					reader = new BufferedReader(new FileReader(new File(args[i++])));
					file = true;
				} else {
					System.err.println("Error : Illegal options for check.");
					printUsage();
					return;
				}

				// use this type of check for a series of flag arguments
			} else {
				for (j = 1; j < arg.length(); j++) {
					flag = arg.charAt(j);
					switch (flag) {
						case 'x' :
							modeXML = true;
							break;
						case 't' :
							modeTeX = true;
							break;
						case 'q' :
							modeQuery = true;
							break;
						default :
							System.err.println("Error: Illegal option " + flag);
							printUsage();
							return;
					}
				}
			}
		}
		if (!stdin && !file) {
			printUsage();
			return;
		}
		if (dataDir == null) dataDir = "dict";
		if(misspells==null) misspells = dataDir + "/common-misspells.txt";
		else misspells = dataDir + "/" + misspells;
		if(jargon==null) jargon = dataDir + "/jargon.txt";
		else jargon = dataDir + "/" + jargon;
		if (lang == null)	dataDir += "/portuguese.txt";
		else	dataDir += "/" + lang;
		if (mode != null) {
			if (mode.equals("none"))
				modeText = true;
			else if (mode.equals("xml"))
				modeXML = true;
			else if (mode.equals("tex"))
				modeTeX = true;
			else if (mode.equals("squery"))
				modeQuery = true;
			else {
				System.err.println(
					"Error: Conflicting options for text filtering mode.");
				printUsage();
				return;
			}
		}
		DefaultWordFinder finder = null;
		if (modeTeX && (!modeXML && !modeText && !modeQuery))
			finder = new TeXWordFinder();
		else if (modeXML && (!modeTeX && !modeText && !modeQuery))
			finder = new XMLWordFinder();
		else if (modeQuery && (!modeTeX && !modeText && !modeXML))
			finder = new DefaultWordFinder();
		else if (!modeTeX && !modeText && !modeQuery)
			finder = new DefaultWordFinder();
		else {
			System.err.println(
				"Error: Conflicting options for text filtering mode.");
			printUsage();
			return;
		}
		if((useFrequencyMethod && notUseFrequencyMethod) ||
		   (useBigrams && notUseBigrams) ||
		   (ignoreAccents && notIgnoreAccents) ||
		   (ignoreCase && notIgnoreCase)) {
			System.err.println("Error: Conflicting options.");
			printUsage();
			return;
		}
		SpellChecker spellCheck = new SpellChecker();
		spellCheck.initialize(dataDir,misspells,jargon);
		System.out.println("JaSpell :: Java Spelling Checking Package");
		String line;
		while ((line = reader.readLine()) != null) {
			finder.setText(line);
			String aux = null;
			while ((aux = finder.next()) != null) {
				if(useBigrams) {
					String next = finder.lookAhead();
					String aux2 = spellCheck.findMostSimilar(aux+" "+next,useFrequencyMethod);
					if (aux2 != null)	finder.replaceBigram(aux2); else {
						aux2 = spellCheck.findMostSimilar(aux,useFrequencyMethod);
						if (aux2 != null)	finder.replace(aux2);
					}
				} else {
					String aux2 = spellCheck.findMostSimilar(aux,useFrequencyMethod);
					if (aux2 != null)	finder.replace(aux2);
				}
			}
			System.out.println(finder.getText());
		}
	}

}
