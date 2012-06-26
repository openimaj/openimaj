package org.openimaj.math.hash;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StringMurmurHashTest {
	
	/**
	 * 
	 */
	@Test
	public void testMurmur(){
		HashFunctionFactory<String, StringMurmurHashFunction> fact = HashFunctionFactory.get(StringMurmurHashFunction.class);
		StringMurmurHashFunction func1 = fact.create();
		String st1 = "Cheese";
		String st2 = "Fheese";
		assertTrue(func1.computeHashCode(st1) == func1.computeHashCode(st1));
		assertTrue(func1.computeHashCode(st1) != func1.computeHashCode(st2));
		StringMurmurHashFunction func2 = fact.create();
		assertTrue(func1.computeHashCode(st1) != func2.computeHashCode(st1));
		
		int min,max;
		min = fact.create().computeHashCode(st1);
		max = fact.create().computeHashCode(st1);
		for (int i = 0; i < 10000; i++) {
			int n = fact.create().computeHashCode(st1);
			if(n < min) min = n;
			if(n > max) max = n;
			n = Math.abs(n);
			assertTrue(n % 1000 < 1000 && n % 1000 >= 0);
		}
		System.out.printf("min = %d, max = %d\n",min,max);
		System.out.printf("minint = %d, maxint = %d",Integer.MIN_VALUE,Integer.MAX_VALUE);
	}
}
