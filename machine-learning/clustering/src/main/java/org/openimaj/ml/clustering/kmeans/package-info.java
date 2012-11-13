/**
 * K-Means in OpenIMAJ is designed to be both extremely fast and flexible. To
 * this end, we've separated out the cluster-assignment part of the algorithm to
 * be performed by a separate object. This means that the core implementation
 * (initial assignment, followed by iterative cluster assignment and centroid
 * averaging) can use any kind of cluster assignment approach. There are a
 * number of assignment techniques that can be used, including exact assignment
 * and approximate assignment based on KD-Tree ensembles.
 */
package org.openimaj.ml.clustering.kmeans;

