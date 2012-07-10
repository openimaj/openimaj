package org.openimaj.text.jaspell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
* CommonMisspellings is a simple bad word to good word lookup table.
*
* @see java.util.Map
* @author      Bruno Martins
*/
public final class CommonMisspellings {

	/** The <code>File</code> path leading up to this dictionary. */
	private String dictionaryFileName = null;

	/** The <code>File</code> path leading up to the dictionary of correct spellings. */
	private String dictionaryGoodFormsFileName = null;

	/** The lookup table for the dictionary, storing badTerm<->goodTerm relations. */
	private Map commonMisspellingIndex = null;

    /** A <code>BloomFilter</code> containing correctly spelled words. */
    private BloomFilter correctSpellings = null;

	/**
	 * Constructor for CommonMisspellings.
	 *
	 *@param dictionaryFileName  The <code>File</code> path leading up to this dictionary.
	 */
	public CommonMisspellings(String dictionaryFileName) throws Exception {
		this(dictionaryFileName,false);
	}
	

	/**
	 * Constructor for CommonMisspellings.
	 *
	 *@param dictionaryFileName  The <code>File</code> path leading up to this dictionary.
	 *@param dictionaryGoodFormsFileName  The <code>File</code> path leading up to the dictionary of correct spellings.
	 */
	public CommonMisspellings(String dictionaryFileName, String dictionaryGoodFormsFileName) throws Exception {
		this(dictionaryFileName,dictionaryGoodFormsFileName,false);
	}

	/**
	 * Constructor for CommonMisspellings.
	 *
	 *@param dictionaryFileName  The <code>File</code> path leading up to this dictionary.
	 *@param compression If true, the dictionary file is compressed with the GZIP algorithm, and if false, 
	 *                                  the file is a normal text document.
	 */
	public CommonMisspellings(String dictionaryFileName, boolean compression) throws Exception {
		this(dictionaryFileName,null,compression);
	}
	
	/**
	 * Constructor for CommonMisspellings.
	 *
	 *@param dictionaryFileName  The <code>File</code> path leading up to this dictionary.
	 *@param dictionaryGoodFormsFileName  The <code>File</code> path leading up to the dictionary of correct spellings.
     *@param compression If true, the dictionary file is compressed with the GZIP algorithm, and if false, 
     *                                  the file is a normal text document.
	 */
	public CommonMisspellings(String dictionaryFileName, String dictionaryGoodFormsFileName, boolean compression) throws Exception {
		this.dictionaryFileName = dictionaryFileName;
		this.dictionaryGoodFormsFileName = dictionaryGoodFormsFileName;
		this.commonMisspellingIndex = new HashMap();
		this.correctSpellings = new BloomFilter(100000);
		Reader aux;
		if(compression) aux = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dictionaryFileName))));
		else aux = new FileReader((new File(dictionaryFileName)));
		index(aux);
		if (dictionaryGoodFormsFileName!=null) {
			if(compression) aux = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dictionaryGoodFormsFileName))));
			else aux = new FileReader((new File(dictionaryGoodFormsFileName)));
			indexCorrectSpellings(aux);
		}
	}

	/**
	 * This method indexes the contents of what comes in from a <code>Reader</code>. 
	 * The input is expected to be in the form of badTerm : goodTerm and rows that start with # are ignored.
	 * 
	 * @param in The <code>Reader</code> from where to read.
	 */
	private void index(Reader in) throws Exception {
		String line = null;
		String badTerm = null;
		String goodTerm = null;
		int index;
		BufferedReader reader = new BufferedReader(in);
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if ((line.length() > 0) && ((index=line.indexOf(":")) != -1) && (line.charAt(0) != '#')) {
				badTerm = line.substring(0,index).trim();
				goodTerm = line.substring(index+1).trim();
				this.index(badTerm, goodTerm);
			}
		}
	} 
	
	/**
	 * This method indexes the contents of what comes in from a <code>Reader</code>. 
	 * The input is expected to be in the form of a single word per line and rows
	 *  that start with # are ignored.
	 * 
	 * @param in The <code>Reader</code> from where to read.
	 */
	private void indexCorrectSpellings(Reader in) throws Exception {
		String line = null;
		BufferedReader reader = new BufferedReader(in);
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if ((line.length() > 0) && (line.charAt(0) != '#')) {
				this.index("",line.trim());
			}
		}
	} 


	/**
	 * Index a given term.
	 * 
	 * @param pBadTerm The incorrect spelling for a term.
	 * @param pGoodTerm The correct spelling for a term.
 	 */
	private void index(String pBadTerm, String pGoodTerm) {
		String key = pBadTerm.trim().toLowerCase();
		String value = pGoodTerm.trim().toLowerCase();
		if(key.length()==0 || key.equals(value)) correctSpellings.put(value); else {
			List aux = (List)(commonMisspellingIndex.get(key));
			if(aux==null) aux = new Vector();
			aux.add(value);
			commonMisspellingIndex.put(pBadTerm,aux);
		}
	}

	/**
	 * Search the lookup table and return the correct spellings for a given misspelled word.
	 * 
	 * @param pTerm A misspelled word.
	 * @return An array with the list of correct spelling alternatives for the given word.
 	 */
	public String[] find(String pTerm) {
		String[] returnValue = null;
		String key = pTerm.trim().toLowerCase();
		if(correctSpellings.hasKey(key)) {
			returnValue = new String[1];
			returnValue[0] = pTerm;
			return returnValue;			
		}
		List aux = (List)(commonMisspellingIndex.get(key));
		if(aux!=null) {
			returnValue = new String[aux.size()];
			for(int i= 0; i<returnValue.length; i++) returnValue[i] = (String)(aux.get(i));		
		}
		return (returnValue);
	}

	/**
	 * Cleanup the lookup table. 
	 */
	public void cleanup() {
		commonMisspellingIndex = new HashMap();
	}

}