package org.openimaj.util.lang;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

/**
 * Test the reflection utils
 * @author ss
 *
 */
public class ReflectionUtilsTest {
	/**
	 * 
	 */
	@Test
	public void testGetClass(){
		Type t = new String(" ").getClass();
		assertTrue(ReflectionUtils.getClass(t).equals(String.class));
	}
	
	class Base<A,B,C>{
		
	}
	class Middle<C,B,A> extends Base<A,B,C>{
		
	}
	class MiddleTwo<C> extends Middle<Double,C,String>{
		
	}
	class Child<A,B> extends MiddleTwo<B>{
		A value;
	}
	
	class FinalComplex extends Child<MiddleTwo<Long>,Middle<Byte,Byte,Byte>>{
	}
	
	class Final extends Child<Integer,Long>{
		
	}
	/**
	 * 
	 */
	@Test
	public void testGenerics(){
		List<Class<?>> types = ReflectionUtils.getTypeArguments(Base.class, Final.class);
		assertTrue(types.get(0).equals(String.class));
		assertTrue(types.get(1).equals(Long.class));
		assertTrue(types.get(2).equals(Double.class));
		List<Class<?>> ctypes = ReflectionUtils.getTypeArguments(Base.class, FinalComplex.class);
		assertTrue(ctypes.get(0).equals(String.class));
		assertTrue(ctypes.get(1).equals(Middle.class));
		assertTrue(ctypes.get(2).equals(Double.class));
	}
}
