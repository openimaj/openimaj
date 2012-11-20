package org.openimaj.rdf.storm.sparql.topology.bolt.sink;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt;
import org.openimaj.rdf.storm.sparql.topology.bolt.StormSPARQLReteConflictSetBolt.StormSPARQLReteConflictSetBoltSink;
import org.openjena.riot.RiotWriter;

import backtype.storm.spout.KestrelThriftClient;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

/**
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelConflictSetSink implements StormSPARQLReteConflictSetBoltSink {

	private static final Logger logger = Logger.getLogger(KestrelConflictSetSink.class);
	private List<String> kestrelServers;
	private String queue;
	private int port;
	private List<KestrelThriftClient> clients;
	private int clientIndex = 0;
	private QuerySolutionSerializer qss;
	private List<String> varOrder;

	/**
	 * The kestrel servers and queue which will be written to with triples or
	 * bindings (depending on the query)
	 * 
	 * @param serverSpecs
	 *            the kestrel servers to write to (in a round robin fasion)
	 * @param queue
	 *            the queue in any given server to write to
	 */
	public KestrelConflictSetSink(List<KestrelServerSpec> serverSpecs, String queue) {
		this.kestrelServers = new ArrayList<String>();
		this.port = 0;
		this.queue = queue;
		this.qss = QuerySolutionSerializer.RDF_NTRIPLES;
		for (KestrelServerSpec kestrelServerSpec : serverSpecs) {
			this.kestrelServers.add(kestrelServerSpec.host);
			this.port = kestrelServerSpec.port;
		}
	}

	/**
	 * see {@link #KestrelConflictSetSink(List, String)}. Also allows the
	 * specification of
	 * the {@link QuerySolutionSerializer} assuming the query is a select
	 * 
	 * @param streamDataSources
	 * @param outputQueue
	 * @param qss
	 */
	public KestrelConflictSetSink(List<KestrelServerSpec> streamDataSources, String outputQueue,
			QuerySolutionSerializer qss) {
		this(streamDataSources, outputQueue);
		this.qss = qss;
	}

	@Override
	public void instantiate(StormSPARQLReteConflictSetBolt conflictSet) {
		Query query = conflictSet.getQuery();
		if (query.isSelectType()) {
			this.varOrder = query.getResultVars();
		}
		clients = new ArrayList<KestrelThriftClient>();
		for (String server : kestrelServers) {
			try {
				KestrelThriftClient client = new KestrelThriftClient(server, port);
				this.clients.add(client);
			} catch (TException e) {
				logger.error("Failed to create Kestrel client for host: " + server);
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void consumeTriple(Triple triple) {
		try {
			KestrelThriftClient client = nextClient();
			Graph graph = GraphFactory.createGraphMem();
			graph.add(triple);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RiotWriter.writeTriples(baos, graph);
			baos.flush();
			byte[] serialised = baos.toByteArray();
			client.put(this.queue, Arrays.asList(ByteBuffer.wrap(serialised)), 0);
		} catch (Exception e) {
			logger.error("Failed to write triple to kestrel client, " + e.getMessage(), e);
		}

	}

	private KestrelThriftClient nextClient() {
		KestrelThriftClient kestrelThriftClient = this.clients.get(clientIndex);
		clientIndex++;
		if (clientIndex == clients.size())
			clientIndex = 0;
		return kestrelThriftClient;
	}

	@Override
	public void consumeBindings(QueryIterator bindingsIter) {
		try {
			KestrelThriftClient client = nextClient();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			qss.serialize(bindingsIter, varOrder, baos);
			byte[] serialised = baos.toByteArray();
			client.put(this.queue, Arrays.asList(ByteBuffer.wrap(serialised)), 0);
		} catch (Exception e) {
			logger.error("Failed to write bindings to kestrel client, " + e.getMessage(), e);
		}
	}
}
