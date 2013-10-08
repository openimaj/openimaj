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
import org.openimaj.util.function.Operation;

/**
 * An adaptor that allows an {@link Operation} to be applied to a single element
 * of the {@link Context}.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of element that the operation is applied to
 */
public class ContextOperationAdaptor<T> extends ContextAdaptor<Operation<T>, T, T> implements Operation<Context> {
	/**
	 * Construct with the given operation and extractor
	 * 
	 * @param inner
	 *            the operation
	 * @param extract
	 *            the extractor
	 */
	public ContextOperationAdaptor(Operation<T> inner, ContextExtractor<T> extract)
	{
		super(inner, extract, null);
	}

	/**
	 * Construct with the given operation. The extractor is a
	 * {@link KeyContextExtractor} created from the given key.
	 * 
	 * @param inner
	 *            the operation
	 * @param extract
	 *            the key
	 */
	public ContextOperationAdaptor(Operation<T> inner, String extract)
	{
		super(inner, extract, extract);
	}

	@Override
	public void perform(Context object) {
		inner.perform(extract.extract(object));
	}

	/**
	 * Helper to create a new {@link ContextOperationAdaptor}.
	 * 
	 * @param operation
	 *            the operation
	 * @param extract
	 *            the key to extract from
	 * 
	 * @see ContextOperationAdaptor#ContextOperationAdaptor(Operation,
	 *      ContextExtractor)
	 * 
	 * @return the context operation
	 */
	public static <IN> Operation<Context> create(Operation<IN> operation, String extract) {
		return new ContextOperationAdaptor<IN>(operation, extract);
	}
}
