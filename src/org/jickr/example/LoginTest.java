/*
 * LoginTest.java
 *
 * Created on July 21, 2006, 2:40 PM
 *
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import org.jickr.*;
/**
 * A class to demonstrate how to do Authentication.
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class LoginTest {
    
    /**
     *
     * Creates a new instance of LoginTest
     */
    public static void main(String[] args) throws Exception {
        
        // We'll read the API Key and Shared Secret from a file, since
        // We don't want to check in our keys.
        // Normally, we'd use the next block of code and hardcode the keys instead

        BufferedReader br = new BufferedReader(new FileReader("keys"));
        String apiKey = br.readLine();
        Flickr.setApiKey(apiKey);
        String sharedSecret = br.readLine();
        Flickr.setSharedSecret(sharedSecret);

        /*
        //  The Following block is the ususal one to include in programs.
        String apiKey = "7c27be6a52b69ceaa0320ed0e1bf8078";
        Flickr.setApiKey(apiKey);
         
        String sharedSecret = "68db5e9a3b2731ea";
        Flickr.setSharedSecret(sharedSecret);
         */
        
        Permission perm = Permission.WRITE; // access level we want
        User user = Auth.getDefaultAuthUser(); // Check to see if we've already authenticated
        //User user = User.findByUsername("jickr"); // For testing
        
        
        if (user != null && Auth.isAuthenticated(user, perm)) {
            System.out.println("Already authenticated to at least "+perm+" level.");
            System.out.println("We're actually at the "+Auth.getPermLevel(user)+" level.");
            System.exit(0);
        }
        // Give the URL to enter in the browser
        System.out.println("Please enter the following URL into a browser, " +
                "follow the instructions, and then come back");
        String authURL = Auth.getAuthURL(perm);
        System.out.println(authURL);
        // Wait for them to come back
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        in.readLine();
        // Alright, now that we're cleared with Flickr, let's try to authenticate
        System.out.println("Trying to get "+perm+" access.");
        try {
            user = Auth.authenticate();
        } catch (FlickrException ex) {
            System.out.println("Failure to authenticate.");
            System.exit(1);
        }
        if (Auth.isAuthenticated(user,perm)) {
            System.out.println("We're authenticated with "+Auth.getPermLevel(user)+" access.");
            Auth.setDefaultAuthUser(user);
        } else {
            //Shouldn't ever get here - we throw an exception above if we can't authenticate.
            System.out.println("Oddly unauthenticated");
        }
    }
}

