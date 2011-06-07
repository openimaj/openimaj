package org.openimaj.image.processing.face;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TFloatArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.parts.FacePipeline;
import org.openimaj.image.processing.face.parts.DetectedFace;

public class FaceTest {
	//	public static void main(String [] args) throws IOException, ClassNotFoundException {
	//		FacePipeline engine = new FacePipeline();
	//		File dir = new File("/Volumes/Raid/face_databases/gt_db");
	//
	//		File imagefile1 = new File(dir, "s01/01.jpg");
	//		File imagefile2 = new File(dir, "s01/02.jpg");
	//		
	//		FacialDescriptor d1 = engine.extractFaces(ImageUtilities.readF(imagefile1)).get(0);
	//		FacialDescriptor d2 = engine.extractFaces(ImageUtilities.readF(imagefile2)).get(0);
	//		
	//		System.out.println(d1.getFeatureVector().compare(d2.getFeatureVector(), FloatFVComparison.EUCLIDEAN));
	//		
	//		for (int i=0; i<13; i++) {
	//			FloatFV f1 = new FloatFV(d1.faceParts.get(i).featureVector);
	//			FloatFV f2 = new FloatFV(d2.faceParts.get(i).featureVector);
	//			
	//			DisplayUtilities.display(d1.faceParts.get(i).getImage());
	//			DisplayUtilities.display(d2.faceParts.get(i).getImage());
	//			
	//			System.out.println(f1.compare(f2, FloatFVComparison.EUCLIDEAN));
	//		}
	//	}

	public static void main(String [] args) throws IOException, ClassNotFoundException {
		File dir = new File("/Volumes/Raid/face_databases/gt_db");

		int N_PEOPLE = 15;

		DetectedFace[][] descriptors = new DetectedFace[50][N_PEOPLE];

		FacePipeline engine = new FacePipeline();

		for (int p=1; p<=50; p++) {
			for (int i=1; i<=N_PEOPLE; i++) {
				File imagefile = new File(dir, String.format("s%02d/%02d.jpg", p, i));
				File featurefile = new File(dir, String.format("s%02d/%02d.bin", p, i));

				System.out.println(imagefile);

				if (featurefile.exists()) {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(featurefile));
					descriptors[p-1][i-1] = (DetectedFace) ois.readObject();
					ois.close();

				} else {
					FImage image = ImageUtilities.readF(imagefile);

					List<DetectedFace> descrs = engine.extractFaces(image);

					if (descrs.size() == 1) {
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(featurefile));
						oos.writeObject(descrs.get(0));
						oos.close();

						descriptors[p-1][i-1] = descrs.get(0);
					}
				}
			}
		}

//				for (FacialDescriptor[] d : descriptors) {
//					for (int f=0; f<13; f++) {
//						DescriptiveStatistics ds = new DescriptiveStatistics();
//						
//						for (int i=0; i<15; i++) {
//							for (int j=0; j<15; j++) {
//								if (i != j && d[i] != null && d[j] != null) {
//									FloatFV fi = new FloatFV(d[i].faceParts.get(f).featureVector);
//									FloatFV fj = new FloatFV(d[j].faceParts.get(f).featureVector);
//									
//									ds.addValue(fi.compare(fj, FloatFVComparison.EUCLIDEAN));
//								}
//							}
//						}
//						
//						System.out.print(ds.getMean() + "\t");
//					}
//				
//					System.out.println();
//				}

		for (int f=0; f<13; f++) { //feature
			DescriptiveStatistics ds = new DescriptiveStatistics();

			for (int pi=0; pi<50; pi++) { //person i
				for (int ii=0; ii<15; ii++) { //instance of person i
					for (int pj=0; pj<50; pj++) { //person j
						for (int jj=0; jj<15; jj++) { //instance of person j
							if (pi != pj && descriptors[pi][ii]!=null && descriptors[pj][jj]!=null) { //don't compare same person
								FloatFV fi = new FloatFV(descriptors[pi][ii].faceParts.get(f).featureVector);
								FloatFV fj = new FloatFV(descriptors[pj][jj].faceParts.get(f).featureVector);

								ds.addValue(fi.compare(fj, FloatFVComparison.EUCLIDEAN));
							}
						}
					}
				}
			}
			System.out.print(ds.getMean() + "\t");
		}
		System.out.println();
	}
}
