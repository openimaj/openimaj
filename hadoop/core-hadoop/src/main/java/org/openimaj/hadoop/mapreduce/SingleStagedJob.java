package org.openimaj.hadoop.mapreduce;

import org.apache.hadoop.fs.Path;
import org.openimaj.hadoop.mapreduce.stage.Stage;

public class SingleStagedJob extends StageRunner{
	private Path[] inputs;
	private Path output;
	private Stage<?, ?, ?, ?, ?, ?, ?, ?> stage;

	public SingleStagedJob(Path[] inputs, Path output) {
		this.inputs = inputs;
		this.output = output;
	}

	public SingleStagedJob(Stage<?, ?, ?, ?, ?, ?, ?, ?> s,Path[] currentInputs, Path constructedOutputPath) {
		this.inputs = inputs;
		this.output = output;
		this.stage = s;
	}

	@Override
	public Path output() {
		return output;
	}

	@Override
	public Path[] inputs() {
		return inputs;
	}

	@Override
	public void args(String[] args) {}

	@Override
	public Stage<?, ?, ?, ?, ?, ?, ?, ?> stage() {
		return stage;
	}

	public void setStage(Stage<?, ?, ?, ?, ?, ?, ?, ?> stage) {
		this.stage = stage;
	}
	
}