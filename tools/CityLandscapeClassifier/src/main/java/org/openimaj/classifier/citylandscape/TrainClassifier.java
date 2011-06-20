package org.openimaj.classifier.citylandscape;

import java.io.*;
import java.math.BigDecimal;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

import uk.ac.soton.ecs.dpd.ir.filters.CityLandscapeDetector;

/**
 * 
 *  @author Ajay Mehta <am24g08@ecs.soton.ac.uk>
 *	@version $Author$, $Revision$, $Date$
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
	
	public static void writeHistograms(int numberOfImages, String inputPath, String outfile) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		System.out.println("Input file: "+inputPath);
		System.out.println("Writing to: "+outfile);
		File dir = new File(inputPath);
		File [] array = dir.listFiles();
		int skipped = 0;
		for (int i = OFFSET; i-skipped<numberOfImages && i < array.length; i++){
			MBFImage rgbimage = null;
			try{
				System.out.println("Attempting write image" +array[i].getName()+ " Number: "+(i+skipped));
				rgbimage = ImageUtilities.readMBF(array[i].getAbsoluteFile());
				
			}
			catch(Exception e){
				System.out.println("Error reading image: " + array[i].getName()+". Number: "+(i+skipped));
				skipped++;
				continue;
			}
			CityLandscapeDetector<MBFImage> cldo = new CityLandscapeDetector<MBFImage>();
			rgbimage.process(cldo);
			
			double[][] vec = cldo.getLastHistogram();
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
