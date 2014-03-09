package org.openimaj.content.slideshow;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplay.EndAction;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 * Slide with audio and video
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class AudioVideoSlide implements Slide {

	private URL url;
	private EndAction endAction;
	private VideoDisplay<MBFImage> display;

	/**
	 * Construct with the given video and {@link EndAction}
	 * 
	 * @param video
	 * @param endAction
	 * @throws IOException
	 */
	public AudioVideoSlide(URL video, EndAction endAction) throws IOException {
		if (url.getProtocol().startsWith("jar:")) {
			final File tmp = File.createTempFile("movie", ".tmp");
			tmp.deleteOnExit();
			FileUtils.copyURLToFile(url, tmp);
			url = tmp.toURI().toURL();
		}

		this.url = video;
		this.endAction = endAction;
	}

	@Override
	public Component getComponent(int width, int height) throws IOException {
		final JPanel base = new JPanel();
		base.setOpaque(false);
		base.setPreferredSize(new Dimension(width, height));
		base.setLayout(new GridBagLayout());

		try {
			final XuggleVideo video = new XuggleVideo(this.url, true);
			final XuggleAudio audio = new XuggleAudio(this.url);

			display = VideoDisplay.createVideoDisplay(video, audio, base);
			display.setEndAction(this.endAction);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return base;
	}

	@Override
	public void close() {
		display.close();
	}
}
