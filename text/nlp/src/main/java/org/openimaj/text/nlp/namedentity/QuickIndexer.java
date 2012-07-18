package org.openimaj.text.nlp.namedentity;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

public class QuickIndexer {

	private Directory index;
	private IndexWriter writer;
	private boolean finalised=false;
	private StandardAnalyzer analyzer;

	public QuickIndexer(Directory index) {
		this.index = index;
		analyzer = new StandardAnalyzer(Version.LUCENE_40);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40,
				analyzer);
		try {
			writer = new IndexWriter(index,config);
		} catch (CorruptIndexException e) {			
			e.printStackTrace();
		} catch (LockObtainFailedException e) {		
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addDocument(Document entry) throws CorruptIndexException, IOException {
		if(!finalised)
		writer.addDocument(entry);
	}
	
	public void addDocumentFromFields(String[] names, String[] values, FieldType[] type) throws CorruptIndexException, IOException{
		if (!finalised) {
			Document doc = new Document();
			for (int i = 0; i < names.length; i++) {
				doc.add(new Field(names[i], values[i], type[i]));
			}
			writer.addDocument(doc);
		}
	}
	
	public void finalise() throws CorruptIndexException, IOException{
		writer.close();
		finalised=true;
	}
	
	public Directory getIndex(){
		return index;
	}

	public StandardAnalyzer getAnalyzer() {
		return analyzer;
	}
	
	
	
	

}
