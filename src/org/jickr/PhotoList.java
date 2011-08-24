/*
 * PhotoList.java
 *
 * Copyright (c) 2006, James G. Driscoll
 * 
 * Created on July 14, 2006, 8:23 PM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;

/**
 *<P>A list of Photos.  Note:  behavior is undefined if the underlying Flickr database
 * of pictures changes during the lifetime of the object.  This means that if you add or delete
 * photos while there's a program with this object, odd things could happen.</P>
 * <P>Currently the list is created by loading a list of all photos from Flickr at the time the class
 * is initialized.  This list is currently limited to the first 5000 returned. </P>
 *<P>This class is not meant to be used directly by the end user.  It's exposed as the return type
 * for classes that return stuff of type List<Photo>.</P>
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
class PhotoList <E extends Photo> extends AbstractList {
    
    private Request listRequest;
    //  Number of photos to get per page
    private final String paging = "500";
    private final int pagingNum = Integer.parseInt(paging);
    // Maximum # of pages to fetch
    // Set to 0 to turn off limit
    // multiply limitpages * pagingNum to get limit on # of photos
    private final int limitpages = 10;
    private ArrayList<Photo> backingList = new ArrayList<Photo>();
    
    
    /** 
     * Creates a new instance of PhotoList 
     */
    PhotoList(Request req) throws FlickrException {
        listRequest = req;
        req.setParameter("per_page",paging);
        //        photoListURLString = baseListURLString + "&per_page="+paging+"&page=";
        loadBackingList();
    }
    
    public Photo get(int index) {
        return backingList.get(index);
    }
    
    /**
     * Get the number of Photos in the PhotoList.
     * @return size Size of PhotoList.
     */
    public int size() {
        return backingList.size();
    }
    
    /**
     * Load the list into the backing list.
     *
     * Since the XML format is different between Photoset Photolists and
     * other photo lists, we'll have to put a bunch of special cases in here.
     *
     */
    private void loadBackingList() throws FlickrException {
        Document doc;
        Element root;
        int pages;
        int pagecount = 0;
        List<Element> photos;
        
        do {
            pagecount++;
            listRequest.setParameter("page",pagecount+"");
            doc = listRequest.getResponse();
            root = doc.getRootElement();
            //  Sometimes, the Photos element has a "pages" attribute,
            //  sometimes it doesn't.
            //  When it doesn't, then there's only one page.
            //  But no matter what you say, I still don't miss schemas.
            Element photosElement = root.getChild("photos");
            if (photosElement == null) { // We're in a Photoset PhotoList
                pages = 1;
                photos = root.getChild("photoset").getChildren("photo");
            } else {
                String pagesString = photosElement.getAttributeValue("pages");
                if (pagesString == null) {
                    pages = 1;
                } else {
                    pages = Integer.parseInt(pagesString);
                    if (limitpages != 0 && limitpages < pages) pages = limitpages;
                }
                photos = root.getChild("photos").getChildren("photo");
            }
            for (Element photo : photos)  {
                backingList.add(new Photo(photo));
            }
        } while (pagecount < pages);
    }

    public String toString() {
        return listRequest.toString();
    }    
}