package org.openimaj.webservice.twitter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.BufferedReaderStream;
import org.openimaj.webservice.twitter.PreProcessApp.PreProcessService;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class PreProcessJobApp extends Application {
	private final static Logger logger = Logger.getLogger(PreProcessJobApp.class);
	/**
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	public static class IdentifiedPreProcessService extends PreProcessService{
		private MongoClient mongoClient;
		private DB db;
		
		@Override
		protected Representation post(Representation entity)throws ResourceException {
			return super.post(entity);
		}
		
		/**
		 * @throws UnknownHostException 
		 * 
		 */
		public IdentifiedPreProcessService() throws UnknownHostException {
			this.mongoClient = new MongoClient("localhost");
			this.db = mongoClient.getDB( "tweetprocdb" );
		}
		@Override
		public Representation represent(Representation entity) {
			final int[] count = new int[1];
			String id = (String) getRequestAttributes().get("identifier");
			final DBCollection collection = db.getCollection(id);
			collection.drop();
			collection.ensureIndex("count");
			try{				
				logger.debug("Starting request");
				logger.debug("Parsing options");
				PreProcessAppOptions options;
				try {
					
					options = new PreProcessAppOptions(getQuery(),getRequestAttributes());
				} catch (CmdLineException e) {
					logger.error("Invalid options",e);
					entity.release();
					return PreProcessApp.errorRep(e);
				}
				PipedOutputStream pos = new PipedOutputStream();
				PipedInputStream pis;
				try {
					pis = new PipedInputStream(pos);
				} catch (IOException e1) {
					this.setStatus(Status.SERVER_ERROR_INTERNAL);
					return new StringRepresentation("Could not create piped input streamstream");
				}
				options.setOutputWriter(new OutputStreamWriter(pos));
				
				logger.debug("Preparing input data");
				if (entity != null) {
					
					if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(),true)) {
						try {
							logger.debug("Starting input thread");
							new Thread(new PreProcessFileUploadTask(getRequest(), options)).start();
						} catch (IOException e) {
							logger.error("No input data found");
							this.setStatus(Status.SERVER_ERROR_INTERNAL);
							return new StringRepresentation("No valid file provided, use variable 'data'");
						}
					} else {
						logger.error("Not a multipart request");
						this.setStatus(Status.SERVER_ERROR_INTERNAL);
						return new StringRepresentation("Not a multipart request, upload a file using variable 'data'");
					}
				} else {
					this.setStatus(Status.SERVER_ERROR_INTERNAL);
					return new StringRepresentation("No valid file provided, use variable 'data'");
				}
				logger.debug("Success! starting output");
				this.setStatus(Status.SUCCESS_OK);
				new BufferedReaderStream(new BufferedReader(new InputStreamReader(pis)))
				.map(new Function<String, DBObject>() {
					
					@Override
					public DBObject apply(String in) {
						DBObject obj = new BasicDBObject(2);
						obj.put("count", count[0]++);
						obj.put("json", in);
						return obj;
					}
				})
				.forEach(new Operation<DBObject>() {
					@Override
					public void perform(DBObject object) {
						logger.debug("Writing object: " + object.get("count"));
						collection.insert(object);
					}
				})
				;
				Map<String,Object> confirm = new HashMap<String, Object>();
				confirm.put("message", "items added");
				confirm.put("count", count[0]);
				return new JsonRepresentation(confirm);
			}
			finally{
				BasicDBObject finalitem = new BasicDBObject("final", true);
				finalitem.put("count", count[0]+1);
				collection.insert(finalitem);
			}
			
		}
	}
	
	/**
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	public static class IdentifiedPreProcessServiceRead extends AppTypedResource<PreProcessJobApp> {
		/**
		 * @throws UnknownHostException 
		 * 
		 */
		public IdentifiedPreProcessServiceRead() throws UnknownHostException {
			
		}
		
		
		@Get
		public Representation readall(Representation entity) {
			MongoClient mongoClient;
			try {
				mongoClient = new MongoClient("localhost");
			} catch (UnknownHostException e1) {
				this.setStatus(Status.SERVER_ERROR_INTERNAL);
				return new StringRepresentation("Failed to open a connection to mongodb");
			}
			final DB db = mongoClient.getDB( "tweetprocdb" );
			final String id = (String) getRequestAttributes().get("identifier");
			
			final int start;
			final int end;
			if(getRequestAttributes().containsKey("start")){
				start = Integer.parseInt((String) getRequestAttributes().get("start"));
				end = Integer.parseInt((String) getRequestAttributes().get("end"));
			}
			else{
				start = -1;
				end = -1;
			}
			IndependentPair<? extends Representation, OutputStreamWriter> irow = PreProcessApp.prepareOutputPipe();
			Representation ir = irow.firstObject();
			OutputStreamWriter ow = irow.secondObject();
			if(ow == null){
				this.setStatus(Status.SERVER_ERROR_INTERNAL);
				return ir;
			}
			final PrintWriter pw = new PrintWriter(ow);
			new Thread(new Runnable(){

				@Override
				public void run() {
					int lastCount = start-1;
					boolean finalfound = false;
					while(!finalfound){
						if(pw.checkError()){
							pw.close();
							break;
						}
						DBCollection collection = db.getCollection(id);
						if(collection.count() == 0){
							try {
								Thread.sleep(1000); // Wait for the input stream to catch up
								continue;
							} catch (InterruptedException e) {
								
							}
						}
						DBCursor ret = collection.find(fromLastCount(lastCount)).sort(new BasicDBObject("count", 1));
						logger.debug("Query performed, found: " + ret.count());
						while(ret.hasNext()){
							DBObject obj = ret.next();
							int ccount = (int) obj.get("count");
							if(obj.containsField("final") || (end != -1 && ccount >= end)){
								logger.debug("Query performed, found: " + ret.count());
								finalfound = true;
								break;
							}else{							
								lastCount = Math.max(ccount,lastCount);
								pw.println(obj.get("json"));
							}
						}
						if(ret.count() == 0){							
							try {
								Thread.sleep(1000); // Wait for the input stream to catch up
							} catch (InterruptedException e) {
								
							}
						}
					}
					pw.close();
				}

				private DBObject fromLastCount(int lastCount) {
					
					return new BasicDBObject("count", new BasicDBObject("$gt", lastCount));
				}
				
			})
			.start();
//			return new JsonRepresentation("{}");
			return ir;
		}
	}
	
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/p/{identifier}.{intype}.{outtype}", IdentifiedPreProcessService.class);
		router.attach("/r/{identifier}", IdentifiedPreProcessServiceRead.class);
		router.attach("/r/{start}/{end}/{identifier}", IdentifiedPreProcessServiceRead.class);
		return router;
	}
}
