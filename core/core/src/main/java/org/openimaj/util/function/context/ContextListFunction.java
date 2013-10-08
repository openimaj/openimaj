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

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Apply a {@link Function} to a list held within a single element of a
 * {@link Context}, writing the resultant list back to the {@link Context},
 * potentially with a different key.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <IN>
 *            The input type of the function (and the type of elements in the
 *            list)
 * @param <OUT>
 *            The output type of the function (and type of elements in the
 *            resultant list)
 */
public class ContextListFunction<IN, OUT> extends ContextAdaptor<Function<IN, OUT>, List<IN>, List<OUT>>
		implements
		Function<Context, Context>
{

	/**
	 * Construct with the given options.
	 * 
	 * @param inner
	 *            the function
	 * @param extract
	 *            the extractor
	 * @param insert
	 *            the insertor
	 */
	public ContextListFunction(Function<IN, OUT> inner, ContextExtractor<List<IN>> extract,
			ContextInsertor<List<OUT>> insert)
	{
		super(inner, extract, insert);
	}

	/**
	 * Construct with the function. The insertor and extractor are created from
	 * the given keys.
	 * 
	 * @param inner
	 *            the function
	 * @param extract
	 *            the key to extract from the context to produce the input for
	 *            the object
	 * @param insert
	 *            the key to insert with the the output for the object
	 */
	public ContextListFunction(Function<IN, OUT> inner, String extract, String insert)
	{
		super(inner, extract, insert);
	}

	/**
	 * Construct with the given function. The insertor and extractor are created
	 * from the same key, so the output will overwrite the input.
	 * 
	 * @param inner
	 *            the function
	 * @param both
	 *            the key to extract/insert
	 */
	public ContextListFunction(Function<IN, OUT> inner, String both)
	{
		super(inner, both, both);
	}

	@Override
	public Context apply(Context in) {
		final List<IN> obj = this.extract.extract(in);
		final List<OUT> out = new ArrayList<OUT>();

		for (final IN inItem : obj) {
			out.add(this.inner.apply(inItem));
		}

		this.insert.insert(out, in);

		return in;
	}
}
