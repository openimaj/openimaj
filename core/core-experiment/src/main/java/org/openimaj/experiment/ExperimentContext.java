package org.openimaj.experiment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.experiment.annotations.DatasetDescription;
import org.openimaj.experiment.annotations.DependentVariable;
import org.openimaj.experiment.annotations.Experiment;
import org.openimaj.experiment.annotations.IndependentVariable;

public class ExperimentContext {
	Experiment experimentDetails;
	List<DatasetDescription> datasets;
	Set<Reference> bibliography;
	Map<String, long[]> timingInfo;
	Map<IndependentVariable, Object> independentVariables;
	Map<DependentVariable, Object> dependentVariables;
}
