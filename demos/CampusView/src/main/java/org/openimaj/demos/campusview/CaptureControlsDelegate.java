package org.openimaj.demos.campusview;

import java.io.File;

public interface CaptureControlsDelegate {
	public void snapshot(File dir, File md);
	public void startRecording(File dir, File md);
	public void stopRecording();
	public void updateCaptureSettings(int capWidth, int capHeight, double capRate);
}
