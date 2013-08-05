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
public class ClusertEvalDataset extends MapBackedDataset<String, ListBackedDataset<Integer>, Integer> {
	
	/**
	 * Read from this file location
	 * @param loc
	 * @throws IOException
	 */
	public ClusertEvalDataset(String loc) throws IOException {
		this(new File(loc));
	}
	/**
	 * Load this file as a dataset
	 * @param f
	 * @throws IOException 
	 */
	public ClusertEvalDataset(File f) throws IOException {
		this(new FileInputStream(f));
	}

	/**
	 * Read the clusters from this stream
	 * @param is
	 * @throws IOException 
	 */
	public ClusertEvalDataset(InputStream is) throws IOException {
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
