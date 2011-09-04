package org.jickr;


import java.io.File;
import java.util.EventObject;

/**
 * Request event 
 * @author jbrek
 *
 */
public class RequestEvent extends EventObject{

	private static final long serialVersionUID = -6444842079696867672L;
	
	private final File progressFile; // receid 
	private final long progress;
	private final long totalProgress;
	
	public RequestEvent(Object source, File progressFile, long progress, long totalProgress) {
		super(source);
		this.progressFile = progressFile;
		this.progress = progress;
		this.totalProgress = totalProgress;
	}

	public File getReceivedFile(){
		return progressFile;
	}

	public long getProgress() {
		return progress;
	}

	public long getTotalProgress() {
		return totalProgress;
	}
		
}

