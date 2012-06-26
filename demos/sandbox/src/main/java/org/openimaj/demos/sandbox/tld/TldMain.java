package org.openimaj.demos.sandbox.tld;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.io.FileUtils;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.MBFImageFileBackedVideo;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

/**
 * Runs the TLD code, equivilant to run_TLD in the matlab
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TldMain {
	static String inputPath = "/Users/ss/Development/matlab/OpenTLD/_input";
	public static void main(String[] args) throws IOException {
		MBFImageFileBackedVideo video = readImageDirVideo();
//		VideoDisplay<MBFImage> disp = VideoDisplay.createVideoDisplay(video);
		Rectangle boundingBox = readBoundingBox();
		System.out.println(boundingBox);
		TLDOptions opt = new TLDOptions();
		final TLDTracker tracker = new TLDTracker(opt);
		MBFImage first = video.getCurrentFrame();
		tracker.init(first.flatten(), boundingBox);
		video.getNextFrame();
		VideoDisplay<MBFImage> disp = VideoDisplay.createVideoDisplay(video);
		disp.addVideoListener(new VideoDisplayListener<MBFImage>() {
			
			@Override
			public void beforeUpdate(MBFImage frame) {
				tracker.processFrame(frame.flatten());
				if(tracker.tracker == null) return;
				frame.drawShape(tracker.currentBoundingBox, 3, RGBColour.RED);
				frame.drawPoints(tracker.getTrackerPoints(), RGBColour.BLUE, 2);
			}
			
			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	private static Rectangle readBoundingBox() throws IOException {
		File bbFile = new File(inputPath,"init.txt");
		String bbStr = FileUtils.readall(bbFile);
		String[] bbParts = bbStr.split(",");
		int x1 = Integer.parseInt(bbParts[0]);
		int y1 = Integer.parseInt(bbParts[1]);
		int x2 = Integer.parseInt(bbParts[2]);
		int y2 = Integer.parseInt(bbParts[3].trim());
		return new Rectangle(x1,y1,(x2-x1),(y2-y1));
	}
	private static MBFImageFileBackedVideo readImageDirVideo() {
		File input = new File(inputPath);
		File[] fileList = input.listFiles(new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("png");
			}
		}) ;
		Arrays.sort(fileList, new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				Integer o1Number = Integer.parseInt(o1.getName().split("[.]")[0]);
				Integer o2Number = Integer.parseInt(o2.getName().split("[.]")[0]);
				return o1Number.compareTo(o2Number);
			}
			
		});
		
		return new MBFImageFileBackedVideo(Arrays.asList(fileList));
	}
}
