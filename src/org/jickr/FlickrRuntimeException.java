
/*
 * FlickrRuntimeException.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 15, 2006, 4:34 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;


/**
 * Generic Runtime exception thrown by Jickr.  
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class FlickrRuntimeException extends java.lang.RuntimeException {
    
    /**
     * Creates a new instance of <code>FlickrRuntimeException</code> without detail message.
     */
    public FlickrRuntimeException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>FlickrRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public FlickrRuntimeException(String msg, Throwable cause) {
        super(msg,cause);
    }
}
