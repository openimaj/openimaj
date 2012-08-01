package org.openimaj.text.nlp.namedentity;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class YagoWikiIndexFactory {

	private static boolean verbose = true;
	private static String wikiApiPrefix = "http://en.wikipedia.org/w/api.php?format=xml&action=query&titles=";
	private static String wikiApiSuffix = "&prop=revisions&rvprop=content";
	private DocumentBuilderFactory docBuilderFactory;
	private DocumentBuilder docBuilder;
	private Document doc;
	private int noWikiCount;
	private int count;
	private WikiModel wikiModel;

	public YagoWikiIndexFactory(boolean verbose) {
		// Initialize XML parsing objects
		this.verbose = verbose;
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilder = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc = null;
		wikiModel = new WikiModel("http://www.mywiki.com/wiki/${image}",
				"http://www.mywiki.com/wiki/${title}");
	}

	public YagoWikiIndex createFromIndexFile(String indexPath)
			throws IOException {
		YagoWikiIndex yci = new YagoWikiIndex();
		File f = new File(indexPath);
		if (f.isDirectory()) {
			yci.index = new SimpleFSDirectory(f);
		} else
			throw new IOException(indexPath
					+ " does not exist or is not a directory");
		return yci;
	}	

	/**
	 * Creates a YagoCompanyIndex from a remote sparql endpoint. The underlying
	 * Index is created in the directory at indexPath.
	 * 
	 * @param endPoint
	 *            = remote sparql endpoint uri.
	 * @param indexPath
	 *            = path to directory for index to be built. Leave null to build
	 *            in memory.
	 * @return YagoCompanyIndex built from yago rdf
	 * @throws IOException
	 */
	public YagoWikiIndex createFromSparqlEndPoint(String endPoint,
			String indexPath) throws IOException {
		YagoWikiIndex yci = getEmptyYCI(endPoint, indexPath);
		jenaBuild(endPoint, yci);
		return yci;
	}

	public YagoWikiIndex createFromYagoURIList(
			ArrayList<String> companyUris, String indexPath, String endPoint)
			throws CorruptIndexException, IOException {
		YagoWikiIndex yci = getEmptyYCI(endPoint, indexPath);
		QuickIndexer qi = new QuickIndexer(yci.index);
		for (String uri : companyUris) {
			// Context
			String context = getContextFor(uri, endPoint);

			String[] values = {
					uri.substring(uri.lastIndexOf("/") + 1)
							.replaceAll("_", " ").trim(), context };
			qi.addDocumentFromFields(yci.names, values, yci.types);
		}
		qi.finalise();
		return yci;
	}

	private void jenaBuild(String endPoint, YagoWikiIndex yci)
			throws CorruptIndexException, IOException {
		/*
		 * Get the companies
		 */
		HashSet<String> companyUris = new HashSet<String>();
		int uniqueCount = 0;
		int dupCount = 0;
		Query q = QueryFactory.create(YagoQueryUtils.wordnetCompanyQuery());
		QueryExecution qexec;
		qexec = QueryExecutionFactory.sparqlService(endPoint, q);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				String uri = soln.getResource("company").getURI();
				if (!companyUris.contains(uri)) {
					companyUris.add(uri);
					uniqueCount++;
				} else
					dupCount++;
			}

		} finally {
			qexec.close();
		}
		SparqlQueryPager sqp = new SparqlQueryPager(endPoint);

		ArrayList<QuerySolution> qresults = sqp.pageQuery(YagoQueryUtils
				.subClassWordnetCompanyQuery());
		for (QuerySolution soln : qresults) {
			String uri = soln.getResource("company").getURI();
			if (!companyUris.contains(uri)) {
				companyUris.add(uri);
				uniqueCount++;
			} else
				dupCount++;
		}
		print("Company List built...\nUnique: " + uniqueCount + " Duplicate: "
				+ dupCount);

		/*
		 * Get Context and put in the index
		 */
		QuickIndexer qi = new QuickIndexer(yci.index);
		noWikiCount = 0;
		count = 0;
		for (String uri : companyUris) {
			count++;
			if ((count % 1000) == 0)
				print("Processed " + count + " out of " + uniqueCount);
			// Context
			String context = getContextFor(uri, endPoint);

			String[] values = { YagoQueryUtils.yagoResourceToString(uri),
					context };
			qi.addDocumentFromFields(yci.names, values, yci.types);
		}
		print("Contexts built...\nNo Wiki: " + noWikiCount);
		qi.finalise();
	}

	private String getContextFor(String uri, String endPoint) {
		StringBuffer context = new StringBuffer();
		Query q = QueryFactory.create(YagoQueryUtils.createdContextQuery(uri));
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endPoint, q);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				String a = YagoQueryUtils.yagoResourceToString(soln
						.getResource("context").toString());
				context.append(a + ", ");
			}
		} finally {
			qexec.close();
		}
		q = QueryFactory.create(YagoQueryUtils.wikiURLContextQuery(uri));
		qexec = QueryExecutionFactory.sparqlService(endPoint, q);
		try {
			ResultSet results = qexec.execSelect();
			boolean foundURL = false;
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				String a = soln.getLiteral("context").toString();
				String url = YagoQueryUtils.yagoLiteralToString(a);
				String title = url.substring(url.lastIndexOf("/") + 1);
				// Get markup dump from wikipedia;

				try {
					doc = docBuilder.parse(wikiApiPrefix + title
							+ wikiApiSuffix);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				doc.getDocumentElement().normalize();
				NodeList revisions = doc.getElementsByTagName("rev");
				if (revisions.getLength() > 0) {
					String markup = revisions.item(0).getTextContent();

					// convert markup dump to plaintext.
					String plainStr = wikiModel.render(
							new PlainTextConverter(), markup);
					// add it to the context.
					context.append(plainStr);
				}
				foundURL = true;
			}
			if (!foundURL) {
				noWikiCount++;
				print("No wiki page: " + uri);
			}
		} finally {
			qexec.close();
		}
		return context.toString();
	}
	
	private YagoWikiIndex getEmptyYCI(String endPoint, String indexPath) throws IOException{
		YagoWikiIndex yci = new YagoWikiIndex();
		// if indexPath null put it in memory
		if (indexPath == null) {
			print("Warning: Creating index in memory may take several hours...");
			yci.index = new RAMDirectory();
		} else {
			File f = new File(indexPath);
			if (f.isDirectory()) {
				yci.index = new SimpleFSDirectory(f);
			} else
				throw new IOException(indexPath
						+ " does not exist or is not a directory");
		}
		return yci;
	}

	private void print(String string) {
		if (verbose)
			System.out.println(string);
	}

	/**
	 * Class that uses an underlying lucene index to match tokens to companies.
	 * Use the enclosing factory class to instantiate.
	 * 
	 * @author laurence
	 * 
	 */
	public class YagoWikiIndex {

		private Directory index = null;
		private String[] names = { "Company", "Context" };
		private FieldType[] types;

		private YagoWikiIndex() {
			FieldType ti = new FieldType();
			ti.setIndexed(true);
			ti.setTokenized(true);
			ti.setStored(true);
			FieldType n = new FieldType();
			n.setStored(true);
			types = new FieldType[3];
			types[0] = n;
			types[1] = ti;
		}

		/**
		 * Returns candidate companies based on the whole string.
		 * 
		 * @param contextString
		 *            = untokenised string of context.
		 * @return candidate companies
		 */
		public HashMap<String, Float> getCompanyListFromContext(
				String contextString) {
			QuickSearcher qs = new QuickSearcher(index, new StandardAnalyzer(
					Version.LUCENE_40));
			HashMap<String, Float> results = new HashMap<String, Float>();
			try {
				// search on the context field
				return qs.search(names[1], names[0], contextString, 10);

			} catch (ParseException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			return null;
		}
	}

}
