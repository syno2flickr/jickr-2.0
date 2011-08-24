/*
 * GroupList.java
 * 
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 29, 2006, 7:09 PM
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
 *
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
class GroupList <E extends Group> extends AbstractList {
    
    private Request listRequest;
    //  Number of Groups to get per page
    private final String paging = "500";
    private final int pagingNum = Integer.parseInt(paging);
    // Maximum # of pages to fetch
    // Set to 0 to turn off limit
    // multiply limitpages * pagingNum to get limit on # of groups
    private final int limitpages = 10;
    private ArrayList<Group> backingList = new ArrayList<Group>();
    
    
    /**
     * Creates a new instance of GroupList.
     */
    GroupList() {
    }
    
    /**
     * Creates a new instance of GroupList
     */
    GroupList(Request req) throws FlickrException {
        listRequest = req;
        req.setParameter("per_page",paging);
        loadBackingList();
    }
    
    public Group get(int index) {
        return backingList.get(index);
    }
    
    /**
     * Get the number of Groups in the GroupList.
     * @return size Size of GroupList.
     */
    public int size() {
        return backingList.size();
    }
    
    
    /**
     * Load the list into the backing list.
     */
    private void loadBackingList() throws FlickrException {
        Document doc;
        Element root;
        int pages;
        int pagecount = 0;
        List<Element> groups;
        
        do {
            pagecount++;
            listRequest.setParameter("page",pagecount+"");
            doc = listRequest.getResponse();
            root = doc.getRootElement();
            try {
                //  Sometimes, the Groups element has a "pages" attribute,
                //  sometimes it doesn't.
                //  When it doesn't, then there's only one page.
                //  But no matter what you say, I still don't miss schemas.
                Element groupsElement = root.getChild("groups");
                if (groupsElement == null) { // We're in a Groupset GroupList
                    pages = 1;
                    groups = root.getChild("groups").getChildren("group");
                } else {
                    String pagesString = groupsElement.getAttributeValue("pages");
                    if (pagesString == null) {
                        pages = 1;
                    } else {
                        pages = Integer.parseInt(pagesString);
                        if (limitpages != 0 && limitpages < pages) pages = limitpages;
                    }
                    groups = root.getChild("groups").getChildren("group");
                }
            } catch (NullPointerException npe) {
                throw new FlickrException("Malformed XML Error",npe);
            }
            for (Element group : groups)  {
                backingList.add(new Group(group));
            }
        } while (pagecount < pages);
    }
    
    /**
     * Get the String representation of this List.
     */
    public String toString() {
        return listRequest.toString();
    }
}
