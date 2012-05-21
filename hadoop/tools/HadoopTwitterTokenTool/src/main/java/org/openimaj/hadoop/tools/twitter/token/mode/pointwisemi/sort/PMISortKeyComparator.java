package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort;

import java.util.Arrays;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.openimaj.util.pair.IndependentPair;


/**
 * Read a tokenpair and make sure the single words appear before the pair words
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PMISortKeyComparator implements RawComparator<Text> {

	@Override
	public int compare(Text o1, Text o2) {
		String o1s = o1.toString();
		String o2s = o2.toString();
		
		IndependentPair<Long,Double> tp1 = PMIPairSort.parseTimeStr(o1s);
		IndependentPair<Long,Double> tp2 = PMIPairSort.parseTimeStr(o2s);
		int tpcmp = tp1.firstObject().compareTo(tp2.firstObject());
		if(tpcmp == 0){
			return -1 * tp1.secondObject().compareTo(tp2.secondObject());
		}
		return tpcmp;
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
