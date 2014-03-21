package org.openimaj.web.video;

import java.net.MalformedURLException;

import org.junit.Test;

import com.github.axet.vget.info.VideoInfo.VideoQuality;
import com.github.axet.vget.info.VideoInfoUser;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestVGetVideo {
	
	@Test
	public void testVget() throws Exception {
		try{
			tryDuelingCarl();
		} catch(Exception e){
			System.out.println("Problem downloading video...");
		}
	}

	private void tryDuelingCarl() throws MalformedURLException {
		String testVideo = "https://www.youtube.com/watch?v=t-7mQhSZRgM";
		VGetVideo v = new VGetVideo(testVideo);
		int frames = 0;
		while (v.hasNextFrame()) {
			frames++;
			v.getNextFrame();
		}
		System.out.println("Seen frames: " + frames);
		VideoInfoUser user = new VideoInfoUser();
        user.setUserQuality(VideoQuality.p144);
        v = new VGetVideo(testVideo, user);
        int newframes = 0;
        while (v.hasNextFrame()) {
			newframes++;
			v.getNextFrame();
		}
		System.out.println("Low Quality frames: " + newframes);
	}

}
