package org.jickr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.jickr.Photo.ContentType;
import org.jickr.Photo.SafetyLevel;

/**
 * Complex class that describe a photo upload element.
 * see Builder pattern.
 * 
 * Example of use: 
 * 	 PhotoUpload upload = 
 * 		new PhotoUpload.Builder(new File(existingFileName)).title("Man on the moon")
 *                                                         .description("First step on the moon")
 *                                                         .publicFlag(true)
 *                                                         .build();
 * 
 * @author jbrek
 *
 */
public class PhotoUpload {

	private final File photo; 
    private final String title;
    private final String description;
    private final Collection<String> tags;
    private final Boolean publicFlag;
    private final Boolean friendFlag;
    private final Boolean familyFlag;
    private final Boolean async;
    private final Boolean hidden;
    private final SafetyLevel safetyLevel;
    private final ContentType contentType;

    private PhotoUpload(Builder builder) {
		this.photo = builder.photo;
		this.title = builder.title;
		this.description = builder.description;
		this.tags = builder.tags;
		this.publicFlag = builder.publicFlag;
		this.friendFlag = builder.friendFlag;
		this.familyFlag = builder.familyFlag;
		this.async = builder.async;
		this.hidden = builder.hidden;
		this.safetyLevel = builder.safetyLevel;
		this.contentType = builder.contentType;
	}
    
	public File getPhoto() {
		return photo;
	}

	public Boolean isHidden() {
		return hidden;
	}

	public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public Boolean isPublicFlag() {
        return publicFlag;
    }

    public Boolean isFriendFlag() {
        return friendFlag;
    }

    public Boolean isFamilyFlag() {
        return familyFlag;
    }

    /**
     * Get the Content-type of the Photo.
     *
     * @return contentType
     */
    public ContentType getContentType() {
        return contentType;
    }

    /**
     * Get the safety-level.
	 *
     * @return The safety-level
     */
    public SafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public Boolean isAsync() {
        return async;
    }
    
       
    /**
     * Builder class for PhotoUpload
     * @author jbrek
     *
     */
    public static class Builder {
    	// required fields
    	private final File photo;
    	
    	// optional fields
    	private String title = "";
	    private String description = "";
	    private Collection<String> tags = new ArrayList<String>();
	    private Boolean publicFlag = false;
	    private Boolean friendFlag = false;
	    private Boolean familyFlag = false;
	    private Boolean async = false;
	    private Boolean hidden = false;
	    private SafetyLevel safetyLevel = SafetyLevel.SECURED;
	    private ContentType contentType = ContentType.PHOTO_VIDEO;
	    
	    public Builder(File photo) {
			this.photo = photo;
		}
	    
	    public Builder title(String val) { this.title = val; return this; }
	    public Builder description(String val) { this.description = val; return this; }
	    public Builder tags(Collection<String> values) { this.tags = values; return this; }
	    public Builder publicFlag(Boolean val) { this.publicFlag = val; return this; }
	    public Builder friendFlag(Boolean val) { this.friendFlag = val; return this; }
	    public Builder familyFlag(Boolean val) { this.familyFlag = val; return this; }
	    public Builder async(Boolean val) { this.async = val; return this; }
	    public Builder hidden(Boolean val) { this.hidden = val; return this; }
	    public Builder safetyLevel(SafetyLevel val) { this.safetyLevel = val; return this; }
	    public Builder contentType(ContentType val) { this.contentType = val; return this; }
	    
	    public PhotoUpload build(){
	    	return new PhotoUpload(this);
	    }
    }
}