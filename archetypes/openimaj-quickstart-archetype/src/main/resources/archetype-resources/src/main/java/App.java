#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import ${package}.image.DisplayUtilities;
import ${package}.image.MBFImage;
import ${package}.image.colour.ColourSpace;
import ${package}.image.colour.RGBColour;
import ${package}.image.processing.convolution.FGaussianConvolve;
import ${package}.image.typography.hershey.HersheyFont;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
    	//Create an image
        MBFImage image = new MBFImage(320,70, ColourSpace.RGB);
        
        //Render some test into the image
        image.drawText("Hello World", 10, 60, HersheyFont.CURSIVE, 50, RGBColour.RED);

        //Apply a Gaussian blur
        image.processInline(new FGaussianConvolve(2f));
        
        //Display the image
        DisplayUtilities.display(image);
    }
}
