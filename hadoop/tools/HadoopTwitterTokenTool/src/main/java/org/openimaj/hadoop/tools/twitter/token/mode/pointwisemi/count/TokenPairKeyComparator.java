package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.RawComparator;
import org.apache.log4j.Logger;


/**
 * Read a tokenpair and make sure the single words appear before the pair words
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TokenPairKeyComparator implements RawComparator<BytesWritable> {
	Logger logger = Logger.getLogger(TokenPairKeyComparator.class);
	@Override
	public int compare(BytesWritable o1, BytesWritable o2) {
		return compareData(o1.getBytes(),0,o1.getLength(),o2.getBytes(),0,o2.getLength());
	}

	@Override
	public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		return compareData(b1,s1+4,l1-4,b2,s2+4,l2-4); // Expecting bytes writeable, skip the BytesWritable length
	}
	
	private int compareData(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2){
		DataInputStream dis1  = null;
		DataInputStream dis2  = null;
		try{
			dis1 = new DataInputStream(new ByteArrayInputStream(b1,s1,l1));
			dis2 = new DataInputStream(new ByteArrayInputStream(b2,s2,l2));
			
			// first check the time, return early if they are not the same time!
			long time1 = dis1.readLong();
			long time2 = dis2.readLong();
			if(time1<time2){
				return -1;
			}
			else if(time1>time2){
				return 1;
			}
			
			// now check if they are both pair counts, if not, make sure the unary count goes first
			boolean single1 = dis1.readBoolean();
			boolean single2 = dis2.readBoolean();
			if(single1 && !single2){
				return -1;
			}
			else if(!single1 && single2){
				return 1;
			}
			
			// now either they are both single, or they are both pairs, either way compare the first strings first
			int cmpFirstString = dis1.readUTF().compareTo(dis2.readUTF());
			if(single1){
				return cmpFirstString;
			}
			// Both are pairs, were their first strings unequal?
			if(cmpFirstString != 0){
				return cmpFirstString;
			}
			
			// Shared the first string! right! now compare the final string!
			return dis1.readUTF().compareTo(dis2.readUTF());
		} catch (IOException e) {
			return 0;
		}
		finally{
			try {
				dis1.close();
				dis2.close();
			} catch (IOException e) {
				// eep!
			}
		}
	}
}
