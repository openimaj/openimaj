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
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.xuggle.XuggleVideo;

import com.github.axet.vget.VGet;
import com.github.axet.vget.info.VideoInfoUser;

/**
 * A wrapper around {@link VGet}, which supports video download from youtube, vimeo 
 * and a few other video sites. Just providing the URL uses the first video URL
 * {@link VGet} finds. You can also provide a {@link VideoInfoUser} to control
 * the quality of video downloaded 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class VGetVideo extends Video<MBFImage>{
	
	private XuggleVideo vid;

	/**
	 * @param url
	 * @throws MalformedURLException 
	 */
	public VGetVideo(String url) throws MalformedURLException {
		VGet v = new VGet(new URL(url));
		v.extract();
		this.vid = new XuggleVideo(v.getVideo().getInfo().getSource());
		
	}
	
	/**
	 * @param url
	 * @param iu
	 * @throws MalformedURLException 
	 */
	public VGetVideo(String url, VideoInfoUser iu) throws MalformedURLException{
		VGet v = new VGet(new URL(url));
		AtomicBoolean stop = new AtomicBoolean(false);
		v.extract(iu,stop, new Runnable() {	@Override public void run() {}});
		this.vid = new XuggleVideo(v.getVideo().getInfo().getSource());
	}

	@Override
	public MBFImage getNextFrame() {
		return vid.getNextFrame();
	}

	@Override
	public MBFImage getCurrentFrame() {
		return vid.getCurrentFrame();
	}

	@Override
	public int getWidth() {
		return vid.getWidth();
	}

	@Override
	public int getHeight() {
		return vid.getHeight();
	}

	@Override
	public long getTimeStamp() {
		return vid.getTimeStamp();
	}

	@Override
	public double getFPS() {
		return vid.getFPS();
	}

	@Override
	public boolean hasNextFrame() {
		return vid.hasNextFrame();
	}

	@Override
	public long countFrames() {
		return vid.countFrames();
	}

	@Override
	public void reset() {
		vid.reset();
	}

}
