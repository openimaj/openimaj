package org.openimaj.ml.clustering.spectral;

import gnu.trove.list.array.TIntArrayList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.openimaj.ml.clustering.SimilarityClusters;
import org.openimaj.ml.clustering.TrainingIndexClusters;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class Clusters implements SimilarityClusters, TrainingIndexClusters{
	private int[][] clusters;
	private int nEntries;
	Clusters() {
	}
	
	/**
	 * Counts the entries in the clusters
	 * @param clusters
	 */
	public Clusters(int[][] clusters){
		this.clusters = clusters;
		this.nEntries = 0;
		for (int[] is : clusters) {
			this.nEntries += + is.length;
		}
		
	}
	/**
	 * @param clusters
	 * @param entries
	 */
	public Clusters(int[][] clusters, int entries){
		this.clusters = clusters;
		this.nEntries = entries;
	}
	/**
	 * @param assignments convert a list of cluster assignments to a 2D array to cluster to assignments
	 */
	public Clusters(int[] assignments) {
		this.nEntries = assignments.length;
		Map<Integer,TIntArrayList> clusters = new HashMap<Integer, TIntArrayList>();
		for (int i = 0; i < assignments.length; i++) {
			int ass = assignments[i];
			TIntArrayList current = clusters.get(ass);
			if(current == null){
				clusters.put(ass, current = new TIntArrayList());
			}
			current.add(i);
		}
		int clustersSeen = 0;
		this.clusters = new int[clusters.size()][];
		for (Entry<Integer, TIntArrayList> i : clusters.entrySet()) {
			this.clusters[clustersSeen] = i.getValue().toArray(); 
			clustersSeen++;
		}
	}
	
	public String toString(){
		StringWriter strWriter = new StringWriter();
		try {
			this.writeASCII(new PrintWriter(strWriter));
		} catch (IOException e) {
			
		}
		return strWriter.toString();
		
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String asciiHeader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] binaryHeader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println("N-Clusters: " + this.numClusters());
		out.println("Entities: " + this.numEntries());
		for (int i = 0; i < this.clusters.length; i++) {
			out.println(String.format("%d: %s",i,Arrays.toString(this.clusters[i])));
		}
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int numEntries() {
		return this.nEntries;
	}

	@Override
	public int numClusters() {
		return this.clusters.length;
	}

	/**
	 * @return the cluster assignments
	 */
	public int[][] getClusters() {
		return clusters;
	}

	@Override
	public int[][] clusters() {
		return this.clusters;
	}	
}