package org.openimaj.text.jaspell;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

/**
 * A word finder for TeX and LaTeX documents, which searches text for
 * sequences of letters, but ignores any  commands and environments as well
 * as Math environments.
 *
 * @see DefaultWordFinder
 * @author      Bruno Martins
 */
public class TeXWordFinder extends DefaultWordFinder {

  /** Boolean flag indicating if TeX comments should be ignored. */
  private boolean IGNORE_COMMENTS = true;
  
  /** A Set of user defined ignores. */
  private Set userDefinedIgnores = new HashSet();
  
  /** An integer specifying the type of expression to use. e.g. REG_EXPR, STRING_EXPR. */
  private int regexUserDefinedIgnores = STRING_EXPR;
  
  /**Constant value specifying strings on user defined ignores.  */
  public static final int STRING_EXPR = 0;
  
  /**Constant value specifying regular expressions on user defined ignores.  */
  public static final int REG_EXPR = 1;

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
   * Constructor for TeXWordFinder.
   *
   * @param inText A String with the input text to tokenize.
   */
  public TeXWordFinder(String inText) {
    super(inText);
  }
  
  /**
   * Constructor for TexWordFinder.
   */
  public TeXWordFinder() {
    super();
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
    search:
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
        }  //}}}
// Ignores should be in order of importance and then specificity.
        int j = i;
// Ignore Comments:
        if(IGNORE_COMMENTS) j = ignore(j, '%', '\n');
// Ignore Maths:
        j = ignore(j, "$$", "$$");
        j = ignore(j, '$', '$');
// Ignore user defined.
        j = ignoreUserDefined(j);
// Ignore certain command parameters.
        j = ignore(j, "\\newcommand", "}");
        j = ignore(j, "\\documentclass", "}");
        j = ignore(j, "\\usepackage", "}");
        j = ignore(j, "\\newcounter{", "}");
        j = ignore(j, "\\setcounter{", "}");
        j = ignore(j, "\\addtocounter{", "}");
        j = ignore(j, "\\value{", "}");
        j = ignore(j, "\\arabic{", "}");
        j = ignore(j, "\\usecounter{", "}");
        j = ignore(j, "\\newenvironment", "}");
        j = ignore(j, "\\setlength", "}");
        j = ignore(j, "\\setkeys", "}");
// Ignore environment names.
        j = ignore(j, "\\begin{", "}");
        j = ignore(j, "\\end{", "}");        
        if (i != j){
          i = j;
          continue search;
        }
// Ignore commands.
        j = ignore(j, '\\');
        if (i != j){
          i = j;
          continue search;
        }
        i++;
      }
	  if (!started) {
    	  nextWord = null;
       } else if (!finished) {
	      nextWord = text.substring(nextWordPos, i);
       }
	   return currentWord;
  }

  /**
   * This method is used to import a user defined set of either strings
   * or regular expressions to ignore.
   *
   * @param expressions a collection of of Objects whose toString() value
   *               should be the expression. Typically String objects.
   * @param regex is an integer specifying the type of expression to 
   *               use. e.g. REG_EXPR, STRING_EXPR.
   */
  public void addUserDefinedIgnores(Collection expressions, int regex){
    userDefinedIgnores.addAll(expressions);
    regexUserDefinedIgnores = regex;
  }

  /**
   * User defined ignore.
   * 
   * @param i
   * @return
   */
  private int ignoreUserDefined(int i) {
  	Iterator it = userDefinedIgnores.iterator();
  	while (it.hasNext()) {
  		String ignore = (String)(it.next());
		String ignore2 = (String)(it.next());
  		i = ignore(i,ignore,ignore2); 
  	}
  	return i;
  }
  
  /**
   * Allows one to indicate if TeX comments should be ignored. 
   *
   * @param ignore true if TeX comments should be ignored and false otherwise.
   */
  public void setIgnoreComments(boolean ignore) {
    IGNORE_COMMENTS = ignore;
  }

}