package org.openimaj.demos.sandbox.image.puzzles;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.shape.Polygon;

public class SelectedImage {
	private static final String IMAGE_OUTPUT = "img.png";
	private static final String SELECTION_OUTPUT = "selection";
	private Polygon poly;
	private MBFImage img;
	public SelectedImage(MBFImage img, Polygon polygon) {
		this.img = img;
		this.poly = polygon;
	}
	public static SelectedImage selectPolygon(MBFImage boardImg) throws IOException {
		ImageSelectionFrame isf = new ImageSelectionFrame(boardImg);
		try {
			isf.waitForPolygonSelection();
			return isf.getSelectedImage();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	public void read(File f) throws IOException{
		File imgOutout = new File(f,IMAGE_OUTPUT);
		File selectionOutput = new File(f,SELECTION_OUTPUT);

		this.img = ImageUtilities.readMBF(imgOutout);
		this.poly = IOUtils.readFromFile(selectionOutput);
	}

	/**
	 * @param f
	 * @throws IOException
	 */
	public void write(File f) throws IOException{
		if(!f.exists()){
			f.mkdirs();
		}
		else{
			throw new IOException("Location exists");
		}
		File imgOutout = new File(f,IMAGE_OUTPUT);
		File selectionOutput = new File(f,SELECTION_OUTPUT);

		ImageUtilities.write(img, imgOutout);
		IOUtils.writeToFile(this.poly, selectionOutput);
	}
}
