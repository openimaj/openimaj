/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.ml.dataset;

import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.MapBackedDataset;
import org.openimaj.experiment.annotations.DatasetDescription;

/**
 * A {@link Dataset} instance of the standard wine clustering experiment found
 * here:
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
@DatasetDescription(
		name = "Wine Data Set",
		description = "" +
				"These data are the results of a chemical analysis of wines grown in the same region in " +
				"Italy but derived from three different cultivars. The analysis determined the quantities " +
				"of 13 constituents found in each of the three types of wines." +
				"" +
				"I think that the initial data set had around 30 variables, but for some reason I only have " +
				"the 13 dimensional version. I had a list of what the 30 or so variables were, but a.) I lost" +
				" it, and b.), I would not know which 13 variables are included in the set." +
				"" +
				"The attributes are (dontated by Riccardo Leardi, riclea '@' anchem.unige.it )" +
				"1) Alcohol" +
				"2) Malic acid" +
				"3) Ash" +
				"4) Alcalinity of ash" +
				"5) Magnesium" +
				"6) Total phenols" +
				"7) Flavanoids" +
				"8) Nonflavanoid phenols" +
				"9) Proanthocyanins" +
				"10)Color intensity" +
				"11)Hue" +
				"12)OD280/OD315 of diluted wines" +
				"13)Proline" +
				"" +
				"In a classification context, this is a well posed problem with \"well behaved\" class structures." +
				" A good data set for first testing of a new classifier, but not very challenging. ",
				creator = "Forina, M. et al, PARVUS - ",
				url = "http://archive.ics.uci.edu/ml/datasets/Wine",
				downloadUrls = {
				"http://archive.ics.uci.edu/ml/machine-learning-databases/wine/wine.data"
		})
public class WineDataset extends MapBackedDataset<Integer, ListDataset<double[]>, double[]> {
	final static Logger logger = Logger.getLogger(WineDataset.class);

	/**
	 * Loads the wine dataset, mean centres the dataset
	 * 
	 * @param clusters
	 *            valid clusters, if empty all clusters are chosen
	 */
	public WineDataset(Integer... clusters) {
		this(true, clusters);
	}

	/**
	 * Loads the wine dataset from wine.data
	 * 
	 * @param normalise
	 *            whether to mean center the dataset
	 * @param clusters
	 *            valid clusters, if empty all clusters are chosen
	 */
	public WineDataset(boolean normalise, Integer... clusters) {
		final BufferedReader br = new BufferedReader(
				new InputStreamReader(WineDataset.class.getResourceAsStream("wine.data")));
		String line = null;
		Vector mean = null;
		Set<Integer> clusterSet = null;
		if (clusters.length != 0) {
			clusterSet = new HashSet<Integer>();
			clusterSet.addAll(Arrays.asList(clusters));
		}

		try {
			while ((line = br.readLine()) != null) {
				final String[] parts = line.split(",");
				final int cluster = Integer.parseInt(parts[0].trim());
				if (clusterSet != null && !clusterSet.contains(cluster))
					continue;
				final double[] data = new double[parts.length - 1];
				for (int i = 0; i < data.length; i++) {
					data[i] = Double.parseDouble(parts[i + 1]);
				}

				ListDataset<double[]> ds = this.get(cluster);
				if (ds == null)
					this.put(cluster, ds = new ListBackedDataset<double[]>());
				ds.add(data);
				final Vector copyArray = VectorFactory.getDefault().copyArray(data);
				if (mean == null) {
					mean = copyArray.clone();
				}
				else {
					mean.plusEquals(copyArray);
				}
			}
			mean.scaleEquals(1. / this.numInstances());
			if (normalise) {
				normalise(mean);
			}
		} catch (final Exception e) {
			logger.error("Wine dataset failed to load", e);
		}
	}

	private void normalise(Vector mean) {
		for (final double[] data : this) {
			for (int i = 0; i < data.length; i++) {
				data[i] -= mean.getElement(i);
			}
		}
	}
}
