package org.openimaj.webservice.twitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.openimaj.tools.twitter.modes.output.TwitterOutputMode;
import org.openimaj.tools.twitter.modes.output.TwitterOutputModeOption;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingModeOption;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.StreamTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusListUtils;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.routing.Router;

import cern.colt.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



@WebService(targetNamespace = "http")
public class PreProcessApp extends Application {
	private static final Gson gson = new GsonBuilder().create();
	private static Logger logger = Logger.getLogger(PreProcessApp.class);
	public static class PreProcessService extends AppTypedResource<PreProcessApp>{

		@Post
		public Representation level(Representation rep) {
			
			List<Map<?,?>> retList = new ArrayList<Map<?,?>>();
			try {
				String data = rep.getText();
				String intype = (String) this.getRequestAttributes().get("intype");
				String outtype = (String) this.getRequestAttributes().get("outtype");
				logger.info(String.format("Input: %s, Output: %s, Data Len: %d",intype,outtype,data.length()));
				Class<? extends GeneralJSON> inputClass = getTypeClass(intype);
				Class<? extends GeneralJSON> outputClass = getTypeClass(outtype);
				InputStream is = new ByteArrayInputStream( data.getBytes("UTF-8") );
				List<USMFStatus> list = StreamTwitterStatusList.readUSMF(is, inputClass,"UTF-8");
				List<TwitterPreprocessingMode<?>> modes  = null;
				try {
					modes = preprocessingModes(this.getQuery().getValuesArray("m"));
				} catch (Exception e) {
					logger.error("Could not produce preprocessing modes",e);
					return null;
				}
				
				for (USMFStatus usmfStatus : list) {
					for (TwitterPreprocessingMode<?> mode: modes) {
						try {
							TwitterPreprocessingMode.results(usmfStatus, mode);
						} catch (Exception e) {
							logger.error(String.format("Problem producing %s for %s",usmfStatus.id,mode.toString()), e);
						}
					}
					TwitterOutputMode om = null;
					if(!this.getQuery().contains("om")){
						om = TwitterOutputModeOption.APPEND.getOptions();
					}
					else{						
						TwitterOutputModeOption.valueOf(this.getQuery().getValues("om")).getOptions();
					}
					final GeneralJSON outInstance = TwitterStatusListUtils.newInstance(outputClass);
					outInstance.fromUSMF(usmfStatus);
					StringWriter sw = new StringWriter();
					om.output(outInstance, new PrintWriter(sw));
					Map<?,?> map = gson.fromJson(sw.toString(), Map.class);
					retList.add(map);
				}
			} catch (IOException e) {
				logger.error("Problem reading input", e);
			}
			
			JsonRepresentation ret = new JsonRepresentation(gson.toJson(retList));
			return ret;
		}
		
		/**
		 * @return an instance of the selected preprocessing mode
		 * @throws Exception
		 */
		public List<TwitterPreprocessingMode<?>> preprocessingModes(String[] modeStrings) throws Exception {
			logger.debug("Modes asked for: " + Arrays.toString(modeStrings));
			ArrayList<TwitterPreprocessingMode<?>> modes = new ArrayList<TwitterPreprocessingMode<?>>();
			Set<String> validModes = new HashSet<String>();
			for (TwitterPreprocessingModeOption string : TwitterPreprocessingModeOption.values()) {
				validModes.add(string.name());
			}
			for (String modeString: modeStrings) {
				if(!validModes.contains(modeString)) continue;
				TwitterPreprocessingModeOption valueOf = TwitterPreprocessingModeOption.valueOf(modeString);
				modes.add(valueOf.getOptions());
			}
			return modes;
		}

		private Class<? extends GeneralJSON> getTypeClass(String intype) {
			if(intype.equals("twitter")){
				return GeneralJSONTwitter.class;
			}
			else if(intype.equals("usmf")){
				return USMFStatus.class;
			}
			return null;
		}
	}
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/process/{intype}.{outtype}", PreProcessService.class);
		return router;
	}	
}
