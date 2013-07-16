package org.openimaj.webservice.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openimaj.logger.LoggerUtils;
import org.restlet.Component;
import org.restlet.data.Protocol;


/**
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TwitterPreprocessingWebService extends Component{

	public TwitterPreprocessingWebService() throws Exception {
		loadProps();
		this.getContext().getLogger().setLevel(java.util.logging.Level.ALL);
		getServers().add(Protocol.HTTP,Integer.parseInt(System.getProperty("eye.service.port","8080")));
		getDefaultHost().attach("/process", new PreProcessApp());
		LoggerUtils.prepareConsoleLogger();
	}
	public void loadProps() throws IOException{
		Properties props = new Properties();
		InputStream res = TwitterPreprocessingWebService.class.getResourceAsStream("smows.properties");
		if(res==null) return;
		props.load(res);
		for (Object key : props.keySet()) {
			if (!System.getProperties().containsKey(key)) {
				System.getProperties().setProperty((String) key,(String) props.get(key));
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		new TwitterPreprocessingWebService().start();
	}
}