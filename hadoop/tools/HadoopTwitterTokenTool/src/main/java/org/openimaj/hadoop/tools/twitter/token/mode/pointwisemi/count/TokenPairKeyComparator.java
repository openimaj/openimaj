package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.openimaj.util.pair.IndependentPair;


/**
 * Read a tokenpair and make sure the single words appear before the pair words
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TokenPairKeyComparator implements RawComparator<Text> {

	@Override
	public int compare(Text o1, Text o2) {
		String o1s = o1.toString();
		String o2s = o2.toString();
		
		
		

		IndependentPair<Long, TokenPairCount> o1TTPair = null;
		IndependentPair<Long, TokenPairCount> o2TTPair = null;
		try {
			o1TTPair = TokenPairCount.parseTimeTokenID(o1s);
			o2TTPair = TokenPairCount.parseTimeTokenID(o2s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Long o1time = o1TTPair.firstObject();
		Long o2time = o2TTPair.firstObject();
		int timeCmp = o1time.compareTo(o2time);
		if(timeCmp == 0){
			try {
				TokenPairCount tpc1 = o1TTPair.secondObject();
				TokenPairCount tpc2 = o2TTPair.secondObject();
				if(tpc1.isSingle && !tpc2.isSingle){
					return -1;
				}
				else if(!tpc1.isSingle && tpc2.isSingle){
					return 1;
				}
				else{
					return tpc1.identifier().compareTo(tpc2.identifier());
				}
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		return timeCmp;
	}

	@Override
	public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		byte[] o1arr = Arrays.copyOfRange(b1, s1, s1+l1);
		byte[] o2arr = Arrays.copyOfRange(b2, s2, s2+l2);
		String o1 = new String(o1arr);
		String o2 = new String(o2arr);
		return compare(new Text(o1),new Text(o2));
	}
}
