package org.openimaj;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.pixel.statistics.HistogramModel;

public class HistogramTest {
	public static void main(String[] args) throws IOException {
		HistogramModel model = new HistogramModel(16, 16, 16);
        MBFImage im = ImageUtilities.readMBF(new File("/Users/jsh2/Pictures/08-earth_shuttle2.jpg"));
        
        im = ColourSpace.convert(im, ColourSpace.CIE_Lab);

        im.bands.get(0).divideInplace(100F);
        im.bands.get(1).subtractInplace(-127F).divideInplace(256F);
        im.bands.get(2).subtractInplace(-127F).divideInplace(256F);
        
        System.out.println(im.bands.get(2));
        
        /* ERROR HERE */
        model.estimateModel(im);
        
        double[] arr1 = model.histogram.values.clone();
        
        im = ImageUtilities.readMBF(new File("/Users/jsh2/Pictures/08-earth_shuttle2.jpg"));
        im = ColourSpace.convert(im, ColourSpace.CIE_Lab_Norm);
        model.estimateModel(im);
        double[] arr2 = model.histogram.values.clone();
        
        
        System.out.println(Arrays.toString(arr1));
        System.out.println(Arrays.toString(arr2));
        System.out.println(Arrays.equals(arr1, arr2));
	}
}
