/**
 * 
 */
package org.openimaj.image.annotation.evaluation.agreement;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openimaj.ml.annotation.ScoredAnnotation;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 16 Aug 2013
 *	@version $Author$, $Revision$, $Date$
 */
public class FleissAgreementTest
{
	/** Each rater (14), 10 subjects */
	public int[][] data = new int[][]
	{
			{5,2,3,2,1,1,1,1,1,2}, // 1
			{5,2,3,2,1,1,1,1,1,2}, // 2
			{5,3,3,2,2,1,1,2,1,3}, // 3
			{5,3,4,3,2,1,2,2,1,3}, // 4
			{5,3,4,3,3,1,2,2,1,4}, // 5
			{5,3,4,3,3,1,3,2,1,4}, // 6
			{5,3,4,3,3,1,3,2,2,4}, // 7
			{5,3,4,3,3,2,3,3,2,5}, // 8
			{5,4,5,3,3,2,3,3,2,5}, // 9
			{5,4,5,3,3,2,3,3,2,5}, // 10
			{5,4,5,3,3,2,3,4,2,5}, // 11
			{5,4,5,3,3,2,4,4,3,5}, // 12
			{5,5,5,4,4,2,4,5,3,5}, // 13
			{5,5,5,4,5,2,4,5,4,5}  // 14
	};
	
	/** The counts directly off Wikipedia */
	public int[][] dataTest = new int[][]
	{
			{0,0,0,0,14},
			{0,2,6,4,2},
			{0,0,3,5,6},
			{0,3,9,2,0},
			{2,2,8,1,1},
			{7,7,0,0,0},
			{3,2,6,3,0},
			{2,5,3,2,2},
			{6,5,2,1,0},
			{0,2,2,3,7}
	};
	
	/**
	 * 	This is a test to ensure we've typed in the data
	 * 	correctly, prior to testing the actual Fleiss value
	 */
	@Test
	public void testData()
	{
		int[][] countTable = new int[10][5];
		for( int r = 0; r < 14; r++ )
			for( int s = 0; s < 10; s++ )
				countTable[s][data[r][s]-1]++;
		
		// If it fails, uncomment these lines to see why:
		// System.out.println( Arrays.deepToString( dataTest ) );
		// System.out.println( Arrays.deepToString( countTable ) );
		
		Assert.assertArrayEquals( dataTest, countTable );
	}
	
	/**
	 *	
	 */
	@Test
	public void test()
	{
		List<Map<Integer,ScoredAnnotation<Integer>>> annotations =
				new ArrayList<Map<Integer,ScoredAnnotation<Integer>>>();
		
		// 14 raters
		for( int r = 0; r < 14; r++ )
		{
			Map<Integer,ScoredAnnotation<Integer>> raterAnswers = new
					HashMap<Integer, ScoredAnnotation<Integer>>();
			
			// 10 subjects
			for( int s = 0; s < 10; s++ )
				raterAnswers.put( s, new ScoredAnnotation<Integer>( data[r][s], 1 ) );
			
			annotations.add( raterAnswers );
		}
		
		double ira = FleissInterraterAgreement.calculate( annotations );
		Assert.assertEquals( 0.210, ira, 0.01 );
	}
}
