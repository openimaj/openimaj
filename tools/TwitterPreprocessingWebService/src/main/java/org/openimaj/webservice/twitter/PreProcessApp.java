package org.openimaj.webservice.twitter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.mortbay.io.RuntimeIOException;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.routing.Router;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
@WebService(targetNamespace = "http")
public class PreProcessApp extends Application {

	private static Logger logger = Logger.getLogger(PreProcessApp.class);

	/**
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	public static class PreProcessService extends
			AppTypedResource<PreProcessApp> {
		class PreProcessAppOptions extends
				AbstractTwitterPreprocessingToolOptions {

			public Class<? extends GeneralJSON> outputClass;
			public Class<? extends GeneralJSON> inputClass;
			private PrintWriter writer = null;

			public PreProcessAppOptions() throws CmdLineException {
				super(constructArgs(getQuery()));
				String intype = (String) getRequestAttributes().get("intype");
				String outtype = (String) getRequestAttributes().get("outtype");
				logger.info(String.format("Input: %s, Output: %s", intype,outtype));
				inputClass = getTypeClass(intype);
				outputClass = getTypeClass(outtype);
			}

			@Override
			public boolean validate() throws CmdLineException {
				return true;
			}

			public void setOutputWriter(Writer ow) {
				this.writer  = new PrintWriter(ow);
			}

			public PrintWriter getOutputWriter() {
				return writer;
			}
		}

		/**
		 * @param entity
		 * @return rep
		 */
		@Post
		public Representation represent(Representation entity) {
			PreProcessAppOptions options;
			try {
				options = new PreProcessAppOptions();
			} catch (CmdLineException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
				return null;
			}
			
			final PipedInputStream pi = new PipedInputStream();
			PipedOutputStream po = null;
			try {
				po = new PipedOutputStream(pi);
			} catch (IOException e) {
				return null;
			}
			Representation ir = new OutputRepresentation(MediaType.APPLICATION_JSON) {
				@Override
				public void write(OutputStream realOutput) throws IOException {
					byte[] b = new byte[8];
					int read;
					while ((read = pi.read(b)) != -1) {
						realOutput.write(b, 0, read);
						realOutput.flush();
					}
				}
			};
			OutputStreamWriter ow = null;
			try {
				ow = new OutputStreamWriter(po,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				try {
					ow.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return null;
			}
			
			options.setOutputWriter(ow);
			if (entity != null) {
				if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(),true)) {
					new Thread(new PreProcessTask(getRequest(), options)).start();
				} else {
					return null;
				}
			} else {
				return null;
			}
			
			return ir;
		}

		private String[] constructArgs(Form query) {
			List<String> arglist = new ArrayList<String>();
			Set<String> argNames = query.getNames();
			for (String argName : argNames) {
				String[] argvals = query.getValuesArray(argName);
				for (String argval : argvals) {
					arglist.add(String.format("-%s", argName));
					arglist.add(String.format("%s", argval));
				}
			}
			return arglist.toArray(new String[arglist.size()]);
		}

		private Class<? extends GeneralJSON> getTypeClass(String intype) {
			if (intype.equals("twitter")) {
				return GeneralJSONTwitter.class;
			} else if (intype.equals("usmf")) {
				return USMFStatus.class;
			}
			return null;
		}
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/{intype}.{outtype}", PreProcessService.class);
		return router;
	}
}
