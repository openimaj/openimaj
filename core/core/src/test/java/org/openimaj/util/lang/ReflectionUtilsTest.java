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
package org.openimaj.util.lang;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;
import org.openimaj.util.reflection.ReflectionUtils;

/**
 * Test the reflection utils
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
