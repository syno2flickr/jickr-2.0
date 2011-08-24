/*
 * Auth.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 26, 2006, 8:53 AM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 */

package org.jickr;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.jdom.Document;

/**
 * <P>The Auth class is a set of static methods used to authenticate with Flickr.</P>
 *
 * <P>Here's how to authenticate.  First, you need a Flickr account.</P>
 *
 *<P>For desktop, call the following from your code:</P>
 *<PRE><CODE>
 *       Permission perm = Permission.READ; // For some value of Permission requested
 *       User user = Auth.getDefaultAuthUser();
 *       if (Auth.isAuthenticated(user,perm)) {
 *          Auth.setAuthContext(user);
 *       } else {
 *          System.out.println("Enter the following URL in your browser, then come back");
 *          System.out.println(Auth.getAuthURL(perm));
 *          user = Auth.authenticate();
 *          &gt;wait here until they come back&lt;
 *          Auth.isAuthenticated(user,perm);
 *          Auth.setDefaultAuthUser(user);
 *      }
 *</CODE></PRE>
 *
 *<P>For a multiuser, multithreaded environment like a servlet, call the following:</P>
 *<PRE><CODE>
 *       Permission perm = Permission.READ; // For some value of Permission requested
 *       System.out.println("Enter the following URL in your browser, then come back");
 *       System.out.println(Auth.getAuthURL(perm));
 *       &gt;wait here until they come back&lt;
 *       User user = Auth.authenticate();
 *<P>Then, in a different thread (i.e., a different servlet):</P>
 *       Auth.setAuthContext(user);
 *       &gt;do the main action&lt;
 *       Auth.resetAuthContext(); // clear the thread
 *</CODE></PRE>
 *
 * <P>These two code fragments do the same thing, the first uses the nsid key of "defaultKey" instead
 * of the actual nsid.</P>
 *
 * <P>Apply exception handling liberally above, since that's how Jickr reports problems.</P>
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class Auth {
    
    /*
    private static String frob;
    private static boolean checkedToken = false;
    private static String token;
    private static Permission tokenPerm;
    private static User tokenUser;
     */
    
    private static Map<String,AuthToken> userMap = new HashMap<String,AuthToken>();
    private static Map<String,Permission> frobMap = new HashMap<String,Permission>();
    private static Map<Long,User> contextMap = new HashMap<Long,User>();
    private static Map<Long,String> frobContextMap = new HashMap<Long,String>();
    
    /** Never create a new instance of Auth */
    private Auth() {
    }
    
    /**
     * Authenticates user to Flickr.  Requires additional action by the user
     * afterward (i.e., visiting the URL in a browser).  Note:  Once a user is
     * authenticated, that information is stored locally, and need not be called
     * again unless changed, or revoked by Flickr.
     *
     * Jickr will disable authentication until you next call authenticate.
     *
     * @see org.jickr.Auth#authenticate()
     * @see org.jickr.Auth#getFrob()
     * @param perm The permissions this application needs.
     * @throws FlickrException on any error getting the URL from Flickr.
     */
    public static String getAuthURL(Permission perm) throws FlickrException {
        if (perm == null) throw new NullPointerException("Permission is not allowed to be null");
        String frobKey = getFrob();
        frobMap.put(frobKey,perm);
        Long id = Thread.currentThread().getId();
        frobContextMap.put(id,frobKey);
        Request req = new Request("http://www.flickr.com/services/auth/");
        req.setParameter("frob",frobKey);
        switch (perm) {
            case READ: req.setParameter("perms","read"); break;
            case WRITE: req.setParameter("perms","write"); break;
            case DELETE: req.setParameter("perms","delete"); break;
        }
        return req.getURL();
    }
    
    /**
     * Authenticate a a session, after the user has gone to the URL from getAuthURL().
     * Also sets the authenticated context to be this authenticated user on this thread.
     * @return The user we just authenticated.
     * @throws FlickrException If anything goes wrong.  This is also what will happen if getAuthURL wasn't visited.
     */
    public static User authenticate() throws FlickrException {
        Long id = Thread.currentThread().getId();
        String frobKey = frobContextMap.get(id);
        frobContextMap.remove(id);
        if (frobKey == null) throw new FlickrRuntimeException("No previous getURL call on this thread");
        Permission perm = frobMap.get(frobKey);
        if (perm == null) throw new FlickrRuntimeException("Frob not valid");
        frobMap.remove(frobKey);
        Request req = new Request();
        req.setParameter("method","flickr.auth.getToken");
        req.setParameter("frob",frobKey);
        Document doc = req.getResponse();
        String token;
        try {
            token = doc.getRootElement().getChild("auth").getChildText("token");
        } catch (NullPointerException npe) {
            throw new FlickrException("Oddly Formatted XML Error",npe);
        }
        AuthToken authtoken = new AuthToken(perm,token);
        try {
            // Note we rely on the loadToken side effect of this call.
            String nsid = authtoken.getNSID();
            userMap.put(nsid,authtoken);
            Preferences pref = Preferences.userNodeForPackage(Flickr.class);
            pref.put("token"+nsid,authtoken.getToken());
        } catch (FlickrException fe) {
            if (fe.getCode() == 98) { // we have an invalid token - invalidate the token
                throw new FlickrException("Just Authenticated user now invalid",fe);
            } else throw fe; // otherwise rethrow the exception
        }
        setAuthContext(authtoken.getUser());
        return authtoken.getUser();
    }
    
    /**
     * Returns whether we're authenticated to at least this level.
     * If user is null, this will return false.
     * @param authUser The the user to check for authentication. Null returns false.
     * @param perm Permission to which we should be authenticated. Must not be null.
     * @return Whether we're fully authenticated to <code>perm</code> level.
     */
    public static boolean isAuthenticated(User authUser, Permission perm) throws FlickrException {
        if (perm == null) throw new NullPointerException("Permission cannot be null");
        if (authUser == null) return false;
        AuthToken authtoken = getAuthToken(authUser);
        if (authtoken == null) return false;
        Permission tokenPerm = authtoken.getPerm();
        switch (perm) {
            case READ:
                return true; // If we're authenicated, it's to this level.
            case WRITE:
                if (tokenPerm == Permission.READ) return false;
                return true;
            case DELETE:
                if (tokenPerm == Permission.DELETE) return true;
                return false;
        }
        throw new FlickrRuntimeException("Deep coding error - Permission has too many values");
    }
    
    
// Get the AuthToken for a user.  Null if unset.
    private static AuthToken getAuthToken(User authUser) throws FlickrException {
        if (authUser == null) throw new FlickrRuntimeException("User cannot be null");
        AuthToken authtoken;
        if (userMap.containsKey(authUser.getNSID())) {
            // We have the token already in memory
            authtoken = userMap.get(authUser.getNSID());
        } else {
            // Get token, if possible, from our Preferences store
            Preferences pref = Preferences.userNodeForPackage(Flickr.class);
            String token = pref.get("token"+authUser.getNSID(),null);
            // Couldn't find it.
            if (token == null) return null;
            // Check to see it still works
            authtoken = new AuthToken(token);
            // Save it to memory
            userMap.put(authUser.getNSID(),authtoken);
            try {
                authtoken.loadToken();
            } catch (FlickrException fe) {
                if (fe.getCode() == 98) { // we have an invalid token - invalidate the token
                    pref.remove("token"+authUser.getNSID());
                    userMap.remove(authUser.getNSID());
                    return null;
                } else throw fe; // otherwise rethrow the exception
            }
        }
        return authtoken;
    }
    
    /**
     * Gets the default authenticated user.  Convenience method for single-user usecases.
     * Note that there is no guarentee if this user is still authenticated - 
     * use isAuthenticated(getDefaultAuthUser()) to check.
     * @return Gets the default authenticated user.  Null if unset.
     */
    public static User getDefaultAuthUser() throws FlickrException {
        Preferences pref = Preferences.userNodeForPackage(Flickr.class);
        String nsid = pref.get("defaultAuthUser",null);
        if (nsid == null) return null;
        User user = User.findByNSID(nsid);
        return user;
    }
    
    /**
     * Sets the default authenticated user, for use in later executions of the program.
     * Convenience method for single-user usecases.  Retrieved by getDefaultAuthUser().
     * @param user The user to make the default. Null to clear value.
     */
    public static void setDefaultAuthUser(User user) throws FlickrException {
        Preferences pref = Preferences.userNodeForPackage(Flickr.class);
        if (user == null)  {
            pref.remove("defaultAuthUser");
        } else {
            pref.put("defaultAuthUser",user.getNSID());
        }
    }

    /**
     * Gets the authentication level for a given user.
     * @param user User to whose permissions to check.
     * @return Permission level authenticated.
     * @throws FlickrException On any error, or if not authenticated
     */
    public static Permission getPermLevel(User user) throws FlickrException {
        // TODO return null if not authenticated.
        AuthToken authtoken = getAuthToken(user);
        return authtoken.getPerm();

    }
    
    /**
     * Either get the token from memory, or get it from Preferences.
     */
    static String getToken(User user) throws FlickrException {
        AuthToken authtoken = getAuthToken(user);
        if (authtoken == null) throw new FlickrRuntimeException("Can't get token for unauthorized user "+user.getUserName());
        return authtoken.getToken();
    }
    
    /**
     * Set the AuthContext for this thread.  Authenticated calls check the authcontext
     * when determining who to execute a Flickr call as.
     * @see org.jickr.Auth#authenticate()
     * @see org.jickr.Auth#getDefaultAuthUser()
     * @param user All calls will be made as this Authenticated user.
     */
    public static void setAuthContext(User user) {
        Long id = Thread.currentThread().getId();
        contextMap.put(id,user);
    }
    
    /**
     * Remove the AuthContext from this thread.  Useful in multithreaded contexts.
     */
    public static void resetAuthContext() {
        Long id = Thread.currentThread().getId();
        contextMap.remove(id);
    }
    
    
    /**
     * Get the AuthContext from this thread.
     * @return Authenticated user associated with this thread.
     */
    public static User getAuthContext() {
        Long id = Thread.currentThread().getId();
        return contextMap.get(id);
    }

        /**
     * Get a key from Flickr for later use in Authentication.
     * @return A Frob - a unique key to use in later authentication calls.
     */
    private static String getFrob() throws FlickrException {
        Request req = new Request();
        req.setParameter("method","flickr.auth.getFrob");
        Document doc = req.getResponse();
        String frob = doc.getRootElement().getChildText("frob");
        if (frob == null) throw new FlickrException("Error creating AuthURL: Could not set frob on Flickr - Bad XML");
        return frob;
    }
    


}

/**
 * Struct class to hold Auth data for a single user.
 */
class AuthToken {
    
    private String token;
    private Permission tokenPerm;
    private User tokenUser;
    
    AuthToken(String token) {
        this.token = token;
    }
    
    AuthToken(Permission perm, String token) {
        this.token = token;
        tokenPerm = perm;
    }
    
    String getToken() {
        return token;
    }
    
    Permission getPerm() throws FlickrException {
        if (tokenPerm == null) loadToken();
        return tokenPerm;
    }
    
    /*
     * checks if this token is valid.  Side effects of setting the perm and user
     * to the correct value.  Throws exception if not valid.
     */
    void loadToken() throws FlickrException {
        Request req = new Request(Flickr.getBase());
        req.setParameter("method","flickr.auth.checkToken");
        req.setParameter("auth_token",token);
        Document doc = req.getResponse();
        try {
            String nsid = doc.getRootElement().getChild("auth").getChild("user").getAttributeValue("nsid");
            tokenUser = User.findByNSID(nsid);
            String permString = doc.getRootElement().getChild("auth").getChildText("perms");
            if (permString.equals("read")) {
                tokenPerm = Permission.READ;
            } else if (permString.equals("write")) {
                tokenPerm = Permission.WRITE;
            } else if (permString.equals("delete")) {
                tokenPerm = Permission.DELETE;
            } else throw new FlickrException("Badly formed XML - can't determine permissions for authenticated user");
        } catch (NullPointerException npe) {
            throw new FlickrException("Oddly Formatted XML Error",npe);
        }
    }
    
    /**
     * Get the nsid for this token.
     */
    String getNSID() throws FlickrException {
        if (tokenUser == null) loadToken();
        return tokenUser.getNSID();
    }
    
    /**
     * Get the authenticated user for this token.
     */
    User getUser() throws FlickrException {
        if (tokenUser == null) loadToken();
        return tokenUser;
    }
}