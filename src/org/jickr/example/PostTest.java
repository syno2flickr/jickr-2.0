/*
 * PostTest.java
 *
 * Created on July 25, 2006, 7:23 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import org.jickr.*;

/**
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class PostTest {
    
    /**
     * Creates a new instance of PostTest
     */
    public PostTest() {
    }
    
    public static void main(String[] args) throws Exception {

        String add_photo_id = "186110452";
        String add_photo_secret = "84f48c7f57";
        System.out.println("addComment");
        
        BufferedReader br;
        br = new BufferedReader(new FileReader("keys"));
        String apiKey = br.readLine();
        Flickr.setApiKey(apiKey);
        String sharedSecret = br.readLine();
        Flickr.setSharedSecret(sharedSecret);
        User user = Auth.getDefaultAuthUser();
        
        String commentText = "Programmatically added comment";
        String commentModText = "Programmatically modified comment";
        Photo photo = new Photo(add_photo_id,add_photo_secret);
        String commentID = photo.addComment(commentText);
        List<Comment> comments = photo.getComments();
        Comment comment = null;
        for (Comment c : comments) {
            if (c.getID().equals(commentID)) comment = c;
        }
        
        System.out.println(""+comment);
    }
    
}
