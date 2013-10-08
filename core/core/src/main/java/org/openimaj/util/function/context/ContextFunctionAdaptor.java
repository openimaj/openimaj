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
package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * An adaptor that allows a {@link Function} to be applied to a single element
 * of a {@link Context}, and store its output in a (potentially) different slot
 * of the {@link Context}.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <IN>
 *            Type of input to the adapted function
 * @param <OUT>
 *            Type of output of the adapted function
 */
public class ContextFunctionAdaptor<IN, OUT> extends ContextAdaptor<Function<IN, OUT>, IN, OUT>
		implements
		Function<Context, Context>
{

	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextFunctionAdaptor(Function<IN, OUT> inner, ContextExtractor<IN> extract,
			ContextInsertor<OUT> insert)
	{
		super(inner, extract, insert);
	}

	/**
	 * Construct {@link ContextFunctionAdaptor} that reads data from a given
	 * key, applies a function and sets the result in a different key.
	 * 
	 * @param extract
	 *            the key to pull data from
	 * @param insert
	 *            the key to write the result to
	 * @param inner
	 *            the function to apply
	 */
	public ContextFunctionAdaptor(Function<IN, OUT> inner, String extract, String insert)
	{
		super(inner, extract, insert);
	}

	/**
	 * Construct {@link ContextFunctionAdaptor} that reads data from a given
	 * key, applies a function and sets the result with the same key.
	 * 
	 * @param both
	 *            the extract and insert key
	 * @param inner
	 *            the function to apply
	 */
	public ContextFunctionAdaptor(String both, Function<IN, OUT> inner)
	{
		super(inner, both, both);
	}

	@Override
	public Context apply(Context in) {
		final OUT obj = inner.apply(extract.extract(in));

		insert.insert(obj, in);

		return in;
	}

	/**
	 * Create a new {@link ContextFunctionAdaptor} that reads data from a given
	 * key, applies a function and sets the result in a different key.
	 * 
	 * @param extract
	 *            the key to pull data from
	 * @param insert
	 *            the key to write the result to
	 * @param inner
	 *            the function to apply
	 * @return the new {@link ContextFunctionAdaptor}
	 */
	public static <IN, OUT> Function<Context, Context> create(String extract, String insert, Function<IN, OUT> inner) {
		return new ContextFunctionAdaptor<IN, OUT>(inner, extract, insert);
	}

}
