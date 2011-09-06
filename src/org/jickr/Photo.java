/*
 * Photo.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 14, 2006, 8:32 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jickr.Photo.PhotoSize;
import org.jickr.Photo.Size;

/**
 *
 * Encapsulates Photo information.  Includes a few static methods to search for photos.
 * Keeps a cache of the backing Images, adjustable in size by the user.  Photo objects
 * are created as part of PhotoList's, or via a find method.
 *
 * @see org.jickr.PhotoList
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class Photo implements Comparable <Photo>{
    
	/**
	 * List of media types
	 * 
	 */
	public enum Media {
		PHOTO,
		VIDEO;
	}
	
    /**
     * List of possibly valid Photo sizes.
     */
    public enum Size {
        /**
         * Square small photo -   Typically 75x75 square. JPG format.
         */
        SQUARE("_s"),
        /**
         * Thumbnail size photo -  Typically 75x100 or 100x67 (Landscape) . JPG format.
         */
        THUMB("_t"),
        /**
         * Small photo - 240x160 (Landscape). JPG format.
         */
        SMALL("_m"),
        /**
         * Medium photo - 500x333 (Landscape). JPG format.
         */
        MEDIUM(""),
        /**
         * Medium 640 photo - 478 x 640.  Typically the default sized photo. JPG format.
         */
        MEDIUM_640("_z"),
        /**
         * Large photo -  typically 1024x683.  JPG format - largest guarenteed JPG.
         */
        LARGE("_b"),
        /**
         * Original photo, same as uploaded. In the original size, in the orginal format (GIF, PNG, JPG, TIF).
         */
        ORIGINAL("_o"),
        /**
         * Video format MP4 
         */
        SITE_MP4("_site"),
        /**
         * Video format MP4 optimzed for mobile
         */
        MOBILE_MP4("_mobile"),
        /**
         * Video original format (and orignal codec)
         */
        VIDEO_ORIGINAL("_orig"),
        /**
         * Video format Flash (SWF) - to be visualized in browser - CANNOT BE DOWNLOADED
         */
        VIDEO_PLAYER(null);
        
        String suffix;
        
        private Size(String suffix) {
			this.suffix = suffix;
		}

		public String getSuffix() {
			return suffix;
		}
        
        
    }
    
    /**
     * Authorization
     * Used to determine the autorizations on each photo for commenting or tagging
     * @author jbrek
     *
     */
    public enum Authorization {
		
    	/**
    	 * Only you
    	 */
    	PRIVATE_ONLY(0),
    	
    	/**
    	 * Friends and/or Family only	
    	 */
		FRIENDS_FAMILIY_ONLY(1),
		
		/**
		 * My contacts only
		 */
		MY_CONTACTS_ONLY(2),
		
		/**
		 * All flickr users 
		 */
		ALL_FLICKR_USER(3);
    	
    	private int xmlValue;
    	
    	Authorization(int xmlValue){
    		
    		this.xmlValue = xmlValue;
    	}

		public int getXmlValue() {
			return xmlValue;
		}
    	
		public static Authorization getEnumFromValue(int value){
			switch (value) {
			case 0:
				return Authorization.PRIVATE_ONLY;
			case 1:
				return Authorization.FRIENDS_FAMILIY_ONLY;
			case 2:
				return Authorization.MY_CONTACTS_ONLY;
			case 3:
				return Authorization.ALL_FLICKR_USER;
			default:
				return Authorization.PRIVATE_ONLY;
			}
		}
	}
    
    
    /**
     * Safety level
     * Content security level of the photo (
     * @author jbrek
     *
     */
    public enum SafetyLevel {		
   
    	SECURED(0), /// Public content
		MODERATE(1), /// If you don't know or you're not sure of the content safety
		RESTRICTED(2); /// Adult content (sex, violence, racism...)
    	
    	private int xmlValue;
    	
    	SafetyLevel(int xmlValue){    		
    		this.xmlValue = xmlValue;
    	}

		public int getXmlValue() {
			return xmlValue;
		}
    	
		public static SafetyLevel getEnumFromValue(int value){
			switch (value) {
			case 0:	return SafetyLevel.SECURED;
			case 1:	return SafetyLevel.MODERATE;
			case 2:	return SafetyLevel.RESTRICTED;
			default: return SafetyLevel.SECURED;
			}
		}
	}
    
    /**
     * Content type
     * Content types of Flickr
     * @author jbrek
     *
     */
    public enum ContentType {		
   
    	PHOTO_VIDEO(0), /// Photos / Videos content
    	ILLUSTRATION_ART_ANIMATION(1), /// Illustration/Art / Animation/CGI or other non-photographic images 
    	SCREENSHOT_SCREENCAST(2); /// Screencasts / Screenshots content
    	
    	private int xmlValue;
    	
    	ContentType(int xmlValue){    		
    		this.xmlValue = xmlValue;
    	}

		public int getXmlValue() {
			return xmlValue;
		}
    	
		public static ContentType getEnumFromValue(int value){
			switch (value) {
			case 0:	return ContentType.PHOTO_VIDEO;
			case 1:	return ContentType.ILLUSTRATION_ART_ANIMATION;
			case 2:	return ContentType.SCREENSHOT_SCREENCAST;
			default: return ContentType.PHOTO_VIDEO;
			}
		}
	}
    
    // Queue to store cache
    private static PriorityQueue<Photo> photoCache = new PriorityQueue<Photo>();
    // Current size of cache
    private static int cachesize = 0;
    // Size, in Megapixels of the cachesize
    private static final int cachesizelimit = 10 * 1024 * 1024;
    
    // Date image is put in cache
    private Date imageCached = new Date(0);
    private BufferedImage image = null;
    private Size imageSize = null;
 
    private Map<Size,PhotoSize> sizes = new EnumMap<Size, PhotoSize>(Size.class);
    
    private String id;
    private String secret;
    private String server;
    private String title;
    private Media media;
    private PhotoPermissions perms = null;
    private SafetyLevel safety_level;
    
    // Boolean to track if we've gotten the extra information associated with a Photo
    private boolean gotInfo = false;
    
    private String description;
    private int numComments;
    
    Photo(Element photo) {
        if (photo == null) throw new FlickrRuntimeException("Can't construct Photo from null");
        this.server = photo.getAttributeValue("server");
        this.id = photo.getAttributeValue("id");
        this.secret = photo.getAttributeValue("secret");
        this.title = photo.getAttributeValue("title");
        
    }
    
    
    /**
     * If you know the ID and the Secret of a photo, you can use it to create a new
     * Photo object.  Typically, you'll get new Photo objects by calling static methods,
     * but this constructor is useful when you know just the photo you want.
     */
    public Photo(String id, String secret) throws FlickrException {
        this.id = id;
        this.secret = secret;
        getInfo();
    }
    
	/**
     * Utility method for uploading new photos to Flickr.
     * This method requires a PhotoUpload object for encapsulate informations 
     * needed to upload.
     * 
     * @return id of photo uploaded or ticket id (if async mode)
     * @throws FlickrException For any error.
     */
    public static String uploadNewPhoto(PhotoUpload photoUpload) throws FlickrException{
    	return uploadNewPhoto(photoUpload, null);
    }
    
    public static String uploadNewPhoto(PhotoUpload photoUpload, RequestListener listener) throws FlickrException{
    	if (photoUpload == null) throw new FlickrException("Can't upload a photo without PhotoUpload");
    	if (photoUpload.getPhoto() == null) throw new FlickrException("Can't upload a photo from null PhotoUpload.photo");
    	
    	// Generate the request
    	Request req = new Request(Request.POST, Flickr.getUploadURL());
    	if (listener!=null) req.addRequestListener(listener);
    	// Set parameters of the request
    	req.setParameter("photo", photoUpload.getPhoto());
    	if (!photoUpload.getTitle().equals(""))
    		req.setParameter("title", photoUpload.getTitle());
    	if (!photoUpload.getDescription().equals(""))
    		req.setParameter("description", photoUpload.getDescription());
    	
    	boolean first = true;
    	String tagsString = "";
    	for(String val : photoUpload.getTags()) { 
    		if (first) {
    			first = false;
    		} else {
    			tagsString += " ";    			
    		}
    		tagsString += val; 
    	}
    	if (!tagsString.equals(""))
    		req.setParameter("tags", tagsString);
    	
    	if (photoUpload.isPublicFlag() != null)
    		req.setParameter("is_public", photoUpload.isPublicFlag() ? "1" : "0");
    	
    	if (photoUpload.isFriendFlag() != null)
    		req.setParameter("is_friend", photoUpload.isFriendFlag() ? "1" : "0");
    	
    	if (photoUpload.isFamilyFlag() != null)
    		req.setParameter("is_family", photoUpload.isFamilyFlag() ? "1" : "0");
    	
    	if (photoUpload.getSafetyLevel() != null)
    		req.setParameter("safety_level", String.valueOf(photoUpload.getSafetyLevel().getXmlValue()));
		
    	if (photoUpload.getContentType() != null)
    		req.setParameter("content_type", String.valueOf(photoUpload.getContentType().getXmlValue()));
    	
    	if (photoUpload.isAsync() != null)
    		req.setParameter("async", photoUpload.isAsync() ? "1" : "0");

    	// POST and get response
		Document doc = req.postAndGetResponse();
		
		// Return photo id or ticket id (if async)
		if (photoUpload.isAsync()==null || !photoUpload.isAsync())
			return doc.getRootElement().getChildText("photoid");
		else
			return doc.getRootElement().getChildText("ticketid");
    }
    
    
    public static void checkTicket (String ticketid) throws FlickrException {
    	if (ticketid == null) throw new FlickrException("Can't check ticket without ticket id");
    	
    	Request req = new Request();
    	req.setParameter("method", "flickr.photos.upload.checkTickets");
    	req.setParameter("tickets", ticketid);
    	
    	Document doc = req.getResponse();
    	Element ticket = doc.getRootElement().getChild("uploader").getChild("ticket");
//    	for (Element e : tickets){
//    		System.out.println("id: "+e.getAttributeValue("id"));
//    		if (e.getAttribute("complete")!=null){
//    			System.out.println("complete: "+e.getAttributeValue("complete"));
//    		}
//    		if (e.getAttribute("invalid")!=null){
//    			System.out.println("invalid: "+e.getAttributeValue("invalid"));
//    		}
//    		if (e.getAttribute("imported")!=null){
//    			System.out.println("imported: "+e.getAttributeValue("imported"));
//    		}
//    		if (e.getAttribute("photoid")!=null){
//    			System.out.println("photoid: "+e.getAttributeValue("photoid"));
//    		}
//    	}
    }
    
    /**
     * Utility method for uploading and replace an existing photo to Flickr.
     * This method requires the id of the photo to replace, the new photo file and type of
     * connection (sync or async).
     * 
     * @param idPhotoToReplace id of the photo to relpace
     * @param newPhoto new photo file
     * @param async connection type (synchronous or asynchronous)
     * @return the Flickr id of the new photo
     * @throws FlickrException For any error.
     */
    public static String uploadAndReplacePhoto(String idPhotoToReplace, File newPhotoFile, Boolean async) throws FlickrException{
    	if (idPhotoToReplace == null) throw new FlickrException("Can't replace a photo without its id");
    	if (newPhotoFile == null) throw new FlickrException("Can't replace a photo without a new file");
    	
    	// Generate the request
    	Request req = new Request(Request.POST, Flickr.getReplaceURL());
    	// Set parameters of the request
    	req.setParameter("photo", newPhotoFile);
    	req.setParameter("photo_id", idPhotoToReplace);
    	if (async != null)
    		req.setParameter("async", async ? "1" : "0");
    	
    	// POST and get response
    	Document doc = req.postAndGetResponse();
    	
		return doc.getRootElement().getChildText("photoid");
    }
    
    /**
     * Get a photo, provided you have the Photo's id, and secret key.
     * @return The Photo object described by the key and secret.
     */
    public static Photo findByID(String id, String secret) throws FlickrException {
        if (id == null) throw new FlickrRuntimeException("Can't construct Photo from null id");
        if (secret == null) throw new FlickrRuntimeException("Can't construct Photo from null secret");
        return new Photo(id,secret);
    }
    
    /**
     *  Get a list of photos that are tagged with the desired keywords.
     * @param tags Comma separated list of tags
     * @throws FlicrException For any error.
     */
    public static List<Photo> findByTags(String tags) throws FlickrException {
        if (tags == null) throw new FlickrRuntimeException("Tags cannot be null");
        return findByTags(tags, true);
    }
    
    /**
     * Get a list of Photos that are tagged with the desired keywords.
     * @param tags Comma separated list of tags
     * @param join True joins the tags with AND, False joins the tags with OR
     * @throws FlickrException For any error.
     */
    public static List<Photo> findByTags(String tags, boolean join) throws FlickrException {
        if (tags == null) throw new FlickrRuntimeException("Tags cannot be null");
        
        Request req = new Request();
        req.setParameter("method","flickr.photos.search");
        req.setParameter("tags",tags);
        if (join) req.setParameter("tag_mode","all"); // else "any" is default
        return new PhotoList(req);
    }
    
    /**
     * Get a list of Photos associated with the <code>PhotoSearch</code> term.
     * Unauthenticated calls return only public photos.  If authenticated at READ
     * level or above, private photos visible to the authenticated user can be
     * viewed as well.
     * @param search A PhotoSearch object describing the search to perform.
     * @return A list of Photos corresponding to the search term supplied.
     * @throws FlickrExcpetion For any error.
     */
    public static List<Photo> search(PhotoSearch search) throws FlickrException {
        if (search == null) throw new FlickrRuntimeException("Search term cannot be null");
        
        boolean termUsed = false;
        
        Request req = new Request();
        req.setParameter("method","flickr.photos.search");
        
        if (search.getUser() != null) {
            termUsed = true;
            req.setParameter("user_id",search.getUser().getNSID());
        }
        
        if (search.getTags() != null) {
            termUsed = true;
            req.setParameter("tags",search.getTags());
            if (search.getTagMode() != null) {
                if (search.getTagMode() == PhotoSearch.Tagmode.ALL) {
                    req.setParameter("tagmode","all");
                } else { // ANY
                    req.setParameter("tagmode","any");
                }
            } else { // Default is ANY
                    req.setParameter("tagmode","any");                
            }
        }
        
        if (search.getSearchText() != null) {
            termUsed = true;
            req.setParameter("text",search.getSearchText());
        }
        
        if (search.getPrivacy() != null) {
            termUsed = true;
            switch (search.getPrivacy()) {
                case PUBLIC:
                    req.setParameter("privacy_filter","1");
                    break;
                case FRIENDS:
                    req.setParameter("privacy_filter","2");
                    break;
                case FAMILY:
                    req.setParameter("privacy_filter","3");
                    break;
                case FRIENDSANDFAMILY:
                    req.setParameter("privacy_filter","4");
                    break;
                case PRIVATE:
                    req.setParameter("privacy_filter","5");
                    break;
            }
        }
        
        if (!termUsed) throw new FlickrRuntimeException("Search object must contain a search condition");

        return new PhotoList(req);
    }
    
    /**
     * Gets a list of the most recent photos on Flickr.
     *
     * @return A <code>PhotoList</code> of Flickr's recent photos
     */
    public static List<Photo> getRecentPhotos() throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.photos.getRecent");
        return new PhotoList(req);
    }
    
    /**
     * Gets a list of Photos that Flickr users find interesting, for today.
     *
     * @return A <code>PhotoList</code> of Flickr's interesting photos.
     */
    public static List<Photo> getInteresting() throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.interestingness.getList");
        return new PhotoList(req);
    }
    
    /**
     * Gets a list of Photos that Flickr users find interesting, for today.
     *
     * @param cal - Used to specify what date to fetch interesting photos from.
     * @return A <code>PhotoList</code> of FJickrs interesting photos.
     */
    public static PhotoList getInteresting(Calendar cal) throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.interestingness.getList");
        String date = String.format("%1tY-%1tm-%1td",cal);
        req.setParameter("date",date);
        return new PhotoList(req);
    }
    
    /**
     * Get the static Flickr URL where you can find this photo. (i.e., the URL
     * that returns the actual image.) This call
     * returns the default sized photo.  To get the page you see in a browser, use
     * <code>Photo.getPage()</code>
     *
     * @return URL of the default static link for this photo.
     * @see org.jickr.Photo#getPage
     */
    public URL getURL() throws FlickrException {
        return getDefaultSize().getURL();
    }
    
    /**
     * Get the Flickr URL of the page where you can find this photo. (i.e., the URL
     * that returns the page where this image is displayed.) This call
     * returns the default sized photo.  To get the page you see in a browser, use
     * <code>Photo.getPage()</code>  <b>Please note:  Flickr requires  that if you display
     * an image from this Photo in a webpage, that you make it a link to the URL returned
     * from this call.  Please do the right thing.</b>
     *
     * @return URL of the default page for this photo.
     * @see org.jickr.Photo#getURL
     */
    public URL getPage() throws FlickrException {
        return getDefaultSize().getPage();
    }
    
    /**
     * Get the title of the photo.
     * @return title - The title of the Photo.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Get the secret key of the photo.
     * @return The secret key of the Photo.
     */
    public String getSecret() {
        return secret;
    }
    
    /**
     * Get the id of the photo.  You need this and the secret key to the retreive the Photo.
     * @return The id of the Photo.
     */
    public String getID() {
        return id;
    }
    
    /**
     * Get the description of the photo.
     * @return The Description Text for this Flickr Photo.
     */
    public String getDescription() throws FlickrException {
        getInfo();
        return description;
    }
    
    /**
     * Get the number of comments attached to this photo in Flickr.
     * @return The total number of comments associated with this <code>Photo</code>
     */
    public int getNumComments() throws FlickrException {
        getInfo();
        return numComments;
    }
    
    /**
     * Get a BufferedImage of this photo (default size).
     * @return image Image of this photo.
     */
    @Deprecated
    public BufferedImage getImage() throws IOException, FlickrException {
        return getImage(getDefaultSize().getSize());
    }
    
    /**
     * Get a BufferedImage of this photo.
     * @param size - Size of the image to fetch.
     * @return image Image of this photo.
     */
    @Deprecated
    public BufferedImage getImage(Size size) throws FlickrException {
        if (image != null  && imageSize == size) return image;
        // TODO add the ability to cache more than one size at a time
        if (image != null && imageSize != size) dropImage(); // Only cache one size at a time, sorry
        initSizes();
        if (!sizes.containsKey(size)) throw new FlickrException("This photo doesn't have the size: "+size);
        try {
            image = ImageIO.read(sizes.get(size).getURL());
        } catch (IllegalArgumentException iae) {
            // There's a bug in the ImageIO class - it throws Illegal Arg on bad image, instead of IOException
            throw new FlickrException("Bad Image Data: Photo id="+id,iae);
        } catch (IOException ioex) {
            // Since it throws IAE anyway, lets wrap the IO exception too
            throw new FlickrException("Error reading image: Photo id="+id,ioex);
        }
        cachesize = cachesize + (image.getHeight() * image.getWidth());
        while (cachesize > cachesizelimit && photoCache.size() > 0) {
            photoCache.poll().dropImage();
        }
        photoCache.add(this);
        imageCached = new Date();
        return image;
    }
    
    /**
     * Download and store locally photo.
     * @param size - Size of the image to fetch.
     * @return image Image of this photo.
     */
    public String getImage(Size size, String destinationDir) throws FlickrException {
        
    	final int _size = 1024;
		String localFileName = "test.jpg";
		OutputStream outStream = null;
		URLConnection uCon = null;
		InputStream is = null;
		
        initSizes();
        if (!sizes.containsKey(size)) throw new FlickrException("This photo doesn't have the size: "+size);
        
        String[] tmp = sizes.get(size).getURL().getFile().split("\\.");
        String ext = tmp[tmp.length-1];
        
        localFileName = getID() + size.getSuffix() + "." + ext;        
        
        
        // Photo already downloaded?
        File photoFile = new File(destinationDir+"/"+localFileName);
		if (photoFile.exists())
			return destinationDir+"/"+localFileName;
        
		try {
			URL Url;
			byte[] buf;
			int ByteRead, ByteWritten = 0;
			Url = sizes.get(size).getURL();

			// Open connection
			uCon = Url.openConnection();
			
			// Get stream
			is = uCon.getInputStream();
			
			// Output
			outStream = new BufferedOutputStream(new
					FileOutputStream(destinationDir+"/"+localFileName));
			
			buf = new byte[_size];
			while ((ByteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, ByteRead);
				ByteWritten += ByteRead;
			}

		} catch (IOException e) {
			throw new FlickrException(e.getMessage());
		} finally {
			try {
				if (is != null)
					is.close();
				if (outStream !=null)
					outStream.close();
			} catch (IOException e) {
				throw new FlickrException(e.getMessage());
			}
		}
    	
 
        return destinationDir+"/"+localFileName;
    }
    
    /**
     * Get a Stream of this video.
     * @param size - Size of the video to fetch.
     * @return video Video path of this "photo".
     */
    public String getVideo(Size size, String destinationDir) throws FlickrException {
        
		final int _size = 1024;
		String localFileName = "test.mov";
		OutputStream outStream = null;
		URLConnection uCon = null;

		InputStream is = null;
		
		// Init
		initSizes();
		
		if (!sizes.containsKey(size)) throw new FlickrException("This video doesn't have the size: "+size);
		if (!(size.equals(Size.VIDEO_ORIGINAL) ||
			size.equals(Size.MOBILE_MP4) || 
			size.equals(Size.SITE_MP4)))
			throw new FlickrException("This size cannot be downloaded");
		
		try {
			URL Url;
			byte[] buf;
			int ByteRead, ByteWritten = 0;
			Url = sizes.get(size).getURL();

			// Open connection
			uCon = Url.openConnection();
			
			// Get host filename
			try {
				HttpURLConnection uConHttp = (HttpURLConnection) uCon;			
				localFileName = uConHttp.getHeaderField("Content-Disposition").split("filename=")[1];
				 
			} catch(Exception e) {
				throw new FlickrException(e.getMessage());
			}
			
			// Video already downloaded?
			File testFile = new File(destinationDir+"/"+localFileName);
			if (testFile.exists())
				return destinationDir+"/"+localFileName;
			
			// Get stream
			is = uCon.getInputStream();
			
			// Output
			outStream = new BufferedOutputStream(new
					FileOutputStream(destinationDir+"/"+localFileName));
			
			buf = new byte[_size];
			while ((ByteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, ByteRead);
				ByteWritten += ByteRead;
			}

		} catch (Exception e) {
			throw new FlickrException(e.getMessage());
		} finally {
			try {
				if (is != null)
					is.close();
				if (outStream !=null)
					outStream.close();
			} catch (IOException e) {
				throw new FlickrException(e.getMessage());
			}
		}
    	
        return destinationDir+"/"+localFileName;
    }
    
    /**
     * Get media type
     * @return media type
     * @throws FlickrException 
     */
    public Media getMedia() throws FlickrException {
    	getInfo();
		return media;
	}

    /**
     * Get photo permissions
     * @return permissions
     * @throws FlickrException boolean conversion can throw exception
     */
	public PhotoPermissions getPerms() throws FlickrException {
		getInfo();
		return perms;
	}


	public SafetyLevel getSafety_level() {
		return safety_level;
	}


	/**
     * Drop the cached Image from this photo.  Will be garbage collected.
     */
    private void dropImage() {
        Logger.global.finest("Dropping Photo From Cache:"+this);
        cachesize = cachesize - (image.getHeight() * image.getWidth());
        Logger.global.finest("CacheSize = "+cachesize);
        image = null;
        imageCached = new Date(0);
    }
    
    /**
     * Get a list of comments for this photo.
     * @return A list of Comment objects for this Photo.
     * @throws FlickrException in the event of any error.
     */
    public List<Comment> getComments() throws FlickrException {
        List<Comment> commentList = new ArrayList();
        Request req = new Request();
        req.setParameter("method","flickr.photos.comments.getList");
        req.setParameter("photo_id",id);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        
        List<Element> comments = root.getChild("comments").getChildren("comment");
        for (Element comment: comments) {
            commentList.add(new Comment(Comment.Type.PHOTOCOMMENT,comment));
        }
        return commentList;
    }
    
    /**
     * <P>Adds a comment to this Photo, returns the new comment ID.  Since Flickr
     * doesn't offer a way to search for a comment via ID, the user will
     * have to do a <code>getComments()</code> and search for the comment with
     * <code>Comment.getID()</code> if they want a Comment object representing this
     * added comment.</P>
     *
     * <P>This call requires that the user be authenticated at the WRITE level.</P>
     *
     * @see org.jickr.Photo#getComments()
     * @see org.jickr.Comment#getID()
     * @param commentText Text for the comment to add.
     * @return The comment's ID string
     * @throws FlickrException in the event of a failure.  Note that if an exception is thrown, it's possible (thought unlikely) that a comment was added anyway.
     */
    public String addComment(String commentText) throws FlickrException {
        if (commentText == null) throw new NullPointerException("Comment Text may not be null");
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.photos.comments.addComment");
        req.setParameter("photo_id",id);
        req.setParameter("comment_text",commentText);
        
        Element root = req.getResponse().getRootElement();
        
        try {
            String commentID = root.getChild("comment").getAttributeValue("id");
            // Sigh.  Flickr doesn't let you get a comment via commentID
            // So instead, we'll have just return the ID.
            // The developer can search for it if they wanna.
            return commentID;
        } catch (NullPointerException npe) {
            throw new FlickrException("Oddly formed XML in response",npe);
        }
        
    }
    
    /**
     *  Add this photo as favorite to the authenticated user.
     *  @throws FlickrException on any error.
     */
    public void addFavorite() throws FlickrException {
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.favorites.add");
        req.setParameter("photo_id",id);
        req.getResponse(); // Empty Response except for status
    }
    
    /**
     * Remove this photo from the authenticated user's list of favorites.
     * @throws FlickrException on any error.
     */
    public void removeFavorite() throws FlickrException {
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.favorites.remove");
        req.setParameter("photo_id",id);
        req.getResponse(); // Empty Response except for status        
    }
    
    private void getInfo() throws FlickrException {
        if (gotInfo) return;
        
        Request req = new Request();
        req.setParameter("method","flickr.photos.getInfo");
        req.setParameter("photo_id",id);
        req.setParameter("secret",secret);
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        
        try {
            this.id = root.getChild("photo").getAttributeValue("id");
            this.secret = root.getChild("photo").getAttributeValue("secret");
            this.server = root.getChild("photo").getAttributeValue("server");
            this.title = root.getChild("photo").getChildText("title");
            this.description = root.getChild("photo").getChildText("description");
            this.numComments = Integer.parseInt(root.getChild("photo").getChildText("comments"));
            this.media = Media.valueOf(root.getChild("photo").getAttributeValue("media").toUpperCase());
            
            // Permission
            perms = new PhotoPermissions(root.getChild("photo"));
            
            // Safety level
            safety_level = SafetyLevel.getEnumFromValue(Integer.parseInt(root.getChild("photo").getAttributeValue("safety_level")));
            
        } catch (NullPointerException npe) {
            throw new FlickrException("Oddly formed XML error",npe);
        } catch (DataConversionException e) {
        	throw new FlickrException("Boolean conversion error for perms creation",e);
		}
        
        gotInfo = true;
    }
    
    /**
     * Get the String representation of the URL for this photo.
     * @return String representing this Photo.  Using the URL for this purpose.
     */
    public String toString() {
        try {
            return getDefaultSize().URL.toString();
        } catch (FlickrException ex) {
            return "Error getting String for photo:"+id;
        }
    }
    
    /**
     * Determine of a Photo is equal to another.
     * @return Whether a Photo object represents the same photo
     */
    public boolean equals(Object o) {
        // TODO catch class cast exception
        Photo p = (Photo)o;
        return this.getID().equals(p.getID());
    }
    
    /**
     * Compares a Photo's image.  Uses the Date the Photo's image object was
     * created to compare.
     * (Note: Not the date of the photo, but the creatation date and time of the
     * Java Object).  Return is the same as for java.util.Date.
     *
     * TODO:
     * Yes, this doesn't actually do what you'd expect.  I need to fix this at some
     * point to return something a little more rational, or hide it.  In particular,
     * equals uses a different equality metric, so I've got to deal with that at some point.
     *
     * @return comparison <1 if Photo newer than supplied photo, 0 if the same, 1 if older.
     */
    public int compareTo(Photo p) {
        // TODO fix this to match equals!  Right now, may give problems
        return this.imageCached.compareTo(p.imageCached);
    }
    
    private void initSizes() throws FlickrException {
        // Only run once.
        if (sizes.size() != 0 ) return;
        
        Request req = new Request();
        req.setParameter("method","flickr.photos.getSizes");
        req.setParameter("photo_id",id);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        
        List<Element> photosizes = root.getChild("sizes").getChildren("size");
        
        for (Element photosize : photosizes)  {
            PhotoSize ps = new PhotoSize(photosize);
            sizes.put(ps.getSize(),ps);
        }
    }
    
    // init perms
    private void initPerms() throws FlickrException, DataConversionException {
        // Only run once.
        if (perms != null ) return;
        
        Request req = new Request();
        req.setParameter("method","flickr.photos.getPerms");
        req.setParameter("photo_id",id);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        Element _perms = root.getChild("perms");
        
        perms = new PhotoPermissions(_perms, 0);
        
    }
    
    /**
     * Gets the default PhotoSize for this item.  Will return Medium sized photos,
     * marching downward until we find something.
     * @return The default PhotoSize.
     */
    private PhotoSize getDefaultSize() throws FlickrException {
        initSizes();
        if (sizes.containsKey(Size.MEDIUM)) {
            return sizes.get(Size.MEDIUM);
        } else if (sizes.containsKey(Size.SMALL)) {
            return sizes.get(Size.SMALL);
        } else if (sizes.containsKey(Size.SQUARE)) {
            return sizes.get(Size.SQUARE);
        } else if (sizes.containsKey(Size.THUMB)) {
            return sizes.get(Size.THUMB);
        } else if (sizes.containsKey(Size.LARGE)) {
            return sizes.get(Size.LARGE);
        } else if (sizes.containsKey(Size.ORIGINAL)) {
            // Now we're desparate.  Wonder if this will ever happen?
            return sizes.get(Size.ORIGINAL);
        } else {
            throw new FlickrRuntimeException("Can't find a Default PhotoSize");
        }
    }
    
    /**
     * PhotoSize encapsulates information for a certain size of a photo.  It's
     * intended that many of these will be associated with a single instance of Photo.
     *
     * @see org.netdance.flickr.Photo
     * @author driscoll
     */
    class PhotoSize {
        
        private Size size ;
        private String sizeString;
        private int width;
        private int height;
        private URL URL;
        private URL pageURL;
        
        /**
         * Creates a new instance of PhotoSize.
         *
         * @throws FlickrRuntimeException - if it receives a size that it doesn't
         * recognize. If this happens, it means Flickr changed it's formats, and it's
         * time to update the API.
         */
        PhotoSize(Element sizeElement) throws FlickrRuntimeException {
            try {
                sizeString = sizeElement.getAttributeValue("label");
                if (sizeString.equals("Thumbnail")) {
                    size = Size.THUMB;
                } else if (sizeString.equals("Square")) {
                    size = Size.SQUARE;
                } else if (sizeString.equals("Small")) {
                    size = Size.SMALL;
                } else if (sizeString.equals("Medium")) {
                    size = Size.MEDIUM;
                } else if (sizeString.equals("Medium 640")) {
                    size = Size.MEDIUM;
                } else if (sizeString.equals("Large")) {
                    size = Size.LARGE;
                } else if (sizeString.equals("Original")) {
                    size = Size.ORIGINAL;
                } else if (sizeString.equals("Site MP4")) {
                    size = Size.SITE_MP4;
                } else if (sizeString.equals("Mobile MP4")) {
                    size = Size.MOBILE_MP4;
                } else if (sizeString.equals("Video Original")) {
                    size = Size.VIDEO_ORIGINAL;
                } else if (sizeString.equals("Video Player")) { 
                    size = Size.VIDEO_PLAYER;
                } else {
                    throw new FlickrRuntimeException("Unknown Size recieved: "+sizeString);
                }
                this.width = Integer.parseInt(sizeElement.getAttributeValue("width"));
                this.height = Integer.parseInt(sizeElement.getAttributeValue("height"));
                try {
                    this.URL = new URL(sizeElement.getAttributeValue("source"));
                    this.pageURL = new URL(sizeElement.getAttributeValue("url"));
                } catch (MalformedURLException ex) {
                    // This would be suprising, but might as well throw it.
                    throw new FlickrRuntimeException("Malformed URL Exception",ex);
                }
            } catch (NullPointerException npe) {
                throw new FlickrRuntimeException("Malformed XML recieved from Flickr",npe);
            }
        }
        
        /**
         * Gets the size of the Photo.
         * @return The size of the Photo in this object.
         */
        Size getSize() {
            return size;
        }
        
        /**
         * Gets the width of the image associated with the PhotoSize.
         * @return The width in pixels.
         */
        int getWidth() {
            return width;
        }
        
        /**
         * Gets the height of the image associated with the PhotoSize.
         * @return The height in pixels.
         */
        int getHeight() {
            return height;
        }
        
        /**
         * Gets the URL of the image for this PhotoSize.
         * @return The URL of the static reference for the image of this PhotoSize.
         */
        URL getURL() {
            return URL;
        }
        
        /**
         * Gets the URL of the Page for this PhotoSize at Flickr.  This is the
         * page that you would want to call up in a browser, for instance, or send
         * in an email.
         *
         * @return The URL of the page containing the Photo of this size.
         */
        URL getPage() {
            return pageURL;
        }
        
    }
   
    /**
     * Photo permissions
     * @author jbrek
     *
     */
    public class PhotoPermissions {    	
    	
    	// Visibility permissions
    	private Privacy privacy;
    	
    	// Registred user comments/meta permissions
    	private Authorization permcomment; // Permission to comment 
    	private Authorization permaddmeta; // Permission to add tag/person/remark
    	
    	// Registred user editability permissions
    	private boolean cancomment;
    	private boolean canaddmeta;
    	
    	// Public editability permisssions
    	private boolean pubcancomment;
    	private boolean pubcanaddmeta;
    	
    	// Usage permissions
    	private boolean candownload;
    	private boolean canblog;
    	private boolean canprint;
    	private boolean canshare;
    	
    	/**
    	 * Constructor
    	 * 
    	 * @param photo element photo
    	 * @throws DataConversionException boolean conversion can failed
    	 */
    	public PhotoPermissions(Element photo) throws DataConversionException {
			
    		// Get elements
    		Element visibility = photo.getChild("visibility");
    		Element permissions = photo.getChild("permissions");
    		Element editability = photo.getChild("editability");
    		Element publiceditability = photo.getChild("publiceditability");
    		Element usage = photo.getChild("usage");
    		
    		privacy = Privacy.getEnumFromValue(visibility.getAttribute("ispublic").getBooleanValue(), 
    												   visibility.getAttribute("isfriend").getBooleanValue(), 
    												   visibility.getAttribute("isfamily").getBooleanValue());
    		
    		permcomment = Authorization.getEnumFromValue(permissions.getAttribute("permcomment").getIntValue());
    		permaddmeta = Authorization.getEnumFromValue(permissions.getAttribute("permaddmeta").getIntValue());
    		
    		cancomment = editability.getAttribute("cancomment").getBooleanValue();
    		canaddmeta = editability.getAttribute("canaddmeta").getBooleanValue();
    		
    		pubcancomment = publiceditability.getAttribute("cancomment").getBooleanValue();
    		pubcanaddmeta = publiceditability.getAttribute("canaddmeta").getBooleanValue();
    		
    		candownload = usage.getAttribute("candownload").getBooleanValue();
    		canblog = usage.getAttribute("canblog").getBooleanValue();
    		canprint = usage.getAttribute("canprint").getBooleanValue();
    		canshare = usage.getAttribute("canshare").getBooleanValue();
		}
	
    	
    	/**
    	 * Constructor
    	 * 
    	 * @param photo element photo
    	 * @throws DataConversionException boolean conversion can failed
    	 */
    	public PhotoPermissions(Element perms, int a) throws DataConversionException {
			
    		// Get attributes
    		privacy = Privacy.getEnumFromValue(perms.getAttribute("ispublic").getBooleanValue(), 
    				perms.getAttribute("isfriend").getBooleanValue(), 
    				perms.getAttribute("isfamily").getBooleanValue());

			permcomment = Authorization.getEnumFromValue(perms.getAttribute("permcomment").getIntValue());
			permaddmeta = Authorization.getEnumFromValue(perms.getAttribute("permaddmeta").getIntValue());
    		
		}

		public Privacy getPermission() {
			return privacy;
		}


		public Authorization isPermcomment() {
			return permcomment;
		}

		public Authorization isPermaddmeta() {
			return permaddmeta;
		}

		public boolean isCancomment() {
			return cancomment;
		}

		public boolean isCanaddmeta() {
			return canaddmeta;
		}

		public boolean isPubcancomment() {
			return pubcancomment;
		}

		public boolean isPubcanaddmeta() {
			return pubcanaddmeta;
		}

		public boolean isCandownload() {
			return candownload;
		}

		public boolean isCanblog() {
			return canblog;
		}

		public boolean isCanprint() {
			return canprint;
		}

		public boolean isCanshare() {
			return canshare;
		}    	
    }
}