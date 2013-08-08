package org.openimaj.tools.cbir;

import java.io.File;

import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.dense.gradient.dsift.FloatDSIFTKeypoint;

public class Test {
	public static void main(String[] args) {
		final File dir = new File("/Volumes/Raid/mirflickr/pyr-dsift/");

		for (final File f : dir.listFiles()) {
			try {
				MemoryLocalFeatureList.read(f, FloatDSIFTKeypoint.class);
			} catch (final Exception e) {
				System.out.println(f);
			}
		}
	}
}
