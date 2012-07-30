package org.openimaj.experiment;

import java.util.List;
import java.util.Map;

import org.openimaj.citation.annotation.References;
import org.openimaj.experiment.annotations.Dataset;
import org.openimaj.experiment.annotations.DependentVariable;
import org.openimaj.experiment.annotations.Experiment;
import org.openimaj.experiment.annotations.IndependentVariable;

public class ExperimentContext {
	Experiment experimentDetails;
	List<Dataset> datasets;
	List<References> bibliography;
	Map<String, long[]> timingInfo;
	Map<IndependentVariable, Object> independentVariables;
	Map<DependentVariable, Object> dependentVariables;
}
