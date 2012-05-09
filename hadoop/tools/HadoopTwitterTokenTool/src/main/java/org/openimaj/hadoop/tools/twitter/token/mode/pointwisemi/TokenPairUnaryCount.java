package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TokenPairUnaryCount extends TokenPairCount{
	private long tok1count;
	private long tok2count;

	public TokenPairUnaryCount() {
	}
	public TokenPairUnaryCount(String tok1, String tok2, long paircount, long tok1count, long tok2count){
		super(tok1,tok2);
		this.paircount = paircount;
		this.tok1count = tok1count;
		this.tok2count = tok2count;
	}
	
	public TokenPairUnaryCount(TokenPairCount tpc, long tok1count,long tok2count) {
		this(tpc.firstObject(),tpc.secondObject(),tpc.paircount,tok1count,tok2count);
	}
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		out.writeLong(tok1count);
		out.writeLong(tok2count);
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		this.tok1count = in.readLong();
		this.tok2count = in.readLong();
	}
}
