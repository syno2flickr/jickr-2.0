package org.jickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.xml.sax.SAXException;

/**
 * Class Uploader
 * Allow upload content to flickr
 * @author jbrek
 *
 */
public class Uploader {
	
	// User element
	private String userId;
	private boolean pro;
	// Username element
	private String username;
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

	
	// Get upload statuts : bandwith max, photo/video sizes max, ...
	public void getUploadStatus(){
        
		Request req;
        Document doc;
        Element root;
		try {
			req = new Request();
		    req.setParameter("method","flickr.people.getUploadStatus");
			doc = req.getResponse();
			
			root = doc.getRootElement();

			Element user = root.getChild("user");			
			this.userId = user.getAttributeValue("id");
			this.pro = "1".equals(user.getAttributeValue("ispro"));
			
			Element username = user.getChild("username");
			this.username = username.getValue();
		
			Element bandwidth = user.getChild("bandwidth");
			this.bandwidthMaxBytes = Long.parseLong(bandwidth.getAttributeValue("maxbytes"));
			this.bandwidthUsedBytes = Long.parseLong(bandwidth.getAttributeValue("usedbytes"));
			this.bandwidthRemainingBytes = Long.parseLong(bandwidth.getAttributeValue("remainingbytes"));
			this.bandwidthUnlimited= "1".equals(bandwidth.getAttributeValue("unlimited"));
			
			Element filesize = user.getChild("filesize");
			this.filesizeMaxBytes = Long.parseLong(filesize.getAttributeValue("maxbytes"));
			
			Element sets = user.getChild("sets");
			this.setsCreated = sets.getAttributeValue("created");
			this.setsRemaining = sets.getAttributeValue("remaining");
			
			Element videosize = user.getChild("videosize");
			this.videosizeMaxBytes = Long.parseLong(videosize.getAttributeValue("maxbytes"));
			
			Element videos = user.getChild("videos");
			this.videosUploaded = Integer.parseInt(videos.getAttributeValue("uploaded"));
			this.videosRemaining = sets.getAttributeValue("remaining");
			
		} catch (FlickrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void upload(InputStream in, UploadMetaData metaData) throws IOException{
		Request req;
        Document doc;
        Element root;
		try {
			req = new Request(Request.POST);
		    req.setParameter("photo", in);
		    
			doc = req.getResponse();
			
			root = doc.getRootElement();
		}catch(FlickrException e){
			e.printStackTrace();
		}
	}
	
	/*public String upload2(InputStream in, UploadMetaData metaData) throws IOException, FlickrException, SAXException {
        List parameters = new ArrayList();

        parameters.add(new Parameter("api_key", apiKey));

        String title = metaData.getTitle();
        if (title != null)
            parameters.add(new Parameter("title", title));

        String description = metaData.getDescription();
        if (description != null)
            parameters.add(new Parameter("description", description));

        Collection tags = metaData.getTags();
        if (tags != null) {
            parameters.add(new Parameter("tags", StringUtilities.join(tags, " ")));
        }

        parameters.add(new Parameter("is_public", metaData.isPublicFlag() ? "1" : "0"));
        parameters.add(new Parameter("is_family", metaData.isFamilyFlag() ? "1" : "0"));
        parameters.add(new Parameter("is_friend", metaData.isFriendFlag() ? "1" : "0"));
        parameters.add(new Parameter("async", metaData.isAsync() ? "1" : "0"));

        parameters.add(new Parameter("photo", in));
        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getMultipartSignature(sharedSecret, parameters)
            )
        );

        UploaderResponse response = (UploaderResponse) transport.post("/services/upload/", parameters, true);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        String id = "";
        if (metaData.isAsync()) {
            id = response.getTicketId();
        } else {
            id = response.getPhotoId();
        }
        return id;
    }*/
	
	public static void main(String[] args) {
		
		
		Uploader up = new Uploader();
		up.getUploadStatus();
	}
	
	/**
	 * 	Metadata that describe a photo upload element.
	 * @author jbrek
	 *
	 */
	class UploadMetaData {

	    private String title;
	    private String description;
	    private Collection tags;
	    private boolean publicFlag;
	    private boolean friendFlag;
	    private boolean familyFlag;
	    private boolean async = false;
	    private Boolean hidden;
	    private String safetyLevel;
	    private String contentType;

	    public String getTitle() {
	        return title;
	    }

	    public void setTitle(String title) {
	        this.title = title;
	    }

	    public String getDescription() {
	        return description;
	    }

	    public void setDescription(String description) {
	        this.description = description;
	    }

	    public Collection getTags() {
	        return tags;
	    }

	    public void setTags(Collection tags) {
	        this.tags = tags;
	    }

	    public boolean isPublicFlag() {
	        return publicFlag;
	    }

	    public void setPublicFlag(boolean publicFlag) {
	        this.publicFlag = publicFlag;
	    }

	    public boolean isFriendFlag() {
	        return friendFlag;
	    }

	    public void setFriendFlag(boolean friendFlag) {
	        this.friendFlag = friendFlag;
	    }

	    public boolean isFamilyFlag() {
	        return familyFlag;
	    }

	    public void setFamilyFlag(boolean familyFlag) {
	        this.familyFlag = familyFlag;
	    }

	    /**
	     * Get the Content-type of the Photo.
	     *
	     * @return contentType
	     */
	    public String getContentType() {
	        return contentType;
	    }

	    /**
	     * Set the Content-type of the Photo.
	     *
	     * @param contentType
	     */
	    public void setContentType(String contentType) {
	        this.contentType = contentType;
	    }

	    public Boolean isHidden() {
	        return hidden;
	    }

	    public void setHidden(Boolean hidden) {
	        this.hidden = hidden;
	    }

	    /**
	     * Get the safety-level.
		 *
	     * @return The safety-level
	     */
	    public String getSafetyLevel() {
	        return safetyLevel;
	    }

	    /**
	     * Set the safety level (adultness) of a photo.<p>
		 *
	     * @param safetyLevel
	     */
	    public void setSafetyLevel(String safetyLevel) {
	        this.safetyLevel = safetyLevel;
	    }

	    public boolean isAsync() {
	        return async;
	    }

	    /**
	     * Switch the Uploader behaviour - sychronous or asyncrounous.<p>
	     *
	     * The default is sychronous.
	     *
	     * @param async boolean
	     */
	    public void setAsync(boolean async) {
	        this.async = async;
	    }
	}
}
