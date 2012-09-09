package org.openimaj.image.neardups;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.neardups.sim.ComboSimulation;
import org.openimaj.image.neardups.sim.CompressSimulation;
import org.openimaj.image.neardups.sim.CropSimulation;
import org.openimaj.image.neardups.sim.Rotate90Simulation;
import org.openimaj.image.neardups.sim.Simulation;
import org.openimaj.image.neardups.sim.WatermarkSimulation;

public class SimulationDriver {
	public static void main(String[] args) throws IOException {
		final int seed = 43;

		final MBFImage input = ImageUtilities.readMBF(new File("/Users/jsh2/Data/ukbench/full/ukbench00000.jpg"));

		// Simulation sim = new CropSimulation(seed);
		// Simulation sim = new ArbitaryRotateSimulation(seed);
		// Simulation sim = new Rotate90Simulation(seed);
		// Simulation sim = new CompressSimulation(seed);
		// Simulation sim = new UniformScaleSimulation(seed);
		// Simulation sim = new ArbitaryStretchSimulation(seed);
		// Simulation sim = new GreyscaleSimulation(seed);
		// Simulation sim = new WatermarkSimulation(seed);

		final Simulation sim = new ComboSimulation(seed,
				new Rotate90Simulation(seed),
				new CropSimulation(seed),
				new WatermarkSimulation(seed),
				new CompressSimulation(seed)
				);

		for (int i = 0; i < 10; i++) {
			DisplayUtilities.display(sim.applySimulation(input));
		}
	}
}
