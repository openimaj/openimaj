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
package org.openimaj.experiment.dataset.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.MapBackedDataset;

/**
 * Reads datasets of items and cluster labels from the clustereval tool:
 * http://chris.de-vries.id.au/2013/06/clustereval-10-release.html
 * 
 * The general format of clustereval is:
 * item cluster_label*
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ClusterEvalDataset extends MapBackedDataset<String, ListBackedDataset<Integer>, Integer> {
	
	/**
	 * Read from this file location
	 * @param loc
	 * @throws IOException
	 */
	public ClusterEvalDataset(String loc) throws IOException {
		this(new File(loc));
	}
	/**
	 * Load this file as a dataset
	 * @param f
	 * @throws IOException 
	 */
	public ClusterEvalDataset(File f) throws IOException {
		this(new FileInputStream(f));
	}

	/**
	 * Read the clusters from this stream
	 * @param is
	 * @throws IOException 
	 */
	public ClusterEvalDataset(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while((line = reader.readLine())!=null){
			String[] itemclusterparts = line.split(" ");
			int item = Integer.parseInt(itemclusterparts[0]);
			for (int i = 1; i < itemclusterparts.length; i++) {
				String clusterLabel = itemclusterparts[i];
				ListBackedDataset<Integer> list = this.get(clusterLabel);
				if(list == null){
					this.put(clusterLabel, list = new ListBackedDataset<Integer>());
				}
				list.add(item);
			}
		}
	}
	/**
	 * @return the dataset as clusters
	 */
	public int[][] toClusters() {
		int[][] clusters = new int[this.size()][];
		int i = 0;
		for (Entry<String, ListBackedDataset<Integer>> is : this.entrySet()) {
			clusters[i] = new int[is.getValue().size()];
			int j = 0;
			for (int js : is.getValue()) {
				clusters[i][j++] = js;
			}
			i++;
		}
		return clusters;
	}
}
