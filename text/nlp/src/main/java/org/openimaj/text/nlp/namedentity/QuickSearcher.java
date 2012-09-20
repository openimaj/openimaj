package org.openimaj.text.nlp.namedentity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 * Given a lucene {@link Directory} index and an {@link Analyzer} allow for
 * searches of particular fields.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class QuickSearcher {

	IndexSearcher searcher;
	Analyzer analyser;

	/**
	 * the index to search and the analyser to use to process queries
	 * 
	 * @param index
	 * @param analyser
	 */
	public QuickSearcher(Directory index, Analyzer analyser) {
		try {
			final DirectoryReader reader = DirectoryReader.open(index);
			searcher = new IndexSearcher(reader);
		} catch (final CorruptIndexException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.analyser = analyser;

	}

	/**
	 * Given a search field to search,the name of the field to return results in
	 * and a query string, return search results up to the limit.
	 * 
	 * @param searchfieldName
	 * @param returnFieldName
	 * @param queryStr
	 * @param limit
	 * @return search results (with confidences)
	 * @throws ParseException
	 * @throws IOException
	 */
	public HashMap<String[], Float> search(String searchfieldName,
			String[] returnFieldName, String queryStr, int limit)
			throws ParseException, IOException {
		if (queryStr == null || queryStr.length() == 0)
			return new HashMap<String[], Float>();
		final String clean = QueryParser.escape(queryStr);
		final Query q = new QueryParser(Version.LUCENE_40, searchfieldName,
				analyser).parse(clean);
		final TopScoreDocCollector collector = TopScoreDocCollector.create(
				limit, true);

		searcher.search(q, collector);
		final ScoreDoc[] hits = collector.topDocs().scoreDocs;
		final HashMap<String[], Float> results = new HashMap<String[], Float>();
		for (int i = 0; i < hits.length; ++i) {
			final int docId = hits[i].doc;
			final Document d = searcher.doc(docId);
			String[] rvalues = new String[returnFieldName.length];
			for(int j=0;j<rvalues.length;j++){
				rvalues[j]=d.get(returnFieldName[j]);
			}
			results.put(rvalues, hits[i].score);
		}
		return results;
	}

	/**
	 * Given a list of values for the filterField, this method will return the
	 * scores of a search for the documents which satisfy one of those filter
	 * values.
	 * 
	 * @see #search(String, String[], String, int)
	 * @param searchfieldName = Name of the field to search
	 * @param returnFieldName = Name of the Field to return
	 * @param queryStr = String that should be used to search
	 * @param filterFieldName = Name of field to filter on
	 * @param filterQueries = Values of the filterField. Only documents with one of these values will be returned.
	 * @return same as the other search
	 */
	public HashMap<String[], Float> searchFiltered(String searchfieldName,
			String[] returnFieldName, String queryStr, String filterFieldName,
			List<String> filterQueries) {
		if (queryStr == null || queryStr.length() == 0)
			return new HashMap<String[], Float>();
		HashMap<String[], Float> results = new HashMap<String[], Float>();
		//Make the query a filter
		TermsFilter qf = new TermsFilter();
		for (String filterValue : filterQueries) {
			qf.addTerm(new Term(filterFieldName, filterValue));
		}
		final String clean = QueryParser.escape(queryStr);
		Query q = null;
		try {
			q = new QueryParser(Version.LUCENE_40, searchfieldName, analyser)
					.parse(clean);
		} catch (final ParseException e) {	
			e.printStackTrace();
		}
		try {			
			final ScoreDoc[] hits = searcher.search(q, qf, filterQueries.size()).scoreDocs;
			for (int i = 0; i < hits.length; ++i) {
				final int docId = hits[i].doc;
				final Document d = searcher.doc(docId);
				String[] rvalues = new String[returnFieldName.length];
				for(int j=0;j<rvalues.length;j++){
					rvalues[j]=d.get(returnFieldName[j]);
				}
				results.put(rvalues, hits[i].score);
			}
		} catch (final IOException e) {			
			e.printStackTrace();
		}
		return results;
		//TODO: Scores of 0 are not returned by the lucene searcher. Need to fill in the 0's and also have fallback disambiguation
	}

}
