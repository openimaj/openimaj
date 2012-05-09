package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;

import org.openimaj.io.IOUtils;
import org.openimaj.io.ReadWriteable;
import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.util.pair.IntIntPair;
import org.openimaj.util.pair.Pair;

/**
 * A pair of strings with 2 distinct counts: 
 * <ul>
 * <li>number of times the pair appears together in a document</li>
 * </ul>
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TokenPairCount extends Pair<String> implements ReadWriteable{

	/**
	 * Number of times this pair appears together
	 */
	public long paircount;
	public boolean isSingle;
	
	/**
	 * 
	 */
	public TokenPairCount() {
		super(null,null);
		this.isSingle = false;
	}
	
	/**
	 * @param tok1
	 * @param tok2
	 */
	public TokenPairCount(String tok1, String tok2) {
		super(tok1, tok2);
		isSingle = tok2 == null;
	}

	public TokenPairCount(String tok1) {
		this(tok1,null);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.isSingle = in.readBoolean();
		this.setFirstObject(in.readUTF());
		if(!isSingle)
			this.setSecondObject(in.readUTF());
		this.paircount = in.readLong();
	}

	@Override
	public byte[] binaryHeader() {
		return "B".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeBoolean(this.isSingle);
		out.writeUTF(this.firstObject());
		if(!this.isSingle)
			out.writeUTF(this.secondObject());
		out.writeLong(paircount);
	}

	public void add(TokenPairCount that) {
		this.paircount +=that.paircount;
		
	}
	
	/**
	 * @return identifier string without a count
	 */
	public String identifier(){
		return (this.isSingle ? 0 : 1) + "\n" + this.firstObject() + "\n" + this.secondObject();
	}
	
	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			IOUtils.writeASCII(writer, this);
		} catch (IOException e) {
			return "ERRORSTRING";
		}
		return writer.toString();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		this.isSingle = Boolean.parseBoolean(in.nextLine());
		this.setFirstObject(in.nextLine());
		if(!this.isSingle){
			this.setSecondObject(in.nextLine());
		}
		this.paircount = Long.parseLong(in.nextLine());
	}

	@Override
	public String asciiHeader() {
		return "A";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(this.isSingle);
		out.println(this.firstObject());
		if(!this.isSingle){
			out.println(this.secondObject());
		}
		out.println(paircount);
	}

}
