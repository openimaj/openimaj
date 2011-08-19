package org.openimaj.image.processing.algorithm;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.renderer.RenderHints;
import org.openimaj.math.geometry.shape.Circle;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * Perform forward and inverse Fast Fourier Transforms on image data.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class FourierTransform {
	private FImage phase;
	private FImage magnitude;
	private boolean centre;

	/**
	 * Construct Fourier Transform by performing a forward transform
	 * on the given image. If the centre option is set, the FFT will 
	 * be re-ordered so that the DC component is in the centre.
	 * @param image the image to transform
	 * @param centre should the FFT be reordered so the centre is DC component
	 */
	public FourierTransform(FImage image, boolean centre) {
		this.centre = centre;

		process(image);
	}
	
	/**
	 * Construct Fourier Transform object from the given magnitude and
	 * phase images in the frequency domain. The resultant object can
	 * then be used to construct the image using the {@link #inverse()}
	 * method.
	 * 
	 * @param magnitude the magnitude image
	 * @param phase the phase image
	 * @param centre is the DC component in the image centre?
	 */
	public FourierTransform(FImage magnitude, FImage phase, boolean centre) {
		this.centre = centre;
		this.magnitude = magnitude;
		this.phase = phase;
	}

	private void process(FImage image) {
		int cs = image.getCols();
		int rs = image.getRows();
		
		phase = new FImage(cs, rs);
		magnitude = new FImage(cs, rs);
		
		FloatFFT_2D fft = new FloatFFT_2D(rs,cs);
		float[][] prepared = new float[rs][cs*2];
		
		if (centre) {
			for(int r = 0; r < rs ; r++) {
				for(int c = 0; c < cs; c++) {
					prepared[r][c*2] = image.pixels[r][c] * (1 - 2 * ((r+c)%2) );
				}
			}
		} else {
			for(int r = 0; r < rs ; r++) {
				for(int c = 0; c < cs; c++) {
					prepared[r][c*2] = image.pixels[r][c];
				}
			}
		}

		fft.complexForward(prepared);
		
		for(int y = 0; y < rs; y++){
			for(int x = 0; x < cs; x++){
				float re = prepared[y][x*2];
				float im = prepared[y][1 + x*2];
				
				phase.pixels[y][x] = (float) Math.atan2(im, re);
				magnitude.pixels[y][x] = (float) Math.sqrt(re*re + im*im);
			}
		}
	}
	
	/**
	 * Perform the inverse FFT using the underlying magnitude
	 * and phase images. The resultant reconstructed image
	 * may need normalisation.
	 * 
	 * @return the reconstructed image
	 */
	public FImage inverse() {
		int cs = magnitude.getCols();
		int rs = magnitude.getRows();
		
		FloatFFT_2D fft = new FloatFFT_2D(rs,cs);
		float[][] prepared = new float[rs][cs*2];
		for(int y = 0; y < rs; y++) {
			for(int x = 0; x < cs; x++) {
				float p = phase.pixels[y][x];
				float m = magnitude.pixels[y][x];

				float re = (float) (m*Math.cos(p));
				float im = (float) (m*Math.sin(p));
				
				prepared[y][x*2] = re;
				prepared[y][1 + x*2] = im;				
			}
		}
		
		fft.complexInverse(prepared, true);
		
		FImage image = new FImage(cs, rs);
		if (centre) {
			for(int r = 0; r < rs ; r++) {
				for(int c = 0; c < cs; c++) {
					image.pixels[r][c] = prepared[r][c*2] * (1 - 2 * ((r+c)%2) );
				}
			}
		} else {
			for(int r = 0; r < rs ; r++) {
				for(int c = 0; c < cs; c++) {
					image.pixels[r][c] = prepared[r][c*2];
				}
			}
		}
		
		return image;
	}
	
	/**
	 * Get a log-normalised copy of the magnitude image suitable
	 * for displaying.
	 * @return a log-normalised copy of the magnitude image 
	 */
	public FImage getLogNormalisedMagnitude() {
		FImage im = magnitude.clone();
		
		for (int y=0; y<im.height; y++) {
			for (int x=0; x<im.width; x++) {
				im.pixels[y][x] = (float) Math.log(im.pixels[y][x] + 1);
			}
		}
		
		return im.normalise();
	}
	
	/**
	 * @return the phase image
	 */
	public FImage getPhase() {
		return phase;
	}

	/**
	 * @return the magnitude image
	 */
	public FImage getMagnitude() {
		return magnitude;
	}

	/**
	 * @return true if the DC component is in the centre; false otherwise
	 */
	public boolean isCentre() {
		return centre;
	}

	public static void main(String [] args) throws MalformedURLException, IOException {
		FImage image = ImageUtilities.readF(new URL("http://upload.wikimedia.org/wikipedia/commons/4/4a/Thumbs_up_for_bokeh.JPG"));
		FourierTransform ft = new FourierTransform(image, true);
		
//		ft.magnitude.drawShapeFilled(new Circle(ft.magnitude.width/2, ft.magnitude.height/2, 10), 0f);
//		FImage mask = new FImage(ft.magnitude.width, ft.magnitude.height);
//		mask.drawShapeFilled(new Circle(ft.magnitude.width/2, ft.magnitude.height/2, 100), 1f);
//		mask.createRenderer(RenderHints.FAST).drawShape(new Circle(ft.magnitude.width/2, ft.magnitude.height/2, 100), 80, 1f);
//		ft.magnitude.multiplyInline(mask);
		
		DisplayUtilities.display(ft.getLogNormalisedMagnitude());
		DisplayUtilities.display(ft.inverse().normalise());
	}

}
