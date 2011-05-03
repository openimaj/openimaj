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
package org.openimaj.util.hash;

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>.
 * The following utility class allows simple construction of an effective hashCode method. 
 * It is based on the recommendations of Effective Java, by Joshua Bloch. 
 *
 * Example use case:
 * <pre>
 *  public int hashCode(){
 *    int result = HashCodeUtil.SEED;
 *    //collect the contributions of various fields
 *    result = HashCodeUtil.hash(result, fPrimitive);
 *    result = HashCodeUtil.hash(result, fObject);
 *    result = HashCodeUtil.hash(result, fArray);
 *    return result;
 *  }
 * </pre>
 */
public final class HashCodeUtil {

	/**
	 * An initial value for a <code>hashCode</code>, to which is added contributions
	 * from fields. Using a non-zero value decreases collisions of <code>hashCode</code>
	 * values.
	 */
	public static final int SEED = 23;

	/**
	 * booleans.
	 * @param aSeed the hash value to append to
	 * @param aBoolean the value to hash
	 * @return the new hash value
	 */
	public static int hash( int aSeed, boolean aBoolean ) {
		return firstTerm( aSeed ) + ( aBoolean ? 1 : 0 );
	}

	/**
	 * chars.
	 * @param aSeed the hash value to append to
	 * @param aChar the value to hash
	 * @return the new hash value
	 */
	public static int hash( int aSeed, char aChar ) {
		return firstTerm( aSeed ) + (int)aChar;
	}

	/**
	 * ints.
	 * @param aSeed the hash value to append to
	 * @param aInt the value to hash
	 * @return the new hash value
	 */
	public static int hash( int aSeed , int aInt ) {
		/*
		 * Implementation Note
		 * Note that byte and short are handled by this method, through
		 * implicit conversion.
		 */
		return firstTerm( aSeed ) + aInt;
	}

	/**
	 * longs.
	 * @param aSeed the hash value to append to
	 * @param aLong the value to hash
	 * @return the new hash value
	 */
	public static int hash( int aSeed , long aLong ) {
		return firstTerm(aSeed)  + (int)( aLong ^ (aLong >>> 32) );
	}

	/**
	 * floats.
	 * @param aSeed the hash value to append to
	 * @param aFloat the value to hash
	 * @return the new hash value
	 */
	public static int hash( int aSeed , float aFloat ) {
		return hash( aSeed, Float.floatToIntBits(aFloat) );
	}

	/**
	 * doubles.
	 * @param aSeed the hash value to append to
	 * @param aDouble the value to hash
	 * @return the new hash value
	 */
	public static int hash( int aSeed , double aDouble ) {
		return hash( aSeed, Double.doubleToLongBits(aDouble) );
	}

	/**
	 * <code>aObject</code> is a possibly-null object field, and possibly an array.
	 *
	 * If <code>aObject</code> is an array, then each element may be a primitive
	 * or a possibly-null object.
	 * 
	 * @param aSeed the hash value to append to
	 * @param aObject the value to hash
	 * @return the new hash value
	 */
	public static int hash( int aSeed , Object aObject ) {
		int result = aSeed;
		if ( aObject == null) {
			result = hash(result, 0);
		}
		else if ( ! isArray(aObject) ) {
			result = hash(result, aObject.hashCode());
		}
		else {
			int length = Array.getLength(aObject);
			for ( int idx = 0; idx < length; ++idx ) {
				Object item = Array.get(aObject, idx);
				//recursive call!
				result = hash(result, item);
			}
		}
		return result;
	}
	
	/**
	 * boolean array
	 * 
	 * @param aSeed the hash value to append to
	 * @param array the value to hash
	 * @return the new hash value
	 */
	public static int hash(int aSeed, boolean[] array){
		int result = aSeed;
		for ( int idx = 0; idx < array.length; ++idx ) {
			result = hash(result, array[idx]);
		}
		return result;
	}


	/// PRIVATE ///
	private static final int fODD_PRIME_NUMBER = 37;

	private static int firstTerm( int aSeed ){
		return fODD_PRIME_NUMBER * aSeed;
	}

	private static boolean isArray(Object aObject){
		return aObject.getClass().isArray();
	}
} 
