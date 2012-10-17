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
