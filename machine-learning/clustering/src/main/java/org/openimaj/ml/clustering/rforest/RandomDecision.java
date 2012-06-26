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
package org.openimaj.ml.clustering.rforest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * A single decision node of a RandomForest tree. This decision holds the feature index and the
 * threshold for that index.  
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RandomDecision {

	/**
	 * Feature threshold
	 */
	public int threshold;
	/**
	 * Feature index
	 */
	public int feature;
	private int randomSeed = -1;
	private Random random = new Random();

	/**
	 * @param featureLength The number of entries in this featurevector
	 * @param minVal the min values of each featurevector entry
	 * @param maxVal the max values of each featurevector entry
	 */
	public RandomDecision(int featureLength, int[] minVal, int[] maxVal) {
		setFeatureDecision(featureLength,minVal,maxVal);
	}
	
	private void setFeatureDecision(int featureLength, int[] minVal,int[] maxVal) {
		this.feature = this.random.nextInt(featureLength);
		if(maxVal[this.feature]-minVal[this.feature] == 0)
			this.threshold = minVal[this.feature];
		else
			this.threshold = this.random.nextInt(maxVal[this.feature]-minVal[this.feature]) + minVal[this.feature];
	}

	/**
	 * @param featureLength The number of entries in this featurevector
	 * @param minVal the min values of each featurevector entry
	 * @param maxVal the max values of each featurevector entry
	 * @param r random seed to set before construction
	 */
	public RandomDecision(int featureLength, int[] minVal, int[] maxVal, Random r) {
		this.random = r;
		setFeatureDecision(featureLength,minVal, maxVal);
	}

	/**
	 * Emtpy contructor provided to allow reading of the decision
	 */
	public RandomDecision() {
	}

	
	
	/**
	 * Write decision to a binary stream, threshold followed by feature.
	 * @param o
	 * @throws IOException
	 */
	public void write(DataOutput o) throws IOException {
		o.writeInt(threshold);
		o.writeInt(feature);
	}

	/**
	 * write decision in a human readable form
	 * @param writer
	 */
	public void writeASCII(PrintWriter writer) {
		writer.print(threshold + "," + feature);
	}

	/**
	 * Read decision
	 * @param dis
	 * @return A decision
	 * @throws IOException
	 */
	public RandomDecision readBinary(DataInput dis) throws IOException {
		threshold = dis.readInt();
		feature = dis.readInt();
		return this;
	}

	/**
	 * Read decision from a string
	 * @param line
	 * @return a decision
	 */
	public RandomDecision readString(String line) {
		String[] bits = line.split(",");
		threshold = Integer.parseInt(bits[0]);
		feature = Integer.parseInt(bits[1]);
		return this;
	}
	
	@Override
	public String toString()
	{
		String s = "(" + this.feature + "," + this.threshold + ")";
		return s;
	}

	/**
	 * Random seed upon which a java {@link Random} object is seeded and used to choose
	 * random indecies and thresholds.
	 * @param randomSeed
	 */
	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
		this.random = new Random(this.randomSeed);
	}
}
