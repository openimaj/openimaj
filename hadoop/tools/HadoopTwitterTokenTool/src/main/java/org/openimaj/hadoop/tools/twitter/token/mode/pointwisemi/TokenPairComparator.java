package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.IOException;

import org.apache.hadoop.io.RawComparator;
import org.openimaj.io.IOUtils;


/**
 * Read a tokenpair and make sure the single words appear before the pair words
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TokenPairComparator implements RawComparator<TokenPairCount> {

	@Override
	public int compare(TokenPairCount o1, TokenPairCount o2) {
		if(o1.isSingle && !o2.isSingle){
			return -1;
		}
		else if(!o1.isSingle && o2.isSingle){
			return 1;
		}
		else{
			String o1f = o1.firstObject();
			String o1s = o1.secondObject();
			String o2f = o2.firstObject();
			String o2s = o2.secondObject();
			int fcmp = o1f.compareTo(o2f);
			if(fcmp == 0){
				if(o1s == null){
					return 0; // same single word
				}
				return o1s.compareTo(o2s); // compare second
			}
			
			return fcmp;
		}
	}

	@Override
	public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		
		try {
			TokenPairCount o1 = IOUtils.deserialize(b1,s1+1, TokenPairCount.class);
			TokenPairCount o2 = IOUtils.deserialize(b2,s2+1, TokenPairCount.class);
			return compare(o1,o2);
		} catch (IOException e) {
			return 0;
		}
	}


}
