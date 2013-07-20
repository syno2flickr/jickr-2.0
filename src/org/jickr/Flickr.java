/*
 * Flickr.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 14, 2006, 2:21 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 */

package org.jickr;

import java.util.logging.Logger;

/**
 * Flickr access information.  Also includes some static methods to perform
 * certain generic tasks.
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class Flickr {
    private static String apiKey;
    private static String sharedSecret;

    private static final String baseURL = "http://www.flickr.com/services/rest/"; /// REST url service
    private static final String uploadURL = "http://api.flickr.com/services/upload/"; /// Upload url service
    private static final String replaceURL = "http://api.flickr.com/services/upload/"; /// Replace photos url service
    
    // No constructor - this class is static only.
    private Flickr() {
    }
    
    
    /**
     * Set the API key for use in other parts of the library.  Note:  This should
     * be the only place where you have to know your API key.  You'll need to get
     * your key from the <a href="http://www.flickr.com/services/api/keys/">Flickr API site</a>.
     *
     * @param newApiKey Flickr API key
     */
    public static void setApiKey(String newApiKey) {
        apiKey = newApiKey;
        
        // I'm unaware of any time I WOULDN'T want to set this
        // I'm stumped why it's not the default.
        // It's certainly a poor design choice to put this here, though.
        // TODO: Figure out a better place to put this, or take it out.
        try {
            System.setProperty("java.net.useSystemProxies","true");
        } catch (SecurityException e) {
            // We'll log at fine, since this error shouldn't be a suprise
            Logger.global.fine("Can't set useSystemProxies");
        }
    }
    
    /**
     * Get the API key, after having stored it via setApiKey.
     *
     * @throws FlickrRuntimeException if API Key not set before this call.
     * @return apiKey  The Flickr API key.
     *
     */
    public static String getApiKey() {
        if (apiKey == null) throw new FlickrRuntimeException("API Key not set");
        return apiKey;
    }
    
    /**
     * Set the shared secret for use in other parts of the library.  Note:  This should
     * be the only place where you have to know your Shared Secret.  You'll need to get
     * the Shared Secret from Flickr at the same place you get your API Key.  Setting the
     * Shared Secret also signs all requests automatically.
     *
     * @param newSharedSecret Flickr Shared Secret
     */
    public static void setSharedSecret(String newSharedSecret) {
        sharedSecret = newSharedSecret;
        Request.sign(true);
    }
    
    /**
     * Get the Shared Secret for this app.
     * @throws FlickrRuntimeException If the Shared Secret not set.
     */
    public static String getSharedSecret() {
        if (sharedSecret == null) throw new FlickrRuntimeException("Shared Secret not set");
        return sharedSecret;
    }
    
    /**
     * Get the base URL for all REST requests.
     *
     * @return the base URL for all requests to Flickr.
     */
    public static String getBase() {
        return baseURL;
    }

    /**
     * Get the URL for upload photos requests.
     *
     * @return the URL for upload photos requests to Flickr.
     */
	public static String getUploadURL() {
		return uploadURL;
	}

	/**
     * Get the URL for replace photos requests.
     *
     * @return the URL for replace photos requests to Flickr.
     */
	public static String getReplaceURL() {
		return replaceURL;
	}
}
