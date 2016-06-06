package org.openimaj.workinprogress.audio;

import org.openimaj.audio.AudioFormat;
import org.openimaj.audio.AudioPlayer;
import org.openimaj.audio.JavaSoundAudioGrabber;

public class AudioTest {
	public static void main(String[] args) {
		try {
			// final Video<MBFImage> video = new VideoCapture(320, 240);
			final JavaSoundAudioGrabber audio = new JavaSoundAudioGrabber(new AudioFormat(16, 44.1, 2));
			audio.setMaxBufferSize(1024);
			new Thread(audio).start();

			Thread.sleep(100);

			// final VideoDisplay<MBFImage> display =
			// VideoDisplay.createVideoDisplay(video, audio);
			new Thread(new AudioPlayer(audio)).start();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
