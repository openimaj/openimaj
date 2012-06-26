package org.openimaj.math.hash;

import cern.jet.random.engine.MersenneTwister;

/**
 * Use MurmurHash (http://murmurhash.googlepages.com/) to generate a random hash for a string.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StringMurmurHashFunction extends HashFunction<String,StringMurmurHashFunction>{
	
	
	/**
	 * init the murmur hash seed 
	 */
	public StringMurmurHashFunction() {
		this(new MersenneTwister());
		
	}
	
	
	/**
	 * @param mersenneTwister
	 */
	public StringMurmurHashFunction(MersenneTwister mersenneTwister) {
		super(mersenneTwister);
	}


	@Override
	public int computeHashCode(String data) {
		return murmurhash(data.getBytes(),seed());
	}
	
	/**
	 * @see #murmurhash(byte[])
	 * @param data
	 * @return hash
	 */
	public static int murmurhash(byte[] data)
    {
        return murmurhash(data, data.length,-1);
    }
	
	/**
	 * @see #murmurhash(byte[])
	 * @param data
	 * @param seed 
	 * @return hash
	 */
	public static int murmurhash(byte[] data, int seed)
    {
        return murmurhash(data, data.length,seed);
    }
	
	/**
	 * A java implementation of Murmur hash (http://murmurhash.googlepages.com/) copied with great 
	 * prejudice from https://github.com/clearspring/stream-lib
	 * @param data
	 * @param length
	 * @param seed
	 * @return a hash
	 */
	public static int murmurhash(byte[] data, int length, int seed)
    {
        int m = 0x5bd1e995;
        int r = 24;

        int h = seed ^ length;

        int len_4 = length >> 2;

        for (int i = 0; i < len_4; i++)
        {
            int i_4 = i << 2;
            int k = data[i_4 + 3];
            k = k << 8;
            k = k | (data[i_4 + 2] & 0xff);
            k = k << 8;
            k = k | (data[i_4 + 1] & 0xff);
            k = k << 8;
            k = k | (data[i_4 + 0] & 0xff);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // avoid calculating modulo
        int len_m = len_4 << 2;
        int left = length - len_m;

        if (left != 0)
        {
            if (left >= 3)
            {
                h ^= (int) data[length - 3] << 16;
            }
            if (left >= 2)
            {
                h ^= (int) data[length - 2] << 8;
            }
            if (left >= 1)
            {
                h ^= (int) data[length - 1];
            }

            h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }
	
}
