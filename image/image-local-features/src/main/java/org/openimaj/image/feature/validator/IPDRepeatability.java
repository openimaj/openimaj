package org.openimaj.image.feature.validator;

import gnu.trove.TDoubleArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.feature.local.interest.InterestPointDetector;
import org.openimaj.image.feature.local.keypoints.InterestPointKeypoint;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

public class IPDRepeatability {
	private MBFImage image1;
	private MBFImage image2;
	private List<InterestPointData> image1Points;
	private List<InterestPointData> image2Points;
	private Matrix homography;
	private List<InterestPointData> validImage2Points;

	public IPDRepeatability(MBFImage image1, MBFImage image2, List<InterestPointData> image1Points,List<InterestPointData> image2Points,Matrix homography){
		setup(image1,image2,image1Points,image2Points,homography);
	}
	
	public IPDRepeatability(File image1f, File image2f, InterestPointDetector ipd,File homographyf) throws IOException{
		MBFImage image1 = ImageUtilities.readMBF(image1f);
		MBFImage image2 = ImageUtilities.readMBF(image2f);
		
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image1));
		List<InterestPointData> image1Points = ipd.getInterestPoints(20);
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image2));
		List<InterestPointData> image2Points = ipd.getInterestPoints(20);
		
		Matrix homography = readHomography(homographyf);
		setup(image1,image2,image1Points,image2Points,homography);
	}

	private void setup(MBFImage image1, MBFImage image2,List<InterestPointData> image1Points,List<InterestPointData> image2Points, Matrix homography) {
		this.image1 = image1; this.image2 = image2; this.image1Points = image1Points; this.image2Points = image2Points; this.homography = homography;
		
		Shape validArea = this.image1.getBounds().transform(homography);
		this.validImage2Points = new ArrayList<InterestPointData>();
		for(InterestPointData data : image2Points){
			if(validArea.isInside(data)){
				this.validImage2Points.add(data);
			}
		}
	}
	
	public static double calculateOverlapPercentage(Ellipse e1, Ellipse e2){
		double e1Area = e1.calculateArea();
		double e2Area = e2.calculateArea();
		
		double intersection = e1.asPolygon().intersectionArea(e2.asPolygon(), 50);
		return intersection/( (e1Area - intersection) + (e2Area - intersection) );
	}
	
	public double calculateTotalOverlap(){
		return 0;
	}

	private Matrix readHomography(File homographyf) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(homographyf)));
		List<TDoubleArrayList> doubleListList = new ArrayList<TDoubleArrayList>();
		String line = null;
		int nCols = -1;
		int nRows = 0;
		while((line = reader.readLine())!=null){
			boolean anyAdded = false;
			String[] parts = line.split(" ");
			TDoubleArrayList doubleList = new TDoubleArrayList();
			int currCols= 0;
			for(String part : parts){
				if(part.length() != 0){
					anyAdded = true;
					doubleList.add(Double.parseDouble(part));
					currCols++;
				}
			}
			if(nCols == -1) nCols = currCols;
			if(currCols!=nCols) throw new IOException("Could not read matrix file");
			if(anyAdded){
				doubleListList.add(doubleList);
				nRows ++;
			}
		}
		Matrix ret = new Matrix(nRows,nCols);
		int rowNumber = 0;
		for(TDoubleArrayList doubleList: doubleListList){
			doubleList.toNativeArray(ret.getArray()[rowNumber++], 0, nCols);
		}
		return ret;
	}
	
	public static void main(String args[]){
		MBFImage image = new MBFImage(800,800,ColourSpace.RGB);
		// Ellipse 22 from feat1 and 13 from feat2t matching with an overlap of 56.2%
		// feat1(x,y,covar): 476.740000000000 332.750000000000 0.0295748000000000 0.00912059000000000 0.168777000000000
		// feat2t(x,y,covar): 478.327795489303 347.307031057487 0.0580511044822673 -0.00352030742001502 0.0430229989807244
		
		Ellipse e1 = EllipseUtilities.ellipseFromCovariance(476.74f,332.75f, new Matrix(new double[][]{{0.0295748,0.00912059},{0.00912059,0.168777}}), 10.0f);
		Ellipse e2 = EllipseUtilities.ellipseFromCovariance(478.33f,347.31f, new Matrix(new double[][]{{0.0580511,-0.00352031},{-0.00352031,0.043023}}), 10.0f);
		
		System.out.println(IPDRepeatability.calculateOverlapPercentage(e1,e2));
		
		image.drawShape(e1, RGBColour.RED);
		image.drawShape(e2, RGBColour.BLUE);
		
		DisplayUtilities.display(image);
	}
}
