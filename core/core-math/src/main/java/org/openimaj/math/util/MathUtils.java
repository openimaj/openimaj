package org.openimaj.math.util;

/**
 * A collection of maths functions not available anywhere else ive seen
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class MathUtils {

	/**
	 * Given log(a) and log(b) calculate log(a + b) boils down to log(
	 * exp(log_a) + exp(log_b) ) but this might overflow, so we turn this into
	 * log([exp(log_a - log_c) + exp(log_b - log_c)]exp(log_c)) and we set log_c
	 * == max(log_a,log_b) and so it becomes: LARGE + log(1 + exp(SMALL -
	 * LARGE)) == LARGE + log(1 + SMALL) ~= large the whole idea being to avoid
	 * an overflow (exp(LARGE) == VERY LARGE == overflow)
	 * 
	 * @param log_a
	 * @param log_b
	 * @return log(a+b)
	 */
	public static double logSum(final double log_a, final double log_b) {
		double v;

		if (log_a < log_b) {
			v = log_b + Math.log(1 + Math.exp(log_a - log_b));
		} else {
			v = log_a + Math.log(1 + Math.exp(log_b - log_a));
		}
		return (v);
	}

	/**
	 * Returns the next power of 2 superior to n.
	 * 
	 * @param n
	 *            The value to find the next power of 2 above
	 * @return The next power of 2
	 */
	public static int nextPowerOf2(final int n) {
		return (int) Math.pow(2, 32 - Integer.numberOfLeadingZeros(n - 1));
	}

	/**
	 * Implementation of the C <code>frexp</code> function to break
	 * floating-point number into normalized fraction and power of 2.
	 * 
	 * @see "http://stackoverflow.com/questions/1552738/is-there-a-java-equivalent-of-frexp"
	 * 
	 * @param value
	 *            the value
	 * @return the exponent and mantissa of the input value
	 */
	public static ExponentAndMantissa frexp(double value) {
		final ExponentAndMantissa ret = new ExponentAndMantissa();

		ret.exponent = 0;
		ret.mantissa = 0;

		if (value == 0.0 || value == -0.0) {
			return ret;
		}

		if (Double.isNaN(value)) {
			ret.mantissa = Double.NaN;
			ret.exponent = -1;
			return ret;
		}

		if (Double.isInfinite(value)) {
			ret.mantissa = value;
			ret.exponent = -1;
			return ret;
		}

		ret.mantissa = value;
		ret.exponent = 0;
		int sign = 1;

		if (ret.mantissa < 0f) {
			sign--;
			ret.mantissa = -(ret.mantissa);
		}
		while (ret.mantissa < 0.5f) {
			ret.mantissa *= 2.0f;
			ret.exponent -= 1;
		}
		while (ret.mantissa >= 1.0f) {
			ret.mantissa *= 0.5f;
			ret.exponent++;
		}
		ret.mantissa *= sign;
		return ret;
	}

	/**
	 * Class to hold an exponent and mantissa
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class ExponentAndMantissa {
		/**
		 * The exponent
		 */
		public int exponent;

		/**
		 * The mantissa
		 */
		public double mantissa;
	}
}
