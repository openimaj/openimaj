package org.openimaj.text.jaspell;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This is a benchmarking class, usefull for measuring how well this package is doing.
 * 
 * @author      Bruno Martins
 */
public final class Benchmark {

	/** To where the benchmark statistics will be outputed. */
	private PrintWriter  outStream = null; 
	
	/** Numer of spelling corrections made where the correct word was the top suggestion. */
	private int topOne = 0;
	
	/** Numer of spelling corrections made where the correct word was in the top 5 suggestions. */
	private int topFive = 0;
	
	/** Numer of spelling corrections made where the correct word was in the top 10 suggestions. */
	private int topTen = 0;
	
	/** Numer of spelling corrections made where the correct word was in the top 100 suggestions. */
	private int topHundred = 0;
	
	/** Numer of tests in the benchmark. */
	private int count = 0;
	
	/** Number of missed spelling corrections. */
	private int missed = 0;
	
	/** A <code>List</code> of all spelling mistakes that were missed. */ 
	private List  missedTerms = null;
	
	/** The <code>File</code> path leading up to the dictionary used for the benchmarks. */
	private String dictionaryName = null;

  /**
   *  Constructor for Benchmark.
   *
   *@param dictionaryName  The <code>File</code> path leading up to the dictionary used for the benchmarks.
   */
  public Benchmark( String dictionaryName ) throws Exception {
	this.dictionaryName = dictionaryName;
	//this.outStream = new PrintWriter( new BufferedWriter(new FileWriter(dictionaryName)));
	this.outStream = new PrintWriter( new BufferedWriter(new PrintWriter(System.out)));
	this.missedTerms = new ArrayList();
  }

  /**
   * This method adds a "term : correct" term pair with its corresponding statistic to the benchmark. 
   * 
   * @param  pBadTerm The correctly spelled term.
   * @param  pCorrectTerm The spelling mistake.
   * @param  pCandidates The candidate corrections.
   * @return A <code>String</code> with the statistic concerning this benchmark.
   */
    public String add( String pBadTerm, String pCorrectTerm, String pCandidates[] )  { 
	    StringBuffer buff = new StringBuffer();
	    int whereFound = computeWhereSuggested( pCorrectTerm, pCandidates );
	    buff.append( pBadTerm + "|" + pCorrectTerm + "|" );
		if ( whereFound != -1 && whereFound <= 1 ) {
      		this.topOne++;
      		buff.append("In top 1|");
    	}
		if ( whereFound != -1 && whereFound <= 5 ) {
      		this.topFive++;
      		buff.append("In top 5|");
    	}
		if ( whereFound != -1 && whereFound <= 10 ) {
      		this.topTen++;
      		buff.append("In top 10|");
    	}
		if ( whereFound != -1 && whereFound <= 100 ) {
    	  this.topHundred++;
      	  buff.append("In top 100|");
    	} else {
      	  whereFound = -1 ;
    	}
	    if (whereFound == -1 ) {
    	  this.missed++;
      	  this.missedTerms.add( pBadTerm +" : " + pCorrectTerm );
      	  buff.append("Missed|");
    	}
	    this.count++;
   		return buff.toString();
   }

  /**
   * This method reports the statistics for this benchmark.
   * 
   * @return The statistics for this benchmark. 
   */
public String reportStats() throws Exception { 
    StringBuffer buff = new StringBuffer();
    float topOnePercentage      = ((float) topOne     / count) * 100;
    float topFivePercentage     = ((float) topFive    / count) * 100;
    float topTenPercentage      = ((float) topTen     / count) * 100;
    float topHundredPercentage  = ((float) topHundred / count) * 100;
    float missedPercentage      = ((float) missed     / count) * 100;
    buff.append( "-----------------  " + dictionaryName + "-----------------\n"); 
    buff.append("-------------------------+---------+-----------------\n");
    buff.append("                         |Count    | Percent of Total\n");
    buff.append("-------------------------+---------+-----------------\n" );
    buff.append("Total terms              | " +  count      + "|    * \n"); 
    buff.append("Total first choice       | " +  topOne     + "|" + topOnePercentage+"\n"); 
    buff.append("Total within top five    | " +  topFive    + "|" + topFivePercentage+"\n"); 
    buff.append("Total within top ten     | " +  topTen     + "|" + topTenPercentage+"\n"); 
    buff.append("Total within top hundred | " +  topHundred + "|" + topHundredPercentage+"\n"); 
    buff.append("Total missed             | " +  missed     + "|" + missedPercentage+"\n"); 
    buff.append("-------------------------+---------+-----------------\n" );
    outStream.println( buff.toString() );
    outStream.println( "---------------------------------------------------" ); 
    outStream.println( "         Terms missed altogether                   " );
    outStream.println( "---------------------------------------------------" ); 
    for ( int i = 0 ; i < missedTerms.size(); i++ ) {
      outStream.println( (String) missedTerms.get(i) );
    } 
    outStream.flush();
    outStream.close();
	return buff.toString();
   }

  /**
   * This method returns the position in the candidates 
   * list where the correct term was found. It compares the
   * correct term with the candidates in a case insensitive manner.
   * <p>
   * If the correct answer is not found, this method  returns -1. What is returned
   * is one based, not zero based. I.e., if the correct term was found
   * in the 0th element, this routine will return 1. 
   * 
   * @param pCorrectTerm The correct spelling correction.		
   * @param pCandidates A list of Possible corrections.
   * @return The position in the candidates list where the correct term was found.
   */
  private int  computeWhereSuggested( String pCorrectTerm, String pCandidates[] ) { 
     int returnValue = -1;
    String correctTerm = pCorrectTerm.toLowerCase() ;
    String aCandidate = null;
    if ( pCandidates != null ) {
      for ( int i = 0; i < pCandidates.length; i++ ) {
      	aCandidate = pCandidates[i].toLowerCase();
	    if( correctTerm.equals( aCandidate.toLowerCase() ) ) {
	  		returnValue = i + 1;
	  		break;
		}
      }
    }
    return( returnValue );
   }

  /**
   * This is a test main, whose purpose is to test the functionality 
   * of each method developed for this class.
   * <p>
   * This main strives to test the boundary conditions as well as
   * some sample common ways each method is intended to be
   * used.  
   *
   * @param argv 	The command line input, tokenized.
   */
  public static void main( String argv[] ) {
   }

}

