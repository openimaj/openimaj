package org.openimaj.demos.campusview;

import java.io.File;

public interface CaptureControlsDelegate 
{
	// Batch controls
	public void startBatch( File dir, File md, String capturer, String type );
	public void stopBatch();
	
	// capture controls
	public void snapshot();
	public void startRecording( int rateSeconds );
	public void stopRecording();
	
	// configuration controls
	public void updateCaptureSettings(int capWidth, int capHeight, double capRate);
}
