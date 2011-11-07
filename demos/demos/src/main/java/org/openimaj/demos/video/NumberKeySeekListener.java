package org.openimaj.demos.video;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.xuggle.XuggleVideo;

public class NumberKeySeekListener implements KeyListener {

	private VideoDisplay<MBFImage> display;
	private long duration;

	public NumberKeySeekListener(VideoDisplay<MBFImage> videoDisplay) {
		this.display = videoDisplay;
		if(!(this.display.getVideo() instanceof XuggleVideo)){
			throw new UnsupportedOperationException("You can only seek in xuggle videos");
		}
		XuggleVideo v = (XuggleVideo) display.getVideo();
		duration = v.getDuration();
//		System.out.println("Video duration is: " + duration);
	}

	@Override
	public void keyPressed(KeyEvent keyEvent) {
		if(keyEvent.getKeyCode() >= KeyEvent.VK_0 && keyEvent.getKeyCode() <= KeyEvent.VK_9 ){
			float number = keyEvent.getKeyCode() - KeyEvent.VK_0;
			double toSeek =  (duration * (number/10.f));
//			System.out.println(this.display.getVideo().getFPS());
//			System.out.println("Seeking to: " + toSeek);
			this.display.seek(toSeek);
		}
	}

	@Override
	public void keyReleased(KeyEvent keyEvent) {}

	@Override
	public void keyTyped(KeyEvent keyEvent) {keyPressed(keyEvent);}

}
