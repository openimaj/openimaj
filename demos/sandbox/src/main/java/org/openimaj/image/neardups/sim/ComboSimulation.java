package org.openimaj.image.neardups.sim;

import org.openimaj.image.MBFImage;

public class ComboSimulation extends Simulation {
	protected Simulation [] simulations;
	
	public ComboSimulation(int seed, Simulation... simulations) {
		super(seed);
		this.simulations = simulations;
	}

	@Override
	public MBFImage applySimulation(MBFImage input) {
		for (Simulation simulation : simulations) {
			input = simulation.applySimulation(input);
		}
		return input;
	}
	
}
