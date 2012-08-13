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
