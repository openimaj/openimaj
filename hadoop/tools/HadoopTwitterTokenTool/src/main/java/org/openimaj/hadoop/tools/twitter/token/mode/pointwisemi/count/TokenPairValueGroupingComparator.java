package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.openimaj.util.pair.IndependentPair;


/**
 * Read a tokenpair and make sure the single words appear before the pair words
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TokenPairValueGroupingComparator implements RawComparator<BytesWritable> {

	@Override
	public int compare(BytesWritable o1, BytesWritable o2) {
		return compareData(o1.getBytes(),0,o1.getLength(),o2.getBytes(),0,o2.getLength());
	}
	
	@Override
	public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		return compareData(b1,s1+4,l1-4,b2,s2+4,l2-4);
	}
	
	private int compareData(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		// read the time, return 0 if the time is the same
		try {
			long t1 = TokenPairUnaryCount.timeFromBinaryIdentity(b1,s1,l1);
			long t2 = TokenPairUnaryCount.timeFromBinaryIdentity(b2,s2,l2);
			if(t1 < t2) {
				return -1;
			}
			else if(t1 > t2){
				return 1;
			}
			else{
				return 0;
			}
			
		} catch (IOException e) {
			return 0;
		}
	}


}
