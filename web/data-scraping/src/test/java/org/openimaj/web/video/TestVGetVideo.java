/**
 * Copyright 2011 The University of Southampton, Yahoo Inc., and the
 * individual contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openimaj.web.video;

import java.net.MalformedURLException;

import org.junit.Test;
import org.openimaj.image.DisplayUtilities;

import com.github.axet.vget.info.VideoInfo.VideoQuality;
import com.github.axet.vget.info.VideoInfoUser;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TestVGetVideo {

	/**
	 * @throws Exception
	 */
	@Test
	public void testVget() throws Exception {
		try {
			tryDuelingCarl();
		} catch (final Exception e) {
			System.out.println("Problem downloading video...");
		}
	}

	private void tryDuelingCarl() throws MalformedURLException {
		final String testVideo = "https://www.youtube.com/watch?v=t-7mQhSZRgM";
		VGetVideo v = new VGetVideo(testVideo);
		int frames = 0;
		while (v.hasNextFrame()) {
			frames++;
			v.getNextFrame();
		}
		System.out.println("Seen frames: " + frames);
		final VideoInfoUser user = new VideoInfoUser();
		user.setUserQuality(VideoQuality.p144);
		v = new VGetVideo(testVideo, user);
		int newframes = 0;
		while (v.hasNextFrame()) {
			newframes++;
			// v.getNextFrame();
			DisplayUtilities.displayName(v.getNextFrame(), "frame");
		}
		System.out.println("Low Quality frames: " + newframes);
	}

}
