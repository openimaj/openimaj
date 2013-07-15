package org.openimaj.webservice.twitter;

import org.restlet.Application;
import org.restlet.resource.ServerResource;

public class AppTypedResource<T extends Application> extends ServerResource {
	protected T app;
	@SuppressWarnings("unchecked")
	public AppTypedResource() {
		this.app = (T) this.getApplication();
	}
	
}