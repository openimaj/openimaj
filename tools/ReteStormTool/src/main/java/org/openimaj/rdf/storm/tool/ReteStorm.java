package org.openimaj.rdf.storm.tool;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.rdf.storm.topology.RuleReteStormTopologyFactory;

/**
 * The rete storm tool wraps the functionality of
 * {@link RuleReteStormTopologyFactory} and
 * allows the construction and deployment of Rete topologies based on various
 * rule languages
 * 
 * Currently only the Jena rules language is supported.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class ReteStorm {
	Logger logger = Logger.getLogger(ReteStorm.class);
	ReteStormOptions options;

	/**
	 * Prepare and launch the ReteStorm
	 * 
	 * @param args
	 * @throws Exception
	 */
	public ReteStorm(String[] args) throws Exception {
		options = new ReteStormOptions(args);
		logger.debug("Parsing arguments");
		options.prepare();
	}

	private void submitTopology() throws Exception {
		logger.info("Initialising monitors");
		this.options.initMonitor();
		logger.info("Submitting topology");
		this.options.tmOp.submitTopology(this.options);
		logger.info("Starting monitors");
		this.options.startMonitor();

	}

	/**
	 * Runs the tool
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Throwable {
		ReteStorm storm = new ReteStorm(args);

		storm.perform();
	}

	private void perform() throws Throwable {
		if (this.options.prepopulate) {
			prepareInputs();
			populateInputs();
			submitTopology();
			toolComplete();
		} else {
			prepareInputs();
			submitTopology();
			populateInputs();
			toolComplete();
		}
	}

	private void prepareInputs() throws TException {
		this.options.prepareQueues();
	}

	private void toolComplete() throws Exception {
		logger.info("Finishing the tool");
		this.options.tmOp.finish(options);
		logger.info("Tool finished!");
	}

	private void populateInputs() throws TException, IOException {
		this.options.populateInputs();
	}
}
