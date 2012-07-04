package org.openimaj.tools.reddit;

/**
 * Harvest 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RedditHarvester {
	RedditHarvesterOptions options;
	
	public RedditHarvester(String[] args) {
		options = new RedditHarvesterOptions(args);
	}

	public static void main(String[] args) {
		RedditHarvester harvester = new RedditHarvester(args);
		harvester.start();
	}

	private void start() {
	}
}
