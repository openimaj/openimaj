package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.util.pair.IntIntPair;
import org.openimaj.util.pair.Pair;

/**
 * A pair of strings with 2 distinct counts: 
 * <ul>
 * <li>number of times the pair appears together in a document</li>
 * <li>number of times item 1 appears with any terms in the document</li>
 * <li>number of times item 2 appears with any terms in the document</li>
 * </ul>
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TokenPairCount extends Pair<String> implements ReadWriteableBinary{

	/**
	 * Number of times this pair appears together
	 */
	public int paircount;
	/**
	 * Number of times item 1 and item 2 appears in a pair overall
	 */
	public IntIntPair totalpaircounts;
	
	/**
	 * 
	 */
	public TokenPairCount() {
		super(null,null);
	}
	
	/**
	 * @param tok1
	 * @param tok2
	 */
	public TokenPairCount(String tok1, String tok2) {
		super(tok1, tok2);
		totalpaircounts = new IntIntPair();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.setFirstObject(in.readUTF());
		this.setSecondObject(in.readUTF());
		this.paircount = in.readInt();
		this.totalpaircounts = new IntIntPair(in.readInt(),in.readInt());
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(this.firstObject());
		out.writeUTF(this.secondObject());
		out.writeInt(paircount);
		out.writeInt(totalpaircounts.first);
		out.writeInt(totalpaircounts.second);
	}

	public void add(TokenPairCount that) {
		this.paircount +=that.paircount;
		this.totalpaircounts.first += that.totalpaircounts.first;
		this.totalpaircounts.second += that.totalpaircounts.second;
		
	}
	
	@Override
	public String toString() {
		return this.firstObject()+""+this.secondObject();
	}

}
