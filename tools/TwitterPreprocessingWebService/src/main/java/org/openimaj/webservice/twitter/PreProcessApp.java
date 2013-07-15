package org.openimaj.webservice.twitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingModeOption;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.StreamTwitterStatusList;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.routing.Router;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



@WebService(targetNamespace = "http")
public class PreProcessApp extends Application {
	private static final Gson gson = new GsonBuilder().create();
	private static Logger logger = Logger.getLogger(PreProcessApp.class);
	public static class PreProcessService extends AppTypedResource<PreProcessApp>{

		@Post
		public Representation level(Representation rep) {
			
			try {
				String data = rep.getText();
				String intype = (String) this.getRequestAttributes().get("intype");
				String outtype = (String) this.getRequestAttributes().get("outtype");
				logger.info(String.format("Input: %s, Output: %s, Data Len: %d",intype,outtype,data.length()));
				Class<? extends GeneralJSON> inputClass = getTypeClass(intype);
				Class<? extends GeneralJSON> outputClass = getTypeClass(outtype);
				Scanner dataScanner = new Scanner(data);
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
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<Object,Object> retMap = new HashMap<Object, Object>();
			JsonRepresentation ret = new JsonRepresentation(gson.toJson(retMap));
			return ret;
		}
		
		/**
		 * @return an instance of the selected preprocessing mode
		 * @throws Exception
		 */
		public List<TwitterPreprocessingMode<?>> preprocessingModes(String[] modeStrings) throws Exception {
			ArrayList<TwitterPreprocessingMode<?>> modes = new ArrayList<TwitterPreprocessingMode<?>>();
			for (String modeString: modeStrings) {
				if(!TwitterPreprocessingModeOption.)
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
