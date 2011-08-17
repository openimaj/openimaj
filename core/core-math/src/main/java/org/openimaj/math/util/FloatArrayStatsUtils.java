package org.openimaj.math.util;

/**
 * 
 * Some basic statistical operations on float arrays
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class FloatArrayStatsUtils {
	/**
	 * Find the mean of a single dimensional float array. returns 0 if the array is empty.
	 * @param arr
	 * @return
	 */
	public static float mean(float[] arr){
		if(arr.length == 0){
			return 0;
		}
		int count = 1;
		float mean = arr[0];
		for (int i = 1; i < arr.length; i++) {
			count++;
			mean = mean + (arr[i] - mean)/count;
		}
		return mean;
	}
	
	/**
	 * Calculate the mean of a two dimensional float array. returns 0 if the array is empty.
	 * @param arr
	 * @return
	 */
	public static float mean(float[][] arr){
		if(arr.length == 0){
			return 0;
		}
		int firstRowIndex = 0;
		while(arr[firstRowIndex].length == 0)firstRowIndex++;
		int firstColIndex = 1;
		
		int count = 1;
		float mean = arr[firstRowIndex][0];
		
		for (int i = firstRowIndex; i < arr.length; i++) {
			for(int j = firstColIndex; j < arr[i].length; j++){
				count++;
				mean = mean + (arr[i][j] - mean)/count;
			}
			firstColIndex = 0;
		}
		
		return mean;
	}
	
	/**
	 * Calculate the variance of a one dimensional float array. If the length of the array is less than 2, variance is 0.
	 * @param arr
	 * @return
	 */
	public static float var(float[] arr){
		if(arr.length < 2){
			return 0;
		}
		
		int count = 1;
		float oldMean = arr[0];
		float newMean = arr[0];
		float var = 0;
		
		for (int i = 1; i < arr.length; i++) {
			count ++;
			float x = arr[i];
			newMean = oldMean + (x - oldMean)/count;
			var = var + (x - oldMean) * (x - newMean);
			oldMean = newMean;
		}
		
		return var / (count - 1);
	}
	
	/**
	 * Calculate the variance of a one dimensional float array. If the length of the array is less than 2, variance is 0.
	 * @param arr
	 * @return
	 */
	public static float var(float[][] arr){
		if(arr.length == 0){
			return 0;
		}
		
		int firstRowIndex = 0;
		while(arr[firstRowIndex].length == 0)firstRowIndex++;
		int firstColIndex = 1;
		
		int count = 1;
		float oldMean = arr[firstRowIndex][0];
		float newMean = arr[firstRowIndex][0];
		float var = 0;
		
		for (int i = firstRowIndex; i < arr.length; i++) {
			for (int j = firstColIndex ; j < arr[i].length; j++) {
				count ++;
				float x = arr[i][j];
				newMean = oldMean + (x - oldMean)/count;
				var = var + (x - oldMean) * (x - newMean);
				oldMean = newMean;
			}
			firstColIndex = 0;
		}
		
		return count > 1 ? var / (count - 1) : 0;
	}
	
	/**
	 * Calculate the standard deviation of a 2D array. Calls {@link FloatArrayStatsUtils#var(float[][])} and does a Math.sqrt.
	 * @param arr
	 * @return
	 */
	public static float std(float[][] arr){
		return (float) Math.sqrt(var(arr));
	}
	
	/**
	 * Calculate the standard deviation of a 1D array. Calls {@link FloatArrayStatsUtils#var(float[])} and does a Math.sqrt.
	 * @param arr
	 * @return
	 */
	public static float std(float[] arr){
		return (float) Math.sqrt(var(arr));
	}
}
