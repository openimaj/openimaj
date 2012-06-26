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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import javax.activation.MimetypesFileTypeMap;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.analysis.algorithm.EdgeDirectionCoherenceVector;

/**
 * Tool for building city/landscape classifiers 
 * 
 * @author Ajay Mehta (am24g08@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk) 
 */
public class CityLandscapeUtilities {
	
	/**
	 * The main method
	 * @param args
	 */
	public static void main (String [] args) {
		try {
			runClassifier(args);
			System.exit(0);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to utilize all necessary classification methods in correct order with given mode
	 * which specifies the classifier to be used (loads correct training set). String array args
	 * is unchanged from the command line
	 * @param args
	 * @throws IOException 
	 */
	public static void runClassifier(String[] args) throws IOException{
		// Check whether arguments are of correct number
		// [dirtoclassify, k, mode, output, pathToFile]
		
		if(args.length<4||args.length>5){
			throw new RuntimeException("Invalid number of arguments given");
			
		}
		
		int output = 0, k = 0, mode = 0;
		
		try{
			k = Integer.parseInt(args[1]);
			mode = Integer.parseInt(args[2]);
			output = Integer.parseInt(args[3]);
		}catch(NumberFormatException er){
			System.out.println("Second argument must be an integer for k between 1-10");
			System.out.println("Third and fourth arguments must be an integers between 1-3");
			throw new RuntimeException();
		}
		
		if(k<1||k>10){
			throw new RuntimeException("Please enter a value for k between 1-10");			
		}
		if(output<1||output>3){
			throw new RuntimeException("Please enter a valid value for output mode (fourth argument)\n1 = standard output\n2 = full output\n3 = output to file");
		}
		if(mode<1||mode>3){
			throw new RuntimeException("Please enter a valid value for for classifier mode (third argument)\n1 = City/Landscape\n2 = City/Not City\n3 = Landscape/Not Landscape");
		}
		
		
		switch(mode){
			case 1:
				System.out.println("Classification Mode: City/Landscape");
				break;
			case 2:
				System.out.println("Classification Mode: City/Not City");
				break;
			case 3:
				System.out.println("Classification Mode: Landscape/Not Landscape");
				break;
		}

		// Classify if directory of images
		File dir = new File(args[1]);
		BufferedWriter bw = null;
		if(output==3){
			try{
				bw = new BufferedWriter(new FileWriter(args[4]));
			}catch(Exception e){
				throw new RuntimeException("Please specify a valid output file as a final argument. This path must be valid");
			}
			
		}
		if (dir.isDirectory()) {
			System.out.println("Checking directory...");
			if (CityLandscapeUtilities.isValidDirectory(args[0])) {
				
				for (File f : dir.listFiles()) {

					RecordDetail[] r = CityLandscapeUtilities
							.classifyImage(CityLandscapeUtilities
									.getImageVector(f
											.getAbsolutePath()), k,
									mode);

					String o = CityLandscapeUtilities
					.getOutput(r, output, mode);
					
					if(output==3){
						bw.write(f+":"+o);
						bw.newLine();
					}else{
						System.out.println("Image Name: " + f);
						System.out.println(CityLandscapeUtilities
								.getOutput(r, output, mode) + "\n");
					}
					
					
				}
				
				

			}
			
		// Classify image file only	
		} else if (CityLandscapeUtilities.isImage(args[0], true)) {
			
			// Classifies image
			RecordDetail[] r = CityLandscapeUtilities
					.classifyImage(CityLandscapeUtilities
							.getImageVector(args[0]), k, mode);
	
			String o = CityLandscapeUtilities
					.getOutput(r, output, mode);
			
			if(output == 3){
				bw.write((args[0]+":"+o));
			}else{
				System.out.println(o);
			}

		}
		
		if(bw!=null){
			bw.flush();
			bw.close();
		}
		
		
		
	}
	
	
	
	/**
	 * Method to handle obtaining, normalizing and storing training data.
	 * @return
	 */
	private static HashMap<String, ArrayList<Record>> getTrainingData(int mode){
		HashMap<String, ArrayList<Record>> allData = new HashMap<String, ArrayList<Record>>();
		ArrayList<Record> positives = new ArrayList<Record>();
		ArrayList<Record> negatives = new ArrayList<Record>();
		String cat1 = null, cat2 = null;
		// Load histogram data into ArrayList
		try {
			switch(mode){
				case 1:
					cat1 = "City";
					cat2 = "Landscape";
					positives = readVector(CityLandscapeUtilities.class.getResourceAsStream("CityHistograms"));
					negatives = readVector(CityLandscapeUtilities.class.getResourceAsStream("LSHistograms"));
					break;
				case 2:
					cat1 = "City";
					cat2 = "Not City";
					positives = readVector(CityLandscapeUtilities.class.getResourceAsStream("CityHistograms"));
					negatives = readVector(CityLandscapeUtilities.class.getResourceAsStream("NotCityHistograms"));
					break;
				case 3:
					cat1 = "Landscape";
					cat2 = "Not Landscape";
					positives = readVector(CityLandscapeUtilities.class.getResourceAsStream("LSHistograms"));
					negatives = readVector(CityLandscapeUtilities.class.getResourceAsStream("NotLSHistograms"));
					break;
			}
				
			// Normalise Histogram data (last field in Records list is total edges)
			normaliseRecords(positives);
			normaliseRecords(negatives);
			
			// Store lists in a map of city or landscape
			allData = new HashMap<String, ArrayList<Record>>();
			allData.put(cat1, positives);
			allData.put(cat2, negatives);
			
			
			
		} catch (IOException e) {
			
			System.out.println("Could not load training set (File not found)");
			System.out.println("System will now exit");
			System.exit(1);
			
			
		} 
		
		return allData;
	}
	
	/**
	 * Calculates and returns message of given record detail array. Classifier is weighted so that images more closely
	 * related to a given image have more classification weighting. This is calculated by 1
	 * @param details the records to describe
	 * @param output the output mode
	 * @param mode the mode
	 * @return the description
	 */
	public static String getOutput(RecordDetail[] details, int output, int mode){
		
		double positiveCount = 0;
		double negativeCount = 0;
		String category, catPos, catNeg;
		double percentage;
		String allNeighbours = "";
		boolean inTrainingSet = false;
		
		switch(mode){
		case 1:
			catPos = "City";
			catNeg = "Landscape";
			break;
		case 2:
			catPos = "City";
			catNeg = "Not City";
			break;
		case 3:
			catPos = "Landscape";
			catNeg = "Not Landscape";
			break;
		default:
			catPos = "Undefined";
			catNeg = "Undefined";
			break;
		}
		
		for(RecordDetail rd: details){
			allNeighbours+= ("\n"+rd.toString());
			if(rd.closestDistance==0){
				inTrainingSet = true;
				catPos = rd.closestClass;
				break;
			}
			else{
				if (rd.closestClass.equals(catPos)){
					positiveCount+= (1/rd.closestDistance);
				}else{
					negativeCount+= (1/rd.closestDistance);
				}
			}
			
		}
		
		if(!inTrainingSet){
			double totalDistance = positiveCount + negativeCount;
			
			if(positiveCount>negativeCount){
				category = catPos;
				percentage = positiveCount/totalDistance*100;
			}else if(positiveCount<negativeCount){
				category = catNeg;
				percentage = negativeCount/totalDistance*100;
			}else{
				category = "Undecided";
				percentage = 0;
			}
		}else{
			percentage = 100;
			category = catPos;
			
		}
		
		BigDecimal percentString = new BigDecimal(percentage);
		BigDecimal rDistance = new BigDecimal(details[0].closestDistance);
		String message = "";
		switch(output){
			case 1:
				message = "Image Category: "+category+" with "+percentString.setScale(1, BigDecimal.ROUND_HALF_UP)+"% confidence"+"\n" +
				"Closest Related Image: "+details[0].closest.getImageName()+" with Euclidean distance of "+rDistance.setScale(4, BigDecimal.ROUND_HALF_UP);
				break;
			case 2:
				 message = "Image Category: "+category+" with "+percentString.setScale(1, BigDecimal.ROUND_HALF_UP)+"% confidence"+"\n" +
					"Closest Related Image: "+details[0].closest.getImageName()+" with Euclidean distance of "+rDistance.setScale(4, BigDecimal.ROUND_HALF_UP)+
					"\nK nearest neighbours (sorted by distance):"+allNeighbours;
				break;
			case 3:
				message = category;
				break;
		}
		return message;
	}
	
	/**
	 * Takes query vector to compare with integer k images from the training set
	 * @param query the query vector
	 * @param k the number of neighbours
	 * @param mode the mode
	 * @return the top k matching records
	 */
	 public static RecordDetail[] classifyImage(ArrayList<Double> query, int k, int mode){
		
		
		HashMap<String, ArrayList<Record>> tempData = getTrainingData(mode);
		
		RecordDetail[] recordDetails = new RecordDetail[k];
		
		for(int i = 0; i<k; i++){
			
			RecordDetail rd = new RecordDetail();
			for(String category:tempData.keySet()){
				
				for(Record r: tempData.get(category)){
					
					double d = CityLandscapeUtilities.distance(query, r.getVector());
					
					if(d < rd.closestDistance){
						rd.closestDistance = d;
						rd.closestClass = category;
						rd.closest = r;
					}
				}
				
			}
			
			recordDetails[i] = rd;
			tempData.get(rd.closestClass).remove(rd.closest);
			tempData.get(rd.closestClass).trimToSize();
			
		}
		
		return recordDetails;
	}
	 
	
	 
	 
	
	/**
	 * Function to read histogram data form text file of comma separated values, with each newline
	 * being the start of a new image. Returns values in an ArrayList. Final index holds total edge count.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	static ArrayList<Record> readVector(InputStream is) throws IOException{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		ArrayList<Record> toReturn = new ArrayList<Record>();
		int counter = 0;
		while((line = br.readLine())!=null){
			String [] array = line.split(",");
			Record r = new Record("Image"+ ++counter+".jpg");
			for(int i = 0; i<array.length; i++){
				
				r.getVector().add(Double.parseDouble(array[i]));
				
			}
			toReturn.add(r);
		}
		
		return toReturn;
		
	}
	
	/**
	 * Returns an ArrayList<Double> of which each index represents one element of a edge
	 * direction coherence vector
	 * @param imageName
	 * @return the EDCV
	 */
	public static ArrayList<Double> getImageVector(String imageName) {
		
		ArrayList<Double> queryVector = new ArrayList<Double>();
		
		FImage crgbimage;
		
		try {
			crgbimage = ImageUtilities.readF(new File(imageName));
			EdgeDirectionCoherenceVector cldo = new EdgeDirectionCoherenceVector();
			crgbimage.analyseWith(cldo);
			
			double[][] vec = new double[][] {
				cldo.getLastHistogram().incoherentHistogram.values,
				cldo.getLastHistogram().coherentHistogram.values
			};
			int n = cldo.getNumberOfDirBins();
			double edgeCounter = 0;
			
			for (int j = 0; j < n; j++){
				//Incoherent
				queryVector.add(vec[0][j]);
				edgeCounter += vec[0][j];
			}
			
			for(int j = 0; j< n; j++){
				//Coherent
				queryVector.add(vec[1][j]);
				edgeCounter += vec[1][j];
			}
			
			queryVector.add(edgeCounter);
			normaliseVector(queryVector);
		} catch (IOException e) {
			System.out.println("File with path: "+imageName+" not found.");
			System.exit(1);
		}
		
		return queryVector;
	}
	
	
	
	
	/**
	 * Checks whether a given directory contains valid images for classification
	 * @param name
	 * @return true if valid; false otherwise
	 */
	public static boolean isValidDirectory(String name) {
		File f = new File(name);
		
		for(String file: f.list()){
			if(!isImage(file, false)){
				System.out.println("Error: Directory contains non-image file:\n"+file.toString());
				return false;
			}
		}
		
		return true;
		
		
		
	}
	
	/**
	 * Obtains a files MIME type and returns true if it is of type image
	 * @param fileName
	 * @param output
	 * @return true if an image; false otherwise
	 */
	public static boolean isImage(String fileName, boolean output) {
		
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		String mimeType = mimeTypesMap.getContentType(fileName);
		
		if(output){
			System.out.println("Validating input file...");
			System.out.println("File type: "+mimeType);
		}
		
		if(mimeType.startsWith("image")){
			return true;
		}else{
			
			if(output){
				System.out.println("Invalid file type input. Please enter an image");
			}
			
			return false;
		}
		
	}
	
	
	
	/**
	 * Function reads from collection of histogram data and normalises values so image size
	 * is irrelevant when comparisons between histograms are made. 
	 */
	static void normaliseRecords(ArrayList<Record> records){
		for(Record r : records){
			ArrayList<Double> hist = r.getVector();
			normaliseVector(hist);
			
		}
		
	}
	
	/**
	 * Function normalises a given edge coherence vector
	 * @param record
	 */
	static void normaliseVector(ArrayList<Double> record){
		
		double totalEdges = record.get(record.size()-1);
		for(int i = 0; i< record.size(); i++){
			record.set(i, record.get(i)/totalEdges);
		}
	}
	
	static void normaliseVector(double [][] array, double totalEdges[]){
		for(int i = 0; i < array.length; i++){
			for (int j = 0; j < array[i].length; j++){
				array[i][j] = array[i][j]/totalEdges[i];
			}
		}
	}
	
	
	
	/**
	 * Takes two vectors and returns distance between them
	 * @param query
	 * @param record
	 * @return
	 */
	static double distance(ArrayList<Double> query, ArrayList<Double> record){
		double toSquareRoot = 0;
		
		for(int i = 0; i<query.size()-1; i++){
			toSquareRoot = toSquareRoot + (Math.pow((query.get(i)-record.get(i)), 2));
		}
		return Math.sqrt(toSquareRoot);
	}
	
}


/**
 * Class that holds details on images
 * @author Ajay Mehta (am24g08@ecs.soton.ac.uk)
 *
 */
class Record{
	
	private ArrayList<Double> vector;
	private String imageName;
	
	public Record(String iname){
		imageName = iname;
		vector = new ArrayList<Double>();
	}
	
	public String getImageName(){
		return imageName;
	}
	
	public ArrayList<Double> getVector(){
		return vector;
	}
	
}

/**
 * Class that stores information about a given input record.
 * @author Ajay Mehta (am24g08@ecs.soton.ac.uk)
 *
 */
class RecordDetail{
	
	protected Record closest;
	protected String closestClass;
	protected double closestDistance;
	
	public RecordDetail(){
		
		closest = null;
		closestClass = null;
		closestDistance = 99999999;
	}
	
	@Override
	public String toString(){
		BigDecimal b = new BigDecimal(closestDistance);
		if(closestClass.equals("city")){
			return "Image: "+closestClass+"/"+closest.getImageName()+"\t\tDistance: "+b.setScale(4, BigDecimal.ROUND_HALF_UP);
		}else{
			return "Image: "+closestClass+"/"+closest.getImageName()+"\tDistance: "+b.setScale(4, BigDecimal.ROUND_HALF_UP);
		}
	}
	
}
