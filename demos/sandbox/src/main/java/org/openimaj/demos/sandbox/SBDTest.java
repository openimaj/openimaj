/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.demos.sandbox;

import java.io.File;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import org.openimaj.image.ImageUtilities;
//import org.openimaj.image.MBFImage;
//import org.openimaj.image.processing.face.detection.DetectedFace;
//import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
//import org.openimaj.video.Video;
//import org.openimaj.video.processing.shotdetector.ShotBoundary;
//import org.openimaj.video.processing.shotdetector.VideoShotDetector;
//import org.openimaj.video.timecode.FrameNumberVideoTimecode;
//import org.openimaj.video.timecode.HrsMinSecFrameTimecode;
//import org.openimaj.video.xuggle.XuggleVideo;
//
//public class SBDTest {
//	static List<Callable<Boolean>> jobs = new ArrayList<Callable<Boolean>>();
//		
//	public static void main(String[] args) throws IOException, InterruptedException {
//		File vidRoot = new File("/Volumes/data/livememories/RTTR/video");
//		
//		for (File f : vidRoot.listFiles()) {
//			searchAndProcess(f);
//		}
//	
//		ExecutorService es = Executors.newFixedThreadPool(8);
//		es.invokeAll(jobs);
//	}
//	
//	private static void searchAndProcess(final File file) throws IOException {
//		if (file.isDirectory()) {
//			for (File f : file.listFiles()) 
//				searchAndProcess(f);
//		} else if (file.getName().endsWith(".mpg")) {
//			System.out.println("Processing: "+file);
//			
//			jobs.add(new Callable<Boolean>() {
//
//				@Override
//				public Boolean call() throws Exception {
//					processVideo(file);
//					return true;
//				}});
//		}
//	}
//
//	static void processVideo(File file) throws IOException {
//		Video<MBFImage> vid = new XuggleVideo(file);
//		
//		VideoShotDetector<MBFImage> vsd = new VideoShotDetector<MBFImage>(vid);
//		vsd.setStoreAllDifferentials(false);
//		vsd.setFindKeyframes(false);
//		vsd.process();
//		
//		List<ShotBoundary> boundaries = vsd.getShotBoundaries();
//		ShotBoundary prev = boundaries.get(0);
//		vid.setCurrentFrameIndex(0);
//		
//		boundaries.add(new ShotBoundary(new HrsMinSecFrameTimecode(vid.countFrames(),vid.getFPS())));
//		
//		HaarCascadeDetector fd = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load();
//		fd.setMinSize(40);
//		
//		for (int i=1; i<boundaries.size(); i++) {
//			ShotBoundary next = boundaries.get(i);
//						
//			long pframe = prev.getTimecode().getFrameNumber();
//			long nframe = next.getTimecode().getFrameNumber();
//			long mframe = pframe + ((nframe - pframe) / 2);
//			
//			vid.setCurrentFrameIndex(mframe);
//			MBFImage frame = vid.getCurrentFrame();
//			
//			List<DetectedFace> faces = fd.detectFaces(frame.flatten());
//			for (DetectedFace f : faces) {
//				MBFImage fi = frame.extractROI(f.getBounds());
//				
//				saveFace(fi, file, mframe);
//			}
//			
//			prev = next;
//		}
//	}
//
//	private static void saveFace(MBFImage fi, File file, long mframe) throws IOException {
//		File base = new File("/Volumes/data/livememories/RTTR/faces");
//		File img = new File(base, file.getName().replace(".mpg", "") + "#" + mframe + ".png");
//		
//		ImageUtilities.write(fi, img);
//	}
//}
import org.openimaj.video.processing.shotdetector.ShotBoundary;
import org.openimaj.video.processing.shotdetector.ShotDetectedListener;
import org.openimaj.video.processing.shotdetector.VideoKeyframe;
import org.openimaj.video.processing.shotdetector.VideoShotDetector;
import org.openimaj.video.timecode.VideoTimecode;
import org.openimaj.video.xuggle.XuggleVideo;



public class SBDTest implements ShotDetectedListener<MBFImage> {
	@Override
	public void shotDetected(ShotBoundary sb, VideoKeyframe<MBFImage> vk) {
		System.out.println("adding keyframe " + sb.getTimecode());
	}

	@Override
	public void differentialCalculated(VideoTimecode vt, double d, MBFImage frame) {
		// do nothing
	}
	
	public static void main(String[] args) {
		SBDTest listener = new SBDTest();
		Video<MBFImage> vid = new XuggleVideo(new File("/Users/jsh2/20110827_181000_bbcone_doctor_who.ts.mpg"));
		VideoShotDetector<MBFImage> vsd = new VideoShotDetector<MBFImage>(vid);
		vsd.setThreshold(55500);
		vsd.setStoreAllDifferentials(false);
		vsd.setFindKeyframes(false);
		vsd.addShotDetectedListener(listener);
		vsd.process();
		
	}
}
