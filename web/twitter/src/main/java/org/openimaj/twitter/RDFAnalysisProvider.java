package org.openimaj.twitter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * The analysis held in {@link GeneralJSON} can be translated to JSON very
 * easily
 * The same analysis cannot be easily translated to RDF so this class must be
 * registered in GeneralJSONRDF's
 * map to do so.
 * 
 * What this means that if things add analysis to a {@link GeneralJSONRDF}
 * instance or a {@link USMFStatus} that
 * will eventually feed a {@link GeneralJSONRDF} instance, they should register
 * with
 * {@link GeneralJSONRDF#registerRDFAnalysisProvider(String, RDFAnalysisProvider)}
 * want to
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 */
public interface RDFAnalysisProvider<T> {
	/**
	 * When given this analysis, fill the model with triples representing its
	 * existence.
	 * The IRI of the status the analysis is against is also provided and must
	 * be
	 * added to the model with the IRI of the analysis also.
	 * 
	 * @param m
	 *            the model to add triples to
	 * @param analysis
	 *            the status to associate the analysis to
	 * @param analysisSource
	 *            the analysis to transform to triples
	 */
	public void addAnalysis(Model m, Resource analysis, GeneralJSON analysisSource);

	/**
	 * Prepare yourself. Called once at the beggining of a write of a batch.
	 */
	public void init();
}
