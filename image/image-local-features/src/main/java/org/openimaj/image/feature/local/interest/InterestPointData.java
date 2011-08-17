package org.openimaj.image.feature.local.interest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.feature.local.ScaleSpaceLocation;
import org.openimaj.math.geometry.shape.Ellipse;

import Jama.Matrix;

public class InterestPointData extends ScaleSpaceLocation{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6710204268210799061L;
	public float score;
	
	
	public InterestPointData(){
		
	}
	
	@Override
	public InterestPointData clone() {
		InterestPointData d = (InterestPointData) super.clone();
		d.score = this.score;
		return d;
	}
	
	boolean equalPos(InterestPointData otherPos){
		return otherPos.x == this.x && otherPos.y == this.y && otherPos.scale == this.scale;
	}
	
	@Override
	public boolean equals(Object other) {
		return super.equals(other) && this.score == ((InterestPointData)other).score;
	}

	public Ellipse getEllipse() {
		return new Ellipse(x, y, scale*scale,scale*scale,0);
	}
	
	public Matrix getTransform() {
		return Matrix.identity(3, 3);
	}
	
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		out.writeFloat(score);
	}
	
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		super.writeASCII(out);
		out.format(" %4.2f", this.score);
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		this.score = in.readFloat();
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		super.readASCII(in);
		this.score = in.nextFloat();
	}

	
}