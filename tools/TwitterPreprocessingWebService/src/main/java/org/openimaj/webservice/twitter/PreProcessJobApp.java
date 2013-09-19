package org.openimaj.webservice.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openimaj.util.stream.BufferedReaderStream;
import org.openimaj.webservice.twitter.PreProcessApp.PreProcessService;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class PreProcessJobApp extends Application {
	
	/**
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	public static class IdentifiedPreProcessService extends PreProcessService{
		@Override
		public Representation represent(Representation entity) {
			String id = (String) getRequestAttributes().get("identifier");
			Representation rep = super.represent(entity);
			try {
				new BufferedReaderStream(new BufferedReader(new InputStreamReader(rep.getStream())))
//				.forEach(new Mong)
				;
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/{identifier}.{intype}.{outtype}", IdentifiedPreProcessService.class);
		return router;
	}
}
