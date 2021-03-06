package org.jickr;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Class Uploader Allow upload content to flickr
 * 
 * @author jbrek
 * 
 */
public class UserLimitations {

	// User element
	private boolean pro;
	// Bandwidth element
	private long bandwidthMaxBytes;
	private long bandwidthUsedBytes;
	private long bandwidthRemainingBytes;
	private boolean bandwidthUnlimited;
	// Filesize element
	private long filesizeMaxBytes;
	// Sets element
	private String setsCreated;
	private String setsRemaining;
	// Videosize element
	private long videosizeMaxBytes;
	// Videos element
	private int videosUploaded;
	private String videosRemaining;
	
	private boolean gotInfos=false;
	

	// Get upload statuts : bandwith max, photo/video sizes max, ...
	private void getUploadStatus() throws FlickrException {

		Request req;
		Document doc;
		Element root;

		req = new Request();
		req.setParameter("method", "flickr.people.getUploadStatus");
		doc = req.getResponse();

		root = doc.getRootElement();

		Element user = root.getChild("user");
		this.pro = "1".equals(user.getAttributeValue("ispro"));

		Element bandwidth = user.getChild("bandwidth");
		this.bandwidthMaxBytes = Long.parseLong(bandwidth
				.getAttributeValue("maxbytes"));
		this.bandwidthUsedBytes = Long.parseLong(bandwidth
				.getAttributeValue("usedbytes"));
		this.bandwidthRemainingBytes = Long.parseLong(bandwidth
				.getAttributeValue("remainingbytes"));
		this.bandwidthUnlimited = "1".equals(bandwidth
				.getAttributeValue("unlimited"));

		Element filesize = user.getChild("filesize");
		this.filesizeMaxBytes = Long.parseLong(filesize
				.getAttributeValue("maxbytes"));

		Element sets = user.getChild("sets");
		this.setsCreated = sets.getAttributeValue("created");
		this.setsRemaining = sets.getAttributeValue("remaining");

		Element videosize = user.getChild("videosize");
		this.videosizeMaxBytes = Long.parseLong(videosize
				.getAttributeValue("maxbytes"));

		Element videos = user.getChild("videos");
		this.videosUploaded = Integer.parseInt(videos
				.getAttributeValue("uploaded"));
		this.videosRemaining = sets.getAttributeValue("remaining");
		this.gotInfos = true;

	}

	public String showUsageAndLimitations() throws FlickrException{
		int denomMega = 1024*1024;
		String res;
		res="Usage and limitations status:\n";
		res+="\tPro account: "+ (this.isPro()?"yes":"no")+"\n";
		res+="\tBandwith:\n";
		res+="\t  Unlimited: "+(this.isBandwidthUnlimited()?"yes":"no"+
						   "\tMax: "+this.getBandwidthMaxBytes()/denomMega+"MB" +
						   "\tRemaining: "+this.getBandwidthRemainingBytes()/denomMega+"MB"+
						   "\tUsed: "+this.getBandwidthUsedBytes()/denomMega+"MB")+"\n";
		res+="\tImage:\n";
		res+="\t  Max size: "+ this.getFilesizeMaxBytes()/denomMega+"MB\n";
		res+="\tVideo:\n";
		res+="\t  Max size: "+this.getVideosizeMaxBytes()/denomMega+"MB"+
						   "\tRemaining: "+this.getVideosRemaining()+
						   "\tUploaded: "+this.getVideosUploaded()+"\n";
		return res;
	}
	
	public boolean isPro() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return pro;
	}

	public long getBandwidthMaxBytes() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return bandwidthMaxBytes;
	}

	public long getBandwidthUsedBytes() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return bandwidthUsedBytes;
	}

	public long getBandwidthRemainingBytes() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return bandwidthRemainingBytes;
	}

	public boolean isBandwidthUnlimited() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return bandwidthUnlimited;
	}

	public long getFilesizeMaxBytes() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return filesizeMaxBytes;
	}

	public String getSetsCreated() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return setsCreated;
	}

	/**
	 * Always return "lots" for all kind (pro, free) of user. 
	 */
	@Deprecated
	public String getSetsRemaining() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return setsRemaining;
	}

	public long getVideosizeMaxBytes() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return videosizeMaxBytes;
	}

	public int getVideosUploaded() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return videosUploaded;
	}

	public String getVideosRemaining() throws FlickrException {
		if (!gotInfos) getUploadStatus();
		return videosRemaining;
	}
	
}
