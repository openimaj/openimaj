package org.openimaj.webservice.twitter;

import org.restlet.Component;
import org.restlet.data.Protocol;


/**
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TwitterPreprocessingWebService extends Component{

	/**
	 * Default port 8080
	 * @throws Exception
	 */
	public TwitterPreprocessingWebService() throws Exception {
		this(8080);
	}
	/**
	 * @param port
	 * @throws Exception
	 */
	public TwitterPreprocessingWebService(int port) throws Exception {
		getServers().add(Protocol.HTTP,port);
		getDefaultHost().attach("/process", new PreProcessApp());
		getDefaultHost().attach("/job", new PreProcessJobApp());
	}
	
	public static void main(String[] args) throws Exception {
		new TwitterPreprocessingWebService(8181).start();
	}
}