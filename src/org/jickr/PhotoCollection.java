package org.jickr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Collection contains a list of PhotoSet
 * 
 */
public class PhotoCollection {

	String id;
	String title;
	String description;
	String iconLarge;
	String iconSmall;
	Date created;
	
	int numPhotoSets=0;
	
	List<PhotoSet> photosets = new ArrayList<PhotoSet>();
	
	/**
     * Creates a new instance of Collection, using the Element returned by Flickr.
     * <code>Collection</code> is created in lists via User.getCollections(), or
     * via the <code>Collection.findByID(id)</code> call.
     *
     * @param collection The Collection information.  Must not be null.
     * @throws FlickrException On any error.
     */
    PhotoCollection(Element collection) throws FlickrException {
		if (collection == null) throw new FlickrRuntimeException("Collection cannot be null");
        this.id = collection.getAttributeValue("id");
        this.title = collection.getAttributeValue("title");
        this.description = collection.getAttributeValue("description");
        this.iconLarge = collection.getAttributeValue("iconlarge");
        this.iconSmall = collection.getAttributeValue("iconsmall");
        this.numPhotoSets = collection.getChildren().size();
        
        List<Element> sets = collection.getChildren("set");
        for(Element e : sets){
        	photosets.add(new PhotoSet(e.getAttributeValue("id")));
        }
	}
	
	PhotoCollection(String id) throws FlickrException {
        if (id == null) throw new FlickrRuntimeException("id cannot be null");
        Request req = new Request();
        req.setParameter("method","flickr.collections.getTree");
        req.setParameter("collection_id",id);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        Element collection = root.getChild("collections").getChild("collection");
        this.id = collection.getAttributeValue("id");
        this.title = collection.getChildText("title");
        this.description = collection.getChildText("description");
        this.iconLarge = collection.getAttributeValue("iconlarge");
        this.iconSmall = collection.getAttributeValue("iconsmall");
        this.numPhotoSets = collection.getChildren().size();
        
        for(Object set : collection.getChildren()){
        	Element e = (Element) set;
        	photosets.add(new PhotoSet(e.getAttributeValue("id")));
        }
    }
	
	/**
     * Get a new Collection identified by the supplied ID (format: userid-collection_id).
     * @param id - The unique user ID + "-" + unique ID of the Collection in Flickr.
     * @return A Collection that is identified by the supplied ID.
     */
    public static PhotoCollection findByID(String id) throws FlickrException {
        return new PhotoCollection(id);
    }

    /**
     * Collection ID. Unique value.
     * @return The ID of this collection on Flickr
     */
	public String getId() {
		return id;
	}

	/**
	 * Collection title
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getIconLarge() {
		return iconLarge;
	}

	public String getIconSmall() {
		return iconSmall;
	}

	public Date getCreated() {
		return created;
	}

	public int getNumPhotoSets() {
		return numPhotoSets;
	}

	public List<PhotoSet> getPhotosets() {
		return photosets;
	}
	
	/**
     * Convert this Collection to a String.
     * @return The collection title.
     */
    public String toString() {
        return title;
    }
    
    /**
     * Check equality.  Uniqueness based on ID.
     * @param o - Object to compare.
     * @return true if these objects have the same ID string.
     */
    public boolean equals(Object o) {
        return ((PhotoCollection)o).getId().equals(this.getId());
    }
}
