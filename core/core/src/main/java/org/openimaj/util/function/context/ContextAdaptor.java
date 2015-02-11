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
 * Base class that holds the elements required to adapt something (i.e. a
 * {@link Function}) to work around around a single element of a {@link Context}
 * and output to a potentially different key of that {@link Context}.
 * <p>
 * This base class just holds the parts required for the adaption (the object
 * being adapted, and the {@link ContextExtractor} and {@link ContextInsertor}),
 * but doesn't actually provide any functionality.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <ADAPTED>
 *            The type of object being adapted
 * @param <IN>
 *            The input type of the object being adapted
 * @param <OUT>
 *            The output type of the object being adapted
 */
public abstract class ContextAdaptor<ADAPTED, IN, OUT> {
	protected ADAPTED inner;
	protected ContextExtractor<IN> extract;
	protected ContextInsertor<OUT> insert;

	/**
	 * Construct with the given options.
	 *
	 * @param inner
	 *            the object being adapted
	 * @param extract
	 *            the extractor
	 * @param insert
	 *            the insertor
	 */
	public ContextAdaptor(ADAPTED inner, ContextExtractor<IN> extract, ContextInsertor<OUT> insert) {
		this.inner = inner;
		this.insert = insert;
		this.extract = extract;
	}

	/**
	 * Construct with the given object to adapt. The insertor and extractor are
	 * created from the given keys.
	 *
	 * @param inner
	 *            the object being adapted
	 * @param keyin
	 *            the key to extract from the context to produce the input for
	 *            the object
	 * @param keyout
	 *            the key to insert with the the output for the object
	 */
	public ContextAdaptor(ADAPTED inner, String keyin, String keyout) {
		this.inner = inner;
		this.extract = new KeyContextExtractor<IN>(keyin);
		this.insert = new KeyContextInsertor<OUT>(keyout);
	}
}
