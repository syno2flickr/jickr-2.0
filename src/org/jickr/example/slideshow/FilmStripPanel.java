/*
 * FilmStripPanel.java
 *
 * Created on July 20, 2006, 11:06 AM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr.example.slideshow;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.jickr.FlickrException;
import org.jickr.Photo;

/**
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class FilmStripPanel extends JPanel implements ActionListener, Runnable {
    
    List<Photo> list;
    int limit;
    
    Collection<JLabel> labels = new ArrayList<JLabel>();
    
    /**
     * Creates a new instance of FilmStripPanel
     */
    public FilmStripPanel(List<Photo> list, int limit) {
        if (list == null) throw new IllegalArgumentException("List must not be null");
        this.list = list;
        if (limit < 1) throw new IllegalArgumentException("Bad limit: "+limit);
        this.limit = limit;
        setLayout(new GridLayout(1,0));
        new Thread(this).start();
    }
    
    /**
     * Call this to display a message instead of a filmstrip.
     */
    public FilmStripPanel(String message) {
        if (message == null) return;
        add(new JLabel(message));
    }
    
    public void actionPerformed(ActionEvent e) {
        for (JLabel imageLabel : labels) {
            this.add(imageLabel);
        }
        this.revalidate();
        this.repaint();
    }
    
    public void run() {
        int count = 0;
        if (limit > list.size()) limit = list.size();
        Iterator<Photo> li = list.iterator();
        while (++count <= limit) {
            JLabel imageLabel;
            try {
                imageLabel = new JLabel(new ImageIcon(li.next().getImage(Photo.Size.SQUARE)));
                labels.add(imageLabel);
            } catch (FlickrException ex) {
                Logger.global.info("Error populating FilmStripPanel");
                Logger.global.info(ex.getMessage());
            }
        }
        Timer timer = new Timer(1,FilmStripPanel.this);
        timer.setRepeats(false);
        timer.start();
    }
    
    
}
