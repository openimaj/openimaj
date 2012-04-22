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
package org.openimaj.image.connectedcomponent;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.math.geometry.shape.Circle;

public class ConnectedComponentCircleLabeler {
	TIntArrayList pixelsAdded = new TIntArrayList();
	private float minSize = 10;
	private int pixelsAddedPointer= 0;
	public ConnectedComponentCircleLabeler() {
		this.minSize = 10;
	}
	public ConnectedComponentCircleLabeler(int minSize) {
		this.minSize = minSize;
	}
	public List<Circle> findComponentsFlood(FImage image) {
		List<Circle> components = new ArrayList<Circle>();
		int [][] labels = new int[image.height][image.width];
		int nextColor=1;
		this.pixelsAdded.ensureCapacity(2*image.width * image.height );
		
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				if (image.pixels[y][x] != 0 && labels[y][x] == 0) {
					Circle c = floodFill(image, new Pixel(x, y), labels, nextColor);
					if(c!=null) components.add(c);
					this.pixelsAddedPointer = 0;
					nextColor++;
				}
			}
		}
		
		return components;
	}
	
	protected Circle floodFill(FImage image, Pixel start, int[][] output, int color) {
		Circle cc = null;
//		Flood-fill (node, target-color, replacement-color):
//			 1. Set Q to the empty queue.
		//Queue<Pixel> queue = new LinkedList<Pixel>();
		LinkedHashSet<Pixel> queue = new LinkedHashSet<Pixel>();
		
//			 2. If the color of node is not equal to target-color, return.
		if (image.pixels[start.y][start.x] == 0) return cc;
		
//			 3. Add node to Q.
		queue.add(start);
		
//			 4. For each element n of Q:
		float npixels = 0;
		double sumy2 = 0;
		double sumy = 0;
		double sumx2 = 0;
		double sumx = 0;
		
		while (queue.size() > 0) {
			//Pixel n = queue.poll();
			Pixel n = queue.iterator().next();
			queue.remove(n);
			
//			 5.  If the color of n is equal to target-color:
			if (image.pixels[n.y][n.x] != 0) {
//			 6.   Set w and e equal to n.
				int e = n.x, w=n.x;
//			 7.   Move w to the west until the color of the node to the west of w no longer matches target-color.
				while (w>0 && image.pixels[n.y][w-1] != 0) w--;
				
//			 8.   Move e to the east until the color of the node to the east of e no longer matches target-color.
				while (e<image.width-1 && image.pixels[n.y][e+1] != 0) e++;
				
//			 9.   Set the color of nodes between w and e to replacement-color.
				for (int i=w; i<=e; i++) {
					if(output[n.y][i]!=color){
						output[n.y][i] = color;
						npixels++;
						sumx2 += i * i;
						sumx += i;
						sumy2 += n.y * n.y;
						sumy += n.y;
						pixelsAdded.setQuick(pixelsAddedPointer,i);
						pixelsAdded.setQuick(pixelsAddedPointer+1,n.y);
						pixelsAddedPointer+=2;
					}
					
//					
					
//			10.   For each node n between w and e:
					int north = n.y - 1;
					int south = n.y + 1;
//			11.    If the color of the node to the north of n is target-color, add that node to Q.
					if (north >= 0 && image.pixels[north][i] != 0 && output[north][i] != color) queue.add(new Pixel(i, north));
//			       If the color of the node to the south of n is target-color, add that node to Q.
					if (south < image.height && image.pixels[south][i] != 0 && output[south][i] != color) queue.add(new Pixel(i, south));
				}
//			12. Continue looping until Q is exhausted.
			}
		}
		// Average height ^2 = sum((y - cy) ^ 2 = y^2 -2cy.y - cy^2 = y^2 - 2y * cy - cy^2 = sum_n(y^2) + sum_n(-y)*2cy - (cy^2 * n)
		// Average Height = Math.sqrt(sum_n(y^2) + sum_n(-y)*2cy - (cy^2 * n)) / n
		if(npixels < minSize )return null;
		float cx = (float)sumx / npixels;
		float cy = (float)sumy / npixels;
		double x,accumPosW = 0,nPosW = 0,accumNegW = 0,nNegW = 0;
		double y,accumPosH = 0,nPosH = 0,accumNegH = 0,nNegH = 0;
		for(int i =0 ; i < pixelsAddedPointer; i+=2){
			x = pixelsAdded.getQuick(i) - cx;
			y = pixelsAdded.getQuick(i+1) - cy;
			if (x >= 0) {
				accumPosW += x;
				nPosW++;
			} else {
				accumNegW += x;
				nNegW++;
			}

			if (y >= 0) {
				accumPosH += y;
				nPosH++;
			} else {
				accumNegH += y;
				nNegH++;
			}
		}
		double height = 2 * ((accumPosH / nPosH) + Math.abs(accumNegH / nNegH));
		double width = 2 * ((accumPosW / nPosW) + Math.abs(accumNegW / nNegW));
		cc = new Circle(cx,cy,(float)Math.sqrt(height*height + width*width));
//			13. Return. 
		return cc;
	}
}
