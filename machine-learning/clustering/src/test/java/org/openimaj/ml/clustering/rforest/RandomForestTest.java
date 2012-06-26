/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.ml.clustering.rforest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.data.RandomData;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.rforest.IntRandomForest.Letter;
import org.openimaj.ml.clustering.rforest.IntRandomForest.Word;

/**
 * 
 * Test the IO and functionality of RandomForest clusters
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RandomForestTest {
	/**
	 * Temporary directory for IO tests
	 */
	@Rule
    public TemporaryFolder folder = new TemporaryFolder();
	
	int [][] structuredData;
	private int[][] comboSet;
	private int[][] dataSourceOne;
	private int[][] dataSourceTwo;
	
	int randomSeed = 120;
	int bottomSmall = 0;
	int topSmall = 3;
	int bottomBig = 8;
	int topBig = 11;
	/**
	 * Generate some structured data
	 * @throws IOException
	 */
	@Before public void setup() throws IOException {
		dataSourceOne = RandomData.getRandomIntArray(10,100, bottomSmall, topSmall,randomSeed);
		dataSourceTwo = RandomData.getRandomIntArray(10,100, bottomBig, topBig,randomSeed);
		
		comboSet = RandomData.getRandomIntArray(100,100, bottomBig, topSmall,randomSeed);
		
		
	}
	
	/**
	 * Test the IO of the random forest
	 * 
	 * @throws IOException
	 */
	public void testRandomForestFileAccess() throws IOException{
		int dim = 5;
		int [][] data = RandomData.getRandomIntArray(10, dim, 0, 255,randomSeed);
		IntRandomForest rdf = new IntRandomForest(10,10);
		rdf.setRandomSeed(randomSeed);
		rdf.cluster(data);
		
		//ascii
		File file = folder.newFile("rdf.ascii");
		IOUtils.writeASCII(file, rdf);
		
		//ascii using readASCII
		IntRandomForest rdf2 =  IOUtils.read(file, new IntRandomForest());
		
		assertTrue(rdf2.equals(rdf));
		//binary
		rdf = new IntRandomForest(10,10);
		rdf.setRandomSeed(randomSeed);
		rdf.cluster(data);
		
		File fileB = folder.newFile("rdf.bin");
		IOUtils.writeBinary(fileB, rdf);
		
		IntRandomForest rdf2B = new IntRandomForest();
		IOUtils.read(fileB, rdf2B);
		
		assertTrue(rdf2B.equals(rdf));
		
		// Test pushing and saving and loading
		for (int i=0; i<data.length; i++)
		{
			int p1 = rdf.assign(data[i]);
			int p2 = rdf2B.assign(data[i]);
			assertEquals(p1, p2);
		}
		
		IOUtils.writeBinary(fileB, rdf);
		rdf2B = new IntRandomForest();
		IOUtils.read(fileB, rdf2B);
		assertTrue(rdf2B.equals(rdf));
		
		file.delete();
		fileB.delete();
	}
	
	/**
	 * Test the functionality (training and pushing) of the random forest
	 * @throws IOException
	 */
	@Test public void testRandomForest() throws IOException  {
//		testRandomForestFileAccess();
		
		IntRandomForest rdf = new IntRandomForest(1,5);
		rdf.setRandomSeed(randomSeed);
		rdf.cluster(comboSet);
		
		int[] clusterSourceOne = rdf.assign(dataSourceOne);
		int[] clusterSourceTwo = rdf.assign(dataSourceTwo);
		
		int[][] newSourceOneDocument = RandomData.getRandomIntArray(10, 100, bottomSmall, topSmall,randomSeed+1);
		int[][] newSourceTwoDocument = RandomData.getRandomIntArray(10, 100, bottomBig, topBig,randomSeed+1);
		
		int[] newClusterSourceOne = rdf.assign(newSourceOneDocument);
		int[] newClusterSourceTwo = rdf.assign(newSourceTwoDocument);
		
		double scoreOneVsOne = scoreSharedTerms(newClusterSourceOne,clusterSourceOne);
		double scoreOneVsTwo = scoreSharedTerms(newClusterSourceOne,clusterSourceTwo);
		
		double scoreTwoVsOne = scoreSharedTerms(newClusterSourceTwo,clusterSourceOne);
		double scoreTwoVsTwo = scoreSharedTerms(newClusterSourceTwo,clusterSourceTwo);
		
		assertTrue(scoreOneVsOne > scoreOneVsTwo);
		assertTrue(scoreTwoVsTwo > scoreTwoVsOne);
	}


	private double scoreSharedTerms(int[] one, int[] two) {
		Map<Integer,Double> termFreqOne = termFreq(one);
		Map<Integer,Double> termFreqTwo = termFreq(two);
		
		Set<Integer> sharedKeys = termFreqOne.keySet();
		sharedKeys.retainAll(termFreqTwo.keySet());
		double score = 0;
		for(int key : sharedKeys){
			double qtf = Math.abs(termFreqOne.get(key));
			double dtf = Math.abs(termFreqTwo.get(key));
			score += -1 * (Math.abs(qtf - dtf) - qtf - dtf);
		}
		return score;
	}


	private Map<Integer, Double> termFreq(int[] two) {
		Map<Integer,Double> out = new HashMap<Integer,Double>();
		for(int i : two){
			if(!out.containsKey(i))
				out.put(i, 0d);
			out.put(i, out.get(i)+1);
		}
		for(int i : out.keySet()){
			out.put(i, out.get(i) / two.length);
		}
		return out;
	}
	
	/**
	 * Test the hashcode of letters and words
	 */
	@Test public void testLetterHash(){
		IntRandomForest r = new IntRandomForest();
		r.setRandomSeed(randomSeed);
		Letter l1 = r.newLetter(new boolean[]{true,false,false},1);
		Letter l2 = r.newLetter(new boolean[]{true,false,false},2);
		Letter l3 = r.newLetter(new boolean[]{false,false,true},1);
		
		assertTrue(l1.hashCode()!=l2.hashCode());
		assertTrue(l2.hashCode()!=l3.hashCode());
		assertTrue(l1.hashCode()!=l3.hashCode());
		
		Word w1 = r.newWord(new Letter[]{l1,l2,l3});
		Word w2 = r.newWord(new Letter[]{l3,l2,l1});
		
		assertTrue(w1.hashCode()!=w2.hashCode());
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException{
		int maxi = 10000;
		for(int i = 0; i < maxi; i++){
			RandomForestTest test = new RandomForestTest();
			test.randomSeed = i;
			test.setup();
			try{
				test.testRandomForest();
			}
			catch(AssertionError err){
				System.out.println("Error when seed is: " + i);
				break;
			}
			
		}
	}
}
