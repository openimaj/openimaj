package org.openimaj.demos.sandbox.audio;

import java.io.File;

import org.openimaj.audio.EffectiveSoundPressure;
import org.openimaj.video.xuggle.XuggleAudio;

public class Loudness {
	public static void main(String[] args) throws Exception {
		XuggleAudio s = new XuggleAudio( 
				new File("../demos/src/main/resources/org/openimaj/demos/audio/140bpm-Arp.mp3") );
		
		EffectiveSoundPressure esp = new EffectiveSoundPressure(s, 1000, -1);
		esp.process(s);
	}
}
