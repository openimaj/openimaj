package org.openimaj.rdf.storm.tool.monitor;

import org.kohsuke.args4j.CmdLineOptionsProvider;

public enum MonitorModeOption implements CmdLineOptionsProvider{
	KESTREL{

		@Override
		public MonitorMode getOptions() {
			return new KestrelQueueStatsMonitorMode();
		}

	};

	@Override
	public abstract MonitorMode getOptions();

}
