package org.openimaj.demos.sandbox.image.gif;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.IIOException;

import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.video.MBFImageFileBackedVideo;

import scala.actors.threadpool.Arrays;

public class GifWriterPayground {
	private static List<Image<?,?>> frames;
	public static void main(String[] args) throws IIOException, FileNotFoundException, IOException {
//		VideoCapture vc = new VideoCapture( 640,480 );
		createGif("/Users/ss/Pictures/emmawedding/lillygif","2012-08-13 18.55.32-");
		createGif("/Users/ss/Pictures/emmawedding/andygif","2012-08-13 20.06.20-");
	}
	private static void createGif(String pathname,final String filepattern) throws IIOException, FileNotFoundException, IOException {
		System.out.println("Creating " + pathname);
		File[] files = new File(pathname).listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				return name .startsWith(filepattern);
			}
		});
		List<File> flist = Arrays.asList(files);
		final GifSequenceWriter gifWriter = GifSequenceWriter.createWriter(200, true, new File(pathname + "out.gif"));
		File f;
		for (int i = 0; i < files.length; i++) {
			f = flist.get(i);
			MBFImage mbfImage = ImageUtilities.readMBF(f);
			System.out.println("Writing frame!");
			gifWriter.writeToSequence(mbfImage.process(new ResizeProcessor(640, 480)));
		}
		for (int i = files.length-2; i > 0; i--) {
			f = flist.get(i);
			MBFImage mbfImage = ImageUtilities.readMBF(f);
			System.out.println("Writing frame!");
			gifWriter.writeToSequence(mbfImage.process(new ResizeProcessor(640, 480)));
		}
		gifWriter.close();
	}
}
