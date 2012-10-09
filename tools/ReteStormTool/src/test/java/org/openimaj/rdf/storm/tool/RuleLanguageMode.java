package org.openimaj.rdf.storm.tool;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * The various supported rule sets
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum RuleLanguageMode implements CmdLineOptionsProvider{
	/**
	 * Return a {@link JenaRuleLanguageHandler}
	 */
	JENA {
		@Override
		public RuleLanguageHandler getOptions() {
			return new JenaRuleLanguageHandler();
		}
	};

	@Override
	public abstract RuleLanguageHandler getOptions() ;


}
