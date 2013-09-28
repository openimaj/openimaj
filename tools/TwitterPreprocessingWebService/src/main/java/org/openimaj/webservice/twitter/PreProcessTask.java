package org.openimaj.webservice.twitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.log4j.Logger;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.StreamTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusListUtils;
import org.restlet.Request;
import org.restlet.ext.fileupload.RestletFileUpload;

class PreProcessTask implements Runnable {

	private Request req;
	private PreProcessAppOptions options;
	private List<FileItem> items;
	private FileItem fi;
	private static Logger logger = Logger.getLogger(PreProcessApp.class);

	public PreProcessTask(Request request, PreProcessAppOptions options) throws IOException {
		this.req = request;
		this.options = options;
		logger.debug("Creating disk file item factory");
		// 1/ Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1000240);
        logger.debug("Creating file upload handler");
        // 2/ Create a new file upload handler
        RestletFileUpload upload = new RestletFileUpload(factory);
        // 3/ Request is parsed by the handler which generates a list of FileItems
        logger.debug("Parsing upload request");
        try {
			items = upload.parseRequest(req);
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (final Iterator<FileItem> it = items.iterator(); it.hasNext();) {
			FileItem next = it.next();
			if(!next.getFieldName().equals("data"))continue;
			this.fi = next;
		}
		if(fi == null){
			throw new IOException("data file not found");
		}
		
	}

	@Override
	public void run() {
		try{
			logger.debug("Processing all requests");
			List<USMFStatus> list = StreamTwitterStatusList.readUSMF(fi.getInputStream(), options.getOutputClass().type(), "UTF-8");
			long seen = 0;
			for (USMFStatus usmfStatus : list) {
				LoggerUtils.debug(logger, String.format("Processing item: %d",seen++), seen%1000==0);
				processStatus(usmfStatus, options);
			}
		} catch(Exception e){
			e.printStackTrace();
			return;
		}
		options.close();
	}

	private void processStatus(USMFStatus usmfStatus,PreProcessAppOptions options) throws Exception {
		if(usmfStatus.isInvalid() || usmfStatus.text.isEmpty()){
			if(options.veryLoud()){
				System.out.println("\nTWEET INVALID, skipping.");
			}
			return;
		}
		if(options.preProcessesSkip(usmfStatus)) return;
		for (TwitterPreprocessingMode<?> mode : options.modeOptionsOp) {
			try {
				TwitterPreprocessingMode.results(usmfStatus, mode);
			} catch (Exception e) {
				logger.error(String.format("Problem producing %s for %s",
						usmfStatus.id, mode.toString()), e);
			}
		}

		if(options.postProcessesSkip(usmfStatus)) return;

		PrintWriter outputWriter = options.getOutputWriter();
		options.ouputMode().output(options.convertToOutputFormat(usmfStatus), outputWriter);
		outputWriter.println();
		outputWriter.flush();
	}

}
