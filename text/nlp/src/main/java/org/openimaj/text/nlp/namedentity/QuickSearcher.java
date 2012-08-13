package org.openimaj.text.nlp.namedentity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
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
	public HashMap<String, Float> search(String searchfieldName, String returnFieldName, String queryStr, int limit)
			throws ParseException, IOException
	{
		if (queryStr == null || queryStr.length() == 0)
			return new HashMap<String, Float>();
		final String clean = QueryParser.escape(queryStr);
		final Query q = new QueryParser(Version.LUCENE_40, searchfieldName, analyser).parse(clean);
		final TopScoreDocCollector collector = TopScoreDocCollector.create(limit, true);

		searcher.search(q, collector);
		final ScoreDoc[] hits = collector.topDocs().scoreDocs;
		final HashMap<String, Float> results = new HashMap<String, Float>();
		for (int i = 0; i < hits.length; ++i) {
			final int docId = hits[i].doc;
			final Document d = searcher.doc(docId);
			results.put(d.get(returnFieldName), hits[i].score);
		}
		return results;
	}

	/**
	 * TODO: Laurence, clarify what this does!
	 * 
	 * @see #search(String, String, String, int)
	 * @param searchfieldName
	 * @param returnFieldName
	 * @param queryStr
	 * @param filterFieldName
	 * @param filterQueries
	 * @return same as the other search
	 */
	public HashMap<String, Float> searchFiltered(String searchfieldName, String returnFieldName, String queryStr,
			String filterFieldName, List<String> filterQueries)
	{
		if (queryStr == null || queryStr.length() == 0)
			return new HashMap<String, Float>();
		final HashMap<String, Float> results = new HashMap<String, Float>();
		final BooleanQuery bq = new BooleanQuery();
		for (final String filterValue : filterQueries) {
			bq.add(new TermQuery(new Term(filterFieldName, filterValue)), Occur.SHOULD);
		}
		final QueryWrapperFilter qf = new QueryWrapperFilter(bq);
		final String clean = QueryParser.escape(queryStr);
		Query q = null;
		try {
			q = new QueryParser(Version.LUCENE_40, searchfieldName, analyser).parse(clean);
		} catch (final ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final TopScoreDocCollector collector = TopScoreDocCollector.create(filterQueries.size(), true);
		try {
			searcher.search(q, qf, collector);
			final ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (int i = 0; i < hits.length; ++i) {
				final int docId = hits[i].doc;
				final Document d = searcher.doc(docId);
				results.put(d.get(returnFieldName), hits[i].score);
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}

}
