package org.openimaj.demos.sandbox.image.puzzles;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class PuzzlePeicePlayground {
	String dropBoxImagesRoot = "/Users/ss/Dropbox/Camera Uploads/";
	String workspaceRoot = "/Users/ss/Dropbox/Projects/Puzzles";
	String completedLoc = "2013-01-06 16.39.56.jpg";
	String texturedPeicesLoc = "2013-01-06 17.10.53-4.jpg";
	String plainPeicesLoc = "2013-01-06 17.02.40-1.jpg";
	String completedSelectedLoc = "butterflys/box";

	ResizeProcessor rp = new ResizeProcessor(800);
	private MBFImage boardImg;
	private MBFImage texturedImg;
	private MBFImage plainImg;

	public PuzzlePeicePlayground() throws IOException {
		initImages();
	}

	private void initImages() throws IOException {
		this.boardImg = ImageUtilities.readMBF(new File(dropBoxImagesRoot,completedLoc));
//		this.texturedImg = ImageUtilities.readMBF(new File(dropBoxImagesRoot,texturedPeicesLoc));
//		this.plainImg = ImageUtilities.readMBF(new File(dropBoxImagesRoot,plainPeicesLoc));

		boardImg.processInplace(rp);
//		texturedImg.processInplace(rp);
//		plainImg.processInplace(rp);

	}

	public static void main(String[] args) throws IOException {
		PuzzlePeicePlayground playground = new PuzzlePeicePlayground();
		playground.selectBoard();
	}

	private void selectBoard() throws IOException {
		SelectedImage selectedImage = SelectedImage.selectPolygon(boardImg);
	}
}
