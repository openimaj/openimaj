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
package org.openimaj.text.nlp.namedentity;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

/**
 * Wrapper around a lucene index constructor
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class QuickIndexer {

	private final Directory index;
	private IndexWriter writer;
	private boolean finalised = false;
	private final StandardAnalyzer analyzer;

	/**
	 * @param index
	 *            construct a lucene index in this directory
	 */
	public QuickIndexer(Directory index) {
		this.index = index;
		analyzer = new StandardAnalyzer(Version.LUCENE_40);
		final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		try {
			writer = new IndexWriter(index, config);
		} catch (final CorruptIndexException e) {
			e.printStackTrace();
		} catch (final LockObtainFailedException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param entry
	 *            document to index
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void addDocument(Document entry) throws CorruptIndexException, IOException {
		if (!finalised)
			writer.addDocument(entry);
	}

	/**
	 * construct a document from names, values and types
	 * 
	 * @param names
	 * @param values
	 * @param type
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void addDocumentFromFields(String[] names, String[] values, FieldType[] type) throws CorruptIndexException,
			IOException
	{
		if (!finalised) {
			final Document doc = new Document();
			for (int i = 0; i < names.length; i++) {
				doc.add(new Field(names[i], values[i], type[i]));
			}
			writer.addDocument(doc);
		}
	}

	/**
	 * call {@link IndexWriter#close()}
	 * 
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void finalise() throws CorruptIndexException, IOException {
		writer.close();
		finalised = true;
	}

	/**
	 * @return the underlying {@link Directory}
	 */
	public Directory getIndex() {
		return index;
	}

	/**
	 * @return the underlying {@link StandardAnalyzer}
	 */
	public StandardAnalyzer getAnalyzer() {
		return analyzer;
	}

}
