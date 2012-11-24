package backtype.storm.spout;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class AckStats {

	/**
	 * The measured throughput
	 */
	public float throughput;
	/**
	 * The time measurment was taken
	 */
	public long timestamp;

	/**
	 * @param throughput
	 *            measured throughput
	 */
	public AckStats(float throughput) {
		this.throughput = throughput;
		this.timestamp = System.currentTimeMillis();
	}

}
