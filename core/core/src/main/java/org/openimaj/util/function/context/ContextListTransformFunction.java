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
 * Transform a {@link List} of {@link Context} instances to a {@link List} of IN
 * and immediately hand this list to an internal function. Return this
 * function's application on the klist as stream
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <IN>
 * @param <OUT>
 */
public class ContextListTransformFunction<IN, OUT> implements Function<List<Context>, OUT> {
	private ContextExtractor<IN> extractor;
	private Function<List<IN>, OUT> inner;

	/**
	 * @param extract
	 *            the extraction strategy
	 * @param inner
	 *            the function applied to the transformed list
	 */
	public ContextListTransformFunction(ContextExtractor<IN> extract, Function<List<IN>, OUT> inner)
	{
		this.extractor = extract;
		this.inner = inner;
	}

	/**
	 * @param key
	 *            the key to extract (a {@link KeyContextExtractor} is used)
	 * @param inner
	 *            function applied to the transformed list
	 */
	public ContextListTransformFunction(String key, Function<List<IN>, OUT> inner) {
		this.extractor = new KeyContextExtractor<IN>(key);
		this.inner = inner;
	}

	@Override
	public OUT apply(List<Context> in) {
		final List<IN> ret = new ArrayList<IN>();
		for (final Context context : in) {
			ret.add(this.extractor.extract(context));
		}
		return inner.apply(ret);
	}
}
