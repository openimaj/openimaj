package ch.akuhn.matrix;

/**
 * Utilities
 * 
 * @author Adrian Kuhn
 */
public class Util {

	/**
	 * Max
	 * 
	 * @param ds
	 * @param max
	 * @return the max
	 */
	public static double max(double[] ds, double max) {
		for (int n = 0; n < ds.length; n++)
			max = Math.max(max, ds[n]);
		return max;
	}

	/**
	 * Max
	 * 
	 * @param dss
	 * @param max
	 * @return the max
	 */
	public static double max(double[][] dss, double max) {
		for (final double[] ds : dss)
			max = max(ds, max);
		return max;
	}

	/**
	 * Min
	 * 
	 * @param ds
	 * @param min
	 * @return the min
	 */
	public static double min(double[] ds, double min) {
		for (int n = 0; n < ds.length; n++)
			min = Math.min(min, ds[n]);
		return min;
	}

	/**
	 * Min
	 * 
	 * @param dss
	 * @param min
	 * @return the min
	 */
	public static double min(double[][] dss, double min) {
		for (final double[] ds : dss)
			min = max(ds, min);
		return min;
	}

	/**
	 * Sum
	 * 
	 * @param dss
	 * @return the sum
	 */
	public static double sum(double[][] dss) {
		double sum = 0;
		for (final double[] ds : dss)
			sum += sum(ds);
		return sum;
	}

	/**
	 * Sum
	 * 
	 * @param ds
	 * @return the sum
	 */
	public static double sum(double[] ds) {
		double sum = 0;
		for (int n = 0; n < ds.length; n++)
			sum += ds[n];
		return sum;
	}

	/**
	 * Count
	 * 
	 * @param dss
	 * @return the sum of the row lengths
	 */
	public static int count(double[][] dss) {
		int length = 0;
		for (final double[] ds : dss)
			length += ds.length;
		return length;
	}

	/**
	 * Get the histogram
	 * 
	 * @param values
	 * @param binCount
	 * @return the histogram
	 */
	public static int[] getHistogram(double[][] values, int binCount) {
		double max = Double.MIN_VALUE;
		for (final double[] row : values) {
			for (final double each : row) {
				max = Math.max(max, each);
			}
		}
		max = 10; // FIXME
		final int[] bins = new int[binCount];
		for (final double[] row : values) {
			for (final double each : row) {
				final int index = (int) Math.floor(each / max * (binCount - 1));
				bins[Math.min(binCount - 1, index)]++;
			}
		}
		return bins;
	}

	/**
	 * Multiply by constant
	 * 
	 * @param dss
	 * @param d
	 */
	public static void times(double[][] dss, double d) {
		for (final double[] ds : dss)
			for (int i = 0; i < ds.length; i++)
				ds[i] *= d;
	}

}
