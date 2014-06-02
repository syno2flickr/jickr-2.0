package org.jickr;

/**
 * Privacy value associated with an object on Flickr.
 * 
 * Updated by jbrek: the integer param value is corresponding to the XML value used by Flickr
 * @author driscoll
 */
public enum Privacy {
    
	/**
     * The object is visible only to the authenticated user, it is completely private.
     */
    PRIVATE(0),
    
    /**
     * The object is visible only to people marked as family on Flickr.
     */
    FAMILY(1),
    
    /**
     * The object is visible only to people marked as a friend on Flickr.
     */
    FRIENDS(2),
    
    /**
     * The object is visible only to people marked either a friend or family on Flickr.
     */
    FRIENDSANDFAMILY(3),
    
	/**
     * The object is visible to everyone.
     */
    PUBLIC(4);
    
    
    private int xmlValue;
	
    Privacy(int xmlValue){
		
		this.xmlValue = xmlValue;
	}

	public int getXmlValue() {
		return xmlValue;
	}
	
	/**
	 * Return the good Enum based on boolean params
	 * If all params are FALSE -> privacy = private
	 * If isFamily AND isFriends are TRUE -> privacy = Firends and Family
	 * 
	 * @param isPublic No privacy
	 * @param isFamily Family privacy
	 * @param isFriends Friends privacy
	 * @return the corresponding Enum to the parameters
	 */
	public static Privacy getEnumFromValue(boolean isPublic, boolean isFamily, boolean isFriends){
		
		if (isPublic)
			return Privacy.PUBLIC;
		else if(isFriends && isFamily)
			return Privacy.FRIENDSANDFAMILY;
		else if(isFamily)
			return Privacy.FAMILY;
		else if(isFriends)
			return Privacy.FRIENDS;
		else 
			return Privacy.PRIVATE;
	}	
	
	public static Privacy valueOf(int value){
		switch(value){
		case 0: return Privacy.PRIVATE;
		case 1: return Privacy.FAMILY;
		case 2: return Privacy.FRIENDS;
		case 3: return Privacy.FRIENDSANDFAMILY;
		case 4: return Privacy.PUBLIC;
		default: return Privacy.PRIVATE;
		}
	}

	@Override
	public String toString() {
		switch(xmlValue) {
			case 0 : return "me only";
			case 1 : return "my family";
			case 2 : return "my friends";
			case 3 : return "my family and my friends";
			case 4 : return "everybody";
			default : return super.toString();
		}
	}
  }
