package org.openimaj.text.jaspell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;

/**
 * Implementation of a Bloom Filter data structure, an elegant alternative to the lookup
 * hash table.</p> <p>
 * 
 * Bloom filters allow you to perform membership tests in just a fraction of the memory 
 * you'd need to store a full list of keys. As you might suspect, the savings in space 
 * comes at a price: you run an adjustable risk of false positives, and you can't remove a
 * key from a filter once you've added it in. But in the many cases where those constraints
 * are acceptable, a Bloom filter can make a useful tool.</p> <p>
 * 
 * Bloom filters are named after Burton Bloom, who first described them in a 1970 paper entitled
 * <a href="http://portal.acm.org/citation.cfm?id=362692&dl=ACM&coll=portal">Space/time
 * trade-offs in hash coding with allowable errors</a>. In those days of limited memory, Bloom
 * filters were prized primarily for their compactness; in fact, one of their earliest applications
 *  was in spell checkers.</p> <p>
 * 
 * A Bloom filter consists of two components: a set of <code>k</code> hash functions and a bit vector of
 * a given length. We choose the length of the bit vector and the number of hash functions
 * depending on how many keys we want to add to the set and how high an error rate we are
 * willing to put up with. </p> <p>
 * 
 * All of the hash functions in a Bloom filter are configured so that their range matches the
 * length of the bit vector. For example, if a vector is 200 bits long, the hash functions return
 * a value between 1 and 200. It's important to use high-quality hash functions in the filter to
 * guarantee that output is equally distributed over all possible values -- "hot spots" in a hash
 * function would increase our false-positive rate.</p> <p>
 * 
 * To enter a key into a Bloom filter, we run it through each one of the k hash functions
 * and treat the result as an offset into the bit vector, turning on whatever bit we find at that
 * position. If the bit is already set, we leave it on. There's no mechanism for turning bits off
 * in a Bloom filter.</p> <p>
 * 
 * Checking to see whether a key already exists in a filter is exactly analogous to adding a
 * new key. We run the key through our set of hash functions, and then check to see whether
 * the bits at those offsets are all turned on. If any of the bits is off, we know for certain the
 * key is not in the filter. If all of the bits are on, we know the key is probably there.</p> <p>
 * 
 * As you might expect, the false-positive rate depends on the bit vector length and the number
 * of keys stored in the filter. The roomier the bit vector, the smaller the probability that all k bits
 * we check will be on, unless the key actually exists in the filter. The relationship between the
 * number of hash functions and the false-positive rate is more subtle. If you use too few hash
 * functions, there won't be enough discrimination between keys; but if you use too many, the
 * filter will be very dense, increasing the probability of collisions. You can calculate the
 * false-positive rate for any filter using the formula:</p> <p>
 * 
 * <code>c = ( 1 - e(-kn/m) )k</code></p> <p>
 *
 * Where c is the false positive rate, k is the number of hash functions, n is the number of
 * keys in the filter, and m is the length of the filter in bits.</p> <p>
 *
 * When using Bloom filters, we very frequently have a desired false-positive rate in mind and
 * we are also likely to have a rough idea of how many keys we want to add to the filter. We
 * need some way of finding out how large a bit vector is to make sure the false-positive rate
 * never exceeds our limit. The following equation will give us vector length from the error rate
 * and number of keys:</p> <p>
 * 
 *<code>m = -kn / ( ln( 1 - c ^ 1/k ) )</code></p> <p>
 *
 * You'll notice another free variable here: k, the number of hash functions. However, it's
 * possible to use calculus to find a minimum for k. You can also find lookup tables for 
 * various combinations of error rate, filter size, and number of hash functions at 
 * <a href="http://www.cs.wisc.edu/~cao/papers/summary-cache/node8.html#tab:bf-config-1">Bloom Filters -- the math</a>.</p> <p>
 *  
 * This implementation uses the <code>hashCode()</code> method supplied for all Java objects, which
 * produces a 32-bit signed int number. For example, in <code>String</code> Objects, the hashcode is usually
 * computed by adding up the character values with an prime multiplier (31, in the case of JDK 1.4).</p> <p> 
 *
 * Alternatively, this class can also use an implementation of a hash function based on Rabin
 * fingerprints, which can efficiently produce a 32-bit hash value for a sequence of bytes.
 * It does so by considering strings of bytes as large polynomials with coefficients of 0 and 1
 * and then reducing them modulo some irreducible polynomial of degree 32. The result is a hash
 * function with very satisfactory properties. In addition the polynomial operations are fast in
 * hardware, and even in this Java implementation the speed is reasonable.</p> <p>
 *
 * The implementation is derived from the paper "Some applications of Rabin's fingerprinting
 * method" by Andrei Broder. See <a href="http://server3.pa-x.dec.com/SRC/publications/src-papers.html">
 * http://server3.pa-x.dec.com/SRC/publications/src-papers.html</a> for a full citation and the
 * paper in PDF format.</p> <p>
 *
 * Included in this class are additional methods that can compute the Rabin hash value
 * for any serializable <code>Object</code>, <code>String</code>, <code>File</code>, or resource denoted by <code>URL</code>.</p> <p>
 *
 * As for the multiple hash functions for the Bloom Filter, these are based on the module of the
 * initial value multiplied by a list of distinct values.
 * 
 * @see java.lang.Object#hashCode()
 * @see java.util.Map
 *
 * @author      Bruno Martins
 */
public class BloomFilter implements Cloneable {

	/** A buffer for the Rabin fingerprinting algorithm. */
	private byte[] buffer;
	
	/**
	 *  The 32 bits of this integer represent the coefficients of the degree 32
	 *  irreducible polynomial over GF(2); that is, every coefficient is 0 or 1. However, a
	 *  degree 32 polynomial has 33 coefficients; the term of degree 32 is
	 *  assumed to have a coefficient of 1. Therefore, the high-order bit of the
	 *  <code>int</code> is the degree 31 term's coefficient, and the low-order
	 *  bit is the constant coefficient.</p> <p>
	 *
	 *  For example the integer 0x00000803, in binary, is:</p> <p>
	 *
	 *  <code>00000000 00000000 00001000 00000011</code></p> <p>
	 *
	 *  Therefore it correponds to the polynomial:</p> <p>
	 *
	 *  <code>x<sup>32</sup> + x<sup>11</sup> + x + 1</code>
	 */
	private static int POLYNOMIAL = 0x000001C7;

	/** Internal values for the Rabin fingerprinting algorithm. */
	private static int[] table32, table40, table48, table54;
	
	/** The degree for the irreducible polynomial used by the Rabin fingerprinting algorithm. */
	private static int P_DEGREE = 32;
	
	/** The size of the buffer for the Rabin fingerprinting algorithm. */
	private static int READ_BUFFER_SIZE = 2048;
	
	/** The degree for the irreducible polynomial used by the Rabin fingerprinting algorithm. */
	private static int X_P_DEGREE = 1 << (P_DEGREE - 1);

	/** The bit vector for the Bloom Filter. */
	private boolean keys[];

	/** Use Rabin's fingerprinting algorithm ( default is true ). */
	private boolean useRabin = true;
	
	/** The number of hash functions. */
	private int numFunctions;

	/**
	 * Constructs an empty BloomFilter with the default number of hash functions (10)
	 * and the default length for the bit vector (1000).
	 */
	public BloomFilter() {
		table32 = new int[256];
		table40 = new int[256];
		table48 = new int[256];
		table54 = new int[256];
		buffer = new byte[READ_BUFFER_SIZE];
		// We want to have mods[i] == x^(P_DEGREE+i)
		int[] mods = new int[P_DEGREE];
		mods[0] = POLYNOMIAL;
		for (int i = 1; i < P_DEGREE; i++) {
			// x^i == x(x^(i-1)) (mod P)
			mods[i] = mods[i - 1] << 1;
			// if x^(i-1) had a x_(P_DEGREE-1) term then x^i has a
			// x^P_DEGREE term that 'fell off' the top end.
			// Since x^P_DEGREE == P (mod P), we should add P
			// to account for this:
			if ((mods[i - 1] & X_P_DEGREE) != 0) {
				mods[i] ^= POLYNOMIAL;
			}
		}
		for (int i = 0; i < 256; i++) {
			int c = i;
			for (int j = 0; j < 8 && c != 0; j++) {
				if ((c & 1) != 0) {
					table32[i] ^= mods[j];
					table40[i] ^= mods[j + 8];
					table48[i] ^= mods[j + 16];
					table54[i] ^= mods[j + 24];
				}
				c >>>= 1;
			}
		}
		mods = null;
		this.keys = new boolean[1000];
		this.numFunctions = 10;
		for (int i = 0; i < 1000; i++)	this.keys[i] = false;
	}

	/**
	 * Constructs a Bloom Filter from a string representation.
	 *
	 * @see #toString()
	 */
	public BloomFilter(String filter) {
		this();
		int index1 = filter.indexOf(":");
		int index2 = filter.lastIndexOf(":");
		numFunctions = new Integer(filter.substring(0, index1)).intValue();
		keys =
			new boolean[new Integer(filter.substring(index1, index2))
				.intValue()];
		for (int i = index2 + 1; i < filter.length(); i++) {
			if (filter.charAt(i) == '1')
				keys[i] = true;
			else
				keys[i] = false;
		}
	}

	/**
	 * Constructs an empty BloomFilter with a given length for the bit vector,
	 * guarenteeing a maximum error rate.  
	 *
	 *@param  errorRate           The maximum error rate (false positives) for the Bloom Filter.
	 */
	public BloomFilter(int numKeys, double errorRate) {
		this();
		double lowest_m = Double.MAX_VALUE;
		int best_k = 1;
		for (int k = 1; k <= 100; k++) {
			double m =
				(-1 * k * numKeys)
					/ (Math.log(1 - (Math.pow(errorRate, (1 / k)))));
			if (m < lowest_m) {
				lowest_m = m;
				best_k = k;
			}
		}
		this.keys = new boolean[numKeys];
		this.numFunctions = best_k;
		for (int i = 0; i < numKeys; i++)
			this.keys[i] = false;
	}

	/**
	 * Constructs an empty BloomFilter with the default number of hash functions (10)
	 * and a given length for the bit vector.
	 *
	 *@param  numKeys           The length of the bit vector.
	 */
	public BloomFilter(int numKeys) {
		this(numKeys, 10);
	}

	/**
	 * Constructs an empty BloomFilter with a given number of hash functions
	 * and a given length for the bit vector.
	 *
	 *@param  numKeys                The length of the bit vector.
	 *@param  numHashFunctions  The number of hash functions.
	 */
	public BloomFilter(int numKeys, int numHashFunctions) {
		this();
		this.keys = new boolean[numKeys];
		this.numFunctions = numHashFunctions;
		for (int i = 0; i < numKeys; i++)
			this.keys[i] = false;
	}

	/**
	 *  Internal method for producing the hash value for a given function number.
	 *
	 * @param  fnum      The number of the hash function.
	 * @param  original   The original value for the hash of the object. 
	 * @see java.lang.Object#hashCode() 
	 * @return   Returns the hash code value for the given function number.
	 */
	private int getHash(int fnum, int original) {
		//int hash = ((int)(Math.pow(2,fnum)) * original) % keys.length;
		//int hash = ((fnum * fnum + 1) * original) % keys.length;
		int hash[] = { original };
		if(!useRabin) for (int i=0; i<fnum; i++) hash[0] = (new Integer(hash[0])).hashCode();
		else for (int i=0; i<fnum; i++) hash[0] = hashRabin(hash);
		hash[0] = hash[0] % keys.length;
		if (hash[0] < 0) hash[0] = -hash[0];
		return hash[0];
	}

	/**
	 *  Returns true if this Bloom Filter contains the specified key.
	 *
	 *@param  obj   The key whose presence in this Bloom Filter is to be tested.
	 *@return   true if this Bloom Filter contains a mapping for the specified key.
	 */
	public boolean hasKey(Object obj) {
		boolean result = true;
		int hashCodeObject;
		if(!useRabin) hashCodeObject = obj.hashCode(); else try {
			hashCodeObject = hashRabin(obj);
		} catch ( Exception e ) {
			useRabin = false;
			hashCodeObject = obj.hashCode();
		}
		for (int i = 0; i < numFunctions && result; i++) {
			result &= keys[getHash(i, hashCodeObject)];
		}
		return result;
	}

	/**
	 *  Adds the specified key in this Bloom Filter.
	 *
	 *@param  obj  The key to be added to this Bloom Filter.
	 */
	public void put(Object obj) {
		int hashCodeObject;
		if(!useRabin) hashCodeObject = obj.hashCode(); else try { 
			hashCodeObject = hashRabin(obj);
		} catch ( Exception e ) {
			useRabin = false;
			hashCodeObject = obj.hashCode();
		}
		for (int i = 0; i < numFunctions; i++) {
			keys[getHash(i, hashCodeObject)] = true;
		}
	}

	/**
	 *
	 * Returns a string representation of this Bloom Filter. The string representation consists of an
	 * integer specifying the number of hash Functions, an integer specifying the length of the
	 * bit vector, and a sequence of 0s and 1s specifying the bit vector. These 3 fields are
	 * separated by the character ":".
	 * 
	 * This implementation creates an empty string buffer, and iterates over the bit vector, 
	 * appending the value of each bit in turn. A string is obtained from the stringbuffer, and returned.
	 * 
	 * @return A string representation of this Bloom Filter.
	 */
	public String toString() {
		StringBuffer aux =
			new StringBuffer(numFunctions + ":" + keys.length + ":");
		for (int i = 0; i < keys.length; i++) {
			if (keys[i])
				aux.append("1");
			else
				aux.append("0");
		}
		return aux.toString();
	}

	/**
	 * Returns a copy of this Bloom Filter instance.
	 *
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return new BloomFilter(this.toString());
	}

	/**
	 *  Return the Rabin hash value of an array of bytes.
	 *
	 *@param  arr  An array of bytes.
	 *@return    The Rabin hash value for the array of bytes.
	 */
	public int hashRabin(byte[] arr) {
		return hashRabin(arr, 0, arr.length, 0);
	}

	/**
	 *  Return the Rabin hash value of an array of bytes.
	 *
	 *@param  arr        An array of bytes.
	 *@param  offset    Index of the first byte of the array to hash.
	 *@param  length   Number of bytes to hash.
	 *@param  ws          ??         
	 *@return  The Rabin hash value for the array of bytes.
	 */
	private int hashRabin(byte[] arr, int offset, int length, int ws) {
		int w = ws;
		int start = length % 4;
		for (int s = offset; s < offset + start; s++) {
			w = (w << 8) ^ (arr[s] & 0xFF);
		}
		for (int s = start + offset; s < length + offset; s += 4) {
			w =
				table32[w
					& 0xFF]
					^ table40[(w >>> 8)
					& 0xFF]
					^ table48[(w >>> 16)
					& 0xFF]
					^ table54[(w >>> 24)
					& 0xFF]
					^ (arr[s] << 24)
					^ ((arr[s + 1] & 0xFF) << 16)
					^ ((arr[s + 2] & 0xFF) << 8)
					^ (arr[s + 3] & 0xFF);
		}
		return w;
	}

	/**
	 *  Return the Rabin hash value of an array of chars.
	 *
	 *@param  arr  An array of chars.
	 *@return   The Rabin hash value for the array of chars.
	 */
	public int hashRabin(char[] arr) {
		int w = 0;
		int start = 0;
		if (arr.length % 2 == 1) {
			w = arr[0] & 0xFFFF;
			start = 1;
		}
		for (int s = start; s < arr.length; s += 2) {
			w =
				table32[w
					& 0xFF]
					^ table40[(w >>> 8)
					& 0xFF]
					^ table48[(w >>> 16)
					& 0xFF]
					^ table54[(w >>> 24)
					& 0xFF]
					^ ((arr[s] & 0xFFFF) << 16)
					^ (arr[s + 1] & 0xFFFF);
		}
		return w;
	}

	/**
	 *  Computes the Rabin hash value of the contents of a <code>File</code>.
	 *
	 *@param  f                       A <code>File</code>.
	 *@return                          The Rabin hash value for the contents of the File.
	 *@throws  FileNotFoundException  If the file cannot be found.
	 *@throws  IOException            If an error occurs while reading the file.
	 */
	public int hashRabin(File f) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(f);
		try {
			return hashRabin(fis);
		} finally {
			fis.close();
		}
	}

	/**
	 *  Computes the Rabin hash value of the data from an <code>InputStream</code>.
	 *
	 *@param  is          An InputStream.
	 *@return               The Rabin hash value for the contents read from the InputStream.
	 *@throws  IOException  if an error occurs while reading from the InputStream.
	 */
	public int hashRabin(InputStream is) throws IOException {
		int hashValue = 0;
		int bytesRead;
		synchronized (buffer) {
			while ((bytesRead = is.read(buffer)) > 0) {
				hashValue = hashRabin(buffer, 0, bytesRead, hashValue);
			}
		}
		return hashValue;
	}

	/**
	 *  Returns the Rabin hash value of an array of integers. This method is the
	 *  most efficient of all the hash methods, so it should be used when
	 *  possible.
	 *
	 *@param   arr  An array of integers.
	 *@return    int The Rabin hash value for the array of integers.
	 */
	public int hashRabin(int[] arr) {
		int w = 0;
		for (int s = 0; s < arr.length; s++) {
			w =
				table32[w
					& 0xFF]
					^ table40[(w >>> 8)
					& 0xFF]
					^ table48[(w >>> 16)
					& 0xFF]
					^ table54[(w >>> 24)
					& 0xFF]
					^ arr[s];
		}
		return w;
	}

	/**
	 *  Computes the Rabin hash value of a given Object.
	 *
	 *@param  obj            An Object.
	 *@return                   The Rabin hash value for the Object.
	 *@throws  IOException  If Object serialization fails.
	 */
	public int hashRabin(Object obj) throws IOException {
		return hashRabin((Serializable) obj);
	}

	/**
	 *  Computes the Rabin hash value of a given serializable Object.
	 *
	 *@param  obj             An Object.
	 *@return                   The Rabin hash value for the Object.
	 *@throws  IOException  If serialization fails.
	 */
	public int hashRabin(Serializable obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			return hashRabin(baos.toByteArray());
		} finally {
			oos.close();
			baos.close();
			oos = null;
			baos = null;
		}
	}

	/**
	 *  Computes the Rabin hash value of a String.
	 *
	 *@param  s  A <code>String</code>.
	 *@return   The Rabin hash value for the String.
	 */
	public int hashRabin(String s) {
		return hashRabin(s.toCharArray());
	}

	/**
	 *  Computes the Rabin hash value of the contents of a Web document,
	 *  specified by an URL.
	 *
	 *@param  url         The URL of the document to be hashed.
	 *@return               The Rabin hash value for the document.
	 *@throws  IOException If an error occurs while reading the document.
	 */
	public int hashRabin(URL url) throws IOException {
		InputStream is = url.openStream();
		try {
			return hashRabin(is);
		} finally {
			is.close();
		}
	}

}
