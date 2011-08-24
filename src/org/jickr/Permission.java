/*
 * Permission.java
 *
 * Copyright (c) 2006, James G. Driscoll
 * 
 * Created on July 22, 2006, 6:38 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;

/**
 * Permission allowed.  They are ordered as: READ, WRITE, DELETE.  Higher permissions
 * also allow access to lower permissions.
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public enum Permission {
    /**
     * READ permission allows the program to look at private photos that the 
     * authenticated user has permission to see on Flickr.
     */
    READ, 
    /**
     * WRITE permission allows the program to modify data about photos on Flickr,
     * if the authenticated user has permission to do it on Flickr.
     */
    WRITE, 
    /**
     * DELETE permission allows the program to delete photos, if the authenticated
     * user has permission to do it on Flickr.
     */
    DELETE}
