package org.openimaj.rdf.storm.tool.lang;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * The various supported rule sets
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum RuleLanguageMode implements CmdLineOptionsProvider {
	/**
	 * Return a {@link JenaRuleLanguageHandler}
	 */
	JENA {
		@Override
		public RuleLanguageHandler getOptions() {
			return new JenaRuleLanguageHandler();
		}
	},
	/**
	 * CSPARQL parser
	 */
	SPARQL {

		@Override
		public RuleLanguageHandler getOptions() {
			return new SPARQLRuleLanguageHandler();
		}

	},
	/**
	 * CSPARQL parser which performs no work
	 */
	IDENTITY_SPARQL {

		@Override
		public RuleLanguageHandler getOptions() {
			return new IdentitySPARQLRuleLanguageHandler();
		}

	};

	@Override
	public abstract RuleLanguageHandler getOptions();

}
