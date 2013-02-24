package org.openimaj.math.util;

/**
 * A collection of maths functions not available anywhere else ive seen
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class MathUtils {

	/**
	 * Given log(a) and log(b) calculate log(a + b)	 
	 * boils down to 
	 * 		log( exp(log_a) + exp(log_b) ) 
	 * but this might overflow, so we turn this into
	 * 		log([exp(log_a - log_c) + exp(log_b - log_c)]exp(log_c))
	 * 		and we set log_c == max(log_a,log_b)
	 * 		and so it becomes: LARGE + log(1 + exp(SMALL - LARGE)) == LARGE + log(1 + SMALL) ~= large 
	 * the whole idea being to avoid an overflow (exp(LARGE) == VERY LARGE == overflow)
	 * @param log_a
	 * @param log_b
	 * @return log(a+b)
	 */
	public static double logSum(double log_a, double log_b) {
		double v;

        if (log_a < log_b) {
            v = log_b + Math.log(1 + Math.exp(log_a - log_b));
        } else {
            v = log_a + Math.log(1 + Math.exp(log_b - log_a));
        }
        return (v);
	}
}
