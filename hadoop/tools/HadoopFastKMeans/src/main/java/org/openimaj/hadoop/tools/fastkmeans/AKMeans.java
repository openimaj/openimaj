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
package org.openimaj.hadoop.tools.fastkmeans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Random;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.sequencefile.ExtractionPolicy;
import org.openimaj.hadoop.sequencefile.KeyValueDump;
import org.openimaj.hadoop.sequencefile.NamingPolicy;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.io.IOUtils;

import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.assignment.hard.ApproximateByteEuclideanAssigner;
import org.openimaj.ml.clustering.assignment.hard.ExactByteAssigner;
import org.openimaj.ml.clustering.kmeans.fast.FastByteKMeans;
import org.openimaj.util.pair.IntFloatPair;

/**
 * Approximate KMeans mapreduce implementation
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class AKMeans {
	/**
	 * Config option where for centroids path
	 */
	public static final String CENTROIDS_PATH = "uk.ac.soton.ecs.jsh2.clusterquantiser.CentroidsPath";
	
	/**
	 * Config option where for number of centroids K
	 */
	public static final String CENTROIDS_K = "uk.ac.soton.ecs.jsh2.clusterquantiser.CentroidsK";
	
	/**
	 * Config option where for exact mode or not
	 */
	public static final String CENTROIDS_EXACT = "uk.ac.soton.ecs.jsh2.clusterquantiser.CentroidsExact";
	
	private static final int DEFAULT_NCHECKS = 768;
	private static final int DEFAULT_NTREES = 8;
	private static final String CENTROIDS_FALLBACK_CHANCE = "uk.ac.soton.ecs.jsh2.clusterquantiser.FallbackChance";
	
	/**
	 * the map for approximate kmeans. Uses the {@link FastByteKMeans} under the hood. For each feature 
	 * assign the feature to a centroid and emit with centroid as key.
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Map extends Mapper<Text, BytesWritable, IntWritable, BytesWritable> {
		private static Path centroidsPath = null;
		private static int k = -1;
		private static FastByteKMeans tree = null;
		private static HardAssigner<byte[], float[], IntFloatPair> assigner = null;
		private static double randomFallbackChance;
		private static boolean exact;
		
		@Override
		protected void setup(Mapper<Text, BytesWritable, IntWritable, BytesWritable>.Context context)throws IOException, InterruptedException{
			loadCluster(context);
		}
		
		protected static synchronized void loadCluster(Mapper<Text, BytesWritable, IntWritable, BytesWritable>.Context context) throws IOException {
			Path newPath = new Path(context.getConfiguration().getStrings(CENTROIDS_PATH)[0]);
			boolean current = centroidsPath != null && centroidsPath.toString().equals(newPath.toString());
			if(!current ){
				k = Integer.parseInt(context.getConfiguration().getStrings(CENTROIDS_K)[0]);
				exact = Boolean.parseBoolean(context.getConfiguration().getStrings(CENTROIDS_EXACT)[0]);
				System.out.println("This is exact mode: " + exact);
				randomFallbackChance = 0.01;
				if(context.getConfiguration().getStrings(CENTROIDS_FALLBACK_CHANCE)!=null){
					randomFallbackChance = Double.parseDouble(context.getConfiguration().getStrings(CENTROIDS_FALLBACK_CHANCE)[0]);
				}
				
				centroidsPath = newPath;
				System.out.println("Loading centroids from: " + centroidsPath);
				URI uri = centroidsPath.toUri();
				FileSystem fs = HadoopFastKMeansOptions.getFileSystem(uri);
				InputStream is = fs.open(centroidsPath);
				tree = IOUtils.read(is, FastByteKMeans.class);
				
				if (exact)
					assigner = new ExactByteAssigner(tree);
				else
					assigner = new ApproximateByteEuclideanAssigner(tree);
			}
			else{
//				System.out.println("No need to reload tree");
			}	
		}
		
		@Override
		public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
			byte[] values = value.getBytes();
			byte[] points = new byte[value.getLength()];
			System.arraycopy(values, 0, points, 0, points.length);
			
			int cluster = assigner.assign(points);
			
			context.write(new IntWritable(cluster), new BytesWritable(points));
			
			if(new Random().nextDouble() < randomFallbackChance){
				context.write(new IntWritable(k+1), new BytesWritable(points));
			}
		}
	}
	
	private static int accumulateFromFeature(int[] sum, byte[] assigned) throws IOException{
		if (assigned.length!=sum.length) throw new IOException("Inconsistency in sum and feature length");
		for(int i = 0; i < sum.length; i++){
			sum[i] +=assigned[i];
		}
		return 1;
	}
	
	private static int accumulateFromSum(int[] sum, byte[] assigned) throws IOException{
		int flen = (assigned.length / 4) - 1;
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(assigned));
		if (flen!=sum.length) throw new IOException("Inconsistency in sum and feature length");
		int totalAssigned = dis.readInt();
		for(int i = 0; i < sum.length; i++){
			sum[i] +=dis.readInt();
		}
		return totalAssigned;
	}
	
	/**
	 * for efficiency, combine centroids early, emitting sums and k for centroids combined
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Combine extends Reducer<IntWritable, BytesWritable, IntWritable, BytesWritable> {	
		private int k;
		
		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			k = Integer.parseInt(context.getConfiguration().getStrings(CENTROIDS_K)[0]);
		}
		
		@Override
		public void reduce(IntWritable key, Iterable<BytesWritable> values, Context context) throws IOException, InterruptedException {
			int[] sum = new int[128];
			int totalAssigned = 0;
			for (BytesWritable val : values) {
				// Copy the important part of the array
				byte[] assigned = new byte[val.getLength()];
				System.arraycopy(val.getBytes(), 0, assigned, 0, assigned.length);
				// Skip over all the random runoff emittions
				if(key.get() > k){
					context.write(key, new BytesWritable(assigned));
					continue;
				}
				int added = 0;
				// Accumulate either with feature or with an existing sum of features
				if(assigned.length == 128) added += accumulateFromFeature(sum,assigned);
				else added += accumulateFromSum(sum,assigned);
				totalAssigned += added;
			}
			if(key.get() > k)return;
			// Write accumulation and current count
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(totalAssigned);
			for(int i : sum){ dos.writeInt(i); }
			context.write(key, new BytesWritable(bos.toByteArray()));
		}
	}
	
	/**
	 * The AKmeans reducer. average the combined features assigned to each centroid, emit new centroids. may (if not assigned)
	 * result in some centroids with no value.
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class Reduce extends Reducer<IntWritable, BytesWritable, IntWritable, BytesWritable> {
		private int k;
		
		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			k = Integer.parseInt(context.getConfiguration().getStrings(CENTROIDS_K)[0]);
		}
		
		@Override
		public void reduce(IntWritable key, Iterable<BytesWritable> values, Context context) throws IOException, InterruptedException {
			int[] sum = new int[128];
			byte[] out = new byte[128];
			
			int totalAssigned = 0;
			for (BytesWritable val : values) {
				byte[] assigned = new byte[val.getLength()];
				System.arraycopy(val.getBytes(), 0, assigned, 0, assigned.length);
				if(key.get()> k){
					context.write(key, new BytesWritable(assigned));
					continue;
				}
				int added=0;
				if(assigned.length == 128) added += accumulateFromFeature(sum,assigned);
				else added += accumulateFromSum(sum,assigned);
				totalAssigned+=added;
			}
			if(key.get() > k)return;
			for(int i = 0; i < sum.length; i++){
				out[i] = (byte)((int)(sum[i] / totalAssigned));
			}
			context.write(key, new BytesWritable(out));
		}
	}
	
	static class SelectTopKDump extends KeyValueDump<IntWritable,BytesWritable>{
		int index = 0;
		int randomGens = 0;
		byte[][] centroids;
		
		SelectTopKDump(int k) {
			centroids = new byte[k][];
		}
		
		@Override
		public void dumpValue(IntWritable key, BytesWritable val) {
			if(index >= centroids.length) return;
			byte [] bytes = new byte[val.getLength()]; 
			System.arraycopy(val.getBytes(), 0, bytes, 0, bytes.length);
			centroids[index] = bytes;
			index++;
			if(key.get() == centroids.length + 1){
				randomGens++;
			}
		}
		
	}
	
	/**
	 * Given the location of a binary dump of centroids on the HDFS, load the binary dump and construct a proper {@link FastByteKMeans} 
	 * instance
	 * @param centroids
	 * @param selected
	 * @param options
	 * @return {@link FastByteKMeans} for the centoirds on the HDFS
	 * @throws Exception
	 */
	public static FastByteKMeans completeCentroids(String centroids, String selected,HadoopFastKMeansOptions options) throws Exception {
		System.out.println("Attempting to complete");
		Path centroidsPath = new Path(centroids);
		SequenceFileUtility<IntWritable, BytesWritable> utility = new IntBytesSequenceMemoryUtility(centroidsPath.toUri(), true);
		SelectTopKDump dump = new SelectTopKDump(options.k);
		utility.exportData(NamingPolicy.KEY, new ExtractionPolicy(), 0, dump);
		
		byte[][] newcentroids;
		newcentroids = dump.centroids;
		// We need to pick k - (dump.index - dump.randomGens) new items
		System.out.println("Expecting " + options.k + " got " + dump.index + " of which " + dump.randomGens + " were random");
		if(dump.index < options.k){
			int randomNeeded = options.k - (dump.index - dump.randomGens);
			
			SequenceFileByteFeatureSelector sfbs = new SequenceFileByteFeatureSelector(selected,options.output + "/randomswap",options);
			String initialCentroids = sfbs.getRandomFeatures(randomNeeded);
			Path newcentroidsPath = new Path(initialCentroids);
			utility = new IntBytesSequenceMemoryUtility(newcentroidsPath.toUri(), true);
			SelectTopKDump neededdump = new SelectTopKDump(randomNeeded);
			utility.exportData(NamingPolicy.KEY, new ExtractionPolicy(), 0, neededdump);
			newcentroids = neededdump.centroids;
		}
		FastByteKMeans newFastKMeansCluster = new FastByteKMeans(newcentroids,DEFAULT_NTREES,DEFAULT_NCHECKS);
		return newFastKMeansCluster;
	}

	/**
	 * load some initially selected centroids from {@link FeatureSelect} as a {@link FastByteKMeans} instance
	 * @param initialCentroids
	 * @param k
	 * @return a {@link FastByteKMeans}
	 * @throws IOException
	 */
	public static FastByteKMeans sequenceFileToCluster(String initialCentroids, int k) throws IOException {
		SelectTopKDump neededdump = new SelectTopKDump(k);
		IntBytesSequenceMemoryUtility utility = new IntBytesSequenceMemoryUtility(initialCentroids, true);
		utility.exportData(NamingPolicy.KEY, new ExtractionPolicy(), 0, neededdump);
		FastByteKMeans newFastKMeansCluster = new FastByteKMeans(neededdump.centroids,DEFAULT_NTREES,DEFAULT_NCHECKS);
		return newFastKMeansCluster;
	}
}
