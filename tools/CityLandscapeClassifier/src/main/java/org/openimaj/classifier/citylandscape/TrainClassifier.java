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
package org.openimaj.classifier.citylandscape;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analysis.algorithm.EdgeDirectionCoherenceVector;

/**
 * 
 *  @author Ajay Mehta (am24g08@ecs.soton.ac.uk)
 *	
 *	@created 20 Jun 2011
 */
public class TrainClassifier {

	
	final static int OFFSET = 0;
	/**
	 * First arg: Number of images in directory to go through
	 * Second arg: Directory of images
	 * Third arg: File to write to
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		if(args.length!=3){
			System.out.println("Invalid number of arguments entered");
			System.exit(1);
		}
		
		System.out.println("Writing training data for classifier");
		long init = System.currentTimeMillis();
		TrainClassifier.writeHistograms(Integer.parseInt(args[0]), args[1],args[2]);
		long runTime = System.currentTimeMillis() - init;
		BigDecimal bd = new BigDecimal(runTime);
		System.out.println("Program time to run: " + bd.movePointLeft(3)
				+ " s");
		System.out.println("All training data files written...\nEnd of program.");
	}
	
	/**
	 * Write histograms to file
	 * 
	 * @param numberOfImages
	 * @param inputPath
	 * @param outfile
	 * @throws IOException
	 */
	public static void writeHistograms(int numberOfImages, String inputPath, String outfile) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		System.out.println("Input file: "+inputPath);
		System.out.println("Writing to: "+outfile);
		File dir = new File(inputPath);
		File [] array = dir.listFiles();
		int skipped = 0;
		for (int i = OFFSET; i-skipped<numberOfImages && i < array.length; i++){
			FImage image = null;
			try{
				System.out.println("Attempting write image" +array[i].getName()+ " Number: "+(i+skipped));
				image = ImageUtilities.readF(array[i].getAbsoluteFile());
				
			}
			catch(Exception e){
				System.out.println("Error reading image: " + array[i].getName()+". Number: "+(i+skipped));
				skipped++;
				continue;
			}
			EdgeDirectionCoherenceVector cldo = new EdgeDirectionCoherenceVector();
			image.analyseWith(cldo);
			
			double[][] vec = new double[][] {
					cldo.getLastHistogram().incoherentHistogram.values,
					cldo.getLastHistogram().coherentHistogram.values
			};
			int n = cldo.getNumberOfDirBins();

			double edgeCounter = 0;
			for (int j = 0; j < n; j++) {
				// Incoherent
				bw.write(vec[0][j] + ",");
				edgeCounter += vec[0][j];

			}
			for (int j = 0; j < n; j++) {
				// Coherent
				bw.write(vec[1][j] + ",");
				edgeCounter += vec[1][j];
			}
			//edgeCounter variable hold sum of all elements in vector, used in normalization
			bw.write(Double.toString(edgeCounter));
			bw.newLine();
			edgeCounter = 0;
		}
		
		bw.flush();
		bw.close();
		System.out.println("Vector Write Completed");
	}
}
