package ch.akuhn.matrix;

public class Util {

	public static double max(double[] ds, double max) {
		for (int n = 0; n < ds.length; n++) max = Math.max(max, ds[n]);
		return max;
	}
	
	public static double max(double[][] dss, double max) {
		for (double[] ds: dss) max = max(ds, max);
		return max;
	}
	
	public static double min(double[] ds, double min) {
		for (int n = 0; n < ds.length; n++) min = Math.min(min, ds[n]);
		return min;
	}
	
	public static double min(double[][] dss, double min) {
		for (double[] ds: dss) min = max(ds, min);
		return min;
	}
	
	public static double sum(double[][] dss) {
		double sum = 0;
		for (double[] ds: dss) sum += sum(ds);
		return sum;
	}
	
	public static double sum(double[] ds) {
		double sum = 0;
		for (int n = 0; n < ds.length; n++) sum += ds[n];
		return sum;
	}
	
	public static int count(double[][] dss) {
		int length = 0;
		for (double[] ds: dss) length += ds.length;
		return length;
	}
	
    public static int[] getHistogram(double[][] values, int binCount) {
        double max = Double.MIN_VALUE;
        for (double[] row: values) {
            for (double each: row) {
                max = Math.max(max, each);
            }
        }
        max = 10; // FIXME
        int[] bins = new int[binCount];
        for (double[] row: values) {
            for (double each: row) {
                int index = (int) Math.floor(each / max * (binCount - 1));
                bins[Math.min(binCount - 1, index)]++;
            }
        }
        return bins;
    }

	public static void times(double[][] dss, double d) {
		for (double[] ds: dss) for (int i = 0; i < ds.length; i++) ds[i] *= d;
	}
	
}
