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
package org.openimaj.image.processing.face.parts;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class Model implements Serializable {
	private static final long serialVersionUID = 1L;

	public static Model DEFAULT_MODEL = loadDefaultModel();
	
	Tree [] tree;
	Part [] part;
	int imgsize;
	int border;
	int winsize;
	double appwt;

	public static class Tree implements Serializable {
		private static final long serialVersionUID = 1L;

		double [][] E;
		double [] var;
		int [][] MU;
		double mix;
		double [] scale;
		int [] parent;
		int [][] children;
		int [] depthorder;
	}

	public static class Part implements Serializable {
		private static final long serialVersionUID = 1L;

		double [][] talpha;
		int [][] HCas;	//haar cascade
		int [] bb;		//bounding box
		boolean [][] M;	//mask
	}
	
	private static Model loadDefaultModel() {
		try {
			//TODO: could build a serialised version instead and load that
			return buildDefaultModel();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Model buildDefaultModel() throws IOException {
		Model model = new Model();
		model.appwt = 2;
		model.winsize = 15;
		model.border = 15;
		model.imgsize = 80;

		model.part = new Part[9];
		for (int i=1; i<=9; i++) {
			model.part[i-1] = new Part();
			model.part[i-1].HCas = readIntDataBin(Model.class.getResourceAsStream("haar"+i+".bin"));
			model.part[i-1].talpha = readDoubleDataBin(Model.class.getResourceAsStream("talpha"+i+".bin"));
			model.part[i-1].M = readBooleanDataBin(Model.class.getResourceAsStream("mask"+i+".bin"));
		}

		model.tree = new Tree[3];

		//TREE 1
		model.tree[0] = new Tree();
		model.tree[0].E = new double[][] {{1, 2}, {2, 3}, {3, 4}, {2, 5}, {5, 6}, {6, 7}, {5, 8}, {7, 9}};
		model.tree[0].var = new double[]  {1.16, 1.03, 1.19, 2.49, 0.88, 0.92, 1.71, 1.81};
		model.tree[0].MU = new int[][] {{0, 0}, {9, 0}, {10,0}, {9, 0}, {1, 12}, {5, 0}, {5, 0}, {-3, 7}, {3, 7}};
		model.tree[0].mix = 0.33;
		model.tree[0].scale = new double[] {0, 0.431203093871548, 0.484492467521240, 0.420546069849079, 0.200844044022372, 0.571191103033717, 0.546043956131382, 0.292395762807960, 0.275798316570445};
		model.tree[0].parent = new int[] {-1, 0, 1, 2, 1, 4, 5, 4, 6}; //sub 1 from matlab
		model.tree[0].children = new int[][] {{1}, {2, 4}, {3}, {}, {5, 7}, {6}, {8}, {}, {}}; //sub 1 from matlab
		model.tree[0].depthorder = new int[] {0, 1, 2, 4, 3, 5, 7, 6, 8}; //sub 1 from matlab

		//TREE 2
		model.tree[1] = new Tree();
		model.tree[1].E = new double[][] {{1, 2}, {2, 3}, {3, 4}, {3, 7}, {7, 6}, {6, 5}, {5, 8}, {7, 9}};
		model.tree[1].var = new double[]  {1.97, 1.85, 1.89, 3.33, 1.89, 2.06, 3.60, 4.00};		
		model.tree[1].MU = new int[][] {{0, 0}, {9, -1}, {10, -1}, {9, -1}, {-5, -2}, {-6, 3}, {2, 11}, {-2, 8}, {4, 7}};
		model.tree[1].mix = 0.36;
		model.tree[1].scale = new double[] {0, 0.254045277312314, 0.270079620048070, 0.264406629463902, 0.243204625087956, 0.263998194617104, 0.150044711363897, 0.138952250832562, 0.124994774540550};
		model.tree[1].parent = new int[] {-1, 0, 1, 2, 5, 6, 2, 4, 6}; //sub 1 from matlab
		model.tree[1].children = new int[][] {{1}, {2}, {3, 6}, {}, {7}, {4}, {5, 8}, {}, {}}; //sub 1 from matlab
		model.tree[1].depthorder = new int[] {0, 1, 2, 3, 6, 5, 8, 4, 7}; //sub 1 from matlab

		//TREE 3
		model.tree[2] = new Tree();
		model.tree[2].E = new double[][] {{1, 2}, {2, 3}, {3, 4}, {2, 5}, {5, 6}, {6, 7}, {7, 9}, {5, 8}};
		model.tree[2].var = new double[]  {2.15, 1.95, 2.25, 3.77, 1.99, 2.49, 3.88, 4.23};
		model.tree[2].MU = new int[][] {{0, 0}, {9, 1}, {10, 1}, {9, 1}, {-1, 11}, {5, 3}, {6, -1}, {-4, 7}, {1, 8}};
		model.tree[2].mix = 0.31;
		model.tree[2].scale = new double[] {0, 0.232309551084211, 0.256560908278279, 0.221946733311257, 0.132713563105743, 0.251186367002818, 0.200459108303263, 0.118167093811055, 0.128771977977574};
		model.tree[2].parent = new int[] {-1, 0, 1, 2, 1, 4, 5, 4, 6}; //sub 1 from matlab
		model.tree[2].children = new int[][] {{1}, {2, 4}, {3}, {}, {5, 7}, {6}, {8}, {}, {}}; //sub 1 from matlab
		model.tree[2].depthorder = new int[] {0, 1, 2, 4, 3, 5, 7, 6, 8}; //sub 1 from matlab

		return model;
	}

	private static double [][] readDoubleDataBin(InputStream is) throws IOException {
		DataInputStream input = new DataInputStream(is);

		int rows = input.readInt();
		int cols = input.readInt();

		double [][] data = new double[rows][cols];

		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				data[r][c] = input.readDouble();
			}
		}

		input.close();

		return data;
	}

	private static int [][] readIntDataBin(InputStream is) throws IOException {
		DataInputStream input = new DataInputStream(is);

		int rows = input.readInt();
		int cols = input.readInt();

		int [][] data = new int[rows][cols];

		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				data[r][c] = input.readInt();
			}
		}

		input.close();

		return data;
	}

	private static boolean [][] readBooleanDataBin(InputStream is) throws IOException {
		DataInputStream input = new DataInputStream(is);

		int rows = input.readInt();
		int cols = input.readInt();

		boolean [][] data = new boolean[rows][cols];

		for (int r=0; r<rows; r++) {
			for (int c=0; c<cols; c++) {
				data[r][c] = input.readInt() == 1;
			}
		}

		input.close();

		return data;
	}
}
