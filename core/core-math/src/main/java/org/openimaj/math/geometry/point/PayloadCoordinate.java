package org.openimaj.math.geometry.point;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.math.geometry.shape.Ellipse;

public class PayloadCoordinate<T extends Coordinate, O> implements Coordinate {
	
	private T coord;
	private O payload;
	
	public PayloadCoordinate(T coord, O payload){
		this.coord = coord;
		this.setPayload(payload);
	}
	
	@Override
	public void readASCII(Scanner in) throws IOException { coord.readASCII(in);}
	@Override
	public String asciiHeader() {return coord.asciiHeader();}

	@Override
	public void readBinary(DataInput in) throws IOException {coord.readBinary(in);}

	@Override
	public byte[] binaryHeader() { return coord.binaryHeader();}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {coord.writeASCII(out);}

	@Override
	public void writeBinary(DataOutput out) throws IOException {coord.writeBinary(out);}

	@Override
	public Number getOrdinate(int dimension) {return coord.getOrdinate(dimension);}

	@Override
	public int getDimensions() {return coord.getDimensions();}

	public void setPayload(O payload) {
		this.payload = payload;
	}

	public O getPayload() {
		return payload;
	}

	public static <T extends Coordinate, O> PayloadCoordinate<T,O> payload(T coord,O payload) {
		return new PayloadCoordinate<T,O>(coord,payload);
	}

}
