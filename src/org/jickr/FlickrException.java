/*
 * FlickrException.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 14, 2006, 2:44 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;

/**
 * Generic exception thrown by Jickr objects.  This exception is thrown both if 
 * an error is returned from Flickr, or if the user makes a coding error.
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class FlickrException extends java.lang.Exception {

    private int code = 0;
    
    /**
     * Creates a new instance of <code>FlickrException</code> without detail 
     * message.  Made private because we don't really want that, after all.
     */
    private  FlickrException() {
    }

    /**
     * Constructs an instance of <code>FlickrException</code> with the specified
     * detail message.
     * @param msg the detail message.
     */
    public FlickrException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>FlickrException</code> with the specified
     * detail message.
     * @param msg the detail message.
     * @param code The error code for the message.  This corresponds to the Flickr Error code. See the <a href="http://www.flickr.com/services/api/">Flickr API documentation</a>.
     * 
     */
    public FlickrException(String msg, int code) {
        super(msg);
        this.code = code;
    }
    
    /**
     * Constructs an instance of <code>FlickrException</code> with the specified
     * detail message, and the root cause exception.
     * @param msg the detail message.
     * @param cause the root cause of the exception
     */
    public FlickrException(String msg, Throwable cause) {
        super(msg,cause);
    }

    /**
     * If the FlickrException is returned because of a Flickr Error, then this 
     * code is set to be the same as that returned by Flickr.  See the <a href="http://www.flickr.com/services/api/">Flickr API documentation</a>.
     * This code is 0 if not set by Flickr.
     * @return code Flickr return code.
     */
    public int getCode() {
        return code;
    }
    
}