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
	
	List<PhotoCollection> subCollections = new ArrayList<PhotoCollection>();
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
		this(collection, true);
	}
	
	/**
     * Creates a new instance of Collection, using the Element returned by Flickr.
     * <code>Collection</code> is created in lists via User.getCollections(), or
     * via the <code>Collection.findByID(id)</code> call.
     *
     * @param collection The Collection information.  Must not be null.
     * @param fetchSets fetch linked sets (very slow)
     * @throws FlickrException On any error.
     */
    PhotoCollection(Element collection, boolean fetchSets) throws FlickrException {
		if (collection == null) throw new FlickrRuntimeException("Collection cannot be null");
        this.id = collection.getAttributeValue("id");
        this.title = collection.getAttributeValue("title");
        this.description = collection.getAttributeValue("description");
        this.iconLarge = collection.getAttributeValue("iconlarge");
        this.iconSmall = collection.getAttributeValue("iconsmall");
        this.numPhotoSets = collection.getChildren().size();
        
        // Get associated sets
        if(fetchSets)
	        for(Object set : collection.getChildren("set")){
	        	Element e = (Element) set;
	        	photosets.add(new PhotoSet(e.getAttributeValue("id")));
	        }

        // Get sub-collections
        for(Object c : collection.getChildren("collection")){
        	Element e = (Element) c;
        	this.subCollections.add(new PhotoCollection(e));
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
        this.title = collection.getAttributeValue("title");
        this.description = collection.getAttributeValue("description");
        this.iconLarge = collection.getAttributeValue("iconlarge");
        this.iconSmall = collection.getAttributeValue("iconsmall");
        this.numPhotoSets = collection.getChildren().size();
        
        for(Object set : collection.getChildren()){
        	Element e = (Element) set;
        	photosets.add(new PhotoSet(e.getAttributeValue("id")));
        }
        
        for(Object c : collection.getChildren("collection")){
        	Element e = (Element) c;
        	this.subCollections.add(new PhotoCollection(e));
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
     * Create new photo collection.
     *
     * @throws FlickrException
     */
    public static String newPhotoCollection(String title, String description, String parentId, String afterNewColl) throws FlickrException{
    	if (title == null) throw new FlickrRuntimeException("title cannot be null");
    	Request req = new Request();
    	req.setParameter("method","flickr.collections.create");
        req.setParameter("title",title);
        if (description!=null)
        	req.setParameter("description", description);
        if(parentId!=null)
        	req.setParameter("parent_id", parentId);
        if(afterNewColl!=null)
        	req.setParameter("after_new_coll", afterNewColl);
        
        Document doc = req.getResponse();
        Element root = doc.getRootElement();
        Element photoCollection = root.getChild("collection");
        
        // return PhotoCollection id
    	return photoCollection.getAttributeValue("id");
    }

    /**
     * find first set equals to setName in collection tree
     * @param setName name of searched set
     * @return set object
     */
    public PhotoSet findSetByName(String setName) {
    	return findSetByName(setName, this);
    }
    
    private PhotoSet findSetByName(String setName, PhotoCollection collection){
    	if (setName == null) throw new FlickrRuntimeException("setName cannot be null");
    	if (collection == null) throw new FlickrRuntimeException("collection cannot be null");
        for(PhotoSet ps : collection.getPhotosets()){
        	if(ps.getTitle().equals(setName))
        		return ps;
        }
        for(PhotoCollection pc : collection.getSubCollections()){
        	PhotoSet set = findSetByName(setName, pc);
        	if(set!=null)
        		return set;
        }
        return null;
    }
    
    /**
     * find first collection equals to collectionName in collection tree
     * @param setName name of searched set
     * @return set object
     */
    public PhotoCollection findCollectionByName(String collectionName) {
    	return findCollectionByName(collectionName, this);
    }
    
    private PhotoCollection findCollectionByName(String collectionName, PhotoCollection collection){
    	if (collectionName == null) throw new FlickrRuntimeException("collectionName cannot be null");
    	if (collection == null) throw new FlickrRuntimeException("collection cannot be null");
    	if(collection.getTitle().equals(collectionName))
    		return collection;
        for(PhotoCollection pc : collection.getSubCollections()){
        	PhotoCollection result = findCollectionByName(collectionName, pc);
        	if(result != null)
        		return result;
        }
        return null;
    }
    
    /**
     * Add set to collection
     * WARNING: this Flickr API method ("flickr.collections.editSets") is not offically supported
     * @param setId set ID
     * @param collection Collection 
     * @return true if created, false if already exists.
     * @throws FlickrException if any error occured 
     */
    public static boolean addSetToCollection(String setId, PhotoCollection collection) throws FlickrException{
    	if (setId == null) throw new NullPointerException("Set id cannot be null");
    	for(PhotoSet set : collection.getPhotosets())
    		if(set.getID().equals(setId))
    			return false;
        Request req = new Request(Request.POST);
        req.setParameter("method","flickr.collections.editSets");
        req.setParameter("collection_id",collection.getId());
        String photoSetIds = "";
        for(int i=0; i < collection.getPhotosets().size(); i++){
        	PhotoSet ps = collection.getPhotosets().get(i);
        	photoSetIds += ps.getID()+",";
        }
        photoSetIds += setId;
        req.setParameter("photoset_ids",photoSetIds);
        req.getResponse();
        return true;
    }

    /**
     * Add set to collection
     * WARNING: this Flickr API method ("flickr.collections.editSets") is not offically supported
     * @param setId set ID
     * @return true if created, false if already exists.
     * @throws FlickrException if any error occured 
     */
    public boolean addSet(String setId) throws FlickrException{
    	PhotoCollection.addSetToCollection(setId, this);
        photosets.add(new PhotoSet(setId));
        return true;
    }
    
    /**
     * Add set to collection
     * WARNING: this Flickr API method ("flickr.collections.editSets") is not offically supported
     * @param setId set ID
     * @return true if created, false if already exists.
     * @throws FlickrException if any error occured 
     */
    public boolean addSet(PhotoSet set) throws FlickrException{
    	PhotoCollection.addSetToCollection(set.getID(), this);
        photosets.add(set);
        return true;
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
	
	public List<PhotoCollection> getSubCollections() {
		return subCollections;
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
