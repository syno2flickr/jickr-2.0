/*
 * FlickrThumb.java
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr.example;

import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ListIterator;
import java.util.logging.Logger;
import javax.swing.*;
import org.jickr.Flickr;
import org.jickr.FlickrException;
import org.jickr.Photo;

/**
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class FlickrThumb extends JFrame implements ActionListener {
    JPanel thumbPanel;
    JScrollPane scrollPane;
    ListIterator<Photo> li;
    int count = 0;
    // Start #, Maximum # of pictures to display.
    final int STARTNUM = 0;
    final int MAXNUM = 60;
    
    /**
     * Creates a new instance of FlickrThumb
     */
    public FlickrThumb(List<Photo> list) {
        super("Flickr Thumbnails");
        JFrame.setDefaultLookAndFeelDecorated(true);
        li = list.listIterator(STARTNUM);
        setSize(650,500);
        
        thumbPanel = new JPanel(new GridLayout(0,6));
        scrollPane = new JScrollPane(thumbPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        getContentPane().add(scrollPane);
        thumbPanel.setBackground(Color.BLACK);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        new Timer(0, this).start();
        setVisible(true);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FlickrException {

        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader("keys"));
            
            String apiKey = br.readLine();
            Flickr.setApiKey(apiKey);
            String sharedSecret = br.readLine();
            Flickr.setSharedSecret(sharedSecret);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        } finally {
        }
        
        /*  The Following block is the ususal one to include in programs.
        String apiKey = "notavalidapikey";
        Flickr.setApiKey(apiKey);
         
        String sharedSecret = "notavalidsecretkey";
        Flickr.setSharedSecret(sharedSecret);
         */
        
        Logger.global.info("Getting Photo List");
        List<Photo> list = Photo.getInteresting();
        new FlickrThumb(list);
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (!li.hasNext()) return;  //we're done
        if (++count > MAXNUM) return; //also done
        try {
            JLabel imageLabel = new JLabel(new ImageIcon(li.next().getImage(Photo.Size.THUMB)));
            thumbPanel.add(imageLabel);
        } catch (FlickrException ex) {
            Logger.global.severe(ex.getMessage());
        }
        thumbPanel.revalidate();
        repaint();
    }
}
