package org.openimaj.rdf.storm.topology.bolt;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.writing.NTripleWritingScheme;

import backtype.storm.spout.KestrelThriftClient;
import backtype.storm.tuple.Tuple;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelReteConflictSetBolt extends ReteConflictSetBolt {
	protected final static Logger logger = Logger.getLogger(KestrelReteConflictSetBolt.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -2288621098852277643L;
	private String outputQueue;
	private KestrelThriftClient client;
	private NTripleWritingScheme scheme;
	private int spec_port;
	private String spec_host;
	private String inputQueue;

	/**
	 * Hold the kestrel server and queues to emit to.
	 * 
	 * @param spec
	 * @param inputQueue
	 * @param outputQueue
	 */
	public KestrelReteConflictSetBolt(KestrelServerSpec spec, String inputQueue, String outputQueue) {
		this.spec_host = spec.host;
		this.spec_port = spec.port;
		this.outputQueue = outputQueue;
		this.inputQueue = inputQueue;
	}

	@Override
	protected void prepare() {
		try {
			client = new KestrelThriftClient(spec_host, spec_port);
			scheme = new NTripleWritingScheme();
		} catch (TException e) {

		}

	}

	@Override
	protected void emitTriple(Tuple input, Triple t) {
		if (client != null) {
			logger.debug(String.format("Adding triple %s to queue %s and %s", t.toString(), this.inputQueue, this.outputQueue));
			List<Object> tripleList = Arrays.asList((Object) t);
			byte[] serialised = this.scheme.serialize(tripleList);
			try {
				this.client.put(this.outputQueue, Arrays.asList(ByteBuffer.wrap(serialised)), 0);
				this.client.put(this.inputQueue, Arrays.asList(ByteBuffer.wrap(serialised)), 0);
			} catch (TException e) {
				logger.error("Failed to write to client: " + e.getMessage());
			}
		}
	}

}
