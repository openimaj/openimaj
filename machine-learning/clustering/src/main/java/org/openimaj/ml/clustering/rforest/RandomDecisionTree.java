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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * A tree of {@link RandomDecision} nodes used for constructing a string of bits which represent a cluster
 * point for a single data point
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RandomDecisionTree {
	List<RandomDecision> decisions;
	private Random random = new Random();
	/**
	 * Construct a new RandomDecisionTree setting the number of decisions and the values needed
	 * to choose a random index and min/max values for each feature vector index.
	 * 
	 * @param nDecisions
	 * @param featureLength
	 * @param minVal
	 * @param maxVal
	 */
	public RandomDecisionTree(int nDecisions, int featureLength, int[] minVal, int[] maxVal) {
		initDecisions(nDecisions,featureLength,minVal,maxVal);
	}
	
	/**
	 * Construct a new RandomDecisionTree setting the number of decisions and the values needed
	 * to choose a random index and min/max values for each feature vector index.
	 * 
	 * @param nDecisions
	 * @param featureLength
	 * @param minVal
	 * @param maxVal
	 * @param r
	 */
	public RandomDecisionTree(int nDecisions, int featureLength, int[] minVal, int[] maxVal,Random r) {
		this.random = r;
		initDecisions(nDecisions,featureLength,minVal,maxVal);
	}
	
	private void initDecisions(int nDecisions, int featureLength, int[] minVal,int[] maxVal) {
		decisions = new LinkedList<RandomDecision>();
		for(int i = 0; i < nDecisions; i++){
			RandomDecision dec = new RandomDecision(featureLength,minVal,maxVal,this.random);
			decisions.add(dec);
		}
	}

	/**
	 * A convenience function allowing the RandomDecisionTree to be written and read.
	 */
	public RandomDecisionTree() {
		decisions = new LinkedList<RandomDecision>();
	}

	/**
	 * The function which finds the path down this random tree for a given feature. Tests each
	 * required feature vector index against the threshold and assigns booleans.
	 * 
	 * @param feature
	 * @return return the letter as a string of bytes
	 */
	public boolean[] getLetter(int[] feature){
		boolean[] out = new boolean[decisions.size()];
		int i = 0;
		for(RandomDecision r : decisions){
			if(feature[r.feature] > r.threshold)
				out[i] = true;
			else
				out[i] = false;
			i++;
		}
		return out;
	}

	/**
	 * Read/Write RandomDecisionTree (including decision nodes)
	 * @param o
	 * @throws IOException
	 */
	public void write(DataOutput o) throws IOException {
		o.writeInt(this.decisions.size());
		for(RandomDecision r : this.decisions){
			r.write(o);
		}
	}

	/**
	 * Read/Write RandomDecisionTree (including decision nodes)
	 * @param writer
	 */
	public void writeASCII(PrintWriter writer) {
		for(RandomDecision r : this.decisions){
			r.writeASCII(writer);
			writer.print(" ");
		}
	}

	/**
	 * Read/Write RandomDecisionTree (including decision nodes)
	 * @param dis
	 * @throws IOException
	 * @return this
	 */
	public RandomDecisionTree readBinary(DataInput dis) throws IOException {
		int nDecisions = dis.readInt();
		if(this.decisions.size() != nDecisions){
			this.decisions = new LinkedList<RandomDecision>();
			for(int i = 0 ; i < nDecisions; i ++){
				RandomDecision r = new RandomDecision().readBinary(dis);
				this.decisions.add(r);
			}
		}
		else{
			for(RandomDecision rd : this.decisions){
				rd.readBinary(dis);
			}
		}
		return this;
	}

	/**
	 * Read/Write RandomDecisionTree (including decision nodes)
	 * @param br
	 * @throws IOException
	 * @return this
	 */
	public RandomDecisionTree readASCII(Scanner br) throws IOException {
		String[] lines = br.nextLine().split(" ");
		if(this.decisions.size() != lines.length){
			this.decisions = new LinkedList<RandomDecision>();
			for(String line : lines){
				this.decisions.add(new RandomDecision().readString(line));
			}
		}
		else{
			int index = 0;
			for(RandomDecision rd : this.decisions){
				rd.readString(lines[index++]);
			}
		}
		
		return this;
	}
	
	@Override
	public String toString(){
		String s = "{";
		for (RandomDecision r : this.decisions){
			s += r.toString() + ",";
		}
		s+="}";
		return s;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof RandomDecisionTree)) return false;
		RandomDecisionTree rdt = (RandomDecisionTree) o;
		for(int i = 0; i < this.decisions.size(); i++){
			RandomDecision d1 = rdt.decisions.get(i);
			RandomDecision d2 = this.decisions.get(i);
			
			if(d1.feature != d2.feature || d1.threshold != d2.threshold)
				return false;
		}
		return true;
	}
}
