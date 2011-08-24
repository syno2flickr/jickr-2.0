/*
 * SimpleBackup.java
 *
 * Created on July 25, 2006, 9:42 AM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr.example;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jickr.*;

/**
 * Simple example which
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class SimpleBackup {
    
    // Hardcoded directory, since I don't want to include the directory choosing
    // code into this - it'd bloat the code.
    public static final String DESTDIR = "/home/driscoll/Desktop/FlickrBackupPhotos";

    // Download no more than this number of Photos
    public static final int MAXPHOTO = 10;
    
    /** Creates a new instance of SimpleBackup */
    public SimpleBackup() {
    }
    
    /**
     * @param args the command line arguments - ignored.
     */
    public static void main(String[] args) throws Exception {
        
        /*
         * This code assumes a file called "keys" in the working directory,
         * of the format apikey\nshared\n
         * I did this so I wouldn't have to check in my api key into the
         * workspace.
         */
        BufferedReader br;
        br = new BufferedReader(new FileReader("keys"));
        String apiKey = br.readLine();
        Flickr.setApiKey(apiKey);
        String sharedSecret = br.readLine();
        Flickr.setSharedSecret(sharedSecret);
        
        /*  The Following block is the ususal one to include in programs.
        String apiKey = "notavalidapikey";
        Flickr.setApiKey(apiKey);
         
        String sharedSecret = "notavalidsecretkey";
        Flickr.setSharedSecret(sharedSecret);
         */
        
        Logger.global.info("Starting Backup");
        File parent = new File(DESTDIR);
        if (!parent.exists()) parent.mkdirs();
        // Get a list of Photos, in this case, all Interesting Photos for today.
        List<Photo> list = Photo.getInteresting();
        int count = 0;
        for (Photo photo: list) {
            // Download the image
            BufferedImage bi = photo.getImage();
            // Get a unique filename, using the photo id
            File f = new File(parent,photo.getID()+".jpg");
            Logger.global.info("Writing image "+f);
            // Write out the image as a jpg
            ImageIO.write(bi,"jpg",f);
            // Get no more than MAXPHOTO number of photos
            if (++count >= MAXPHOTO) break;
        }
        Logger.global.info("Backup complete");
    }
    
}
