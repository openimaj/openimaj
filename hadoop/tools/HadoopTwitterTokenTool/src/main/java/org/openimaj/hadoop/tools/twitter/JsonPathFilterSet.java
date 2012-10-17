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
package org.openimaj.hadoop.tools.twitter;

import java.util.ArrayList;
import java.util.Collection;

import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.*;

import org.openimaj.util.pair.IndependentPair;

import com.jayway.jsonassert.JsonAsserter;
import static com.jayway.jsonassert.JsonAssert.*;

/**
 * Given a collection of JSONpath, treat each path as a filter and return true if
 * a given json input matches every filter
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JsonPathFilterSet {
	private ArrayList<IndependentPair<String,Matcher<?>>> filters;
	
	
	/**
	 * @param paths the json paths against which to filter
	 */
	public JsonPathFilterSet(Collection<String> paths) {
		this();
		if(paths == null) return;
		for (String string : paths) {
			addString(string);
		}
	}
	
	private void addString(String string) {
		System.out.println("Adding String: " + string);
		String[] split = string.split(":==");
		IndependentPair<String, Matcher<?>> r = null;
		if(split.length == 1)
		{
			r = new IndependentPair<String,Matcher<?>>(split[0], null);
		}
		else{
			r = new IndependentPair<String,Matcher<?>>(split[0],equalTo(split[1]));
		}
		this.filters.add(r);
	}

	/**
	 * @param paths the json paths against which to filter
	 */
	public JsonPathFilterSet(String ... paths) {
		this();
		for (String string : paths) {
			addString(string);
		}
	}

	private JsonPathFilterSet() {
		this.filters = new ArrayList<IndependentPair<String,Matcher<?>>>();
	}
	
	/**
	 * @param json the input to be checked
	 * @return true if the inputed json matches all filters
	 */
	public boolean filter(String json){
		JsonAsserter jass = with(json);
		for (IndependentPair<String, Matcher<?>> filter : this.filters) {
			String path = filter.firstObject();
			Matcher<?> matcher = filter.secondObject();
			try{
				if(matcher==null)
					jass = jass.assertNotNull(path);
				else
					jass = jass.assertThat(path, matcher);
			}catch(AssertionError e){
				return false;
			}
		}
		return true;
	}
}
