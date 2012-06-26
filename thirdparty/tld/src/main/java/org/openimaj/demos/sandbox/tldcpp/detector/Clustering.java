package org.openimaj.demos.sandbox.tldcpp.detector;

import java.util.List;

import org.openimaj.demos.sandbox.tldcpp.videotld.TLDUtil;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A simple greedy clustering algorithm which puts windows in the same cluster
 * if they are close to each other, and combines clusters if they are close
 * 
 * the goal is to find a set of windows which all represent a good location for the object
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Clustering {
	/**
	 * The windows to compare
	 */
	public ScaleIndexRectangle[] windows;

	DetectionResult detectionResult;
	
	/**
	 * sets a default cutoff of .5f
	 */
	public Clustering() {
		cutoff = .5f;
		windows = null;
	}
	//Configurable members
	/**
	 * how far overlapped two things must be before they can be in the same cluster 
	 */
	public float cutoff;

	/**
	 * the pointer to windows is set to null
	 */
	public void release() {
		windows = null;
	}
	
	void calcDistances(float [] distances) {
		float [] distances_tmp = distances;
		int distances_tmp_index = 0;

		List<Integer> confidentIndices = detectionResult.confidentIndices;

		int indices_size = confidentIndices.size();

		for(int i = 0; i < confidentIndices.size(); i++) {
			int firstIndex = confidentIndices.get(0);
			confidentIndices.remove(0);
			TLDUtil.tldOverlapOne(windows, firstIndex, confidentIndices,distances_tmp, distances_tmp_index);
			distances_tmp_index += indices_size-i-1;
		}

		for(int i = 0; i < indices_size*(indices_size-1)/2; i++) {
			distances[i] = 1-distances[i];
		}

	}
	
	/**
	 * Clusters the overlap of each window to each other window and
	 * sets the results in the {@link DetectionResult} instance. 
	 * 
	 * 2 things are saved to {@link DetectionResult}:
	 * - The number of clusters (1 means we have a very good grouping of windows)
	 * - the detector bounding box, a mean of all the bounding boxes in the single cluster
	 */
	public void clusterConfidentIndices() {
		int numConfidentIndices = detectionResult.confidentIndices.size();
		float [] distances = new float[numConfidentIndices*(numConfidentIndices-1)/2];
		calcDistances(distances);
		int [] clusterIndices = new int[numConfidentIndices];
		cluster(distances, clusterIndices);
		if(detectionResult.numClusters == 1) {
			calcMeanRect(detectionResult.confidentIndices);
			//TODO: Take the maximum confidence as the result confidence.
		}
	}
	
	void calcMeanRect(List<Integer> indices) {

		float x,y,w,h;
		x=y=w=h=0;

		int numIndices = indices.size();
		for(int i = 0; i < numIndices; i++) {
			ScaleIndexRectangle bb = this.windows[indices.get(i)];
//			int * bb = &windows[TLD_WINDOW_SIZE*indices.at(i)];
			x += bb.x;
			y += bb.y;
			w += bb.width;
			h += bb.height;
		}

		x /= numIndices;
		y /= numIndices;
		w /= numIndices;
		h /= numIndices;

		Rectangle rect = new Rectangle();
		detectionResult.detectorBB = rect;
		rect.x = (float) Math.floor(x+0.5);
		rect.y = (float) Math.floor(y+0.5);
		rect.width = (float) Math.floor(w+0.5);
		rect.height = (float) Math.floor(h+0.5);

	}

	
	void cluster(float [] distances, int [] clusterIndices) {
		int numConfidentIndices = detectionResult.confidentIndices.size();

		if(numConfidentIndices == 1) {
			clusterIndices[0] = 0;
			detectionResult.numClusters = 1;
			return;
		}

		int numDistances = numConfidentIndices*(numConfidentIndices-1)/2;

		//Now: Cluster distances
		boolean [] distUsed = new boolean[numDistances];

		for(int i = 0; i < numConfidentIndices; i++) {
			clusterIndices[i] = -1;
		}

		int newClusterIndex = 0;

		int numClusters = 0;
		while(true) {

			//Search for the shortest distance
			float shortestDist = -1;
			int shortestDistIndex = -1;
			int i1 = 0;
			int i2 = 0;
			int distIndex = 0;
			for(int i = 0; i < numConfidentIndices; i++) { //Row index
				for(int j = i+1; j < numConfidentIndices; j++) { //Start from i+1

					if(!distUsed[distIndex] && (shortestDistIndex == -1 || distances[distIndex] < shortestDist)) {
						shortestDist = distances[distIndex];
						shortestDistIndex = distIndex;
						i1=i;
						i2=j;
					}

					distIndex++;
				}
			}

			if(shortestDistIndex == -1) {
				break; // We are done
			}

			distUsed[shortestDistIndex] = true;

			//Now: Compare the cluster indices
			//If both have no cluster and distance is low, put them both to a new cluster
			if(clusterIndices[i1] == -1 && clusterIndices[i2] == -1) {
				//If distance is short, put them to the same cluster
				if(shortestDist < cutoff) {
					clusterIndices[i1] = clusterIndices[i2] = newClusterIndex;
					newClusterIndex++;
					numClusters++;
				} else { //If distance is long, put them to different clusters
					clusterIndices[i1] = newClusterIndex;
					newClusterIndex++;
					numClusters++;
					clusterIndices[i2] = newClusterIndex;
					newClusterIndex++;
					numClusters++;
				}
				//Second point is  in cluster already
			} else if (clusterIndices[i1] == -1 && clusterIndices[i2] != -1) {
				if(shortestDist < cutoff) {
					clusterIndices[i1] = clusterIndices[i2];
				} else { //If distance is long, put them to different clusters
					clusterIndices[i1] = newClusterIndex;
					newClusterIndex++;
					numClusters++;
				}
			} else if (clusterIndices[i1] != -1 && clusterIndices[i2] == -1) {
				if(shortestDist < cutoff) {
					clusterIndices[i2] = clusterIndices[i1];
				} else { //If distance is long, put them to different clusters
					clusterIndices[i2] = newClusterIndex;
					newClusterIndex++;
					numClusters++;
				}
			} else { //Both indices are in clusters already
				if(clusterIndices[i1] != clusterIndices[i2] && shortestDist < cutoff) {
					//Merge clusters

					int oldClusterIndex = clusterIndices[i2];

					for(int i = 0; i < numConfidentIndices; i++) {
						if(clusterIndices[i] == oldClusterIndex) {
							clusterIndices[i] = clusterIndices[i1];
						}
					}

					numClusters--;
				}
			}
		}

		detectionResult.numClusters = numClusters;
	}


}
