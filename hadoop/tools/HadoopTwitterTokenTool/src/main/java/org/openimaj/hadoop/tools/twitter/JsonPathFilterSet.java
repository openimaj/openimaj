package org.openimaj.hadoop.tools.twitter;

import java.util.ArrayList;
import java.util.Collection;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.*;

import org.openimaj.util.pair.IndependentPair;

import com.jayway.jsonassert.JsonAssert;
import com.jayway.jsonassert.JsonAsserter;
import com.jayway.jsonassert.impl.JsonAsserterImpl;

import static com.jayway.jsonassert.JsonAssert.*;
import static org.openimaj.hadoop.tools.twitter.NotEmptyCollection.*;
import com.jayway.jsonpath.JsonPath;

/**
 * Given a collection of JSONpath, treat each path as a filter and return true if
 * a given json input matches every filter
 * 
 * @author ss
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
