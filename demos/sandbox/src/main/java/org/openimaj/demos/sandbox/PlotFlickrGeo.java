package org.openimaj.demos.sandbox;

import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.ui.ApplicationFrame;
import org.openimaj.math.geometry.point.Coordinate;

public class PlotFlickrGeo {
	static class ImgRec implements Coordinate {
		public static final Pattern csvregex = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))");

		String farm;
		String server;
		String id;
		String secret;
		String originalSecret;
		String mediumUrl;
		String imageDir;
		String title;
		String description;
		String license;
		String datePosted;
		String dateTaken;
		String ownerid;
		String username;
		String accuracy;
		double latitude;
		double longitude;
		String tags;

		@Override
		public Number getOrdinate(int dimension) {
			if (dimension==0) return latitude;
			return longitude;
		}

		@Override
		public int getDimensions() { return 2; }

		public float getOrdinateValue(int dimension) {
			if (dimension==0) return (float) latitude;
			return (float) longitude;
		}

		public static ImgRec makeImgRec(String s) {
			String [] parts = csvregex.split(s);

			if (parts.length == 19) {
				if (parts[15].trim() != "null" && parts[16].trim() != "null") {
					ImgRec rec = new ImgRec();
//					rec.farm            = parts[0].trim();
//					rec.server          = parts[1].trim();
//					rec.id              = parts[2].trim();
//					rec.secret          = parts[3].trim();
//					rec.originalSecret  = parts[4].trim();
//					rec.mediumUrl       = parts[5].trim();
//					rec.imageDir        = parts[6].trim();
//					rec.title           = parts[7].trim();
//					rec.description     = parts[8].trim();
//					rec.license         = parts[9].trim();
//					rec.datePosted      = parts[10].trim();
//					rec.dateTaken       = parts[11].trim();
//					rec.ownerid         = parts[12].trim();
//					rec.username        = parts[13].trim();
//					rec.accuracy        = parts[14].trim();
					if (parts[15].trim().length() == 0 ) return null; 
					rec.latitude        = Double.parseDouble(parts[15].trim());
					if (parts[16].trim().length() == 0 ) return null;
					rec.longitude       = Double.parseDouble(parts[16].trim());
//					rec.tags            = parts[17].trim();

					return rec;
				}
			}
			return null;
		}

		@Override
		public void readASCII(Scanner in) throws IOException {}

		@Override
		public String asciiHeader() { return null; }

		@Override
		public void readBinary(DataInput in) throws IOException {}

		@Override
		public byte[] binaryHeader() { return null; }

		@Override
		public void writeASCII(PrintWriter out) throws IOException {}

		@Override
		public void writeBinary(DataOutput out) throws IOException {}


		@Override
		public String toString() {
			String csv = "";
			csv += String.format("%s, ", farm);
			csv += String.format("%s, ", server);
			csv += String.format("%s, ", id);
			csv += String.format("%s, ", secret);
			csv += String.format("%s, ", originalSecret);
			csv += String.format("%s, ", mediumUrl);
			csv += String.format("%s, ", imageDir);
			csv += String.format("%s, ", title);
			csv += String.format("%s, ", description);
			csv += String.format("%s, ", license);
			csv += String.format("%s, ", datePosted);
			csv += String.format("%s, ", dateTaken);
			csv += String.format("%s, ", ownerid);
			csv += String.format("%s, ", username);
			csv += String.format("%s, ", accuracy);
			csv += String.format("%f, ", latitude);
			csv += String.format("%f, ", longitude);
			csv += String.format("%s, ", tags);
			csv += "\n";
			return csv;
		}
	}

//	public static void main(String[] args) throws IOException {
//		File inputcsv = new File("/Volumes/Raid/FlickrCrawls/AllGeo16/images.csv");
//
//		int size = 10000000;
//		List<float[]> data = new ArrayList<float[]>(size);
//		//read in images
//		BufferedReader br = new BufferedReader(new FileReader(inputcsv));
//		String line;
//		int i = 0;
//		while ((line = br.readLine()) != null) {
//			//ImgRec rec = ImgRec.makeImgRec(line);
//			
//			String [] parts = ImgRec.csvregex.split(line);
//
//			if (parts.length == 19) {
//				String p15 = parts[15].trim();
//				String p16 = parts[16].trim();
//				
//				if (p15 != "null" && p16 != "null" && p15.length()>0 && p16.length()>0) {
//					float latitude        = Float.parseFloat(p15);
//					float longitude       = Float.parseFloat(p16);
//					
//					data.add(new float[] {longitude, latitude});
//				}
//			}
//
//			if (i%10000 == 0) System.out.println(i);
//			if (i++ > size) break;
//		}
//
//		System.out.println("Done reading");
//		
//		float[][] dataArr = new float[2][data.size()];
//		for (i=0; i<data.size(); i++) {
//			dataArr[0][i] = data.get(i)[0];
//			dataArr[1][i] = data.get(i)[1];
//		}
//		
//		NumberAxis domainAxis = new NumberAxis("X");
//        domainAxis.setRange(-180, 180);
//        NumberAxis rangeAxis = new NumberAxis("Y");
//        rangeAxis.setRange(-90, 90);
//        FastScatterPlot plot = new FastScatterPlot(dataArr, domainAxis, rangeAxis);
//        
//        JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);
//        chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		
//		ChartPanel chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
//        final ApplicationFrame frame = new ApplicationFrame("Title");
//        frame.setContentPane(chartPanel);
//        frame.pack();
//        frame.setVisible(true);
//	}
	
//	public static void main(String[] args) throws IOException {
//		File inputcsv = new File("/Volumes/Raid/FlickrCrawls/AllGeo16/images.csv");
//		File outputcsv = new File("/Users/jsh2/Desktop/world-geo.csv");
//
//		BufferedWriter bw = new BufferedWriter(new FileWriter(outputcsv));
//		
//		//read in images
//		BufferedReader br = new BufferedReader(new FileReader(inputcsv));
//		String line;
//		int i = 0;
//		while ((line = br.readLine()) != null) {
//			String [] parts = ImgRec.csvregex.split(line);
//
//			if (parts.length == 19) {
//				String p15 = parts[15].trim();
//				String p16 = parts[16].trim();
//				
//				if (p15 != "null" && p16 != "null" && p15.length()>0 && p16.length()>0) {
//					float latitude        = Float.parseFloat(p15);
//					float longitude       = Float.parseFloat(p16);
//					
//					bw.write(String.format("%f, %f\n", longitude, latitude));
//				}
//			}
//
//			if (i++%10000 == 0) System.out.println(i);
//		}
//	}
	
	public static void main(String[] args) throws IOException {
		File inputcsv = new File("/Users/jsh2/Desktop/world-geo.csv");
		List<float[]> data = new ArrayList<float[]>(10000000);

		//read in images
		BufferedReader br = new BufferedReader(new FileReader(inputcsv));
		String line;
		int i = 0;
		while ((line = br.readLine()) != null) {
			String [] parts = line.split(",");
			
			float longitude       = Float.parseFloat(parts[0]);
			float latitude        = Float.parseFloat(parts[1]);

			data.add(new float[] {longitude, latitude});

			if (i++%10000 == 0) System.out.println(i);
		}
		
		System.out.println("Done reading");
		
		float[][] dataArr = new float[2][data.size()];
		for (i=0; i<data.size(); i++) {
			dataArr[0][i] = data.get(i)[0];
			dataArr[1][i] = data.get(i)[1];
		}
		
		NumberAxis domainAxis = new NumberAxis("X");
        domainAxis.setRange(-180, 180);
        NumberAxis rangeAxis = new NumberAxis("Y");
        rangeAxis.setRange(-90, 90);
        FastScatterPlot plot = new FastScatterPlot(dataArr, domainAxis, rangeAxis);
        
        JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);
        chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        final ApplicationFrame frame = new ApplicationFrame("Title");
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
	}
}
