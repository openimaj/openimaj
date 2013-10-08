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
package org.openimaj.pgm.util;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.io.FileUtils;

/**
 * A corpus from a document whose lines are documents and whose words are
 * seperated by a space
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SimpleCorpusReader implements CorpusReader {

	private InputStream is;

	/**
	 * @param resourceAsStream
	 */
	public SimpleCorpusReader(InputStream resourceAsStream) {
		is = resourceAsStream;
	}

	@Override
	public Corpus readCorpus() throws IOException {
		final String[] lines = FileUtils.readlines(is);
		final Map<String, Integer> vocabulary = new HashMap<String, Integer>();
		final List<TIntIntHashMap> docs = new ArrayList<TIntIntHashMap>();
		for (final String docLine : lines) {
			final String[] words = docLine.split(" ");
			final TIntIntHashMap d = new TIntIntHashMap();
			for (final String word : words) {
				Integer value = 0;
				if ((value = vocabulary.get(word)) == null) {
					vocabulary.put(word, value = vocabulary.size());
				}
				d.adjustValue(value, 1);
			}
			docs.add(d);
		}
		final Corpus c = new Corpus(vocabulary.size());
		for (final TIntIntHashMap doc : docs) {
			final Document d = new Document(c);
			doc.forEachEntry(new TIntIntProcedure() {
				@Override
				public boolean execute(int word, int count) {
					d.values.set(word, count);
					return true;
				}
			});
			c.addDocument(d);
		}

		return c;
	}

}
