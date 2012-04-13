package org.openimaj;

public class RandomGaussian {
	
	public static void main(String[] args) {
		byte b= 0;
		for(int i = 0; i < 257; i++){
			System.out.println(0xff & (b ++));
			System.out.println((byte)(i));
		}
	}
}
