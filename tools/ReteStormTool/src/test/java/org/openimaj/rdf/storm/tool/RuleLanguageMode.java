package org.openimaj.rdf.storm.tool;

import org.kohsuke.args4j.CmdLineOptionsProvider;

public enum RuleLanguageMode implements CmdLineOptionsProvider{
	JENA {
		@Override
		public RuleLanguageHandler getOptions() {
			return new JenaRuleLanguageHandler();
		}
	};

	@Override
	public abstract RuleLanguageHandler getOptions() ;


}
