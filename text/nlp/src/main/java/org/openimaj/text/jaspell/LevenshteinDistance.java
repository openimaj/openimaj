package org.openimaj.text.jaspell;

/**
 *  This classs holds the methods to compute a modified Levenshtein distance.
 * <p>
 * Levenshtein distance (LD) is a measure of the similarity between two <code>String</code> objects,
 * which we will refer to as the source string (s) and the target string (t). The distance is 
 * the number of deletions, insertions, or substitutions required to transform s into t. 
 * For example, 
 * <p>
 * If s is "test" and t is "test", then LD(s,t) = 0, because no transformations are needed and the strings are already identical.<br/> 
 * If s is "test" and t is "tent", then LD(s,t) = 1, because one substitution (change "s" to "n") is sufficient to transform s into t. <br/>
 * <p>
 * The greater the Levenshtein distance, the more different the strings are. The measure is 
 * named after the Russian scientist Vladimir Levenshtein, who devised the algorithm in 1965.
 * If you can't spell or pronounce Levenshtein, the metric is also sometimes called edit distance. 
 *
 * TODO: The spelling checker currently doesn't use this class. The code also needs to be optimized.
 * 
 */
public final class LevenshteinDistance {

	/** A reusable int matrix, used to compute the Levenshtein distance. */ 
	private int[][] matrix;
	
	/** The maximum used number of rows in the matrix. */
	private int maxN = -1;
	
	/** The maximum used number of columns in the matrix. */
	private int maxM = -1;

	/**
	 * Sole constructor for LevenshteinDistance. 
	 * 
	 */
	public LevenshteinDistance() {
		getMatrix(50, 50);
	}

	/**
	 * The Levenshtein distance for any given two <code>String</code> objects. 
	 *
	 * @param s Source <code>String</code>.
	 * @param t Target <code>String</code>.
	 * @return The Levenshtein distance between the two <code>String</code>objects. 
	 */
	public double levenshteinDistance(String s, String t) {
		return (levenshteinDistance(s.toCharArray(),t.toCharArray(),s.toLowerCase().toCharArray(),t.toLowerCase().toCharArray(),false))/1000.0;
	}

	/**
	 * A modified Levenshtein distance for any given two <code>String</code> objects. 
	 * It is noted whether there is any case difference, an extra .5 is added to the distance.
	 * This slighly penalizes for case differences, rather than giving a severe penalization
	 * when each character case difference is counted as a full transformation.
	 *
	 * @param s Source <code>String</code>.
	 * @param t Target <code>String</code>.
	 * @return The modified Levenshtein distance between the two <code>String</code> objects. 
	 */
	public double modifiedLevenshteinDistance(String s, String t) {
		return (levenshteinDistance(s.toCharArray(),t.toCharArray(),s.toLowerCase().toCharArray(),t.toLowerCase().toCharArray(),true))/1000.0;
	}

	/**
	 * This method returns a modified Levenshtein distance for any given two <code>String</code> objects. 
	 * It is noted whether there is any case difference, an extra .5 is added to the distance. This
	 * slighly penalizes for case differences, rather than giving a severe penalization
	 * when each character case difference is counted as a full transformation.
	 * <p>
	 * The char arrays are being passed in to speed things up. Also,
	 * the lowercase char arrays are being passed in to speed things up.
	 * It was observed that in a typical invocation of this method, 
	 * this method gets called over and over again, with one of the
	 * strings being held constant, and one of them changing. By being
	 * able to pass in the char arrays, I can convert the string 
	 * that remains constant only once to a char array, and lowercase
	 * it only once. I can do array indexing rather than method invocation
	 * which should speed things up a bit as well.
	 * <p>
	 * To futher the efficiencies, this method returns an int of the
	 * distance that is the distance multiplied by 1000. 
	 * Thus, an edit distance of 2  multiplied by  1000 = 2000;
	 *
	 * @param s Source char array
	 * @param t Target char array
	 * @param lowercaseS LowerCase version of the source char array
	 * @param lowercaseT LowerCase version of the target char array
	 * @param useCaseDifference  If true, than case differences result in an extra .5 added to the distance
	 * @return The modified Levenshtein distance multiplied by 1000.
	 */
	public int levenshteinDistance(
		char[] s,
		char[] t,
		char[] lowercaseS,
		char[] lowercaseT,
		boolean useCaseDifference) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		char u_s_i; // ith character of s (lowercased)
		char u_t_j; // jth character of t (lowercased)
		int cost; // cost
		boolean caseDifference = false;
		int returnValue = 0;
		n = s.length;
		m = t.length;
		if (n == 0)	return m;
		if (m == 0) return n;
		d = getMatrix(n + 1, m + 1);
		for (i = 0; i <= n; i++)	d[i][0] = i;
		for (j = 0; j <= m; j++) d[0][j] = j;
		boolean not_finished = true;
		for (i = 1; not_finished == true && i <= n; i++) {
			s_i = s[i - 1];
			u_s_i = lowercaseS[i - 1];
			for (j = 1; not_finished == true && j <= m; j++) {
				t_j = t[j - 1];
				u_t_j = lowercaseT[j - 1];
				if (u_s_i == u_t_j) {
					cost = 0;
					if ((s_i != t_j) && (i == j)) {
						caseDifference = true;
					}
				} else
					cost = 1;
				d[i][j] =	minimum(d[i - 1][j] + 1,d[i][j - 1] + 1,d[i - 1][j - 1] + cost);
			}
		}
		returnValue = d[n][m] * 1000;
		if (caseDifference && useCaseDifference)	returnValue = returnValue + 500;
		return (returnValue);
	}

   /**
	 * Return the minimum of three values. 
	 *
	 * @param a Value 1		
	 * @param b Value 2
	 * @param c Value 3
	 * @return Return the minimum between the three values.
	 *
	 */
	private static int minimum(int a, int b, int c) {
		int mi;
		mi = a;
		if (b < mi) mi = b;
     	if (c < mi) mi = c;
		return mi;

	}

	/**
	 * Reuses the same matrix, making sure that there is enough room in it.
	 *
	 * @param n  The number of rows.
	 * @param m  The number of columns. 
	 * @return int[][] A matrix.
	 */
	private int[][] getMatrix(int n, int m) {
		boolean rebuild = false;
		if (n > this.maxN) {
			this.maxN = n + 10;
			rebuild = true;
		}
		if (m > this.maxM) {
			this.maxM = m + 10;
			rebuild = true;
		}
		if (rebuild == true) this.matrix = new int[maxN][maxM];
		return (this.matrix);
	}

}
