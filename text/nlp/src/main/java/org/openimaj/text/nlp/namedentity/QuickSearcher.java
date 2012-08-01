package org.openimaj.text.nlp.namedentity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;


public class QuickSearcher {
	
	IndexSearcher searcher;
	Analyzer analyser;
	
	public QuickSearcher(Directory index, Analyzer analyser){
		try {
			DirectoryReader reader = DirectoryReader.open(index);
			searcher = new IndexSearcher(reader);
		} catch (CorruptIndexException e) {		
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {			
			e.printStackTrace();
			System.exit(1);
		}
		this.analyser=analyser;
		
	}
	
	public HashMap<String, Float> search(String searchfieldName, String returnFieldName, String queryStr, int limit) throws ParseException, IOException{
		Query q = new QueryParser(Version.LUCENE_40, searchfieldName, analyser).parse(queryStr);
		TopScoreDocCollector collector = TopScoreDocCollector.create(limit, true);
	    
		searcher.search(q, collector);
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    HashMap<String,Float> results = new HashMap<String,Float>();
	    for(int i=0;i<hits.length;++i) {
	        int docId = hits[i].doc;	        
	        Document d = searcher.doc(docId);	        
	        results.put(d.get(returnFieldName), hits[i].score);	        
	      }
	    return results;
	}

}
