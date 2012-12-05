/**
 * 
 */
package org.openimaj.demos.sandbox.video.gt;

import org.openimaj.audio.AudioStream;
import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.xuggle.XuggleAudio;
import org.openimaj.video.xuggle.XuggleVideo;

/**
 *	A scene labeller for labelling whether video scenes (shots) have
 *	music, speech, both, or neither.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 5 Dec 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class MusicSpeechVideoAnnotationTool extends
	SceneLabellingVideoAnnotationTool
{
	/***/
	private static final long serialVersionUID = 1L;
	
	/** The possible states we'll allow */
	private static final String[] states = new String[]{
		"Music", "Speech", "Music and Speech", "Neither Music nor Speech"	
	};	

	/**
	 *	@param video
	 *	@param audio
	 */
	public MusicSpeechVideoAnnotationTool( final Video<MBFImage> video,
			final AudioStream audio )
	{
		super( video, audio );
	}

	/** 
	 *	{@inheritDoc}
	 * 	@see org.openimaj.demos.sandbox.video.gt.SceneLabellingVideoAnnotationTool#getStates()
	 */
	@Override
	public String[] getStates()
	{
		return MusicSpeechVideoAnnotationTool.states;
	}

	/**
	 * 
	 *	@param args
	 */
	public static void main( final String[] args )
	{
		String videoFile = "heads1.mpeg";
		
		if( args.length > 0 )
			videoFile = args[0];
		
		final XuggleVideo video = new XuggleVideo( videoFile );
		final XuggleAudio audio = new XuggleAudio( videoFile );
		new MusicSpeechVideoAnnotationTool( video, audio );
	}
}
