package org.openimaj.experiment;

import java.util.List;
import java.util.Map;

import org.openimaj.citation.annotation.References;

public interface RunnableExperiment {
	public void setup();
	public void perform();
	public void finish(ExperimentContext context);
}
