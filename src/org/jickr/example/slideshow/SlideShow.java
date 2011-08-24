/*
 *  SlideShow.java
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr.example.slideshow;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.jickr.Flickr;
import org.jickr.FlickrException;
import org.jickr.Photo;

/**
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class SlideShow extends JFrame implements ActionListener {
    
    private List<Photo> photolist;
    private String path;
    private JPanel panel;
    private int position = 0; // used to track which image we are currently viewing
    // Whether to run slideshow
    private boolean slide = true;
    // Delay in seconds between photos
    private int delay = 3;
    // Skip amount when you hit a "Page" button, either PG_DN or PG_UP
    private int skip = 25;
    
    List<Photo> choose;
    
    public SlideShow(List<Photo> pl) {
        super("Slideshow");
        photolist = pl;
        if (photolist == null) return;
        JLabel picture = null;
        try {
            picture = new ImageLabel(((Photo) photolist.get(position)).getImage());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (FlickrException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        panel = new JPanel(new BorderLayout());
        panel.add(picture);
        getContentPane().add(panel);
        setUndecorated(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0,0,screenSize.width, screenSize.height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
        // Add a key listener
        KeyHandler listener = new KeyHandler();
        addKeyListener(listener);
        
        // Add a Timer
        if (slide) new Timer(delay*1000, this).start();
    }
    
    private void changeImage(Photo photo) {
        try {
            JLabel picture = new ImageLabel(photo.getImage());
            panel.removeAll();
            panel.add(picture);
            panel.revalidate();
        } catch (Exception e){
            System.err.println(e);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        // Get the next photo, go to next photo
        if (position<photolist.size()-1) {
            position++;
        } else {
            position = 0;
        }
        changeImage((Photo)photolist.get(position));
    }
    
    /**
     * Allow panel to get input focus
     */
    public boolean isFocusable() {
        return true;
    }
    
    public static void main(String[] args) {

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("keys"));   
            String apiKey = br.readLine();
            Flickr.setApiKey(apiKey);
            String sharedSecret = br.readLine();
            Flickr.setSharedSecret(sharedSecret);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
        /*  The Following block is the ususal one to include in programs.
        String apiKey = "notavalidapikey";
        Flickr.setApiKey(apiKey);
         
        String sharedSecret = "notavalidsecret";
        Flickr.setSharedSecret(sharedSecret);
         */
        
        JFrame frame = new ConfigureFrame();
    }
    
    // Inner class for Key listener
    private class KeyHandler implements KeyListener {
        public void keyPressed(KeyEvent event) {
            int keyCode = event.getKeyCode();
            
            if ((keyCode == KeyEvent.VK_LEFT)||(keyCode == KeyEvent.VK_UP)) {
                if (position>0) {
                    position--;
                } else {
                    position = photolist.size()-1;
                }
                changeImage((Photo)photolist.get(position));
            } else if ((keyCode == KeyEvent.VK_RIGHT)|| (keyCode == KeyEvent.VK_DOWN)) {
                if (position<photolist.size()-1) {
                    position++;
                } else {
                    position = 0;
                }
                changeImage((Photo)photolist.get(position));
            } else if (keyCode == KeyEvent.VK_PAGE_DOWN) {
                position += 25;
                if (position > photolist.size()-1) position = 0;
                changeImage((Photo)photolist.get(position));
            } else if (keyCode == KeyEvent.VK_PAGE_UP ) {
                position -= 25;
                if (position < 0) position = photolist.size()-1;
                changeImage((Photo)photolist.get(position));
            } else if (keyCode == KeyEvent.VK_HOME) {
                position=0;
                changeImage((Photo)photolist.get(position));
            } else if (keyCode == KeyEvent.VK_END) {
                position=photolist.size()-1;
                changeImage((Photo)photolist.get(position));
            } else if ((keyCode == KeyEvent.VK_ESCAPE) ||
                    (keyCode == KeyEvent.VK_Q)) {
                System.exit(0); //Escape key
            }
        }
        
        public void keyReleased(KeyEvent event) {}
        
        public void keyTyped(KeyEvent event) {
            char keyChar = event.getKeyChar();
            
        }
    }
    
    private class ImageLabel extends JLabel {
        public ImageLabel(Image image) {
            setIcon(new ImageIcon(image));
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
            setOpaque(true);
            setBackground(Color.BLACK);
        }
        
    }
    
}