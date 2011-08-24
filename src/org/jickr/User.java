/*
 * User.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 14, 2006, 2:20 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jickr.Request;
/**
 * Information on a User of Flickr.
 *
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class User {
    
    enum Lookup { USER, EMAIL, NSID }
    
    // Flickr User Name
    private String username;
    // Real Name of User
    private String realname;
    // Flickr unique Identifier for user
    private String nsid;
    // Freeform text location of user
    private String location;
    private URL photosurl;
    private URL profileurl;
    private int photocount;
    
    /**
     * Creates a new instance of User
     */
    private User(Lookup type, String search) throws FlickrException {
        
        Document doc;
        Element root;
        Request req = new Request();
        
        if  (!type.equals(Lookup.NSID)) {
            switch (type) {
                case USER:
                    req.setParameter("method","flickr.people.findByUsername");
                    req.setParameter("username",search);
                    break;
                case EMAIL:
                    req.setParameter("method","flickr.people.findByEmail");
                    req.setParameter("find_email",search);
                    break;
            }
            doc = req.getResponse();
            root = doc.getRootElement();
            nsid = root.getChild("user").getAttributeValue("nsid");
        } else {
            nsid = search;
        }
        // Now that we have the nsid, we can use it to do a lookup of other data for the user.
        req = new Request();
        req.setParameter("method","flickr.people.getInfo");
        req.setParameter("user_id",nsid);
        
        doc = req.getResponse();
        root = doc.getRootElement();
        
        try {
            username = root.getChild("person").getChildText("username");
            realname = root.getChild("person").getChildText("realname");
            location = root.getChild("person").getChildText("location");
            photocount = Integer.parseInt(root.getChild("person").getChild("photos").getChildText("count"));
            try {
                photosurl = new URL(root.getChild("person").getChild("photosurl").getText());
                profileurl = new URL(root.getChild("person").getChild("profileurl").getText());
            } catch (MalformedURLException ex) {
                throw new FlickrException("Malformed URL: ",ex);
            }
        } catch (NullPointerException npe) {
            throw new FlickrException("Oddly Formed XML",npe);
        }
    }
    
    /**
     *  Return a new User object that corresponds to the stated username.
     *
     * @param username FJickrUsername
     * @throws FlickrException for almost anything, including not found errors
     */
    public static User findByUsername(String username) throws FlickrException {
        return new User(Lookup.USER, username);
    }
    
    /**
     * Return a new User object that corresponds to the stated Email Address.
     *
     * @param emailAddr Email address <code>&qt;user@domain.com&qt;</code>
     * @return user a FUserobject that corresponds to that email address.
     * @throws FlickrException for almost anything, including not found errors
     */
    public static User findByEmail(String emailAddr) throws FlickrException {
        return new User(Lookup.EMAIL, emailAddr);
    }
    
    /**
     * Return a new User object that corresponds to the stated NSID.  An NSID
     * is a Flickr specific unique identifier.
     *
     * @param nsid Flickr specific identifier
     * @return user a FliUserject that corresponds to that email address.
     * @throws FlickrException for almost anything, including not found errors
     */
    public static User findByNSID(String nsid) throws FlickrException {
        return new User(Lookup.NSID, nsid);
    }
    
    /**
     * Get the User Name
     *
     *
     * @return username Flickr user name
     */
    public String getUserName() {
        return username;
    }
    
    /**
     * Get the real name.
     *
     *
     * @return realname The user's real name, as listed in Flickr.
     */
    public String getRealName() {
        return realname;
    }
    
    /**
     * Get the location listed for this user in Flickr.
     *
     *
     * @return location Location of this user, as listed in Flickr.
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Gets the URL for photos for this user.
     * @return URL of the user's photos
     */
    public URL getPhotosURL() {
        return photosurl;
    }
    
    /**
     * Gets the URL of the user profile.
     * @return URL of the user profile
     */
    public URL getProfileURL() {
        return profileurl;
    }
    
    /**
     * Gets the number of photos the user owns.
     * @return photocount - count of photos the user owns
     *
     */
    public int getPhotoCount() {
        return photocount;
    }
    
    /**
     *  Gets the value of NSID, the flickr unique identifier used in most of their URLs.
     *
     * @return nsid - Unique identifier used in FJickrURLs.
     */
    public String getNSID() {
        return nsid;
    }
    
    /**
     * Get a list of users that are contacts for this User.  Only public users are
     * returned.
     * @return userlist A list of FlickrUsers.
     */
    public List<User> getPublicContacts() throws FlickrException {
        
        ArrayList<User> userlist = new ArrayList();
        
        Request req = new Request();
        req.setParameter("method","flickr.contacts.getPublicList");
        req.setParameter("user_id",nsid);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        
        List<Element> contacts = root.getChild("contacts").getChildren("contact");
        
        for (Element contact: contacts)  {
            userlist.add(User.findByNSID(contact.getAttributeValue("nsid")));
        }
        
        return userlist;
        
    }
    
    /**
     * Get a list of users that are contacts for the authenticated user.  Requires
     * READ privlege.
     * @return userlist A list of users who are contacts for the authenticated user.
     * @throws FlickrException on any error, including not being authenticated.
     */
    public static List<User> getContacts() throws FlickrException {
        //TODO - add contact filter
        User user = Auth.getAuthContext();
        if (!Auth.isAuthenticated(user,Permission.READ)) throw new FlickrRuntimeException("Needs READ permission");
        
        ArrayList<User> userlist = new ArrayList();
        
        Request req = new Request();
        req.setParameter("method","flickr.contacts.getList");
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        
        List<Element> contacts = root.getChild("contacts").getChildren("contact");
        for (Element contact: contacts) {
            userlist.add(User.findByNSID(contact.getAttributeValue("nsid")));
        }
        return userlist;
    }
    
    /**
     * Get a list of all public photos for this user.
     * @return photolist A list of all public photos  belonging to this user.
     * @throws FlickrException in the event of any error.
     */
    public List<Photo> getPublicPhotos() throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.people.getPublicPhotos");
        req.setParameter("user_id",nsid);
        return new PhotoList(req);
    }
    
    /**
     * Get a list of all public favorite photos for this user.
     * @return photolist A list of all public favorite photos for this user.
     * @throws FlickrException in the event of any error.
     */
    public List<Photo> getPublicFavoritePhotos() throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.favorites.getPublicList");
        req.setParameter("user_id",nsid);
        return new PhotoList(req);
    }
    
    /**
     * Get a list of all favorite photos for this user.  This call requires
     * READ Permissions.
     * @return photolist A list of all favorite photos for this user.
     * @throws FlickrException in the event of any error.
     */
    public List<Photo> getFavoritePhotos() throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.favorites.getList");
        req.setParameter("user_id",nsid);
        return new PhotoList(req);
    }
    
    /**
     * Get a list of all public photos for this user's contacts.
     * @return photolist A list of 10 publicly viewable photos for this user's contacts.
     * @throws FlickrException in the event of any error.
     */
    public List<Photo> getPublicContactPhotos() throws FlickrException {
        return getPublicContactPhotos(false, 10);
    }
    
    /**
     * Get a list of all public photos for this user's contacts.
     * @return photolist A list of all public photos for this user's contacts.
     * @param justFriends Return only friends' photos, or all contacts' photos.
     * @param count How many photos to return - default is 10, max is 50.
     * @throws FlickrException in the event of any error.
     */
    public List<Photo> getPublicContactPhotos(boolean justFriends, int count) throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.photos.getContactsPublicPhotos");
        req.setParameter("user_id",nsid);
        req.setParameter("count",count+"");
        if (justFriends) req.setParameter("just_friends","1");
        return new PhotoList(req);
    }
    
    /**
     * Get a list of the PhotoSets for this user.
     * @return photosetlist A list of all PhotoSets for this user
     * @throws FlickrException in the event of any error
     */
    public List<PhotoSet> getPhotoSets() throws FlickrException {
        
        Request req = new Request();
        req.setParameter("method","flickr.photosets.getList");
        req.setParameter("user_id",nsid);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        List tempList = new ArrayList();
        
        List<Element> photosets = root.getChild("photosets").getChildren("photoset");
        for (Element photoset : photosets)  {
            tempList.add(new PhotoSet(photoset));
        }
        return tempList;
    }
    
    /**
     * Get a list of the Collections for this user.
     * @return collectionlist A list of all Collections for this user
     * @throws FlickrException in the event of any error
     */
    public List<PhotoCollection> getCollections() throws FlickrException {
        
        Request req = new Request();
        req.setParameter("method","flickr.collections.getTree");
        req.setParameter("user_id",nsid);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        List tempList = new ArrayList();
        
        List<Element> collections = root.getChild("collections").getChildren("collection");
        for (Element collection : collections)  {
            tempList.add(new PhotoCollection(collection));
        }
        return tempList;
    }
    
    /**
     * Return a String rendition of this object.  Use username for this purpose.
     * @return A String representation of this object.  For this object, it's the username.
     */
    public String toString() {
        return username;
    }
    
    /**
     * Check for equality of two User objects.  Uses NSID interally.
     *
     * @param o - Object to compare.
     * @return Whether the object is equal to this object.
     */
    public boolean equals(Object o) {
        try {
            User u = (User)o;
            return this.getNSID().equalsIgnoreCase(u.getNSID());
        } catch (ClassCastException cce) {
            return false;
        }
    }
    
    /**
     * Create a hashcode for the User object, based on NSID.
     * @return A hashcode for this object.
     */
    public int hashCode() {
        return this.getNSID().hashCode();
    }
}
