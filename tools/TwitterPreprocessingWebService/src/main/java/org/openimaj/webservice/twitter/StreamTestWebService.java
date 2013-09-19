package org.openimaj.webservice.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.openimaj.logger.LoggerUtils;
import org.openimaj.webservice.twitter.PreProcessApp.PreProcessService;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.routing.Router;


/**
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class StreamTestWebService extends Component{

	public static class StreamProcService extends AppTypedResource<PreProcessApp> {
		@Post
		public Representation cheese(Representation in) throws IOException{
			System.out.println("wang");
			String content = "";
			InputStream is = in.getStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while((line=reader.readLine())!=null){
				System.out.println(line);
				content += line;
			}
			return new StringRepresentation(content);
		}
	}
	public static class StreamTestApp extends Application{
		@Override
		public Restlet createInboundRoot() {
			Router router = new Router(getContext());
			router.attach("/wang", StreamProcService.class);
			return router;
		}
	}
	public StreamTestWebService() throws Exception {
	
		this.getContext().getLogger().setLevel(java.util.logging.Level.ALL);
		getServers().add(Protocol.HTTP,Integer.parseInt("8080"));
		getDefaultHost().attach("/process", new StreamTestApp());
		LoggerUtils.prepareConsoleLogger();
	}
	
	public static void main(String[] args) throws Exception {
		new StreamTestWebService().start();
	}
}