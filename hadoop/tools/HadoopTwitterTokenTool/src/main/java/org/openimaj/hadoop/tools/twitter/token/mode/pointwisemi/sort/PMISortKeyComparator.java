package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.RawComparator;


/**
 * Read a tokenpair and make sure the single words appear before the pair words
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PMISortKeyComparator implements RawComparator<BytesWritable> {

	@Override
	public int compare(BytesWritable o1, BytesWritable o2) {
		return compareData(o1.getBytes(),0,o1.getLength(),o2.getBytes(),0,o2.getLength());
	}

	@Override
	public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		return compareData(b1,s1+4,l1-4,b2,s2+4,l2-4);
	}

	private int compareData(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		DataInputStream dis1 = new DataInputStream(new ByteArrayInputStream(b1,s1,l1));
		DataInputStream dis2 = new DataInputStream(new ByteArrayInputStream(b2,s2,l2));
		
		try {
			// group up by times first
			long t1;
			long t2;
			t1 = dis1.readLong();
			t2 = dis2.readLong();
			
			if(t1 < t2){
				return -1;
			}
			else if(t1 > t2){
				return 1;
			}
			// then sort by pmi score
			double p1 = dis1.readDouble();
			double p2 = dis2.readDouble();
			// NOTE: pmi goes biggest first!
			if(p1 > p2) return -1;
			else if(p1 < p2) return 1;
			return 0;
		} catch (IOException e) {
		}
		return 0;
	}
}
