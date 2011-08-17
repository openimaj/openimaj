package org.openimaj.image.feature.local.interest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;

import Jama.Matrix;

public class EllipticInterestPointData extends InterestPointData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3442580574124477236L;
	
	public Matrix transform;
	
	
	public void setTransform(Matrix transform) {
		this.transform = transform;
	}
	
	@Override
	public Matrix getTransform(){
		Matrix m = new Matrix(3,3);
		m.setMatrix(0, 1, 0,1,this.transform);
		m.set(0, 2, 0);
		m.set(1, 2, 0);
		m.set(2, 2, 1);
		return m;
	}
	
	@Override
	public Ellipse getEllipse() {
		return EllipseUtilities.fromTransformMatrix2x2(transform, x, y, scale);
	}
	
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		out.writeFloat((float) transform.get(0,0));
		out.writeFloat((float) transform.get(0,1));
		out.writeFloat((float) transform.get(1,0));
		out.writeFloat((float) transform.get(1,1));
	}
	
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		super.writeASCII(out);
		out.format(" %4.2f %4.2f %4.2f %4.2f", (float) transform.get(0,0),(float) transform.get(0,1),(float) transform.get(1,0),(float) transform.get(1,1));
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		this.transform = new Matrix(2,2);
		this.transform.set(0, 0, in.readFloat());
		this.transform.set(0, 1, in.readFloat());
		this.transform.set(1, 0, in.readFloat());
		this.transform.set(1, 1, in.readFloat());
		this.setTransform(this.transform);
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException {
		super.readASCII(in);
		this.transform = new Matrix(2,2);
		this.transform.set(0, 0, in.nextFloat());
		this.transform.set(0, 1, in.nextFloat());
		this.transform.set(1, 0, in.nextFloat());
		this.transform.set(1, 1, in.nextFloat());
		this.setTransform(this.transform);
	}
	
	@Override
	public EllipticInterestPointData clone() {
		EllipticInterestPointData d = (EllipticInterestPointData) super.clone();
		d.transform = this.transform.copy();
		return d;
	}
}
