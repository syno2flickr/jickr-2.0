/*
 * Comment.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 22, 2006, 11:07 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 */

package org.jickr;

import org.jdom.Element;

/**
 * A Comment, attached to either a PhotoSet or a Photo.
 * This object is created by the <code>getComments</code> call from
 * either <code>Photo</code> or <code>Photoset</code>
 *
 * @see org.jickr.Photo#getComments
 * @see org.jickr.PhotoSet#getComments
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class Comment {
    
    enum Type {PHOTOCOMMENT, PHOTOSETCOMMENT}
    
    private final Type type;
    private boolean deleted = false;
    
    private String id;
    private User author;
    private String link;
    private String commentText;
    
    /** Creates a new instance of Comment */
    Comment(Type type, Element comment) throws FlickrException {
        if (comment == null) throw new FlickrRuntimeException("Comment must not be null");
        this.type = type;
        id = comment.getAttributeValue("id");
        author = User.findByNSID(comment.getAttributeValue("author"));
        link = comment.getAttributeValue("permalink");
        commentText = comment.getText();
    }
    
    /**
     * Get the ID for this Comment
     * @return Comment ID for this Comment.
     */
    public String getID() {
        if (deleted) throw new IllegalStateException("This Comment has been deleted");
        return id;
    }
    
    /**
     * Get the author of the comment.
     *
     * @return The User who is listed as the author of the <code>Comment</code>.
     */
    public User getAuthor() {
        if (deleted) throw new IllegalStateException("This Comment has been deleted");
        return author;
    }
    
    /**
     * Get the permanent link that leads to this comment.
     * @return A string representation of the URL leading to this comment on Flickr.
     */
    public String getPermalink() {
        if (deleted) throw new IllegalStateException("This Comment has been deleted");
        return link;
    }
    
    /**
     * Get the text associated with the comment.
     * @return The content of this Comment.
     */
    public String getCommentText() {
        if (deleted) throw new IllegalStateException("This Comment has been deleted");
        return commentText;
    }
    
    /**
     * Change the text of this comment to the supplied text.
     * @param commentText New text that this comment should contain.
     * @throws FlickrException in the event of any error.
     */
    public void updateCommentText(String commentText) throws FlickrException {
        if (deleted) throw new IllegalStateException("This Comment has been deleted");
        Request req = new Request(Request.POST);
        switch (type) {
            case PHOTOCOMMENT:
                req.setParameter("method","flickr.photos.comments.editComment");
                break;
            case PHOTOSETCOMMENT:
                req.setParameter("method","flickr.photosets.comments.editComment");
                break;
        }
        req.setParameter("comment_id",id);
        req.setParameter("comment_text",commentText);
        // There's no return except for status
        req.getResponse();
    }
    
    /**
     * Delete this comment from Flickr.  After deletion, no more operations
     * may be performed on this object.
     * @throws FlickrException On the event of any error.
     */
    public void deleteComment() throws FlickrException {
        if (deleted) throw new IllegalStateException("This Comment has been deleted");
        Request req = new Request(Request.POST);
        switch (type) {
            case PHOTOCOMMENT:
                req.setParameter("method","flickr.photos.comments.deleteComment");
                break;
            case PHOTOSETCOMMENT:
                req.setParameter("method","flickr.photosets.comments.deleteComment");
                break;
        }
        req.setParameter("comment_id",id);
        // There's no return except for status
        req.getResponse();
        deleted = true;
    }
}
