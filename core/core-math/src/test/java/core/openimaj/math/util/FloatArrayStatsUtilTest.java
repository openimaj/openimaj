package core.openimaj.math.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.math.util.FloatArrayStatsUtils;

public class FloatArrayStatsUtilTest {
	@Test
	public void testMean(){
		float[] arr1D = {1,2,3,4};
		float[][] arr2D = {{1,2},{3,4}};
		
		assertTrue(FloatArrayStatsUtils.mean(arr1D) == 2.5);
		assertTrue(FloatArrayStatsUtils.mean(arr2D) == 2.5);
		assertTrue(FloatArrayStatsUtils.mean(new float[0]) == 0);
		assertTrue(FloatArrayStatsUtils.mean(new float[]{1}) == 1);
	}
	
	@Test
	public void testStd(){
		float[] arr1D = {1,2,4,5,6};
		float[][] arr2D = {{1,2,4},{5,6}};
		
		System.out.println(FloatArrayStatsUtils.var(arr2D));
		
		assertTrue(FloatArrayStatsUtils.var(arr1D) == 4.3f);
		assertTrue(FloatArrayStatsUtils.var(arr2D) == 4.3f);
		assertTrue(FloatArrayStatsUtils.var(new float[0]) == 0);
		assertTrue(FloatArrayStatsUtils.var(new float[]{1}) == 0);
		assertTrue(FloatArrayStatsUtils.var(new float[]{1,2}) == 0.5);
	}
}
