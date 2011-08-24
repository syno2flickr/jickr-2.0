/*
 * PhotoSearch.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 26, 2006, 12:00 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;

/**
 * Class used for complex searches on Photo objects.  This class should
 * be populated with search terms, and then passed to
 * <code>Photo.search(PhotoSearch)</code>.
 *
 * @see org.jickr.Photo#search(PhotoSearch)
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class PhotoSearch {
    
    private User user = null;
    private String tags = null;
    private Tagmode tagmode = null;
    private String text = null;
    private Privacy priv = null;
    
    /**
     * The join mode for multitag searches.
     */
    public enum Tagmode {
        /**
         * Search for any tag in the taglist - i.e., this is an OR operation.
         */
        ANY,
        /**
         * Search for every tag in the taglist - i.e., this is an AND operation.
         */
        ALL }
    
    /** Creates a new instance of PhotoSearch */
    public PhotoSearch() {
    }
    
    /**
     * Search for Photos owned by the specified user.
     * @param user The user to search for.  Null to search for all users.
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * Get the user in the search term.
     * @return The owner of the Photos you want to find.  Null if unset.
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Search for Photos with the following tags.
     * @param tags Comma separated list of tags. Null to search for all.
     */
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    /**
     * Get the comma separated list of tags to search for.
     * @return The list of tags. Null if unset.
     */
    public String getTags() {
        return tags;
    }
    
    /**
     * Set the join mode for multi-tag searches.  ANY looks for any tag in the list (OR).
     * ALL looks for every tag in the list (AND).  Defaults to ANY if unset.  May be
     * set to null to indicate default behavior.
     *
     * @param tagmode Join mode for multi-tag searches.
     */
    public void setTagMode(Tagmode tagmode) {
        this.tagmode = tagmode;
    }
    
    /**
     * Get the join mode for multi-tag searches.
     * @return The join mode for tag searches, null if unset.
     */
    public Tagmode getTagMode() {
        return tagmode;
    }
    
    /**
     *  Full text search string.  Flickr will look for this string in
     *  descriptions, title and tags.  Set to null to clear.
     * @param searchText The string to use to search Flickr.
     */
    public void setSearchText(String searchText) {
        this.text = searchText;
    }
    
    /**
     * Get the text search string.
     * @return The search string for full text search, null if unset.
     */
    public String getSearchText() {
        return text;
    }
    
    public void setPrivacy(Privacy privacy) {
        this.priv = privacy;
    }
    
    public Privacy getPrivacy() {
        return priv;
    }
    
    
    
    
    /**
     * Privacy value associated with an object on Flickr.
     * @author driscoll
     */
    public enum Privacy {
        /**
         * The object is visible to everyone.
         */
        PUBLIC,
        /**
         * The object is visible only to people marked as a friend on Flickr.
         */
        FRIENDS,
        /**
         * The object is visible only to people marked as family on Flickr.
         */
        FAMILY,
        /**
         * The object is visible only to people marked either a friend or family on Flickr.
         */
        FRIENDSANDFAMILY,
        /**
         * The object is visible only to the authenticated user, it is completely private.
         */
        PRIVATE }
    
}