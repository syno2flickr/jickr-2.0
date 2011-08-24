/*
 * Group.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 29, 2006, 6:59 PM
 *
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;

import java.util.List;
import org.jdom.Document;
import org.jdom.Element;

/**
 * Encapsulates a Flickr Group.
 * Create a new Group object with the <code>Group.search(String)</code> method.
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class Group {
    
    private String id;
    
    private boolean infoChecked = false;
    private String name = null;
    private String description = null;
    private int numMembers = -1;
    
    /** 
     * Creates a new instance of Group, based on the Element returned from a 
     * Flickr query.
     * @param group Element representing group information.
     */
    Group(Element group) {
        if (group == null) throw new NullPointerException("Can't construct Group from null");
        this.id = group.getAttributeValue("id");
        this.name = group.getAttributeValue("name");
    }
    
    /**
     * Create a new Group object corresponding to the Flickr Group.
     * More usually, Group objects are created by a Group.search(String).
     * @param groupID Unique Flickr ID of the Group.
     */
    public Group(String groupID) {
        if (groupID == null) throw new NullPointerException("groupID may not be null");
        this.id = groupID;
    }
    
    
    /**
     * Full text search for groups which contain searchText.  Note that this is
     * an extremely expensive call, and may take many seconds (>5 seconds) to return.
     * @param searchText The String to search for.
     * @return A list of Groups.
     * @throws FlickrException on any error.
     */
    public static List<Group> search(String searchText) throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.groups.search");
        req.setParameter("text",searchText);
        return new GroupList(req);
    }
    
    /**
     * Get the list of groups that the authenticated user can add photos to.
     * Requires READ permission for User in AuthContext.
     * @return A list of Groups
     * @throws FlickrException on any error.
     */
    public static List<Group> getGroups() throws FlickrException {
       Request req = new Request();
       req.setParameter("method","flickr.groups.pools.getGroups");
       return new GroupList(req);
    }
    
    /**
     * Get the ID of this group.  Note that this value is cached at the time
     * of the Group object creation.
     * @return ID String the uniquely identifies this group.
     */
    public String getID() {
        return id;
    }
    
    /**
     * Get the Title of this Group.  Note that this value is cached, it's only
     * checked once in the lifetime of the Group object.  In practice, this should
     * rarely be an issue.
     * @return The name of the group.
     */
    public String getName() throws FlickrException {
        if (name == null && !infoChecked) checkInfo();
        return name;
    }
    
    /**
     * Get the Description of this Group.  Note that this value is cached, it's 
     * only checked once for the lifetime of the Group object.  In practice,
     * this should rarely be an issue.
     * @return The description of the Group.
     */
    public String getDescription() throws FlickrException {
        if (!infoChecked) checkInfo();
        return description;
    }
    
    /**
     * Get the number of Members who belong to this group.
     * Note that this response is cached - it's only checked once.  Create a 
     * new Group object to repeatedly query the number of users.
     * @return The count of the members in this group.
     * @throws FlickrException on any error.
     */
    public int getNumMembers() throws FlickrException {
        if (infoChecked) return numMembers;
        checkInfo();
        return numMembers;
    }
    
    /**
     * Get the photos available in this Group's pool.
     * @return A list of Photos for this Group.
     * @throws FlickrException on any error.
     */
    public List<Photo> getPhotos() throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.groups.pools.getPhotos");
        req.setParameter("group_id",id);
        return new PhotoList(req);
    }
    
    /**
     * Add a photo to the Group's pool.  Requires WRITE authentication.
     * @param photo Photo to add.
     */
    public void add(Photo photo) throws FlickrException {
        if (photo == null) throw new NullPointerException("Photo must not be null");
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.groups.pools.add");
        req.setParameter("group_id",id);
        req.setParameter("photo_id",photo.getID());
        req.getResponse(); // Null response
        return;
    }

    /**
     * Remove a photo to the Group's pool.  Requires WRITE authentication.
     * @param photo Photo to remove.
     */
    public void remove(Photo photo) throws FlickrException {
        if (photo == null) throw new NullPointerException("Photo must not be null");
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.groups.pools.remove");
        req.setParameter("group_id",id);
        req.setParameter("photo_id",photo.getID());
        req.getResponse(); // Null response
        return;
    }
    
    private void checkInfo() throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.groups.getInfo");
        req.setParameter("group_id",id);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        
        try {
            name = root.getChild("group").getChildText("name");
            description = root.getChild("group").getChildText("description");
            numMembers = Integer.parseInt(root.getChild("group").getChildText("members"));
            infoChecked = true;
        } catch (NullPointerException npe) {
            throw new FlickrException("Badly Formed XML",npe);
        }
    }
}