package org.openimaj.text.nlp.namedentity;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import com.hp.hpl.jena.query.QuerySolution;

import edu.stanford.nlp.util.StringUtils;

/**
 * Using wikipedia artciles construct an index which associates entities in yago
 * to text documents which provide context
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YagoWikiIndexFactory {

	private boolean verbose = true;
	private static String wikiApiPrefix = "http://en.wikipedia.org/w/api.php?format=xml&action=query&titles=";
	private static String wikiApiSuffix = "&prop=revisions&rvprop=content";
	private final DocumentBuilderFactory docBuilderFactory;
	private DocumentBuilder docBuilder;
	private Document doc;
	private int noWikiCount;
	private int count;
	private final WikiModel wikiModel;
	private SparqlQueryPager pager;

	/**
	 * instantiate the lucene index and prepare the {@link WikiModel}
	 * 
	 * @param verbose
	 */
	public YagoWikiIndexFactory(boolean verbose) {
		// Initialize XML parsing objects
		this.verbose = verbose;
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilder = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc = null;
		wikiModel = new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");
	}

	/**
	 * Create the yago lucene index in the given indexPath location
	 * 
	 * @param indexPath
	 * @return the annotator encapsulating the context lucene index
	 * @throws IOException
	 */
	public EntityContextScorerLuceneWiki createFromIndexFile(String indexPath) throws IOException {
		final EntityContextScorerLuceneWiki yci = new EntityContextScorerLuceneWiki();
		final File f = new File(indexPath);
		if (f.isDirectory()) {
			yci.index = new SimpleFSDirectory(f);
		} else
			throw new IOException(indexPath + " does not exist or is not a directory");
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
	public EntityContextScorerLuceneWiki createFromSparqlEndPoint(String endPoint, String indexPath) throws IOException {
		final EntityContextScorerLuceneWiki yci = getEmptyYCI(endPoint, indexPath);
		jenaBuild(endPoint, yci);
		return yci;
	}

	/**
	 * @param companyUris
	 * @param indexPath
	 * @param endPoint
	 * @return the annotator encapsulating the context lucene index
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public EntityContextScorerLuceneWiki createFromYagoURIList(ArrayList<String> companyUris, String indexPath,
			String endPoint) throws CorruptIndexException, IOException
	{
		final EntityContextScorerLuceneWiki yci = getEmptyYCI(endPoint, indexPath);
		pager = new SparqlQueryPager(endPoint);
		final QuickIndexer qi = new QuickIndexer(yci.index);
		for (final String uri : companyUris) {
			// Context
			final String context = getContextFor(uri);

			final String[] values = { YagoQueryUtils.yagoResourceToString(uri), context };
			qi.addDocumentFromFields(yci.names, values, yci.types);
		}
		qi.finalise();
		return yci;
	}

	private void jenaBuild(String endPoint, EntityContextScorerLuceneWiki yci) throws CorruptIndexException, IOException {
		/*
		 * Get the companies
		 */
		final YagoEntityFinder ef = new YagoEntityFinder();
		final HashSet<String> companyUris = new HashSet<String>();
		for (final String uri : YagoQueryUtils.WORDNET_ORGANISATION_ROOT_URIS) {
			print("Getting from: " + uri);
			companyUris.addAll(ef.getLeafURIsFor(uri));
		}
		final int uniqueCount = companyUris.size();
		/*
		 * Get Context and put in the index
		 */
		final QuickIndexer qi = new QuickIndexer(yci.index);
		pager = new SparqlQueryPager(endPoint);
		noWikiCount = 0;
		count = 0;
		for (final String uri : companyUris) {
			count++;
			if ((count % 1000) == 0)
				print("Processed " + count + " out of " + uniqueCount);
			// Context
			final String context = getContextFor(uri);

			final String[] values = { YagoQueryUtils.yagoResourceToString(uri), context };
			qi.addDocumentFromFields(yci.names, values, yci.types);
		}
		print("Contexts built...\nNo Wiki: " + noWikiCount);
		qi.finalise();
	}

	private String getContextFor(String uri) {
		final StringBuffer context = new StringBuffer();
		final ArrayList<QuerySolution> pagerResults = pager.pageQuery(YagoQueryUtils.createdContextQuery(uri));

		for (final QuerySolution soln : pagerResults) {
			final String a = YagoQueryUtils.yagoResourceToString(soln.getResource("context").toString());
			context.append(a + ", ");
		}
		final ArrayList<QuerySolution> pagerResults2 = pager.pageQuery(YagoQueryUtils.wikiURLContextQuery(uri));
		boolean foundURL = false;
		for (final QuerySolution soln : pagerResults2) {
			final String a = soln.getLiteral("context").toString();
			final String url = YagoQueryUtils.yagoLiteralToString(a);
			final String title = url.substring(url.lastIndexOf("/") + 1);
			// Get markup dump from wikipedia;
			try {
				doc = docBuilder.parse(wikiApiPrefix + title + wikiApiSuffix);
			} catch (final SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			doc.getDocumentElement().normalize();
			final NodeList revisions = doc.getElementsByTagName("rev");
			if (revisions.getLength() > 0) {
				final String markup = revisions.item(0).getTextContent();

				// convert markup dump to plaintext.
				final String plainStr = wikiModel.render(new PlainTextConverter(), markup);
				// add it to the context.
				context.append(plainStr);
			}
			foundURL = true;
		}
		if (!foundURL) {
			noWikiCount++;
			print("No wiki page: " + uri);
		}
		return context.toString();
	}

	private EntityContextScorerLuceneWiki getEmptyYCI(String endPoint, String indexPath) throws IOException {
		final EntityContextScorerLuceneWiki yci = new EntityContextScorerLuceneWiki();
		// if indexPath null put it in memory
		if (indexPath == null) {
			print("Warning: Creating index in memory may take several hours...");
			yci.index = new RAMDirectory();
		} else {
			final File f = new File(indexPath);
			if (f.isDirectory()) {
				yci.index = new SimpleFSDirectory(f);
			} else
				throw new IOException(indexPath + " does not exist or is not a directory");
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
	public class EntityContextScorerLuceneWiki extends EntityContextScorer<List<String>> {

		private Directory index = null;
		private final String[] names = { "Company", "Context" };
		private final FieldType[] types;
		private final StopWordStripper ss;
		private QuickSearcher qs;

		private EntityContextScorerLuceneWiki() {
			final FieldType ti = new FieldType();
			ti.setIndexed(true);
			ti.setTokenized(true);
			ti.setStored(true);
			final FieldType n = new FieldType();
			n.setStored(true);
			n.setIndexed(true);
			types = new FieldType[3];
			types[0] = n;
			types[1] = ti;
			ss = new StopWordStripper(StopWordStripper.Language.ENGLISH);
			qs = null;
		}

		@Override
		public HashMap<String, Float> getScoredEntitiesFromContext(List<String> context) {
			if (qs == null)
				instantiateQS();
			final String contextString = StringUtils.join(ss.getNonStopWords(context), " ");
			try {
				// search on the context field
				return qs.search(names[1], names[0], contextString, 1);

			} catch (final ParseException e) {

				e.printStackTrace();
			} catch (final IOException e) {

				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Map<String, Float> getScoresForEntityList(List<String> entityUris, List<String> context) {
			if (qs == null)
				instantiateQS();
			final String contextString = StringUtils.join(ss.getNonStopWords(context), " ");
			return qs.searchFiltered(names[1], names[0], contextString, names[0], entityUris);
		}

		private void instantiateQS() {
			qs = new QuickSearcher(index, new StandardAnalyzer(Version.LUCENE_40));
		}
	}

}
