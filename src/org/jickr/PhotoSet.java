/*
 * PhotoSet.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 16, 2006, 3:35 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;

/**
 * A Flickr PhotoSet.  Created by calls that get a <code>List&lt;PhotoSet&gt;</code>,
 * such as <code>FlickrUser.getPhotoSet()</code> and <code>PhotoSet.findByID(id)</code>
 * @see User#getPhotoSets()
 * @see PhotoSet#findByID(String)
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class PhotoSet {
    
    private String description;
    private String title;
    private String id;
    private int numPhotos;
    
    /**
     * Creates a new instance of PhotoSet, using the Element returned by Flickr.
     * <code>PhotoSet</code> is created in lists via User.getPhotoSets(), or
     * via the <code>PhotoSet.findByID(id)</code> call.
     *
     *
     * @param photoset The Photoset information.  Must not be null.
     * @throws FlickrException On any error.
     */
    PhotoSet(Element photoset) throws FlickrException {
        if (photoset == null) throw new FlickrRuntimeException("Photoset cannot be null");
        this.id = photoset.getAttributeValue("id");
        String photoCountStr = photoset.getAttributeValue("photos");
        if (photoCountStr == null)
            throw new FlickrException("Read Invalid value for PhotoCount from Flickr");
        this.numPhotos = Integer.parseInt(photoCountStr);
        this.title = photoset.getChildText("title");
        this.description = photoset.getChildText("description");
    }
    
    PhotoSet(String id) throws FlickrException {
        if (id == null) throw new FlickrRuntimeException("id cannot be null");
        Request req = new Request();
        req.setParameter("method","flickr.photosets.getInfo");
        req.setParameter("photoset_id",id);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        Element photoset = root.getChild("photoset");
        this.id = photoset.getAttributeValue("id");
        String photoCountStr = photoset.getAttributeValue("photos");
        if (photoCountStr == null)
            throw new FlickrException("Read Invalid value for PhotoCount from Flickr");
        this.numPhotos = Integer.parseInt(photoCountStr);
        this.title = photoset.getChildText("title");
        this.description = photoset.getChildText("description");
    }
    
    /**
     * Get a new PhotoSet identified by the supplied ID.
     * @param id - The unique ID of the PhotoSet in Flickr.
     * @return A PhotoSet that is identified by the supplied ID.
     */
    public static PhotoSet findByID(String id) throws FlickrException {
        return new PhotoSet(id);
    }
    
    /**
     * Get the Title of the PhotoSet.
     * @return Title of the PhotoSet.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Get the Description of the PhotoSet.
     * @return The description of the PhotoSet.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the number of Photos in the PhotoSet. Note: this value is cached, and
     * is not updated once checked.  Use PhotoSet.getPhotos().size() to get an
     * up to date value, though note that this is an expensive call.
     * @return The number of photos in the PhotoSet.
     */
    public int getNumPhotos() {
        return numPhotos;
    }
    
    /**
     * Get the ID of the PhotoSet.  This value is unique.
     * @return The ID of this PhotoSet on Flickr.
     */
    public String getID() {
        return id;
    }
    
    /**
     * Retrieve the list of photos associated with this PhotoSet.  This
     * value is not cached, and may be expensive to execute, depending on
     * how many Photos are in the PhotoSet.
     * @return photoset A list of photos for this Photoset.
     * @throws FlickrException for almost any error.
     */
    public PhotoList getPhotos() throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.photosets.getPhotos");
        req.setParameter("photoset_id",id);
        return new PhotoList(req);
    }
    
    /**
     * Get a list of comments for this PhotoSet.
     * @return A list of comments for this PhotoSet.
     * @throws FlickrException in the event of any error.
     */
    public List<Comment> getComments() throws FlickrException {
        List<Comment> commentList = new ArrayList();
        Request req = new Request();
        req.setParameter("method","flickr.photosets.comments.getList");
        req.setParameter("photoset_id",id);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        
        List<Element> comments = root.getChild("comments").getChildren("comment");
        for (Element comment: comments) {
            commentList.add(new Comment(Comment.Type.PHOTOSETCOMMENT,comment));
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
     * @return The comment's ID string
     * @throws FlickrException in the event of a failure.  Note that if an exception is thrown, it's possible (thought unlikely) that a comment was added anyway.
     */
    public String addComment(String commentText) throws FlickrException {
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.photosets.comments.addComment");
        req.setParameter("photoset_id",id);
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
     * Update the Title and Description of the PhotoSet.  Requires WRITE
     * authentication.
     * @param titleText New title of the PhotoSet.  May not be null.
     * @param descText New description of the PhotoSet.  If null, description is not updated.
     * @throws FlickrException on any error.
     */
    public void updateTitleDesc(String titleText,String descText) throws FlickrException {
        if (titleText == null) throw new NullPointerException("Title may not be null");
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.photosets.editMeta");
        req.setParameter("photoset_id",id);
        req.setParameter("title",titleText);
        if (descText != null) req.setParameter("description",descText);
        req.getResponse();
        title = titleText;
        if (descText != null) description = descText;
        return;
    }
    
    /**
     * Adds a photo to a PhotoSet.  Requires WRITE authentication.
     * @param photo Photo to add to the PhotoSet.
     */
    public void add(Photo photo) throws FlickrException {
        if (photo == null) throw new NullPointerException("Photo cannot be null");
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.photosets.addPhoto");
        req.setParameter("photoset_id",id);
        req.setParameter("photo_id",photo.getID());
        req.getResponse();
        return;
    }
    
    /**
     * Removes a photo to a PhotoSet.  Requires WRITE authentication.
     * @param photo Photo to remove from the PhotoSet.
     */
    public void remove(Photo photo) throws FlickrException {
        if (photo == null) throw new NullPointerException("Photo cannot be null");
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.photosets.removePhoto");
        req.setParameter("photoset_id",id);
        req.setParameter("photo_id",photo.getID());
        req.getResponse();
        return;
    }
    
    /**
     * Convert this PhotoSet to a String.
     * @return The photoset title.
     */
    public String toString() {
        return title;
    }
    
    /**
     * Check equality.  Uniqueness based on ID.
     * @param o - Object to compare.
     * @return true if these objects have the same ID string.
     */
    public boolean equals(Object o) {
        return ((PhotoSet)o).getID().equals(this.getID());
    }
    
}
