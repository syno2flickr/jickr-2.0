/*
 * ConfigureFrame.java
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr.example.slideshow;

import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;
import org.jickr.*;

/**
 * Create a Frame to get a PhotoSet.
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
public class ConfigureFrame extends JFrame {
    
    JLabel userSearchLabel;
    JTextField userText;
    // Allowed values - favorites, contacts, stream, recent
    String userButtonPressed = "favorites";
    User user;
    PhotoSet photos;
    FilmStripPanel strip;
    JPanel viewPanel;
    
    /** Creates a new instance of ConfigureFrame */
    ConfigureFrame() {
        super("Select a PhotoSet");
        
        Border border = BorderFactory.createLineBorder(Color.black);
        
        // Next Section - Set up the different panels
        
        /*
        JPanel selectPanel = new JPanel();
        JComboBox selectCombo = new JComboBox();
        selectCombo.setEditable(false);
        selectCombo.addItem("User based selections");
        selectCombo.addItem("Generic Selections");
        selectPanel.add(selectCombo);
        */
        
        UserTextListener utl = new UserTextListener();
        JLabel userLabel = new JLabel("Username: ");
        userLabel.setToolTipText("Flickr Username");
        userText = new JTextField(20);
        userText.setToolTipText("Flickr Username");
        userText.addActionListener(utl);
        userSearchLabel = new JLabel("Not Searched");
        userSearchLabel.setToolTipText("Result of search for Flickr Username");
        JButton userButton = new JButton("Search");
        userButton.setToolTipText("Search for Flickr Username");
        userButton.addActionListener(utl);
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        userPanel.add(userLabel);
        userPanel.add(userText);
        userPanel.add(userButton);
        userPanel.add(userSearchLabel);
        userPanel.setBorder(border);
        
        UserButtonListener ubl = new UserButtonListener();
        JRadioButton favoritePhotos = new JRadioButton("Favorite Photos",true);
        favoritePhotos.setToolTipText("All the public photos selected by the above user as Favorites");
        favoritePhotos.setActionCommand("favorites");
        favoritePhotos.addActionListener(ubl);
        JRadioButton contactPhotos = new JRadioButton("Contact Photos");
        contactPhotos.setToolTipText("A selection of recent public photos from the above user's Contacts");
        contactPhotos.setActionCommand("contacts");
        contactPhotos.addActionListener(ubl);
        JRadioButton streamPhotos = new JRadioButton("Photosteam Photos");
        streamPhotos.setToolTipText("All of the public photos belonging to the above user");
        streamPhotos.setActionCommand("stream");
        streamPhotos.addActionListener(ubl);
        ButtonGroup userButtons = new ButtonGroup();
        userButtons.add(favoritePhotos);
        userButtons.add(contactPhotos);
        userButtons.add(streamPhotos);
        JPanel userButtonPanel = new JPanel();
        userButtonPanel.setLayout(new GridLayout(0,1));
        userButtonPanel.add(favoritePhotos);
        userButtonPanel.add(contactPhotos);
        userButtonPanel.add(streamPhotos);
        userButtonPanel.setBorder(border);
        
        JPanel genericButtonPanel = new JPanel();
        JRadioButton recentPhotos = new JRadioButton("Recent Photos",true);
        recentPhotos.setToolTipText("All the public photos recently added to Flickr");
        recentPhotos.setActionCommand("recent");
        recentPhotos.addActionListener(ubl);
        JRadioButton interestingPhotos = new JRadioButton("Interesting Photos",true);
        interestingPhotos.setToolTipText("All of Flickr's interesting photos from today");
        interestingPhotos.setActionCommand("interesting");
        interestingPhotos.addActionListener(ubl);
        userButtons.add(recentPhotos);
        userButtons.add(interestingPhotos);
        genericButtonPanel.add(recentPhotos);
        genericButtonPanel.add(interestingPhotos);
        genericButtonPanel.setBorder(border);
        
        strip = new FilmStripPanel("Nothing yet selected");
        
        JPanel userGroupPanel = new JPanel();
        userGroupPanel.setLayout(new GridLayout(0,1));
        userGroupPanel.add(userPanel);
        userGroupPanel.add(userButtonPanel);
        userGroupPanel.add(genericButtonPanel);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(0,1));
        contentPanel.add(userGroupPanel);
        
        viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        viewPanel.add(contentPanel,BorderLayout.NORTH);
        viewPanel.add(strip,BorderLayout.SOUTH);
        
        JButton submitButton = new JButton("Done");
        submitButton.setToolTipText("When you've finished selecting pictures, Press Here");
        submitButton.addActionListener(new SubmitButtonListener());
        JPanel submitPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel out without selecting photos");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ConfigureFrame.this.dispose();
            }
        });
        submitPanel.add(submitButton);
        submitPanel.add(cancelButton);
        
        // Remember the last entry, and repopulate
        
        Preferences prefs = Preferences.userNodeForPackage(ConfigureFrame.class);
        String username = prefs.get("DefaultUsername",null);
        if (username != null) {
            userText.setText(username);
        }
        
        setSize(600,400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(viewPanel,BorderLayout.NORTH);
        getContentPane().add(submitPanel,BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private void changeFilmStrip() {
        List<Photo> list;
        
        viewPanel.remove(strip);
        try {
            strip = new FilmStripPanel(getPhotoList(),7);
        } catch (FlickrException ex) {
            strip = new FilmStripPanel("Error getting preview");
        }
        viewPanel.add(strip,BorderLayout.SOUTH);
        this.validate();
        this.repaint();
    }
    
    private List<Photo> getPhotoList() throws FlickrException {
        if (userButtonPressed.equals("recent")) {
            return Photo.getRecentPhotos();
        } else if (userButtonPressed.equals("interesting")) {
            return Photo.getInteresting();
        }
        if (user != null) {
            if (userButtonPressed.equals("favorites")) {
                return user.getPublicFavoritePhotos();
            } else if (userButtonPressed.equals("contacts")) {
                return user.getPublicContactPhotos();
            } else if (userButtonPressed.equals("stream")) {
                return user.getPublicPhotos();
            } else { // uh oh, no match
                throw new RuntimeException("Coding Error - invalid string:"+userButtonPressed);
            }
        } 
        return null;  // if user == null
    }
    
    private class UserTextListener implements ActionListener {
        
        public void actionPerformed(ActionEvent ae) {
            ConfigureFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                // Note I'm using trim, since I believe usernames don't start or end with space
                String userString = userText.getText().trim();
                User oldUser = user;
                user = User.findByUsername(userString);
                userSearchLabel.setText("Found");
                Preferences prefs = Preferences.userNodeForPackage(ConfigureFrame.class);
                prefs.put("DefaultUsername",userString);
                if (oldUser != user) {
                    changeFilmStrip();
                }
            }  catch (FlickrException fe) {
                if (fe.getCode() == 1) {
                    userSearchLabel.setText("Not Found");
                } else {
                    userSearchLabel.setText("Error");
                    JOptionPane.showMessageDialog(ConfigureFrame.this,fe,"Flickr Error",JOptionPane.ERROR_MESSAGE);
                    fe.printStackTrace();
                }
                user = null;
            } finally {
                ConfigureFrame.this.setCursor(Cursor.getDefaultCursor());
            }
            
        }
    }
    
    private class SubmitButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            ConfigureFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ConfigureFrame.this.setVisible(false);
            try {
                new SlideShow(getPhotoList());
            } catch (FlickrException ex) {
                Logger.global.severe("Exception getting PhotoList");
            }
            ConfigureFrame.this.setCursor(Cursor.getDefaultCursor());
            ConfigureFrame.this.dispose();
            
        }
    }
    
    private class UserButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            Logger.global.finest("User Button Pushed");
            Logger.global.finest(ae.getActionCommand());
            String oldUserButtonPressed = userButtonPressed;
            userButtonPressed = ae.getActionCommand();
            ConfigureFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (userButtonPressed != oldUserButtonPressed) {
                changeFilmStrip();
            }
            ConfigureFrame.this.setCursor(Cursor.getDefaultCursor());
        }
    }
    
}
