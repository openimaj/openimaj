package org.openimaj.webservice.twitter;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.log4j.Logger;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.StreamTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusListUtils;
import org.openimaj.webservice.twitter.PreProcessApp.PreProcessService.PreProcessAppOptions;
import org.restlet.Request;
import org.restlet.ext.fileupload.RestletFileUpload;

class PreProcessTask implements Runnable {

	private Request req;
	private PreProcessAppOptions options;
	private List<FileItem> items;
	private static Logger logger = Logger.getLogger(PreProcessApp.class);

	public PreProcessTask(Request request, PreProcessAppOptions options) {
		this.req = request;
		this.options = options;
		// 1/ Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1000240);

        // 2/ Create a new file upload handler
        RestletFileUpload upload = new RestletFileUpload(factory);
        // 3/ Request is parsed by the handler which generates a list of FileItems
        try {
			items = upload.parseRequest(req);
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		for (final Iterator<FileItem> it = items.iterator(); it.hasNext();) {
			FileItem fi = it.next();
			try{				
				List<USMFStatus> list = StreamTwitterStatusList.readUSMF(fi.getInputStream(), options.inputClass, "UTF-8");
				for (USMFStatus usmfStatus : list) {
					processStatus(usmfStatus, options);
				}
			} catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}

	private void processStatus(USMFStatus usmfStatus,
			PreProcessAppOptions options) throws Exception {
		for (TwitterPreprocessingMode<?> mode : options.modeOptionsOp) {
			try {
				TwitterPreprocessingMode.results(usmfStatus, mode);
			} catch (Exception e) {
				logger.error(String.format("Problem producing %s for %s",
						usmfStatus.id, mode.toString()), e);
			}
		}

		final GeneralJSON outInstance = TwitterStatusListUtils.newInstance(options.outputClass);
		outInstance.fromUSMF(usmfStatus);

		PrintWriter outputWriter = options.getOutputWriter();
		options.ouputMode().output(outInstance, outputWriter);
		outputWriter.println();
		outputWriter.flush();
	}

}
